package com.chainton.dankeshare.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chainton.dankeshare.ShareCircleInfo;
import com.chainton.dankeshare.ShareCircleServer;
import com.chainton.dankeshare.ServerMessageHandler;
import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ResourceInfo;
import com.chainton.dankeshare.data.enums.ShareCircleClientMessageType;
import com.chainton.dankeshare.data.enums.ShareCircleServerMessageType;
import com.chainton.dankeshare.util.LogUtil;
import com.chainton.forest.core.NioSession;
import com.chainton.forest.core.helper.ForestMessageServer;
import com.chainton.forest.core.helper.ForestMessageServerEvents;
import com.chainton.forest.core.message.UserMessage;

/**
 * 默认分享圈管理端接口实现
 * 
 * @author 富林
 * 
 */
public final class DefaultShareCircleServer implements ShareCircleServer {

	/**
	 * NioSocket管理器实例
	 */
	private volatile ForestMessageServer messageServer;
	/**
	 * 线程池
	 */
	private volatile ExecutorService executorService;
	/**
	 * 所有共享资源
	 */
	private volatile Set<ResourceInfo> allSharedResources;

	// 用户信息和会话的相互映射
	private volatile Map<ClientInfo, NioSession> clientSessionMap;
	private volatile Map<NioSession, ClientBundle> clientBundles;
	private volatile List<ClientInfo> acceptedClients;
	
	private volatile ShareCircleInfo shareCircleInfo;
	private volatile ServerMessageHandler serverMessageHandler;
		
	/**
	 * 本地客户端信息
	 */
	private volatile ClientInfo myInfo;
	
	private volatile boolean isDestroyingServer = false;

	public DefaultShareCircleServer(ShareCircleInfo shareCircleInfo, ClientInfo myInfo, int maxClients) {
		this.myInfo = myInfo;
		System.out.println(Thread.currentThread().getId() + " Server IP: " + this.myInfo.getIp());
		this.shareCircleInfo = shareCircleInfo;
		this.shareCircleInfo.setMaxClients(maxClients);
		System.out.println(Thread.currentThread().getId() + " ShareCircleServer IP: " + this.shareCircleInfo.getServerIP());
		this.messageServer = new ForestMessageServer(this.serverEventsHandler);

		this.clientSessionMap = new HashMap<ClientInfo, NioSession>();
		this.clientBundles = new HashMap<NioSession, ClientBundle>();
		this.allSharedResources = new HashSet<ResourceInfo>();
		this.acceptedClients = new CopyOnWriteArrayList<ClientInfo>();
		
		this.executorService = Executors.newCachedThreadPool();
	}
	
	private ForestMessageServerEvents serverEventsHandler = new ForestMessageServerEvents() {

		@Override
		public void onServerStarted() {
		}

		@Override
		public void onServerStartFailed() {
		}

		@Override
		public void onServerStoped() {
			isDestroyingServer = false;
			clearData();
		}

		@Override
		public void onSessionOpened(NioSession session) {
			System.out.println(Thread.currentThread().getId() + " IoSession opened on server.");
			ClientBundle clientBundle = new ClientBundle();
			clientBundles.put(session, clientBundle);
		}

		@Override
		public void onSessionClosed(NioSession session) {
			ClientBundle clientBundle = clientBundles.get(session);
			if (clientBundle != null) {
				final ClientInfo client = clientBundle.clientInfo;
				System.out.println(Thread.currentThread().getId() + " IoSession of " + client.getIp() + "(" + client.getName() + ") closed on server.");
				if (!isDestroyingServer) {
					if (acceptedClients.contains(client)) {
						clientExit(client);
						executorService.execute(new Runnable() {
							public void run() {
								serverMessageHandler.onClientSessionClosed(client);
							}
						});
					}
				}
			} else {
				System.out.println(Thread.currentThread().getId() + " No client information found on session " + session.toString() + ". Session closed.");
			}
			clientBundles.remove(session);
		}

		@Override
		public void onMessageReceived(UserMessage message) {
			final ClientInfo client;
			final byte[] bytes;
			ResourceInfo resource;
			NioSession session = message.session;
			ShareCircleClientMessageType msgType = ShareCircleClientMessageType.parseFromInt(message.messageType);
			if (msgType.equals(ShareCircleClientMessageType.REGISTER_CLIENT)) {
				client = (ClientInfo)message.messageData;
				ClientBundle cb = clientBundles.get(session);
				if (cb != null && !client.equals(cb.clientInfo)) {
					cb.clientInfo = client;
					clientSessionMap.put(client, session);
				}
			} else {
				client = clientBundles.get(session).clientInfo;
			}
			System.out.println(Thread.currentThread().getId() + " " + LogUtil.logClientMessageReceived(message, client));
			switch (msgType) {
			case REGISTER_CLIENT:
				UserMessage msg = new UserMessage();
				msg.messageType = ShareCircleServerMessageType.ACCEPT_REGISTER.intValue();
				sendServerMessage(session, msg);
				break;
			case QUERY_SHARE_CIRCLE_INFO:
				returnShareCircleInfo(client);
				break;
			case REQUEST_ENTER:
				if (client.equals(myInfo)) {
					acceptClient(client);
				} else {
					if (shareCircleInfo.isFull()) {
						rejectClient(client);
					} else {
						executorService.execute(new Runnable() {
							public void run() {
								serverMessageHandler.onClientRequestEnter(client);
							}
						});
					}
				}
				break;
			case REQUEST_SYNCHRONIZE:
				executorService.execute(new Runnable() {
					public void run() {
						serverMessageHandler.onClientRequestSynchronize(client);
					}
				});
				break;
			case EXIT_SHARE_CIRCLE:
				clientExit(client);
				session.startClosing(true);
				break;
			case GET_ALL_SHARED_RESOURCES:
				informAllSharedResources(client);
				break;
			case GET_ALL_CLIENTS:
				informAllAcceptedClients(client);
				break;
			case ADD_SHARE_RESOURCE:
				resource = (ResourceInfo)message.messageData;
				addShareResource(client, resource);
				break;
			case REMOVE_SHARED_RESOURCE:
				resource = (ResourceInfo)message.messageData;
				removeSharedResource(resource);
				break;
			case AUTO_DISTRIBUTE_DATA:
				bytes = (byte[])message.messageData;
				distributeDataToAllClients(bytes);
				break;
			case DISTRIBUTE_DATA_TO_OTHERS:
				bytes = (byte[])message.messageData;
				distributeDataToOthers(bytes, client);
				break;
			case DATA_PACKET:
				bytes = (byte[])message.messageData;
				executorService.execute(new Runnable() {
					public void run() {
						serverMessageHandler.onDataReceived(client, bytes);
					}
				});
			default:
				break;
			}
		}
		
	};
	
