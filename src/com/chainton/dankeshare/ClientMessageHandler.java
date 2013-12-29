package com.chainton.dankeshare;

import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ResourceInfo;

/**
 * 分享圈客户端消息处理回调接口
 * @author Rivers
 * 
 */
public interface ClientMessageHandler {
	
	/**
	 * 分享圈创建者解散分享圈时回调
	 */
	void onServerExited();
	
	/**
	 * 被分享圈创建者踢出分享圈时回调
	 */
	void onKickedOff();

	/**
	 * 有用户添加分享资源时回调
	 * @param resource 资源信息
	 */
	void onResourceAdded(ResourceInfo resource);
	
	/**
	 * 有用户删除分享资源时回调
	 * @param resource 资源信息
	 */
	void onResourceRemoved(ResourceInfo resource);
	
	/**
	 * 有新客户端加入时回调
	 * @param info 新加入的客户端信息
	 */
	void onClientJoined(ClientInfo info);
	
	/**
	 * 有客户端离开时回调
	 * @param info 离开用户的信息
	 */
	void onClientExited(ClientInfo info);
	
	/**
	 * 接收到字节流数据包时回调
	 */
	void onDataReceived(byte[] bytes);

}
