package com.chainton.dankeshare.service;

import java.io.File;

import android.content.Context;
import android.util.Log;

import com.chainton.dankeshare.OperationResult;
import com.chainton.dankeshare.ShareCircleClient;
import com.chainton.dankeshare.ShareCircleServer;
import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ShareCircleAppInfo;
import com.chainton.dankeshare.data.ShareCircleInfo;
import com.chainton.dankeshare.data.enums.ShareCircleType;
import com.chainton.dankeshare.util.LogUtil;
import com.chainton.dankeshare.wifi.WifiApShareCircleInfo;
import com.chainton.dankeshare.wifi.util.WifiApManagerAdmin;
import com.chainton.dankeshare.wifi.util.WifiSSIDNameCodec;
import com.chainton.dankeshare.wifi.util.WifiUtil;

/**
 * 分享圈服务包装接口的缺省实现 本类的实现包含了对于安卓手机Wifi模块的一些基本操作和设置，以简化应用层处理。本类为单例。
 */
public final class WifiApController {
	/**
	 * android上下文
	 */
	//private Context mApplicationContext;

	/**
	 * 唯一实例
	 */
	private static WifiApController instance;

	/**
	 * 单例锁
	 */
	private static final Object LOCK = new Object();

	private boolean isServer = false;
	private String serverIp = null;
	
	private TaskProcessor mTaskProcessor;
	private WifiApManagerAdmin mWifiApManagerAdmin;
	/**
	 * 私有构造函数, 单例
	 * 
	 * @param context
	 */
	private WifiApController() {
		
	}

	/**
	 * 获取接口实现单例
	 * 
	 * @return 接口实现实例
	 */
	public static WifiApController getInstance() {
		if (instance == null) {
			synchronized (LOCK) {
				if (instance == null) {
					instance = new WifiApController();
				}
			}
		}
		return instance;
	}

	/**
	 * 创建服务，第一次使用其他方法前调用
	 * 
	 * @param context
	 *            android上下文
	 */
	public void create(final Context context) {
		// get the task processor instance for next process
		mTaskProcessor = TaskProcessor.getTaskProcessorInstance(context);
		mWifiApManagerAdmin = new WifiApManagerAdmin(context);
		saveWifiState();
	}

	/**
	 * 恢复服务
	 * 
	 * @param context
	 *            android上下文
	 */
	public void resume(final Context context) {
		// get the task processor instance for next process
		mTaskProcessor = TaskProcessor.getTaskProcessorInstance(context);
		mWifiApManagerAdmin = new WifiApManagerAdmin(context);
	}

	/**
	 * 销毁服务，释放相应资源 在应用退出时调用一次即可
	 */
	public void destroy() {
		Log.e(LogUtil.LOG_TAG,
				"destroydestroydestroydestroydestroydestroydestroydestroydestroydestroydestroydestroydestroy..........");
		restoreWifiState(null);
	}

	/**
	 * 取得当前任务处理机状态
	 * 
	 * @return 当前任务处理机状态
	 */
	public int getServiceStatus() {
		return mTaskProcessor.getTaskProcessorDetailStatus();
	}

	/**
	 * 用缺省实现恢复之前保存的WIFI配置
	 */
	public void restoreWifiState(OperationResult result) {
		mWifiApManagerAdmin.restoreWifiState(result);
	}

	/**
	 * 用缺省实现保存当前的WIFI配置
	 */
	public void saveWifiState() {
		mWifiApManagerAdmin.saveWifiState();
	}

	/**
	 * 创建分享圈
	 * 
	 * @param shareCircleName
	 *            分享圈名字
	 * @param shareCircleType
	 *            分享圈类型
	 * @param appInfo
	 *            创建分享圈的应用程序信息
	 * @param selfInfo
	 *            本地的客户端信息
	 * @param maxClients
	 *            分享圈允许接入的最大客户端数
	 * @param createResult
	 *            创建操作结果回调
	 * @param server
	 *            分享圈服务端实例（ShareCircleServer的自定义实现类）
	 * @param client
	 *            分享圈客户端实例（ShareCircleClient的自定义实现类）
	 * @param context
	 *            UI上下文
	 */
	public void setApServerState(String shareCircleName, ShareCircleType shareCircleType, ShareCircleAppInfo appInfo,
			ClientInfo selfInfo, int maxClients, WifiControllerCallBack createApResultCallback, ShareCircleServer server,
			ShareCircleClient client, Context context) {
		TargetStatusInfo targetStatusInfo = new TargetStatusInfo();
		final WifiApShareCircleInfo wifiApShareCircleInfo = new WifiApShareCircleInfo(shareCircleName, appInfo);
		
		wifiApShareCircleInfo.ssid = WifiSSIDNameCodec.encodeWifiApName(shareCircleName, appInfo);
		wifiApShareCircleInfo.shareKey = WifiSSIDNameCodec.generateSharedKey(shareCircleName, appInfo);
		Log.d(LogUtil.LOG_TAG, "Create SSID " + wifiApShareCircleInfo.ssid + " with sharedKey "
				+ wifiApShareCircleInfo.shareKey);
		Log.e(LogUtil.LOG_TAG_NEW, " TaskProcessorStatus : " + mTaskProcessor.getTaskProcessorStatus());
		if (mTaskProcessor.getTaskProcessorStatus() != TaskProcessor.TASKPROCESSOR_RUNNING) {
			mTaskProcessor.startTaskProcessor();
		}
		targetStatusInfo.statusTaskType = TaskProcessor.TASK_TYPE_AP;
		targetStatusInfo.SSID = wifiApShareCircleInfo.ssid;
		targetStatusInfo.passkey = wifiApShareCircleInfo.shareKey;
		targetStatusInfo.secretType = WifiUtil.TYPE_WPA;
		targetStatusInfo.resultCallback = createApResultCallback;
		targetStatusInfo.shareCircleClient = client;
		targetStatusInfo.shareCircleServer = server;
		targetStatusInfo.shareCircleInfo = wifiApShareCircleInfo;
		targetStatusInfo.selfInfo = selfInfo;
		mTaskProcessor.setTargetWifiStatus(targetStatusInfo);
	}

