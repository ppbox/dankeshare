/**
 * 
 */
package com.chainton.dankeshare;

import com.chainton.dankeshare.ShareCircleInfo;

/**
 * 分享圈客户端回调接口
 * @author Rivers
 *
 */
public interface ShareCircleClientCallback {

	/**
	 * 启动http共享文件服务失败时回调
	 */
	void onStartHttpFileServiceFailed();
	
	/**
	 * 连接逻辑服务器失败时回调
	 */
	void onConnectToServerFailed();
		
	/**
	 * 与服务器连接会话创建成功时回调
	 */
	void onSessionOpened();
	
	/**
	 * 当服务器端返回分享圈信息时回调
	 * @param shareCircleInfo 服务器端返回的分享圈信息
	 */
	void onShareCircleInfoReturned(ShareCircleInfo shareCircleInfo);
	
	/**
	 * 分享圈创建者同意加入分享圈时回调
	 */
	void onJoinRequestAccepted();
	
	/**
	 * 加入分享圈被拒绝时回调
	 */
	void onJoinRequestRejected();

	/**
	 * 与服务器连接会话关闭时回调
	 */
	void onSessionClosed();
	
	/**
	 * 与服务器连接会话发生异常时回调
	 * @param cause 异常信息实例
	 */
	void onSessionExceptionCaught(Throwable cause);

}
