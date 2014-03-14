/**
 * 
 */
package com.chainton.dankeshare.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.chainton.dankeshare.HttpFileResult;
import com.chainton.dankeshare.data.enums.ShareCircleType;
import com.chainton.dankeshare.util.LogUtil;
import com.chainton.dankeshare.util.NetworkUtil;
import com.chainton.dankeshare.wifi.util.WifiAdmin;
import com.chainton.dankeshare.wifi.util.WifiApAdmin;
import com.chainton.dankeshare.wifi.util.WifiApManagerAdmin;
import com.chainton.dankeshare.wifi.util.WifiUtil;
import com.chainton.forest.core.util.GlobalUtil;

/**
 * @author Soar
 * 
 */
public class HotspotHttpFileService {

	/**
	 * android context
	 */
	private Context mContext;

	private static HotspotHttpFileService hotPotHttpFileService;

	/**
	 * 主线程Handler实例
	 */
	protected Handler handler;

	/**
	 * http server 类
	 */
	private HttpFileServer httpFileServer;

	/**
	 * 默认启动http server 端口
	 */
	private static final int HTTP_PORT = 8080;

	private static final String STANDALONE_STYLE = "STANDALONE";
	private static final String SHARE_STYLE = "SHARE";

	private boolean mNeedHotPot = true;

	private HotspotHttpFileService() {
		mNeedHotPot = true;
	}

	/**
	 * 获取操作实例
	 * 
	 * @return
	 */
	public static HotspotHttpFileService getInstance() {
		if (hotPotHttpFileService == null) {
			hotPotHttpFileService = new HotspotHttpFileService();
		}
		return hotPotHttpFileService;
	}

