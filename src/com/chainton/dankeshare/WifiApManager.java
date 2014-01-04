package com.chainton.dankeshare;

import android.net.wifi.WifiConfiguration;

/**
 * 热点管理接口
 * @author 富林
 *
 */
public interface WifiApManager {
	/**
	 * 打开热点回调接口
	 * @author 富林
	 *
	 */
	interface WifiApOpenListener{
		
		/**
		 * 热点打开成功时回调
		 */
		void onStartSucceed();
		
		/**
		 * 热点打开失败时回调
		 */
		void onStartFailed();
	}
	
	/**
	 * 关闭热点回调接口
	 * @author 富林
	 *
	 */
	interface WifiApCloseListener{
		/**
		 * 热点关闭成功回调
		 */
		void onCloseSucceed();
		/**
		 * 热点关闭失败回调
		 */
		void onCloseFailed();
	}
	
	/**
	 * 创建热点配置信息
	 * @param ssid 要创建的热点的ssid
	 * @param shareKey 要创建的热点的密码
	 * @return	热点配置信息对象
	 */
	WifiConfiguration createWifiApConfig(String ssid, String shareKey);
	
	/**
	 * 打开热点
	 * @param config 热点配置信息
	 * @param openListener	打开热点回调接口实例
	 */
	void openWifiAp(WifiConfiguration config, WifiApOpenListener openListener);
	
	/**
	 * 关闭热点
	 * @param closeListener 关闭热点回调接口实例
	 */
	void closeWifiAp(WifiApCloseListener closeListener);
	
	/**
	 * 保存当前的WIFI配置
	 */
	void saveWifiState();
	
	/**
	 * 恢复之前保存的WIFI配置
	 */
	void restoreWifiState();
	
	/**
	 * 获取本地ip地址
	 * @return 本地ip地址
	 */
	String getApLocalIp();
}
