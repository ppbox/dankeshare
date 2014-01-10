package com.chainton.dankeshare;

import java.util.List;

import com.chainton.dankeshare.data.ClientInfo;

/**
 * 分享圈服务端接口
 * @author 富林
 *
 */
public interface ShareCircleServer {
	
	/**
	 * 启动服务器开始监听
	 */
	void startServer(ServerMessageHandler handler, CreateShareCircleServerCallback callback);
	
	/**
	 * 取得当前服务端信息
	 * @return 当前服务端信息
	 */
	ClientInfo getMyInfo();
		
	/**
	 * 接收客户端加入分享圈
	 * @param client 客户端信息
	 */
	void acceptClient(ClientInfo client);
	
	/**
	 * 拒绝客户端加入分享圈
	 * @param client 客户端信息
	 */
	void rejectClient(ClientInfo client);
	
	/**
	 * 通知指定客户端分享圈内的已有客户端
	 * @param client 客户端信息
	 */
	void informAllAcceptedClients(ClientInfo client);

	/**
	 * 通知指定客户端分享圈内的所有已分享资源
	 * @param client 客户端信息
	 */
	void informAllSharedResources(ClientInfo client);
	
	/**
	 * 取得已经在分享圈内的所有客户端信息
	 * @return 已经在分享圈内的所有客户端信息
	 */
	List<ClientInfo> getAllAcceptedClients();
	
	/**
	 * 解散分享圈, 同时释放资源
	 */
	void destroyServer();
	
	/**
	 * 返回服务器是否正在关闭
	 * @return 服务器是否正在关闭
	 */
	boolean isClosing();
	
	/**
	 * 返回服务端是否已经停止
	 * @return 服务端是否已经停止
	 */
	boolean isStopped();
	
	/**
	 * 将某个客户端踢出分享圈
	 * @param client 被踢出分享圈的客户端信息
	 */
	void kickOffClient(ClientInfo client);
	
	/**
	 * 分发服务端处理后的数据
	 * @param bytes 数据包
	 */
	void distributeDataToAllClients(byte[] bytes);
	
	/**
	 * 向指定客户端发送数据包
	 * @param client 目标客户端
	 * @param bytes 数据包
	 */
	void sendDataToClient(ClientInfo client, byte[] bytes);
	
}
