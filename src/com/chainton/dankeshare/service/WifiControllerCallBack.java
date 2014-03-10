package com.chainton.dankeshare.service;

import java.util.Collection;

import com.chainton.dankeshare.data.ShareCircleInfo;

public interface WifiControllerCallBack {
	/**
	 * wifi操作返回接口定义
	 * 		AP_：热点开启关闭状态定义
	 * 		CONNECT_：连接开启关闭状态定义
	 * 		SEARCH_：搜索开启关闭状态定义
	 */
	public void onApCreateOK();
	public void onApCreateFailed();
	public void onApStopOK();
	public void onApStopFailed();
	public void onApServiceCreateOK(ShareCircleInfo shareCircleInfo);
	public void onApServiceCreateFailed();
	public void onApServiceStopOK();
	public void onApServiceStopFailed();
	
	public void onConnectOK();
	public void onConnectFailed();
	public void onDisconnectOK();
	public void onDisconnectFailed();
	public void onConnectServiceOK();
	public void onConnectServiceFailed();
	public void onDisconnectServiceOK();
	public void onDisconnectServiceFailed();
	
	public void onSearchOK();
	public void onSearchNewScanResult(Collection<ShareCircleInfo> scanResultList);
	public void onSearchFailed();
	public void onSearchStopOK();
	public void onSearchStopFailed();
	
	public void onHttpApStartOK();
	public void onHttpApStartFailed();
	public void onHttpApStopOK();
	public void onHttpApStopFailed();
	public void onHttpServiceStartOK(String url);
	public void onHttpServiceStartOK();
	public void onHttpServiceStartFailed();
	public void onHttpServiceStopOK();
	public void onHttpServiceStopFailed();
}
