/**
 * 
 */
package com.chainton.dankeshare.util;

import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ResourceInfo;
import com.chainton.dankeshare.data.enums.ShareCircleClientMessageType;
import com.chainton.dankeshare.data.enums.ShareCircleServerMessageType;
import com.chainton.forest.core.message.UserMessage;

/**
 * 转换客户端／服务端消息至日志字符串的工具类
 * @author Rivers
 *
 */
public final class LogUtil {

	/**
	 * 将服务器端收到的客户端消息转为日志字符串
	 * @param message 客户端消息
	 * @param client 发送此消息的客户端信息
	 * @return
	 */
	public static String logClientMessageReceived(UserMessage message, ClientInfo client) {
		StringBuilder logMsg = new StringBuilder();
		ShareCircleClientMessageType msgType = ShareCircleClientMessageType.parseFromInt(message.messageType);
		ResourceInfo resource;
		byte[] bytes;
		
		logMsg.append("Server received ");
		logMsg.append(msgType.valueText());
		logMsg.append(" messsage from client ");
		logMsg.append(client.getIp());
		switch (msgType) {
		case ADD_SHARE_RESOURCE:
		case REMOVE_SHARED_RESOURCE:
			resource = (ResourceInfo)message.messageData;
			logMsg.append(" Resource: ");
			logMsg.append(resource.getUrl());
			break;
		case AUTO_DISTRIBUTE_DATA:
		case DISTRIBUTE_DATA_TO_OTHERS:
		case DATA_PACKET:
			bytes = (byte[])message.messageData;
			if (bytes != null) {
				logMsg.append(" Data length: ");
				logMsg.append(bytes.length);
			}
			break;
		default:
			break;
		}
		return logMsg.toString();
	}

	/**
	 * 将客户端发送的消息转为日志字符串
	 * @param message 客户端消息
	 * @return
	 */
	public static String logClientMessageSend(UserMessage message) {
		StringBuilder logMsg = new StringBuilder();
		ShareCircleClientMessageType msgType = ShareCircleClientMessageType.parseFromInt(message.messageType);
		ResourceInfo resource;
		byte[] bytes;
		
		logMsg.append("Client send ");
		logMsg.append(msgType.valueText());
		logMsg.append(" messsage.");
		switch (msgType) {
		case ADD_SHARE_RESOURCE:
		case REMOVE_SHARED_RESOURCE:
			resource = (ResourceInfo)message.messageData;
			logMsg.append(" Resource: ");
			logMsg.append(resource.getUrl());
			break;
		case AUTO_DISTRIBUTE_DATA:
		case DISTRIBUTE_DATA_TO_OTHERS:
		case DATA_PACKET:
			bytes = (byte[])message.messageData;
			if (bytes != null) {
				logMsg.append(" Data length: ");
				logMsg.append(bytes.length);
			}
			break;
		default:
			break;
		}
		return logMsg.toString();
	}

	/**
	 * 将客户端收到的消息转为日志字符串
	 * @param message 服务器端消息
	 * @return
	 */
	public static String logServerMessageReceived(UserMessage message) {
		StringBuilder logMsg = new StringBuilder();
		ShareCircleServerMessageType msgType = ShareCircleServerMessageType.parseFromInt(message.messageType);
		ClientInfo client;
		ResourceInfo resource;
		byte[] bytes;

		logMsg.append("Client received ");
		logMsg.append(msgType.valueText());
		logMsg.append(" message.");
		switch (msgType) {
		case CLIENT_JOINED:
			client = (ClientInfo)message.messageData;
			logMsg.append(" ");
			logMsg.append(client.getIp());
			logMsg.append(" joined");
			break;
		case CLIENT_EXITED:
			client = (ClientInfo)message.messageData;
			logMsg.append(" ");
			logMsg.append(client.getIp());
			logMsg.append(" exited");
			break;
		case RESOURCE_ADDED:
		case RESOURCE_REMOVED:
			resource = (ResourceInfo)message.messageData;
			logMsg.append(" Resource: ");
			logMsg.append(resource.getUrl());
			break;
		case DATA_PACKET:
			bytes = (byte[])message.messageData;
			if (bytes != null) {
				logMsg.append(" Data length: ");
				logMsg.append(bytes.length);
			}
			break;
		default:
			break;
		}
		return logMsg.toString();
	}

	/**
	 * 将服务端发送的消息转为日志字符串
	 * @param message 服务器端消息实例
	 * @param tgtClient 发送目标客户端
	 * @return
	 */
	public static String logServerMessageSend(UserMessage message, ClientInfo tgtClient) {
		StringBuilder logMsg = new StringBuilder();
		ShareCircleServerMessageType msgType = ShareCircleServerMessageType.parseFromInt(message.messageType);
		ClientInfo client;
		ResourceInfo resource;
		byte[] bytes;

		logMsg.append("Server send ");
		logMsg.append(msgType.valueText());
		logMsg.append(" message to client ");
		logMsg.append(tgtClient.getIp());
		switch (msgType) {
		case CLIENT_JOINED:
			client = (ClientInfo)message.messageData;
			logMsg.append(" ");
			logMsg.append(client.getIp());
			logMsg.append(" joined");
			break;
		case CLIENT_EXITED:
			client = (ClientInfo)message.messageData;
			logMsg.append(" ");
			logMsg.append(client.getIp());
			logMsg.append(" exited");
			break;
		case RESOURCE_ADDED:
		case RESOURCE_REMOVED:
			resource = (ResourceInfo)message.messageData;
			logMsg.append(" Resource: ");
			logMsg.append(resource.getUrl());
			break;
		case DATA_PACKET:
			bytes = (byte[])message.messageData;
			if (bytes != null) {
				logMsg.append(" Data length: ");
				logMsg.append(bytes.length);
			}
			break;
		default:
			break;
		}
		return logMsg.toString();
	}

}
