/**
 * 
 */
package com.chainton.dankeshare.impl;

import java.io.File;
import java.io.IOException;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.util.Log;

import com.chainton.dankeshare.CreateShareCircleResult;
import com.chainton.dankeshare.OperationResult;
import com.chainton.dankeshare.WifiApManager;
import com.chainton.dankeshare.WifiApNameCodec;
import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ShareCircleAppInfo;
import com.chainton.dankeshare.data.ShareCircleInfo;
import com.chainton.dankeshare.data.enums.ShareCircleType;
import com.chainton.dankeshare.util.LogUtil;
import com.chainton.dankeshare.wifi.DefaultWifiApManager;
import com.chainton.dankeshare.wifi.DefaultWifiApNameCodec;
import com.chainton.dankeshare.wifi.WifiApShareCircleInfo;

/**
 * @author Administrator
 *
 */
public class HotPotHttpFileService {
	
	private WifiApManager wifiApManager;
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

	private Context context;
	
	private static HotPotHttpFileService hotPotHttpFileService = new HotPotHttpFileService();
	
	/**
	 * 主线程Handler实例
	 */
	protected Handler handler;
	private HttpFileServer httpFileServer;  //= HttpFileServer.getInstance();
	private static final int HTTP_PORT = 8080;

	
	private HotPotHttpFileService(){
		
	}
	public static HotPotHttpFileService getInstance(){
		return hotPotHttpFileService;
	}
	
	/**
	 * start http server 
	 * ap style
	 * lan style
	 *//*
	public void startHttpshare() {
		
		DefaultWifiApManager manager = new DefaultWifiApManager(context);
		this.wifiApManager = manager;
		
		if (shareCircleType.equals(ShareCircleType.WIFIAP)) {
			this.createApShareCircle();
		} else if (shareCircleType.equals(ShareCircleType.WIFILAN)) {
			this.createLanShareCircle();
		}
	}*/
	
	
	/**
	 * start http server 
	 * ap style
	 * lan style
	 */
	public void startHttpshare(String ssid,String shareKey,ShareCircleType shareType,int port,CreateShareCircleResult result,Context context) {
		
		if( port<=0 ){
			port = HTTP_PORT;
		}
		httpFileServer = new HttpFileServer(port);
		System.out.println("port:  "+port);
		
		
		this.context = context.getApplicationContext();
		this.wifiApManager = new DefaultWifiApManager(this.context);
		this.handler = new Handler(this.context.getMainLooper());
		if (shareType.equals(ShareCircleType.WIFIAP)) {
			this.createApShareCircle(ssid,shareKey);
		} else if (shareType.equals(ShareCircleType.WIFILAN)) {
			this.createLanShareCircle();
		}
		
	}
	
	/**
	 * add file to http server
	 * @param localIp      phone ip (Ap ip or lan ip)
	 * @param md5         the unique key for this file
	 * @param file           the local file
	 */
	public String addHttpResouce(String md5, File file){
		return httpFileServer.addFile(getLocalIp(), md5, file);
	}
	
	
	/**
	 * stop the http server and
	 * close the hotpot
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
	 * start ap hotpot and start http server
	 */
	private  void createApShareCircle(String ssid, String shareKey) {
		Log.d(LogUtil.LOG_TAG, "Start creating AP ShareCircle.");
		WifiConfiguration config = wifiApManager.createWifiApConfig(ssid,shareKey);
		
		Log.d(LogUtil.LOG_TAG, "Create SSID " + ssid + " with sharedKey " + shareKey);
		
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
	 * create lan hotpot
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
	 * get local ap ip;
	 * @return
	 */
	private String getLocalIp() {
		String ip = wifiApManager.getApLocalIp();
		return ip;
	}
}
