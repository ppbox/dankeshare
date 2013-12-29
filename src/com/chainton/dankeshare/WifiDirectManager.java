package com.chainton.dankeshare;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;

/**
 * WIFI直连管理接口
 * @author 富林
 *
 */
public interface WifiDirectManager {

	/**
	 * 获取关于设备列表的监听器对象，发现设备后会调用监听器里的方法
	 * @return
	 */
	PeerListListener getPeerListListener();
	
	/**
	 * 设置关于设备列表的监听器对象
	 * @param peerListListener
	 */
	void setPeerListListener(PeerListListener peerListListener);
	
	/**
	 * 获取直连连接成功的监听器对象，连接成功后会调用监听器里的方法
	 * @return
	 */
	ConnectionInfoListener getConnectionInfoListener();
	
	/**
	 * 设置直连连接成功的监听器对象
	 * @param connectionInfoListener
	 */
	void setConnectionInfoListener(ConnectionInfoListener connectionInfoListener);
	
	/**
	 * @param isWifiP2pEnabled wifi直连的可用状态
	 */
	void setIsWifiP2pEnabled(boolean isWifiP2pEnabled);
	
	/**
	 * 检查是否支持直连，即检查系统版本和wifi可用状态。并且toast提示
	 */
	boolean check();
	
	/**
	 * 注册 wifi direct 的广播接收者
	 */
	void registerReceiver();
	
	/**
	 * 解除对 wifi direct 的广播接收者的注册
	 */
	void unregisterReceiver();
	
	/**
	 * 当wifi direct 状态从可用变成不可用时，清除一些数据，比如在显示发现的设备列表界面
	 */
	void resetData();
	
	/**
	 * 发现搜索设备
	 */
	void discoverPeers();
	
	/**
	 * 连接设备
	 * @param device 要连接的设备
	 */
	void connect(WifiP2pDevice device, WifiDirectConnectCallback callback);
	
	/**
	 * 更新设备的状态
	 * @param device
	 */
	void updateThisDevice(WifiP2pDevice device);
}
