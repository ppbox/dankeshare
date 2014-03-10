package com.chainton.dankeshare.service;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.chainton.dankeshare.util.LogUtil;
import com.chainton.dankeshare.wifi.util.WifiUtil;

public class WifiProcessHandler extends Handler {

	public WifiProcessHandler() {
		super();
		// TODO Auto-generated constructor stub
	}

	public WifiProcessHandler(Callback callback) {
		super(callback);
		// TODO Auto-generated constructor stub
	}

	public WifiProcessHandler(Looper looper, Callback callback) {
		super(looper, callback);
		// TODO Auto-generated constructor stub
	}

	public WifiProcessHandler(Looper looper) {
		super(looper);
		// TODO Auto-generated constructor stub
	}

	/**
	 * 处理所有wifi操作返回的消息
	 */
	@Override
	public void handleMessage(Message msg) {
		switch (msg.what) {
		// ap创建消息处理
		case WifiUtil.AP_CREATE_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_CREATE_STARTING");
			break;
		case WifiUtil.AP_CREATE_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_CREATE_OK");
			break;
		case WifiUtil.AP_CREATE_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_CREATE_FAILED");
			break;

		// ap服务打开消息处理
		case WifiUtil.AP_SERVICE_CREATE_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_SERVICE_CREATE_STARTING");
			break;
		case WifiUtil.AP_SERVICE_CREATE_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_SERVICE_CREATE_OK");
			break;
		case WifiUtil.AP_SERVICE_CREATE_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_SERVICE_CREATE_FAILED");
			break;

		// ap服务停止消息处理
		case WifiUtil.AP_SERVICE_STOP_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_SERVICE_STOP_STARTING");
			break;
		case WifiUtil.AP_SERVICE_STOP_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_SERVICE_STOP_FAILED");
			break;
		case WifiUtil.AP_SERVICE_STOP_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_SERVICE_STOP_OK");
			break;

		// ap停止消息处理
		case WifiUtil.AP_STOP_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_STOP_STARTING");
			break;
		case WifiUtil.AP_STOP_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_STOP_OK");
			break;
		case WifiUtil.AP_STOP_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "AP_STOP_FAILED");
			break;

		// 连接wifi ap消息处理
		case WifiUtil.CONNECT_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "CONNECT_STARTING");
			break;
		case WifiUtil.CONNECT_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "CONTACT_OK");
			break;
		case WifiUtil.CONNECT_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "CONTACT_FAILED");
			break;

		// 连接服务消息处理
		case WifiUtil.CONNECT_SERVICE_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "CONNECT_SERVICE_STARTING");
			break;
		case WifiUtil.CONNECT_SERVICE_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "CONNECT_SERVICE_OK");
			break;
		case WifiUtil.CONNECT_SERVICE_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "CONNECT_SERVICE_FAILED");
			break;

		// 断开连接服务层消息处理
		case WifiUtil.DISCONNECT_SERVICE_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "DISCONNECT_SERVICE_STARTING");
			break;
		case WifiUtil.DISCONNECT_SERVICE_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "DISCONNECT_SERVICE_OK");
			break;
		case WifiUtil.DISCONNECT_SERVICE_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "DISCONNECT_SERVICE_FAILED");
			break;

		// 断开wifi连接消息处理
		case WifiUtil.DISCONNECT_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "DISCONNECT_STARTING");
			break;
		case WifiUtil.DISCONNECT_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "DISCONNECT_OK");
			break;
		case WifiUtil.DISCONNECT_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "DISCONNECT_FAILED");
			break;

		// 启动search服务消息处理
		case WifiUtil.SEARCH_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "SEARCH_STARTING");
			break;
		case WifiUtil.SEARCH_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "SEARCH_OK");
			break;
		case WifiUtil.SEARCH_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "SEARCH_FAILED");
			break;

		// 停止搜索服务消息处理
		case WifiUtil.SEARCH_STOP_STARTING:
			Log.e(LogUtil.LOG_TAG_NEW, "SEARCH_STOP_STARTING");
			break;
		case WifiUtil.SEARCH_STOP_OK:
			Log.e(LogUtil.LOG_TAG_NEW, "SEARCH_STOP_OK");
			break;
		case WifiUtil.SEARCH_STOP_FAILED:
			Log.e(LogUtil.LOG_TAG_NEW, "SEARCH_STOP_FAILED");
			break;
		}

	}
}
