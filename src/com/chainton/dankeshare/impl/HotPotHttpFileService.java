/**
 * 
 */
package com.chainton.dankeshare.impl;

import java.io.File;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.util.Log;

import com.chainton.dankeshare.CreateShareCircleResult;
import com.chainton.dankeshare.OperationResult;
import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ShareCircleAppInfo;
import com.chainton.dankeshare.data.ShareCircleInfo;
import com.chainton.dankeshare.data.enums.ShareCircleType;
import com.chainton.dankeshare.util.LogUtil;
import com.chainton.dankeshare.util.NetworkUtil;
import com.chainton.dankeshare.wifi.DefaultWifiApManager;

/**
 * @author Soar
 *
 */
public class HotPotHttpFileService {
	
	/**
	 * 操作wifi 的 manager 接口类
	 */
	private DefaultWifiApManager wifiApManager;
	/**
	 * 分享圈名称
	 */
	private String shareCircleName;

	/**
	 * 分享圈应用程序信息
	 */
	private ShareCircleAppInfo appInfo;
	
	/**
	 * 本机信息
	 */
	private ClientInfo selfInfo;
	/**
	 * 创建结果回调接口
	 */
	private CreateShareCircleResult createResult;

	/**
	 * android context
	 */
	private Context context;
	
	private static HotPotHttpFileService hotPotHttpFileService = new HotPotHttpFileService();
	
	/**
	 * 主线程Handler实例
	 */
	protected Handler handler;
	
	private HttpFileServer httpFileServer; 
	
	/**
	 * 默认启动http server 端口
	 */
	private static final int HTTP_PORT = 8080;

	
	private HotPotHttpFileService(){
		
	}
	
	public static HotPotHttpFileService getInstance(){
		return hotPotHttpFileService;
	}
	
	/**
	 * 启动 http 服务器
	 * @param ssid    名称
	 * @param shareType 启动类型
	 * @param port  端口号
	 * @param result 操作结果
	 * @param context android context
	 */
	public void startHttpShare(String ssid,ShareCircleType shareType,int port,CreateShareCircleResult result,Context context) {
		
		if( port<=0 ){
			port = HTTP_PORT;
		}
		httpFileServer = new HttpFileServer(port);
		System.out.println("port:  "+port);
		
		this.context = context.getApplicationContext();
		this.wifiApManager = new DefaultWifiApManager(this.context);
		this.handler = new Handler(this.context.getMainLooper());
		
		if (shareType == ShareCircleType.AUTO) {
			if (this.wifiApManager. isWifiConnected()) {
				shareType = ShareCircleType.WIFILAN;
			} else {
				shareType = ShareCircleType.WIFIAP;
			}
		}
		
		if (shareType.equals(ShareCircleType.WIFIAP)) {
			this.createApShareCircle(ssid);
		} else if (shareType.equals(ShareCircleType.WIFILAN)) {
			this.createLanShareCircle();
		}
		
	}
	
	/**
	 * 添加文件
	 * @param md5  文件key, 由调用应用生成
	 * @param file	文件信息
	 * @return url  能够访问到文件的 url
	 */
	public String addHttpResouce(String md5, File file){
		return httpFileServer.addFile(getLocalIp(), md5, file);
	}
	
	
	/**
	 * 停止http server
	 */
	public void stopHttpShare(){
		
		httpFileServer.stopServer();
		wifiApManager.closeWifiAp(new OperationResult() {
			@Override
			public void onSucceed() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						createResult.onFailed();
					}
				});
			}
			@Override
			public void onFailed() {
				handler.post(new Runnable() {
					@Override
					public void run() {
						createResult.onFailed();
					}
				});
			}
		});
	}
	
	/**
	 * 创建热点
	 * @param ssid
	 */
	private  void createApShareCircle(String ssid) {
		
		Log.d(LogUtil.LOG_TAG, "Start creating AP ShareCircle.");
		WifiConfiguration config = wifiApManager.creatSimpleConfig(ssid);
		Log.d(LogUtil.LOG_TAG, "Create SSID " + ssid + " without sharedKey ");
		
		wifiApManager.openWifiAp(config, new OperationResult() {
			@Override
			public void onSucceed() {
				new Thread() {
					@Override
					public void run() {
						String ip = getLocalIp();
						if (ip == null) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									createResult.onFailed();
								}
							});
							
							return;
						}
						Log.i(LogUtil.LOG_TAG, "hot ip: "+ip);
						
						try{
							httpFileServer.startServer();
						}catch(Exception e){
							handler.post(new Runnable() {
								@Override
								public void run() {
									createResult.onFailed();
								}
							});
						}
						
					}
				}.start();
				
			}
			@Override
			public void onFailed() {
				
				wifiApManager.closeWifiAp(new OperationResult() {
					@Override
					public void onSucceed() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								createResult.onFailed();
							}
						});
					
					}
					@Override
					public void onFailed() {
						handler.post(new Runnable() {
							@Override
							public void run() {
								createResult.onFailed();
							}
						});
						
					}
				});
			
			}
		});
	}
	
	/**
	 * 创建局域网类型server
	 */
	private void createLanShareCircle() {
		final ShareCircleInfo shareCircleInfo = new ShareCircleInfo(shareCircleName, ShareCircleType.WIFILAN, appInfo);
		String ip = getLocalIp();
		if (ip == null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					createResult.onFailed();
				}
			});
			return;
		}
		selfInfo.ip = ip;
		shareCircleInfo.serverIP = ip;
	}
	
	/**
	 * 返回本机ip
	 * @return ip
	 */
	private String getLocalIp() {
		int timeoutCount = 0;
		String ip = null;
		while (timeoutCount <= NetworkUtil.GET_LOCAL_IP_TIMEOUT) {
			ip = wifiApManager.getApLocalIp();
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