	@Override
	public void startServer(ServerMessageHandler handler) {
		this.serverMessageHandler = handler;
		this.messageServer.startServer(9999, 10);
	}

	@Override
	public ClientInfo getMyInfo() {
		return this.myInfo.getCopy();
	}
	
	@Override
	public boolean isClosing() {
		return this.isDestroyingServer;
	}
	
	@Override
	public boolean isStopped() {
		return this.messageServer.isClosed();
	}
	
	@Override
	public void acceptClient(final ClientInfo client) {
		UserMessage msg = new UserMessage();
		NioSession session = clientSessionMap.get(client);
		if (session != null) {
			msg.messageType = ShareCircleServerMessageType.ACCEPT_JOIN.intValue();
			sendServerMessage(session, msg);
		}
		acceptedClients.add(client);
		shareCircleInfo.setAcceptedClients(acceptedClients.size());
	}
	
	@Override
	public void rejectClient(final ClientInfo client) {
		NioSession session = clientSessionMap.get(client);
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleServerMessageType.REJECT_JOIN.intValue();
		sendServerMessage(session, msg);
	}

	@Override
	public void informAllAcceptedClients(final ClientInfo client) {
		UserMessage msg = new UserMessage();
		NioSession session = clientSessionMap.get(client);
		if (session != null) {
			msg.messageType = ShareCircleServerMessageType.CLIENT_JOINED.intValue();
			for (ClientInfo cInfo : acceptedClients) {
				msg.messageData = cInfo;
				sendServerMessage(session, msg);
			}
		}
	}
	
	@Override
	public void informAllSharedResources(final ClientInfo client) {
		UserMessage msg = new UserMessage();
		NioSession session = clientSessionMap.get(client);
		if (session != null) {
			msg.messageType = ShareCircleServerMessageType.RESOURCE_ADDED.intValue();
			for (ResourceInfo rInfo : allSharedResources) {
				msg.messageData = rInfo;
				sendServerMessage(session, msg);
			}
		}
	}
	
	private void clientExit(final ClientInfo client) {
		acceptedClients.remove(client);
		shareCircleInfo.setAcceptedClients(acceptedClients.size());
		//通知所有客户端有客户端退出
		UserMessage msg = new UserMessage();
		for (ClientInfo cInfo : acceptedClients) {
			NioSession ssn = clientSessionMap.get(cInfo);
			if (ssn != null) {
				msg.messageType = ShareCircleServerMessageType.CLIENT_EXITED.intValue();
				msg.messageData = client;
				sendServerMessage(ssn, msg);
			}
		}
		//删除所有此客户端共享的资源信息
		NioSession session = clientSessionMap.get(client);
		ClientBundle cBundle = clientBundles.get(session);
		if (cBundle != null) {
			for (ResourceInfo rInfo : cBundle.sharedResources) {
				removeSharedResource(rInfo);
			}
		}
		clientSessionMap.remove(client);
	}
	
