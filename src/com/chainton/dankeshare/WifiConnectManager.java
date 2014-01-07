package com.chainton.dankeshare;

import com.chainton.dankeshare.WifiApManager.RestoreWifiStateResult;

import android.net.wifi.ScanResult;

/**
 * WIFI连接管理接口
 * @author 富林
 *
 */
public interface WifiConnectManager {
	
	/**
	 * WIFI连接回调接口
	 * @author 富林
	 *
	 */
	public interface ConnectCallback{
		/**
		 * 连接成功时回调
		 * @param ssid SSID
		 * @param gatewayIp 网关的ip地址
		 * @param localIp	本地的ip地址
		 */
		void onConnectSuccess(String ssid, String gatewayIp, String localIp);
		
		/**
		 * 连接失败时回调
		 * @param errorMessage 
		 */
		void onConnectFailed(String errorMessage);
	}
	
	/**
	 * 搜索热点
	 */
	void searchWifiAp();
	
	/**
	 * 连接到指定的热点
	 * @param scanResult 指定的要连接的热点信息
	 * @param shareKey	热点密码
	 * @param callback	连接回调接口实例
	 */
	void connectWifi(ScanResult scanResult, String shareKey, ConnectCallback callback);
	
	/**
	 * 保存当前的WIFI配置
	 */
	void saveWifiState();
	
	/**
	 * 恢复之前保存的WIFI配置
	 */
	void restoreWifiState(RestoreWifiStateResult result);
	
}
