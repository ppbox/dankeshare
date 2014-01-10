package com.chainton.dankeshare.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import android.os.Handler;
import android.util.Log;

import com.chainton.dankeshare.ClientMessageHandler;
import com.chainton.dankeshare.HttpDownloadFile;
import com.chainton.dankeshare.ReceiveShareListener;
import com.chainton.dankeshare.ShareCircleClient;
import com.chainton.dankeshare.ShareCircleClientCallback;
import com.chainton.dankeshare.ShareCircleInfo;
import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ResourceInfo;
import com.chainton.dankeshare.data.enums.ClientStatus;
import com.chainton.dankeshare.data.enums.ShareCircleClientMessageType;
import com.chainton.dankeshare.data.enums.ShareCircleServerMessageType;
import com.chainton.dankeshare.util.LogUtil;
import com.chainton.forest.core.NioSession;
import com.chainton.forest.core.helper.ForestMessageClient;
import com.chainton.forest.core.helper.ForestMessageClientEvents;
import com.chainton.forest.core.message.UserMessage;

/**
 * 默认分享圈客户端控制接口实现
 * @author 富林
 *
 */
public final class DefaultShareCircleClient implements ShareCircleClient {

	/**
	 * NioSocket实例
	 */
	private ForestMessageClient messageClient;
	/**
	 * 本地客户端信息
	 */
	private ClientInfo myInfo;
	/**
	 * 本地分享的资源url集合
	 */
	private final Set<String> myShare;

	private volatile ClientStatus clientStatus;
	private volatile ShareCircleClientCallback shareCircleClientCallback;
	private volatile ClientMessageHandler clientMessageHandler;
	private volatile Handler osHandler;
	private ShareServiceFileServer httpFileServer = null;
	private boolean disconnectByUser;
	
	public DefaultShareCircleClient(ClientInfo myInfo, Handler handler) {
		this.messageClient = new ForestMessageClient(this.clientEventsHandler);
		this.myInfo = myInfo;
		this.myShare = new HashSet<String>();
		this.osHandler = handler;
		this.clientStatus = ClientStatus.UNCONNECTED;
	}
	
	private ForestMessageClientEvents clientEventsHandler = new ForestMessageClientEvents() {

		@Override
		public void onConnectFailed() {
			osHandler.post(new Runnable() {
				@Override
				public void run() {
					shareCircleClientCallback.onConnectToServerFailed();
				}
			});
		}

		@Override
		public void onSessionOpened(NioSession session) {
			Log.d("ShareService", Thread.currentThread().getId() + " Client " + myInfo.getIp() + " session opened.");
			clientStatus = ClientStatus.CONNECTED;
			registerClient();
		}

		@Override
		public void onSessionClosed(NioSession session) {
			Log.d("ShareService", Thread.currentThread().getId() + " Client " + myInfo.getIp() + " session closed.");
			clientStatus = ClientStatus.UNCONNECTED;
			osHandler.post(new Runnable() {
				@Override
				public void run() {
					if (disconnectByUser) {
						shareCircleClientCallback.onDisconnectedByUser();
					} else {
						shareCircleClientCallback.onSessionClosed();
					}
				}
			});
		}

		@Override
		public void onMessageReceived(UserMessage message) {
			final ClientInfo client;
			final ResourceInfo resource;
			ShareCircleServerMessageType msgType = ShareCircleServerMessageType.parseFromInt(message.messageType);
			Log.d("ShareService", Thread.currentThread().getId() + " " + LogUtil.logServerMessageReceived(message));
			switch (msgType) {
			case ACCEPT_REGISTER:
				osHandler.post(new Runnable() {
					@Override
					public void run() {
						shareCircleClientCallback.onSessionOpened();
					}
				});
				break;
			case RETURN_SHARE_CIRCLE_INFO:
				final ShareCircleInfo shareCircleInfo = (ShareCircleInfo)message.messageData;
				osHandler.post(new Runnable() {
					public void run() {
						shareCircleClientCallback.onShareCircleInfoReturned(shareCircleInfo);
					}
				});
				break;
			case ACCEPT_JOIN:
				clientStatus = ClientStatus.JOINED;
				osHandler.post(new Runnable() {
					public void run() {
						shareCircleClientCallback.onJoinRequestAccepted();
					}
				});
				
				

				
				break;
			case REJECT_JOIN:
				osHandler.post(new Runnable() {
					public void run() {
						shareCircleClientCallback.onJoinRequestRejected();
					}
				});
				break;
			case CLIENT_JOINED:
				client = (ClientInfo)message.messageData;
				osHandler.post(new Runnable() {
					public void run() {
						clientMessageHandler.onClientJoined(client);
					}
				});
				
/*				UserMessage joinMessage = new UserMessage();
				joinMessage.messageType = ShareCircleClientMessageType.GET_ALL_SHARED_RESOURCES.intValue();
				joinMessage.messageData = myInfo;
				sendClientMessage(joinMessage);
				*/
				break;
			case CLIENT_EXITED:
				client = (ClientInfo)message.messageData;
				osHandler.post(new Runnable() {
					public void run() {
						clientMessageHandler.onClientExited(client);
					}
				});
				break;
			case RESOURCE_ADDED:
				resource = (ResourceInfo)message.messageData;
				osHandler.post(new Runnable() {
					public void run() {
						clientMessageHandler.onResourceAdded(resource);
					}
				});
				break;
			case RESOURCE_REMOVED:
				resource = (ResourceInfo)message.messageData;
				osHandler.post(new Runnable() {
					public void run() {
						clientMessageHandler.onResourceRemoved(resource);
					}
				});
				break;
			case SERVER_EXITED:
				messageClient.startClosing(false);
				osHandler.post(new Runnable() {
					public void run() {
						clientMessageHandler.onServerExited();
					}
				});
				break;
			case KICK_OFF:
				osHandler.post(new Runnable() {
					public void run() {
						clientMessageHandler.onKickedOff();
					}
				});
				break;
			case DATA_PACKET:
				final byte[] bytes = (byte[])message.messageData;
				osHandler.post(new Runnable() {
					public void run() {
						clientMessageHandler.onDataReceived(bytes);
					}
				});
				break;
			default:
				break;
			}
		}
		
	};
	
