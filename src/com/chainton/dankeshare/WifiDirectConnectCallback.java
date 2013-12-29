/**
 * 
 */
package com.chainton.dankeshare;

/**
 * 创建Wifi直连连接回调接口
 * @author Rivers
 *
 */
public interface WifiDirectConnectCallback {

	/**
	 * 成功时回调
	 */
	void onSuccess();
	
	/**
	 * 失败时回调
	 */
	void onFailure();

}