	/**
	 * 启动 http 服务器
	 * 
	 * @param ssid
	 *            名称
	 * @param shareType
	 *            启动类型
	 * @param port
	 *            端口号
	 * @param mNeedHotPot
	 *            启动服务器时是否需要先启动热点
	 * @param result
	 *            操作结果
	 * @param context
	 *            android context
	 */
	public void startHttpShare(String ssid, ShareCircleType shareType, int port, boolean needHotPot, Context context) {
		// 保留app层的handler，在有结果后返回消息给app层
		mContext = context.getApplicationContext();
		mWifiAdmin = new WifiAdmin(mContext);
		mWifiApAdmin = new WifiApAdmin(mContext);
		mWifiApManagerAdmin = new WifiApManagerAdmin(mContext);

		if (port <= 0) {
			port = HTTP_PORT;
		}
		httpFileServer = null;
		httpFileServer = new HttpFileServer(port, SHARE_STYLE);
		handler = new Handler(context.getMainLooper());
		mNeedHotPot = needHotPot;

		// 是否需要启动热点
		if (mNeedHotPot) {
			if (shareType == ShareCircleType.AUTO) {
				if (mWifiAdmin.isWifiConnected()) {
					shareType = ShareCircleType.WIFILAN;
				} else {
					shareType = ShareCircleType.WIFIAP;
				}
			}
			if (shareType.equals(ShareCircleType.WIFIAP)) {
				createApAndHttp(ssid, null, mHandlerHttpResult);
			} else if (shareType.equals(ShareCircleType.WIFILAN)) {
				createLanShareCircle(mHandlerHttpResult);
			}
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						httpFileServer.startServer(mHandlerHttpResult);
					} catch (IOException e) {
						// mHandlerHttpResult对象没有被初始化，这个函数暂时用不到，以后需要修改
						// note by lyj
						mHandlerHttpResult.sendEmptyMessage(WifiUtil.HTTP_SERVICE_START_FAILED);
					}
				}
			}.start();
		}

	}

	// 用来接收给http用的热点创建结果信息
	private class HandlerHttpResult extends Handler {

		public HandlerHttpResult() {
			super();
			// TODO Auto-generated constructor stub
		}

		public HandlerHttpResult(Callback callback) {
			super(callback);
			// TODO Auto-generated constructor stub
		}

		public HandlerHttpResult(Looper looper, Callback callback) {
			super(looper, callback);
			// TODO Auto-generated constructor stub
		}

		public HandlerHttpResult(Looper looper) {
			super(looper);
			// TODO Auto-generated constructor stub
		}

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			switch (msg.what) {
			case WifiUtil.AP_CREATE_OK:
				Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: AP_CREATE_OK");
				mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_AP_START_OK);
				break;
			case WifiUtil.AP_CREATE_FAILED:
				Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: AP_CREATE_FAILED");
				mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_AP_START_FAILED);
				break;
			case WifiUtil.AP_STOP_OK:
				Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: AP_STOP_OK");
				mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_AP_STOP_OK);
				break;
			case WifiUtil.AP_STOP_FAILED:
				Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: AP_STOP_FAILED");
				mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_AP_STOP_FAILED);
				break;
			case WifiUtil.HTTP_SERVICE_START_OK:
				Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: HTTP_SERVICE_START_OK");
				if (null != fileShare) {
					final String url = addOneResouce(getLocalIp(), fileShare);
					String urlServer = url.substring(0, url.lastIndexOf("/"));
					Message msgServerInfo = new Message();
					msgServerInfo.what = WifiUtil.HTTP_SERVICE_START_OK;
					msgServerInfo.obj = urlServer;
					mHandlerForTaskProcessor.sendMessage(msgServerInfo);
					Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: HTTP_SERVICE_START_OK, Url: " + urlServer);
				} else {
					Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: HTTP_SERVICE_START_OK");
					mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_SERVICE_START_OK);
				}
				mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_SERVICE_START_OK);
				break;
			case WifiUtil.HTTP_SERVICE_START_FAILED:
				Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: HTTP_SERVICE_START_FAILED");
				mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_SERVICE_START_FAILED);
				break;
			case WifiUtil.HTTP_SERVICE_STOP_OK:
				Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: HTTP_SERVICE_STOP_OK");
				mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_SERVICE_STOP_OK);
				break;
			case WifiUtil.HTTP_SERVICE_STOP_FAILED:
				Log.e(LogUtil.LOG_TAG_NEW, "HttpShareOneFile: HTTP_SERVICE_STOP_FAILED");
				mHandlerForTaskProcessor.sendEmptyMessage(WifiUtil.HTTP_SERVICE_STOP_FAILED);
				break;
			}
		}

	}

	private HandlerHttpResult mHandlerHttpResult;
	private Handler mHandlerForTaskProcessor;
	private WifiAdmin mWifiAdmin;
	private WifiApAdmin mWifiApAdmin;
	private WifiApManagerAdmin mWifiApManagerAdmin;
	private File fileShare;

	/**
	 * 初始化HttpShare服务
	 */
	public void initHttpShareAp(Context context, Handler handlerHttpShare){
		mContext = context.getApplicationContext();
		mWifiAdmin = new WifiAdmin(mContext);
		mWifiApAdmin = new WifiApAdmin(mContext);
		mWifiApManagerAdmin = new WifiApManagerAdmin(mContext);
		mHandlerHttpResult = new HandlerHttpResult(Looper.getMainLooper());
		mHandlerForTaskProcessor = handlerHttpShare;
		httpFileServer = null;
		httpFileServer = new HttpFileServer(HTTP_PORT, STANDALONE_STYLE);
		handler = new Handler(mContext.getMainLooper());
	}
	/**
	 * 启动 http 服务器只分享一次文件
	 * 
	 * @param ssid
	 *            名称
	 * @param result
	 *            操作结果
	 * @param mContext
	 *            android context
	 * @param file
	 *            要分享的文件
	 */
	public void startHttpShareAp(String ssid, File file) {
		fileShare = file;
		createApAndHttp(ssid, file, mHandlerHttpResult);
	}

	/**
	 * 启动HttpShare服务
	 */
	public void startHttpShareServer(){
		if (getLocalIp() == null) {
			mHandlerHttpResult.sendEmptyMessage(WifiUtil.HTTP_SERVICE_START_FAILED);
		} else {
			new Thread() {
				@Override
				public void run() {
					try {
						httpFileServer.startServer(mHandlerHttpResult);
					} catch (IOException e) {
						mHandlerHttpResult.sendEmptyMessage(WifiUtil.HTTP_SERVICE_START_FAILED);
					}
				}
			}.start();
		}
		// mCurrentState.resultCallback.onHttpApStartOK();
	}
	/**
	 * 添加文件
	 * 
	 * @param httpFileResult
	 *            添加文件成功后的回调函数
	 * @param file
	 *            文件信息
	 * 
	 */
	public void addHttpResouce(final File file, final HttpFileResult httpFileResult) {
		GlobalUtil.threadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				UUID uuid = UUID.randomUUID();
				final String url = httpFileServer.addFile(getLocalIp(), uuid.toString(), file);
				handler.post(new Runnable() {
					@Override
					public void run() {
						httpFileResult.onFileUrlGen(url);
					}
				});
			}
		});
	}

	/**
	 * 添加一个文件
	 * 
	 * @param ip
	 *            启动热点后的iP
	 * @param file
	 *            文件信息
	 * @return url 能够访问到文件的 url
	 */
	private String addOneResouce(String ip, File file) {
		UUID uuid = UUID.randomUUID();
		return httpFileServer.addFile(ip, uuid.toString(), file);
	}

	/**
	 * 停止http server
	 * 
	 * @param HotspotHttpResult
	 *            回调函数
	 */
	public void stopHttpShare(Handler handlerHttpShare) {
		mHandlerForTaskProcessor = handlerHttpShare;
		try {
			stopHttpAndHotspot(mHandlerHttpResult);
		} catch (Exception e) {
			e.printStackTrace();
			mHandlerHttpResult.sendEmptyMessage(WifiUtil.HTTP_AP_STOP_FAILED);
		}
	}

	/**
	 * 关闭http 和 hotspot
	 * 
	 * @param result
	 */
	private void stopHttpAndHotspot(Handler handlerStopHttpResult) {
		// 关闭http 服务
		if (httpFileServer.isRunning()) {
			httpFileServer.stopServer();
		}
		mHandlerHttpResult.sendEmptyMessage(WifiUtil.HTTP_SERVICE_STOP_OK);
		// 关闭热点服务
		if (mNeedHotPot) {
			mWifiApManagerAdmin.closeWifiAp(handlerStopHttpResult);
		}
	}

	/**
	 * 创建热点
	 * 
	 * @param ssid
	 *            热点名称
	 */
	private void createApAndHttp(String ssid, final File file, Handler handlerApCreateResult) {
		Log.d(LogUtil.LOG_TAG, "Create SSID " + ssid + " without sharedKey ");

		mWifiApManagerAdmin.openWifiAp(ssid, "", WifiUtil.TYPE_NO_PASSWD, handlerApCreateResult);
		// , new OperationResult() {
		//
		// String ip;
		//
		// @Override
		// public void onSucceed() {
		// new Thread() {
		// @Override
		// public void run() {
		// ip = getLocalIp();
		// if (ip == null) {
		// handler.post(new Runnable() {
		// @Override
		// public void run() {
		// result.onFailed();
		// }
		// });
		// return;
		// }
		// Log.i(LogUtil.LOG_TAG, "hot ip: " + ip);
		// try {
		//
		// httpFileServer.startServer(new OperationResult() {
		// @Override
		// public void onSucceed() {
		// if (null != file) {
		// final String url = addOneResouce(ip, file);
		// handler.post(new Runnable() {
		// @Override
		// public void run() {
		// result.onSucceed(url.substring(0, url.lastIndexOf("/")));
		// }
		// });
		// } else {
		// result.onSucceed(null);
		// }
		//
		// }
		//
		// @Override
		// public void onFailed() {
		// handler.post(new Runnable() {
		// @Override
		// public void run() {
		// result.onFailed();
		// }
		// });
		// }
		//
		// });
		// } catch (IOException e) {
		// handler.post(new Runnable() {
		// @Override
		// public void run() {
		// result.onFailed();
		// }
		// });
		// }
		// }
		// }.start();
		//
		// }
		//
		// @Override
		// public void onFailed() {
		// wifiApManager.closeWifiAp(null);
		// }
		// });
	}

	/**
	 * 创建局域网类型server
	 */
	private void createLanShareCircle(Handler handlerResult) {
		String ip = getLocalIp();
		if (ip == null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					// result.onFailed();
				}
			});
			return;
		}
	}

	/**
	 * 返回本机ip
	 * 
	 * @return ip
	 */
	private String getLocalIp() {
		int timeoutCount = 0;
		String ip = null;
		while (timeoutCount <= NetworkUtil.GET_LOCAL_IP_TIMEOUT) {
			ip = mWifiApAdmin.getIPAddress();
			if (null != ip) {
				break;
			}
			try {
				Thread.sleep(500);
			} catch (Exception e) {
				Log.e(LogUtil.LOG_TAG, Log.getStackTraceString(e));
			}
			timeoutCount += 500;
		}
		return ip;
	}

}
