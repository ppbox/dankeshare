/**
 * 
 */
package com.chainton.dankeshare;

import com.chainton.dankeshare.data.ClientInfo;

/**
 * 分享圈服务端消息处理回调接口
 * @author Rivers
 *
 */
public interface ServerMessageHandler {
	
	/**
	 * 服务端收到客户端加入请求时回调
	 * @param client 客户端信息
	 */
	void onClientRequestEnter(ClientInfo client);
	
	/**
	 * 指定客户端请求同步当前分享圈状态回调
	 * @param client 客户端信息
	 */
	void onClientRequestSynchronize(ClientInfo client);
	
	/**
	 * 客户端会话关闭时回调
	 * @param client 客户端信息
	 */
	void onClientSessionClosed(ClientInfo client);
	
	/**
	 * 服务端接收到数据包时回调
	 * @param bytes 数据包
	 */
	void onDataReceived(ClientInfo client, byte[] bytes);
}
