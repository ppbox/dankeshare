package com.chainton.dankeshare;

import java.io.File;

import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ResourceInfo;
import com.chainton.dankeshare.data.enums.ClientStatus;

/**
 * 分享圈客户端接口
 * @author 富林，Rivers
 *
 */
public interface ShareCircleClient {

	/**
	 * 设置客户端回调
	 */
	void setCallback(ShareCircleClientCallback callback);
	
	/**
	 * 启动文件共享服务
	 */
	void startHttpFileService();
	
	/**
	 * 发起与服务器的连接
	 * @param serverIP 逻辑服务器IP
	 */
	void connectToServer(String serverIP);
	
	/**
	 * 重新连接到当前服务器
	 * @param maxAttempt 最大尝试次数
	 */
	void reConnect(int maxAttempt);
	
	/**
	 * 断开与当前服务器的连接
	 */
	void disconnect();
	
	/**
	 * 查询当前分享圈信息
	 */
	void queryShareCircleInfo();
	
	/**
	 * 请求同步分享圈数据
	 */
	void requestSynchronize();

	/**
	 * 取得当前客户端信息
	 * @return 当前客户端信息
	 */
	ClientInfo getMyInfo();
	
	/**
	 * 取得客户端当前状态
	 * @return 客户端当前状态
	 */
	ClientStatus getClientStatus();
	
	/**
	 * 设置消息处理回调
	 * @param handler 客户端消息处理回调
	 */
	void setMessageHandler(ClientMessageHandler handler);
	
	/**
	 * 向服务器请求加入分享圈
	 */
	void requestEnterShareCircle();
	
	/**
	 * 添加一个分享到分享圈,同时将对应文件加入到http服务器
	 * @param resource 资源信息
	 * @param file	资源文件
	 * @param thumbFile	缩略图文件
	 */
	void addShare(ResourceInfo resource, File file, File thumbFile);
	
	/**
	 * 删除一个分享
	 * @param resource
	 */
	void removeSharedResource(ResourceInfo resource);

	/**
	 * 判断某一个共享资源是否为当前客户端所共享
	 * @param info 资源信息
	 * @return
	 */
	boolean isMyShare(ResourceInfo info);

	/**
	 * 向服务器请求当前分享圈内所有资源
	 */
	void getAllSharedResources();
	
	/**
	 * 向服务器请求当前分享圈内所有客户端, 包括自己
	 */
	void getAllClients();
	
	/**
	 * 主动退出分享圈
	 */
	void exitShareCircle();
	
	/**
	 * 下载资源
	 * @param resource 资源信息
	 * @param destPath 目标路径
	 * @param listener 回调实例
	 */
	void downloadShare(ResourceInfo resource, String destPath, ReceiveShareListener listener);
	
	/**
	 * 发送广播数据包
	 * @param bytes 数据包
	 */
	void sendAutoDistributeData(byte[] bytes);
	
	/**
	 * 给其他客户端发送数据包
	 * @param bytes 数据包
	 */
	void distributeDataToOthers(byte[] bytes);

	/**
	 * 向服务器发送数据包
	 * @param bytes 数据包
	 */
	void sendData(byte[] bytes);
	
}
