/**
 * 
 */
package com.chainton.dankeshare.impl;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.os.Handler;
import android.util.Log;

import com.chainton.dankeshare.HotspotHttpResult;
import com.chainton.dankeshare.HttpFileResult;
import com.chainton.dankeshare.OperationResult;
import com.chainton.dankeshare.data.enums.ShareCircleType;
import com.chainton.dankeshare.util.LogUtil;
import com.chainton.dankeshare.util.NetworkUtil;
import com.chainton.dankeshare.wifi.DefaultWifiApManager;
import com.chainton.forest.core.util.GlobalUtil;

/**
 * @author Soar
 *
 */
public class HotspotHttpFileService {
	
	/**
	 * 操作wifi 的 manager 接口类
	 */
	private DefaultWifiApManager wifiApManager;
	
	/**
	 * android context
	 */
	private Context context;
	
	private static HotspotHttpFileService hotPotHttpFileService = new HotspotHttpFileService();
	
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
	
	private boolean needHotPot = true;
	
	private HotspotHttpFileService(){
		
	}
	
	public static HotspotHttpFileService getInstance(){
		return hotPotHttpFileService;
	}
	
	/**
	 * 启动 http 服务器
	 * @param ssid    名称
	 * @param shareType 启动类型
	 * @param port  端口号
	 * @param needHotPot 启动服务器时是否需要先启动热点
	 * @param result 操作结果
	 * @param context android context
	 */
	public void startHttpShare(String ssid,ShareCircleType shareType,int port,boolean needHotPot,final HotspotHttpResult result,Context context) {
		
		if( port<=0 ){
			port = HTTP_PORT;
		}
		httpFileServer = new HttpFileServer(port,SHARE_STYLE);

		this.context = context.getApplicationContext();
		this.wifiApManager = new DefaultWifiApManager(this.context);
		this.handler = new Handler(this.context.getMainLooper());
		this.needHotPot = needHotPot;
		
		//是否需要启动热点
		if(needHotPot){
			if (shareType == ShareCircleType.AUTO) {
				if (this.wifiApManager. isWifiConnected()) {
					shareType = ShareCircleType.WIFILAN;
				} else {
					shareType = ShareCircleType.WIFIAP;
				}
			}
			if (shareType.equals(ShareCircleType.WIFIAP)) {
				this.createApAndHttp(ssid,null, result);
			} else if (shareType.equals(ShareCircleType.WIFILAN)) {
				this.createLanShareCircle(result);
			}
		}else{
			new Thread() {
				@Override
				public void run() {
					try {
						httpFileServer.startServer(new OperationResult(){

							@Override
							public void onSucceed() {
								result.onSucceed(null);
							}
							@Override
							public void onFailed() {
								result.onFailed();
							}
							
						});
					} catch (IOException e) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								result.onFailed();
							}
						});
					}
				}
			}.start();
		}
		
	}
	
	/**
	 * 启动 http 服务器只分享一次文件
	 * @param ssid    名称
	 * @param result 操作结果
	 * @param context android context
	 * @param file 要分享的文件
	 */
	public void shareOneFile(String ssid,final HotspotHttpResult result,Context context,final File file) {
		this.needHotPot = true;
		this.httpFileServer = new HttpFileServer(HTTP_PORT,STANDALONE_STYLE);
		this.context = context.getApplicationContext();
		this.wifiApManager = new DefaultWifiApManager(this.context);
		this.handler = new Handler(this.context.getMainLooper());
		this.createApAndHttp(ssid,file,result);

	}
	
	/**
	 * 添加文件
	 * @param httpFileResult  添加文件成功后的回调函数
	 * @param file	文件信息
	 * 
	 */
	public void addHttpResouce(final File file,final HttpFileResult httpFileResult){
		GlobalUtil.threadExecutor().execute(
				new Runnable() {
					@Override
					public void run() {
						UUID uuid = UUID.randomUUID();
						final String url = httpFileServer.addFile(getLocalIp(),uuid.toString(), file);
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
	 * @param ip  启动热点后的iP
	 * @param file	文件信息
	 * @return url  能够访问到文件的 url
	 */
	private String addOneResouce(String ip,File file){
		UUID uuid = UUID.randomUUID();
		return httpFileServer.addFile(ip,uuid.toString(), file);
	}
	
	/**
	 * 停止http server
	 * @param HotspotHttpResult  回调函数
	 */
	public void stopHttpShare(final OperationResult result){
		
		if(null != result){
			try{
				stopHttpAndHotspot(result);
			}catch(Exception e){
				e.printStackTrace();
				result.onFailed();
			}
		}else{
			stopHttpAndHotspot(null);
		}
		
	}
	
	/**
	 * 关闭http 和 hotspot 
	 * @param result
	 */
	private void stopHttpAndHotspot(final OperationResult result){
		//关闭http 服务
		if(httpFileServer.isRunning()){
			httpFileServer.stopServer();
		}
		//关闭热点服务
		if(needHotPot){
			wifiApManager.closeWifiAp(result); 
		}
	}
	
	/**
	 * 创建热点
	 * @param ssid 热点名称
	 */
	private  void createApAndHttp(String ssid,final File file,final HotspotHttpResult result) {
		
		WifiConfiguration config = wifiApManager.creatSimpleConfig(ssid);
		Log.d(LogUtil.LOG_TAG, "Create SSID " + ssid + " without sharedKey ");
		
		wifiApManager.openWifiAp(config, new OperationResult() {
			
			String ip;
			@Override
			public void onSucceed() {
				new Thread() {
					@Override
					public void run() {
						ip = getLocalIp();
						if (ip == null) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									result.onFailed();
								}
							});
							return;
						}
						Log.i(LogUtil.LOG_TAG, "hot ip: "+ip);
						try {
							
							httpFileServer.startServer(new OperationResult() {
								@Override
								public void onSucceed() {
									if(null != file){
										final String url =  addOneResouce(ip,file);
										handler.post(new Runnable() {
											@Override
											public void run() {
												result.onSucceed(url.substring(0, url.lastIndexOf("/")));
											}
										});
									}else{
										result.onSucceed(null);
									}
									
								}

								@Override
								public void onFailed() {
									handler.post(new Runnable() {
										@Override
										public void run() {
											result.onFailed();
										}
									});
								}
								
							});
						} catch (IOException e) {
							handler.post(new Runnable() {
								@Override
								public void run() {
									result.onFailed();
								}
							});
						}
					}
				}.start();
				
			}
			@Override
			public void onFailed() {
				wifiApManager.closeWifiAp(null);
			}
		});
	}
	

	/**
	 * 创建局域网类型server
	 */
	private void createLanShareCircle(final HotspotHttpResult result) {
		String ip = getLocalIp();
		if (ip == null) {
			handler.post(new Runnable() {
				@Override
				public void run() {
					result.onFailed();
				}
			});
			return;
		}
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
			if(null!=ip){
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