	@Override
	public void setCallback(ShareCircleClientCallback callback) {
		this.shareCircleClientCallback = callback;
	}
	
	@Override
	public void startHttpFileService() {
		if (this.httpFileServer == null) {
			this.httpFileServer = ShareServiceFileServer.getInstance();
		}
		try {
			httpFileServer.start();
		} catch (IOException e) {
			e.printStackTrace();
			osHandler.post(new Runnable() {				
				@Override
				public void run() {
					shareCircleClientCallback.onStartHttpFileServiceFailed();
				}
			});
		}
	}
	
	@Override
	public void connectToServer(String sIP) {
		this.messageClient.connectToServer(sIP);
		this.disconnectByUser = false;
	}
	
	@Override
	public void setMessageHandler(ClientMessageHandler handler) {
		this.clientMessageHandler = handler;
	}
	
	@Override
	public ClientInfo getMyInfo() {
		return this.myInfo.getCopy();
	}
	
	@Override
	public ClientStatus getClientStatus() {
		return this.clientStatus;
	}

	@Override
	public void reConnect(int maxAttempt) {
		this.messageClient.reconnect(maxAttempt);
	}
	
	@Override
	public void disconnect() {
		this.exitShareCircle();
		this.messageClient.disconnect();
		this.disconnectByUser = true;
	}
	
	private void registerClient() {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.REGISTER_CLIENT.intValue();
		msg.messageData = this.myInfo;
		sendClientMessage(msg);
	}
	
	@Override
	public void queryShareCircleInfo() {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.QUERY_SHARE_CIRCLE_INFO.intValue();
		sendClientMessage(msg);
	}

	@Override
	public void requestEnterShareCircle() {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.REQUEST_ENTER.intValue();
		msg.messageData = myInfo;
		sendClientMessage(msg);
	}
	
	@Override
	public void requestSynchronize() {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.REQUEST_SYNCHRONIZE.intValue();
		sendClientMessage(msg);
	}

	@Override
	public void getAllSharedResources() {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.GET_ALL_SHARED_RESOURCES.intValue();
		sendClientMessage(msg);
	}
	
	@Override
	public void getAllClients() {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.GET_ALL_CLIENTS.intValue();
		sendClientMessage(msg);
	}
	
	@Override
	public void downloadShare(final ResourceInfo resource, final String destPath, final ReceiveShareListener listener) {
		HttpDownloadFile download = new DefaultHttpDownloadFile();
		download.downloadShare(resource.getUrl(), destPath, listener);
	}

	@Override
	public void addShare(final ResourceInfo resource, final File file, final File thumbFile) {
		String thumbUrl = httpFileServer.addImage(myInfo.getIp(), thumbFile);
		resource.setThumbUrl(thumbUrl);
		resource.setUrl(httpFileServer.addFile(myInfo.getIp(), resource.getMd5(), file));
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.ADD_SHARE_RESOURCE.intValue();
		msg.messageData = resource;
		sendClientMessage(msg);
		if(!myShare.contains(resource.getUrl())){
			myShare.add(resource.getUrl());
		}
	}
	
	@Override
	public void removeSharedResource(final ResourceInfo resource) {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.REMOVE_SHARED_RESOURCE.intValue();
		msg.messageData = resource;
		sendClientMessage(msg);
		if(myShare.contains(resource.getUrl())){
			myShare.remove(resource.getUrl());
		}
	}

	@Override
	public boolean isMyShare(ResourceInfo info) {
		return myShare.contains(info.getUrl());
	}

	private void exitShareCircle() {
		if (httpFileServer != null) {
			httpFileServer.stop();
			httpFileServer = null;
		}
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.EXIT_SHARE_CIRCLE.intValue();
		msg.messageData = myInfo;
		sendClientMessage(msg);
		clientStatus = ClientStatus.CONNECTED;
	}
	
	@Override
	public void sendAutoDistributeData(final byte[] bytes) {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.AUTO_DISTRIBUTE_DATA.intValue();
		msg.messageData = bytes;
		sendClientMessage(msg);
	}

	@Override
	public void distributeDataToOthers(final byte[] bytes) {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.DISTRIBUTE_DATA_TO_OTHERS.intValue();
		msg.messageData = bytes;
		sendClientMessage(msg);
	}

	@Override
	public void sendData(final byte[] bytes) {
		UserMessage msg = new UserMessage();
		msg.messageType = ShareCircleClientMessageType.DATA_PACKET.intValue();
		msg.messageData = bytes;
		sendClientMessage(msg);
	}
	
	private void sendClientMessage(UserMessage message) {
		if (this.messageClient.isConnected()) {
			Log.d("ShareService", Thread.currentThread().getId() + " " + LogUtil.logClientMessageSend(message));
			this.messageClient.sendMessage(message);
		}
	}

}
