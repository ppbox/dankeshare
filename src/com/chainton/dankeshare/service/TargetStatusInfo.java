package com.chainton.dankeshare.service;

import java.io.File;

import com.chainton.dankeshare.ShareCircleClient;
import com.chainton.dankeshare.ShareCircleServer;
import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ShareCircleInfo;
import com.chainton.dankeshare.wifi.util.WifiUtil;

/**
 * 用来存放设置目标状态的基本信息
 * 
 * @author liyunjun
 * 
 */
public class TargetStatusInfo {
	/**
	 * 取值：TASK_TYPE_AP; TASK_TYPE_CLIENT; TASK_TYPE_SEARCH;
	 */
	public int statusTaskType;
	/**
	 * ssid及passkey，用来创建热点或者连接指定热点
	 */
	public String SSID;
	public String passkey;
	public int secretType = WifiUtil.TYPE_WPA;
	public WifiControllerCallBack resultCallback;
	// 分享圈相关信息
	public ShareCircleInfo shareCircleInfo;
	// shareCircleServer, shareCircleClient引用
	public ShareCircleServer shareCircleServer;
	public ShareCircleClient shareCircleClient;
	public ClientInfo selfInfo;
	/**
	 * 当前任务的处理状态 取值：STATUS_TASK_WAIT; STATUS_TASK_STARTING; STATUS_TASK_OK;
	 * STATUS_TASK_STOPPING; STATUS_TASK_STOP
	 */
	public int taskStatus = TaskProcessor.STATUS_TASK_WAITING;
	public File fileForShare;

	public void clear() {
		statusTaskType = 0;
		SSID = "";
		passkey = "";
		secretType = 0;
		resultCallback = null;
		shareCircleInfo = null;
		shareCircleServer = null;
		shareCircleClient = null;
		selfInfo = null;
		fileForShare = null;
		taskStatus = TaskProcessor.STATUS_TASK_WAITING;
	}

	@Override
	public String toString() {
		return "TargetStatusInfo [statusTaskType=" + statusTaskType + ", SSID=" + SSID + ", passkey=" + passkey
				+ ", secretType=" + secretType + ", resultHandler=" + resultCallback + ", shareCircleInfo="
				+ shareCircleInfo + ", shareCircleServer=" + shareCircleServer + ", shareCircleClient="
				+ shareCircleClient + ", selfInfo=" + selfInfo + ", taskStatus=" + taskStatus + "]";
	}

}