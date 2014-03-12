package com.chainton.dankeshare.service;

import java.util.Collection;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.chainton.dankeshare.OperationResult;
import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ShareCircleInfo;
import com.chainton.dankeshare.impl.HotspotHttpFileService;
import com.chainton.dankeshare.util.LogUtil;
import com.chainton.dankeshare.wifi.util.WifiAdmin;
import com.chainton.dankeshare.wifi.util.WifiApAdmin;
import com.chainton.dankeshare.wifi.util.WifiApManagerAdmin;
import com.chainton.dankeshare.wifi.util.WifiUtil;
import com.chainton.forest.core.util.GlobalUtil;

public class TaskProcessor extends Handler {
	/**
	 * 空闲状态
	 */
	public static final int TASK_TYPE_IDLE = 0x00;
	/**
	 * 创建热点／取消热点
	 */
	public static final int TASK_TYPE_AP = 0x10;
	/**
	 * 创建客户端并连接服务／取消创建
	 */
	public static final int TASK_TYPE_CONNECT = 0x20;
	/**
	 * 搜索可用热点／取消搜索
	 */
	public static final int TASK_TYPE_SEARCH = 0x30;
	/**
	 * 自分享Http服务状态
	 */
	public static final int TASK_TYPE_HTTP_SHARE = 0x40;
	/**
	 * TaskProcessor状态
	 */
	public static final int TASKPROCESSOR_RUNNING = 0;
	public static final int TASKPROCESSOR_CANCELING = 1;
	public static final int TASKPROCESSOR_STOP = 2;

	/**
	 * 任务状态
	 */
	public static final int STATUS_TASK_WAITING = 0;
	public static final int STATUS_TASK_STARTING = 1;
	public static final int STATUS_TASK_OK = 2;
	public static final int STATUS_TASK_STOPPING = 3;
	public static final int STATUS_TASK_STOP = 4;

	private Context mContext;

	// 当前正在做的任务
	private TargetStatusInfo mCurrentState = null;
	private TargetStatusInfo mNextState = null;
	/**
	 * 任务处理器运行状态 取值：TASKPROCESSOR_RUNNING; TASKPROCESSOR_CANCELING;
	 * TASKPROCESSOR_STOPING
	 */
	private int mTaskProcessorStatus = TASKPROCESSOR_STOP;
	private int mTaskProcessorDetailStatus = WifiUtil.TASK_PROCESSOR_STOP;
	// 是否停止任务处理器标志
	private int mIsStopTaskProcessor = 0;

	private WifiApManagerAdmin mWifiApManagerAdmin;
	private WifiApAdmin mWifiApAdmin;
	private WifiAdmin mWifiAdmin;
	private TaskProcessor selfHandler;

	public TaskProcessor(Context context) {
		super();
		// TODO Auto-generated constructor stub
		mContext = context;
		mWifiApManagerAdmin = new WifiApManagerAdmin(mContext);
		mWifiAdmin = new WifiAdmin(mContext);
		mWifiApAdmin = new WifiApAdmin(mContext);
		mIsStopTaskProcessor = 0;
		mTaskProcessorStatus = TASKPROCESSOR_STOP;
		selfHandler = this;
		mCurrentState = null;
		mNextState = null;
	}

	public TaskProcessor(Context context, Callback callback) {
		super(callback);
		// TODO Auto-generated constructor stub
		mContext = context;
		mWifiApManagerAdmin = new WifiApManagerAdmin(mContext);
		mWifiAdmin = new WifiAdmin(mContext);
		mWifiApAdmin = new WifiApAdmin(mContext);
		mIsStopTaskProcessor = 0;
		mTaskProcessorStatus = TASKPROCESSOR_STOP;
		selfHandler = this;
		mCurrentState = null;
		mNextState = null;
	}