	/**
	 * 创建分享圈客户端
	 * 
	 * @param circleInfo
	 *            分享圈的信息
	 * @param selfInfo
	 *            客户端信息
	 * @param result
	 *            创建操作结果回调
	 * @param client
	 *            分享圈客户端实例（ShareCircleClient的自定义实现类）
	 * @param context
	 *            UI上下文
	 */
	public void setConnectClientState(ShareCircleInfo circleInfo, ClientInfo selfInfo, WifiControllerCallBack createClientResultCallback,
			ShareCircleClient client, Context context) {	
		TargetStatusInfo targetStatusInfo = new TargetStatusInfo();
		WifiApShareCircleInfo waci = (WifiApShareCircleInfo)circleInfo;
		
		Log.d(LogUtil.LOG_TAG, "Connect to SSID: " + waci.scanResult.SSID + " - sharekey: " + waci.shareKey);
		Log.e(LogUtil.LOG_TAG_NEW, " TaskProcessorStatus : " + mTaskProcessor.getTaskProcessorStatus());
		if (mTaskProcessor.getTaskProcessorStatus() != TaskProcessor.TASKPROCESSOR_RUNNING) {
			mTaskProcessor.startTaskProcessor();
		}
		targetStatusInfo.statusTaskType = TaskProcessor.TASK_TYPE_CONNECT;
		targetStatusInfo.SSID = waci.scanResult.SSID;
		targetStatusInfo.passkey = waci.shareKey;
		targetStatusInfo.secretType = WifiUtil.TYPE_WPA;
		targetStatusInfo.resultCallback = createClientResultCallback;
		targetStatusInfo.shareCircleClient = client;
		targetStatusInfo.shareCircleServer = null;
		targetStatusInfo.shareCircleInfo = circleInfo;
		targetStatusInfo.selfInfo = selfInfo;
		mTaskProcessor.setTargetWifiStatus(targetStatusInfo);
	}

	/**
	 * 启动搜索分享圈的后台任务线程
	 * 
	 * @param appInfo
	 *            创建分享圈的应用程序信息
	 * @param scType
	 *            要搜索分享圈的类型
	 * @param callback
	 *            搜索分享圈回调实例
	 * @param context
	 *            UI上下文
	 * @param autoStop
	 *            WIFI未打开时是否自动停止搜索
	 */
	public void setSearchShareCircleState(ShareCircleAppInfo appInfo, ShareCircleType scType,
			WifiControllerCallBack searchResultCallback, Context context, boolean autoStop) {
		TargetStatusInfo targetStatusInfo = new TargetStatusInfo();
		
		if (mTaskProcessor.getTaskProcessorStatus() != TaskProcessor.TASKPROCESSOR_RUNNING) {
			mTaskProcessor.startTaskProcessor();
		}
		targetStatusInfo.statusTaskType = TaskProcessor.TASK_TYPE_SEARCH;
		targetStatusInfo.secretType = WifiUtil.TYPE_WPA;
		targetStatusInfo.resultCallback = searchResultCallback;
		targetStatusInfo.shareCircleInfo = new ShareCircleInfo("ForSearch", ShareCircleType.WIFIAP, appInfo);
		mTaskProcessor.setTargetWifiStatus(targetStatusInfo);
	}
	
	/**
	 * 设置当前状态为Http文件分享状态
	 * @param ssid
	 * @param resultCallback
	 * @param file
	 */
	public void setHttpShareOneFileState(String ssid, WifiControllerCallBack resultCallback, File file){
		TargetStatusInfo targetStatusInfo = new TargetStatusInfo();

		if (mTaskProcessor.getTaskProcessorStatus() != TaskProcessor.TASKPROCESSOR_RUNNING) {
			mTaskProcessor.startTaskProcessor();
		}
		targetStatusInfo.statusTaskType = TaskProcessor.TASK_TYPE_HTTP_SHARE;
		targetStatusInfo.SSID = ssid;
		targetStatusInfo.resultCallback = resultCallback;
		targetStatusInfo.fileForShare = file;
		mTaskProcessor.setTargetWifiStatus(targetStatusInfo);
	}
	
	/**
	 * 设置当前状态为空闲状态
	 */
	public void setIdleState(){
		TargetStatusInfo targetStatusInfo = new TargetStatusInfo();
		
		if (mTaskProcessor.getTaskProcessorStatus() != TaskProcessor.TASKPROCESSOR_RUNNING) {
			mTaskProcessor.startTaskProcessor();
		}
		targetStatusInfo.statusTaskType = TaskProcessor.TASK_TYPE_IDLE;
		mTaskProcessor.setTargetWifiStatus(targetStatusInfo);
	}
}