	private void addShareResource(final ClientInfo client, final ResourceInfo resource) {
		allSharedResources.add(resource);
		NioSession session = clientSessionMap.get(client);
		ClientBundle cBundle = clientBundles.get(session);
		cBundle.sharedResources.add(resource);
		UserMessage msg = new UserMessage();
		for (ClientInfo cInfo : acceptedClients) {
			NioSession ssn = clientSessionMap.get(cInfo);
			if (ssn != null) {
				msg.messageType = ShareCircleServerMessageType.RESOURCE_ADDED.intValue();
				msg.messageData = resource;
				sendServerMessage(ssn, msg);
			}
		}
	}
	
	private void removeSharedResource(final ResourceInfo resource) {
		allSharedResources.remove(resource);
		UserMessage msg = new UserMessage();
		NioSession session;
		for (ClientInfo cInfo : acceptedClients) {
			session = clientSessionMap.get(cInfo);
			if (session != null) {
				msg.messageType = ShareCircleServerMessageType.RESOURCE_REMOVED.intValue();
				msg.messageData = resource;
				sendServerMessage(session, msg);
			}
		}
	}
	
	@Override
	public List<ClientInfo> getAllAcceptedClients() {
		List<ClientInfo> clients = new ArrayList<ClientInfo>();
		for (ClientInfo ci : acceptedClients) {
			clients.add(ci);
		}
		return clients;
	}
	
	@Override
	public void kickOffClient(final ClientInfo client) {
		NioSession session = clientSessionMap.get(client);
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleServerMessageType.KICK_OFF.intValue();
		msg.messageData = client;
		sendServerMessage(session, msg);
		clientExit(client);
	}
	
	@Override
	public void distributeDataToAllClients(final byte[] bytes) {
		UserMessage msg = new UserMessage();
		NioSession session;
		for (ClientInfo cInfo : acceptedClients) {
			session = clientSessionMap.get(cInfo);
			if (session != null) {
				msg.messageType = ShareCircleServerMessageType.DATA_PACKET.intValue();
				msg.messageData = bytes;
				sendServerMessage(session, msg);
			}
		}
	}

	private void distributeDataToOthers(final byte[] bytes, final ClientInfo client) {
		UserMessage msg = new UserMessage();
		NioSession session;
		for (ClientInfo cInfo : acceptedClients) {
			if (!cInfo.equals(client)) {
				session = clientSessionMap.get(cInfo);
				if (session != null) {
					msg.messageType = ShareCircleServerMessageType.DATA_PACKET.intValue();
					msg.messageData = bytes;
					sendServerMessage(session, msg);
				}
			}
		}
	}

	@Override
	public void sendDataToClient(final ClientInfo client, final byte[] bytes) {
		UserMessage msg = new UserMessage();
		NioSession session = clientSessionMap.get(client);
		if (session != null) {
			msg.messageType = ShareCircleServerMessageType.DATA_PACKET.intValue();
			msg.messageData = bytes;
			sendServerMessage(session, msg);
		}
	}

	@Override
	public void destroyServer() {
		this.isDestroyingServer = true;
		UserMessage msg = new UserMessage();
		for (NioSession session : clientBundles.keySet()) {
			if (session != null) {
				msg.messageType = ShareCircleServerMessageType.SERVER_EXITED.intValue();
				sendServerMessage(session, msg);
			}
		}
		this.messageServer.startClosing();
	}
	
	private void clearData() {
		this.clientSessionMap.clear();
		this.clientBundles.clear();
		this.allSharedResources.clear();
		this.acceptedClients.clear();
	}

	/**
	 * 客户端信息
	 * @author Rivers
	 * 
	 */
	private class ClientBundle {

		public ClientInfo clientInfo;
		public Set<ResourceInfo> sharedResources;

		public ClientBundle() {
			clientInfo = new ClientInfo();
			sharedResources = new HashSet<ResourceInfo>();
		}

	}

	private void returnShareCircleInfo(final ClientInfo client) {
		NioSession session = clientSessionMap.get(client);
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleServerMessageType.RETURN_SHARE_CIRCLE_INFO.intValue();
		msg.messageData = shareCircleInfo;
		sendServerMessage(session, msg);
	}
	
	private void sendServerMessage(NioSession session, UserMessage message) {
		ClientBundle cb = clientBundles.get(session);
		if (cb != null) {
			ClientInfo client = cb.clientInfo;
			System.out.println(Thread.currentThread().getId() + " " + LogUtil.logServerMessageSend(message, client));
			session.sendMessage(message);
		}
	}

}