	public TaskProcessor(Context context, Looper looper, Callback callback) {
		super(looper, callback);
		// TODO Auto-generated constructor stub
		mContext = context;
		mWifiApManagerAdmin = new WifiApManagerAdmin(mContext);
		mWifiAdmin = new WifiAdmin(mContext);
		mWifiApAdmin = new WifiApAdmin(mContext);
		mIsStopTaskProcessor = 0;
		mTaskProcessorStatus = TASKPROCESSOR_STOP;
		selfHandler = this;
		mCurrentState = null;
		mNextState = null;
	}

	public TaskProcessor(Context context, Looper looper) {
		super(looper);
		// TODO Auto-generated constructor stub
		mContext = context;
		mWifiApManagerAdmin = new WifiApManagerAdmin(mContext);
		mWifiAdmin = new WifiAdmin(mContext);
		mWifiApAdmin = new WifiApAdmin(mContext);
		mIsStopTaskProcessor = 0;
		mTaskProcessorStatus = TASKPROCESSOR_STOP;
		selfHandler = this;
		mCurrentState = null;
		mNextState = null;
	}

	private static TaskProcessor mTaskProcessorInstance;

	public static TaskProcessor getTaskProcessorInstance(Context context) {
		// 如果对象为空，则创建，否则直接返回
		synchronized (context) {
			if (mTaskProcessorInstance == null) {
				mTaskProcessorInstance = new TaskProcessor(context);
			}
		}
		return mTaskProcessorInstance;
	}

