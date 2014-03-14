package com.chainton.dankeshare.service;

import java.util.Collection;

import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ShareCircleInfo;

public interface WifiControllerCallBack {
	/**
	 * wifi操作返回接口定义 AP_：热点开启关闭状态定义 CONNECT_：连接开启关闭状态定义 SEARCH_：搜索开启关闭状态定义
	 */

	/**
	 * 服务器端回调
	 */
	public void onApServerStateOK(ShareCircleInfo shareCircleInfo);

	public void onApServerStateFailed();

	public void onApServerStateExitOK();

	public void onApServerStateExitFailed();

	public void onApServerStateExitAbnormal();

	// 客户端有人退出的时候调用
	public void onClientExit(ClientInfo client);

	/**
	 * 客户端回调
	 */
	public void onApClientStateOK();

	public void onApClientStateFailed();

	/**
	 * exitInfo用来标记退出的原因
	 * DISCONNECT_NORMAL表示正常断开； DISCONNECT_BY_USER表示用户自己断开；
	 * DISCONNECT_BY_SERVER表示被服务器端断开； DISCONNECT_BY_EXCEPTION表示异常情况断开
	 */
	public void onApClientStateExitOK(int exitInfo);

	public void onApClientStateExitFailed();

	public void onSearchShareStateOK();

	public void onSearchNewScanResult(Collection<ShareCircleInfo> scanResultList);

	public void onSearchShareStateFailed();

	public void onSearchShareStateExitOK();

	public void onSearchShareStateExitFailed();

	/**
	 * 自分享回调
	 */
	public void onHttpApStateOK();

	public void onHttpApStateOK(String url);

	public void onHttpApStateFailed();

	public void onHttpApStateExitOK();

	public void onHttpApStateExitFailed();
}