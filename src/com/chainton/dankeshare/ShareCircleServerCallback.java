/**
 * 
 */
package com.chainton.dankeshare;

/**
 * @author Rivers
 *
 */
public interface ShareCircleServerCallback {
	
	/**
	 * 服务器启动成功时回调
	 */
	void onServerStarted();
	
	/**
	 * 服务器启动失败时回调
	 */
	void onServerStartFailed();
	
}