	/**
	 * 后台任务运行 1、处理添加进来的任务 2、维护当前任务和下一个任务的处理
	 */
	public void startTaskProcessor() {
		if (getTaskProcessorStatus() != TASKPROCESSOR_RUNNING) {
			Log.w(LogUtil.LOG_TAG_NEW, "--- mTaskProcessorStatus : " + mTaskProcessorStatus);
			mCurrentState = null;
			mNextState = null;
			selfHandler = this;
			mTaskProcessorStatus = TASKPROCESSOR_RUNNING;
			// TODO Auto-generated method stub
			GlobalUtil.threadExecutor().execute(new Runnable() {

				@Override
				public void run() {
					Log.i(LogUtil.LOG_TAG_NEW, "TaskProcessor begin to run.");
					// TODO Auto-generated method stub
					while (mIsStopTaskProcessor == 0) {
						if (mCurrentState != null) {
							// Log.i(LogUtil.LOG_TAG_NEW,
							// "TaskProcessor currentstate: =" +
							// mCurrentState.toString());
							switch (mCurrentState.taskStatus) {
							case TaskProcessor.STATUS_TASK_WAITING:
								// 进入任务开始阶段
								mCurrentState.taskStatus = TaskProcessor.STATUS_TASK_STARTING;
								switch (mCurrentState.statusTaskType) {
								case TaskProcessor.TASK_TYPE_IDLE:
									// 空闲状态什么也不做
									Log.e(LogUtil.LOG_TAG_NEW,
											" TaskProcessor: process current state. start TASK_TYPE_IDLE");
									Log.e(LogUtil.LOG_TAG_NEW, "start AP. Status: " + mCurrentState.taskStatus
											+ " - Type: " + mCurrentState.statusTaskType);
									// 直接设置停止标志
									mCurrentState.taskStatus = STATUS_TASK_STOP;
									break;
								case TaskProcessor.TASK_TYPE_AP:
									Log.e(LogUtil.LOG_TAG_NEW,
											" TaskProcessor: process current state. start TASK_TYPE_AP");
									Log.e(LogUtil.LOG_TAG_NEW, "start AP. Status: " + mCurrentState.taskStatus
											+ " - Type: " + mCurrentState.statusTaskType);
									startAP(selfHandler);
									break;
								case TaskProcessor.TASK_TYPE_CONNECT:
									Log.e(LogUtil.LOG_TAG_NEW,
											" TaskProcessor: process current state. start TASK_TYPE_CONNECT");
									startWifiConnect(selfHandler);
									break;
								case TaskProcessor.TASK_TYPE_SEARCH:
									Log.e(LogUtil.LOG_TAG_NEW,
											" TaskProcessor: process current state. start TASK_TYPE_SEARCH");
									startSearchWifi(selfHandler);
									break;
								case TaskProcessor.TASK_TYPE_HTTP_SHARE:
									Log.e(LogUtil.LOG_TAG_NEW,
											" TaskProcessor: process current state. start TASK_TYPE_HTTP_SHARE");
									// 开启http自分享状态
									startHttpOneFileShare();
									break;
								}
								break;
							case TaskProcessor.STATUS_TASK_OK:
								if (mNextState != null) {
									Log.w(LogUtil.LOG_TAG_NEW,
											" find next target state, cancel current state. next statusinfo: "
													+ mNextState.toString());
									Log.e(LogUtil.LOG_TAG_NEW,
											" find next target state, cancel current state. next task type: "
													+ mNextState.statusTaskType);
									// 设置任务状态为开始停止
									mCurrentState.taskStatus = STATUS_TASK_STOPPING;
									// 停止当前正在执行的任务
									switch (mCurrentState.statusTaskType) {
									case TaskProcessor.TASK_TYPE_AP:
										Log.e(LogUtil.LOG_TAG_NEW,
												" TaskProcessor: process current state cancel. cancel TASK_TYPE_AP");
										Log.e(LogUtil.LOG_TAG_NEW, "stop AP. Status: " + mCurrentState.taskStatus
												+ " - Type: " + mCurrentState.statusTaskType);
										stopServer();
										break;
									case TaskProcessor.TASK_TYPE_CONNECT:
										Log.e(LogUtil.LOG_TAG_NEW,
												" TaskProcessor: process current state cancel. cancel TASK_TYPE_CONNECT");
										disconnectServerService();
										break;
									case TaskProcessor.TASK_TYPE_SEARCH:
										Log.e(LogUtil.LOG_TAG_NEW,
												" TaskProcessor: process current state cancel. cancel TASK_TYPE_SEARCH");
										stopSearchWifi();
										break;
									case TaskProcessor.TASK_TYPE_HTTP_SHARE:
										Log.e(LogUtil.LOG_TAG_NEW,
												" TaskProcessor: process current state cancel. cancel TASK_TYPE_HTTP_SHARE");
										// 关闭自分享状态
										stopHttpOneFileShare();
										break;
									}
								}
								break;
							case TaskProcessor.STATUS_TASK_STOP:
								synchronized (selfHandler) {
									if (mNextState != null) {
										Log.w(LogUtil.LOG_TAG_NEW,
												" find next target state, exchange begin. statusinfo: "
														+ mNextState.toString());
										Log.e(LogUtil.LOG_TAG_NEW,
												" find next target state, exchange begin. next task type: "
														+ mNextState.statusTaskType);
										// 当前任务已经结束，切换到下一个任务
										mCurrentState = mNextState;
										mNextState = null;
									}
								}
								break;
							}
						}
						// 休眠一些时间，防止占满手机资源
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					mTaskProcessorStatus = TASKPROCESSOR_STOP;
				}

			});
		} else {
			Log.e(LogUtil.LOG_TAG_NEW, "Task is running.");
		}
	}

	/**
	 * 停止当前任务处理器
	 */
	public void stopTaskProcessor() {
		// 停止当前任务处理器
		mIsStopTaskProcessor = 1;
		mTaskProcessorStatus = TASKPROCESSOR_CANCELING;
		// 清空变量
		mContext = null;
		mWifiApManagerAdmin = null;
		mWifiAdmin = null;
		mWifiApAdmin = null;
		selfHandler = null;
	}

	/**
	 * 设置下一个目标状态
	 * 
	 * @param targetStatusInfo
	 * @param resultCallback
	 */
	public synchronized boolean setTargetWifiStatus(TargetStatusInfo targetStatusInfo) {
		Log.w(LogUtil.LOG_TAG_NEW,
				" TaskProcessor:  begin to set target wifi status. info: " + targetStatusInfo.toString());
		Log.e(LogUtil.LOG_TAG_NEW,
				" TaskProcessor:  begin to set target wifi status. task type: " + targetStatusInfo.statusTaskType);		// 如果设置的信息不完整，则直接返回信息不完整的错误消息
		// 如果完整，则将该任务放入
		if (!checkTargetStatusInfo(targetStatusInfo)) {
			return false;
		} else {
			// 设置目标状态
			if (mCurrentState == null) {
				mCurrentState = targetStatusInfo;
				mCurrentState.taskStatus = TaskProcessor.STATUS_TASK_WAITING;
				Log.w(LogUtil.LOG_TAG_NEW, " TaskProcessor: set current state.  " + mCurrentState.toString());
				Log.e(LogUtil.LOG_TAG_NEW, " TaskProcessor: set current state task type:  " + mCurrentState.statusTaskType);
			} else {
				// 如果发现当前状态已经是需要设置的状态，则直接返回
				if ((mCurrentState.statusTaskType == targetStatusInfo.statusTaskType)
						&& ((mCurrentState.taskStatus == STATUS_TASK_WAITING)
								|| (mCurrentState.taskStatus == STATUS_TASK_STARTING) || (mCurrentState.taskStatus == STATUS_TASK_OK))) {
					Log.e(LogUtil.LOG_TAG_NEW, " TaskProcessor: current state is already ok. target task type: " + targetStatusInfo.statusTaskType);
					return true;
				} else {
					Log.e(LogUtil.LOG_TAG_NEW, " TaskProcessor: set next state task type:  " + mCurrentState.statusTaskType);
					mNextState = targetStatusInfo;
					mNextState.taskStatus = TaskProcessor.STATUS_TASK_WAITING;
				}
			}
		}
		return true;
	}

	private boolean checkTargetStatusInfo(TargetStatusInfo targetStatusInfo) {
		boolean isInfoValid = true;
		switch (targetStatusInfo.statusTaskType) {
		// 热点类型的参数完整性检查
		case TASK_TYPE_AP:
			if ((targetStatusInfo.SSID == null) || (targetStatusInfo.resultCallback == null)
					|| (targetStatusInfo.shareCircleInfo == null) || (targetStatusInfo.shareCircleServer == null)
					|| (targetStatusInfo.selfInfo == null)) {
				isInfoValid = false;
			}
			break;
		// wifi连接类型的参数完整性检查
		case TASK_TYPE_CONNECT:
			if ((targetStatusInfo.SSID == null) || (targetStatusInfo.resultCallback == null)
					|| (targetStatusInfo.shareCircleInfo == null) || (targetStatusInfo.shareCircleClient == null)
					|| (targetStatusInfo.selfInfo == null)) {
				isInfoValid = false;
			}
			break;
		// 搜索类型的参数完整性检查
		case TASK_TYPE_SEARCH:
			if ((targetStatusInfo.resultCallback == null) || (targetStatusInfo.shareCircleInfo == null)) {
				isInfoValid = false;
			}
			break;
		// http文件分享类型的参数完整性检查
		case TASK_TYPE_HTTP_SHARE:
			if ((targetStatusInfo.SSID == null) || (targetStatusInfo.resultCallback == null)) {
				isInfoValid = false;
			}
			break;
		// 空闲类型的参数完整性检查
		case TASK_TYPE_IDLE:
			break;
		default:
			isInfoValid = false;
		}
		return isInfoValid;
	}

	/**
	 * 得到任务处理器当前状态 取值：TASKPROCESSOR_RUNNING; TASKPROCESSOR_CANCELING;
	 * TASKPROCESSOR_STOPING
	 */
	public int getTaskProcessorStatus() {
		return mTaskProcessorStatus;
	}

	/**
	 * 得到任务处理器当前详细状态 取值为当前处理的详细状态，如：AP_CREATE_STARTING; AP_CREATE_OK等
	 */
	public int getTaskProcessorDetailStatus() {
		return mTaskProcessorDetailStatus;
	}

	private void startAP(Handler handlerAP) {
		Log.e(LogUtil.LOG_TAG_NEW, "-- ssid: " + mCurrentState.SSID + " -- passkey: " + mCurrentState.passkey);
		sendEmptyMessage(WifiUtil.AP_CREATE_STARTING);
		mWifiApManagerAdmin.openWifiAp(mCurrentState.SSID, mCurrentState.passkey, mCurrentState.secretType, handlerAP);
	}

	/**
	 * server断开服务，并且关闭热点
	 */
	private void stopAP(Handler handlerAP) {
		sendEmptyMessage(WifiUtil.AP_STOP_STARTING);
		mWifiApManagerAdmin.closeWifiAp(handlerAP);
	}

	private void startWifiConnect(Handler handlerConnect) {
		sendEmptyMessage(WifiUtil.CONNECT_STARTING);
		mWifiApManagerAdmin.connectWifi(mCurrentState.SSID, mCurrentState.passkey, mCurrentState.secretType, handlerConnect);
	}

	/**
	 * client断开连接热点
	 */
	private boolean disconnectWifi() {
		sendEmptyMessage(WifiUtil.DISCONNECT_STARTING);
		if (mWifiApManagerAdmin.disconnectWifi()) {
			sendEmptyMessage(WifiUtil.DISCONNECT_OK);
			return true;
		} else {
			sendEmptyMessage(WifiUtil.DISCONNECT_FAILED);
			return false;
		}
	}

	/**
	 * client开始搜索热点
	 */
	private void startSearchWifi(Handler handlerSearch) {
		sendEmptyMessage(WifiUtil.SEARCH_STARTING);
		mWifiApManagerAdmin.startSearchWifi(mCurrentState.shareCircleInfo.appInfo, handlerSearch);
	}

	/**
	 * client停止搜索热点
	 */
	private void stopSearchWifi() {
		sendEmptyMessage(WifiUtil.SEARCH_STOP_STARTING);
		mWifiApManagerAdmin.stopSearchWifi();
		sendEmptyMessage(WifiUtil.SEARCH_STOP_OK);
	}

	/**
	 * server端开启服务
	 */
	private void startServer(Handler handlerServer) {
		// 开始进入启动服务的阶段
		sendEmptyMessage(WifiUtil.AP_SERVICE_CREATE_STARTING);
		ShareCircleInfo shareCircleInfo = mCurrentState.shareCircleInfo;
		shareCircleInfo.serverIP = mWifiApAdmin.getIPAddress();
		if (mCurrentState.shareCircleServer != null) {
			mCurrentState.shareCircleServer.startServer(shareCircleInfo, mCurrentState.selfInfo,
					mCurrentState.shareCircleInfo.maxClients, handlerServer);
		} else {
			handlerServer.sendEmptyMessage(WifiUtil.AP_SERVICE_CREATE_FAILED);
		}
	}

	/**
	 * server端停止服务
	 */
	private void stopServer() {
		sendEmptyMessage(WifiUtil.AP_SERVICE_STOP_STARTING);
		mCurrentState.shareCircleServer.destroyServer(selfHandler);
	}

	/**
	 * client端连接服务
	 */
	private void connectServerService(final Handler handlerServerConnect) {
		// 开始连接服务
		sendEmptyMessage(WifiUtil.CONNECT_SERVICE_STARTING);
		final ClientInfo info = mCurrentState.selfInfo;
		info.ip = mWifiAdmin.getIPAddress() + "";
		mCurrentState.shareCircleInfo.serverIP = WifiUtil.ipLongToString(mWifiAdmin.getServerIPAddress());
		GlobalUtil.threadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				// TODO Auto-generated method stub
				mCurrentState.shareCircleClient.connectToShareCircle(mCurrentState.shareCircleInfo, info,
						handlerServerConnect);
			}
		});
	}

	/**
	 * client断开连接
	 */
	private void disconnectServerService() {
		// 开始断开服务层连接
		sendEmptyMessage(WifiUtil.DISCONNECT_SERVICE_STARTING);
		mCurrentState.shareCircleClient.disconnect(false);
		sendEmptyMessage(WifiUtil.DISCONNECT_SERVICE_OK);
	}

	private OperationResult mEmptyOperationResult = new OperationResult() {

		@Override
		public void onSucceed() {
			// TODO Auto-generated method stub

		}

		@Override
		public void onFailed() {
			// TODO Auto-generated method stub

		}
	};
	private HotspotHttpFileService selfShareService;
	/**
	 * 启动ap及http服务进行文件分享
	 */
	private void startHttpOneFileShare() {
		// 保存当前状态
		mWifiApManagerAdmin.saveWifiState();
		// 打开分享服务
		selfShareService = HotspotHttpFileService.getInstance();
		selfShareService.shareOneFile(mCurrentState.SSID, mCurrentState.resultCallback, mContext,
				mCurrentState.fileForShare);
	}

	/**
	 * 停止ap及http服务
	 */
	private void stopHttpOneFileShare() {
		// 恢复之前状态
		mWifiApManagerAdmin.restoreWifiState(mEmptyOperationResult);
		// 停止分享服务
		if (selfShareService != null) {
			selfShareService.stopHttpShare(mCurrentState.resultCallback);
		}
	}

	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		// ap创建消息处理
		case WifiUtil.AP_CREATE_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_CREATE_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.AP_CREATE_STARTING;
			// 开始启动热点
			break;
		case WifiUtil.AP_CREATE_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_CREATE_OK");
			mTaskProcessorDetailStatus = WifiUtil.AP_CREATE_OK;
			// mCurrentState.mTaskStatus = STATUS_TASK_OK;
			// mCurrentState.mResultHandler.sendEmptyMessage(WifiUtil.AP_CREATE_OK);
			startServer(selfHandler);
			break;
		case WifiUtil.AP_CREATE_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_CREATE_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.AP_CREATE_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onApCreateFailed();
			break;

		// ap服务打开消息处理
		case WifiUtil.AP_SERVICE_CREATE_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_SERVICE_CREATE_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.AP_SERVICE_CREATE_STARTING;
			break;
		case WifiUtil.AP_SERVICE_CREATE_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_SERVICE_CREATE_OK");
			mTaskProcessorDetailStatus = WifiUtil.AP_SERVICE_CREATE_OK;
			mCurrentState.taskStatus = STATUS_TASK_OK;
			mCurrentState.resultCallback.onApServiceCreateOK((ShareCircleInfo) msg.obj);
			break;
		case WifiUtil.AP_SERVICE_CREATE_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_SERVICE_CREATE_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.AP_SERVICE_CREATE_FAILED;
			stopAP(selfHandler);
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onApServiceCreateFailed();
			break;

		// ap服务停止消息处理
		case WifiUtil.AP_SERVICE_STOP_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_SERVICE_STOP_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.AP_SERVICE_STOP_STARTING;
			break;
		case WifiUtil.AP_SERVICE_STOP_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_SERVICE_STOP_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.AP_SERVICE_STOP_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onApServiceStopFailed();
			break;
		case WifiUtil.AP_SERVICE_STOP_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_SERVICE_STOP_OK");
			mTaskProcessorDetailStatus = WifiUtil.AP_SERVICE_STOP_OK;
			stopAP(selfHandler);
			break;

		// ap停止消息处理
		case WifiUtil.AP_STOP_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_STOP_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.AP_STOP_STARTING;
			break;
		case WifiUtil.AP_STOP_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_STOP_OK");
			mTaskProcessorDetailStatus = WifiUtil.AP_STOP_OK;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onApStopOK();
			break;
		case WifiUtil.AP_STOP_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: AP_STOP_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.AP_STOP_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onApStopFailed();
			break;

		// 连接wifi ap消息处理
		case WifiUtil.CONNECT_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: CONNECT_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.CONNECT_STARTING;
			break;
		case WifiUtil.CONNECT_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: CONNECT_OK");
			mTaskProcessorDetailStatus = WifiUtil.CONNECT_OK;
			connectServerService(selfHandler);
			break;
		case WifiUtil.CONNECT_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: CONNECT_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.CONNECT_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onConnectFailed();
			break;

		// 连接服务消息处理
		case WifiUtil.CONNECT_SERVICE_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: CONNECT_SERVICE_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.CONNECT_SERVICE_STARTING;
			break;
		case WifiUtil.CONNECT_SERVICE_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: CONNECT_SERVICE_OK");
			mTaskProcessorDetailStatus = WifiUtil.CONNECT_SERVICE_OK;
			mCurrentState.taskStatus = STATUS_TASK_OK;
			mCurrentState.resultCallback.onConnectServiceOK();
			break;
		case WifiUtil.CONNECT_SERVICE_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: CONNECT_SERVICE_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.CONNECT_SERVICE_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onConnectServiceFailed();
			break;

		// 断开连接服务层消息处理
		case WifiUtil.DISCONNECT_SERVICE_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: DISCONNECT_SERVICE_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.DISCONNECT_SERVICE_STARTING;
			break;
		case WifiUtil.DISCONNECT_SERVICE_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: DISCONNECT_SERVICE_OK");
			mTaskProcessorDetailStatus = WifiUtil.DISCONNECT_SERVICE_OK;
			mCurrentState.resultCallback.onDisconnectServiceOK();
			// 服务断开成功后，开始断开wifi连接
			disconnectWifi();
			break;
		case WifiUtil.DISCONNECT_SERVICE_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: DISCONNECT_SERVICE_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.DISCONNECT_SERVICE_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onDisconnectServiceFailed();
			break;

		// 断开wifi连接消息处理
		case WifiUtil.DISCONNECT_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: DISCONNECT_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.DISCONNECT_STARTING;
			break;
		case WifiUtil.DISCONNECT_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: DISCONNECT_OK");
			mTaskProcessorDetailStatus = WifiUtil.DISCONNECT_OK;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onDisconnectOK();
			break;
		case WifiUtil.DISCONNECT_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: DISCONNECT_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.DISCONNECT_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onDisconnectFailed();
			break;

		// 启动search服务消息处理
		case WifiUtil.SEARCH_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: SEARCH_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.SEARCH_STARTING;
			break;
		case WifiUtil.SEARCH_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: SEARCH_OK");
			mTaskProcessorDetailStatus = WifiUtil.SEARCH_OK;
			mCurrentState.taskStatus = STATUS_TASK_OK;
			mCurrentState.resultCallback.onSearchOK();
			// mCurrentState.resultHandler.sendMessage(msg);
			break;
		case WifiUtil.SEARCH_NEW_SCAN_RESULT:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: SEARCH_NEW_SCAN_RESULT");
			// mTaskProcessorDetailStatus = WifiUtil.SEARCH_NEW_SCAN_RESULT;
			// mCurrentState.taskStatus = STATUS_TASK_OK;
			mCurrentState.resultCallback.onSearchNewScanResult((Collection<ShareCircleInfo>) msg.obj);
			break;
		case WifiUtil.SEARCH_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: SEARCH_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.SEARCH_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onSearchFailed();
			break;

		// 停止搜索服务消息处理
		case WifiUtil.SEARCH_STOP_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: SEARCH_STOP_STARTING");
			mTaskProcessorDetailStatus = WifiUtil.SEARCH_STOP_STARTING;
			break;
		case WifiUtil.SEARCH_STOP_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: SEARCH_STOP_OK");
			mTaskProcessorDetailStatus = WifiUtil.SEARCH_STOP_OK;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onSearchStopOK();
			break;
		case WifiUtil.SEARCH_STOP_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "TaskProcessor: SEARCH_STOP_FAILED");
			mTaskProcessorDetailStatus = WifiUtil.SEARCH_STOP_FAILED;
			mCurrentState.taskStatus = STATUS_TASK_STOP;
			mCurrentState.resultCallback.onSearchStopFailed();
			break;
		}
	}
}
