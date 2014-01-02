package com.chainton.dankeshare.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Handler;
import android.util.Log;

import com.chainton.dankeshare.CreateShareCircleCallback;
import com.chainton.dankeshare.CreateShareCircleClientCallback;
import com.chainton.dankeshare.SearchShareCircleCallback;
import com.chainton.dankeshare.ServiceManager;
import com.chainton.dankeshare.ShareCircleClient;
import com.chainton.dankeshare.ShareCircleInfo;
import com.chainton.dankeshare.ShareCircleServer;
import com.chainton.dankeshare.WifiApManager;
import com.chainton.dankeshare.WifiConnectManager;
import com.chainton.dankeshare.WifiDirectConnectCallback;
import com.chainton.dankeshare.WifiDirectManager;
import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ServiceManagerOpMode;
import com.chainton.dankeshare.data.ShareCircleAppInfo;
import com.chainton.dankeshare.data.WifiApShareCircleInfo;
import com.chainton.dankeshare.data.WifiDirectShareCircleInfo;
import com.chainton.dankeshare.data.enums.ShareCircleType;
import com.chainton.dankeshare.exception.WrongServiceModeException;

/**
 * 分享圈服务包装接口的缺省实现
 *
 * 本类的实现包含了对于安卓手机Wifi模块的一些基本操作和设置，以简化上层应用。
 * 本类为单例，本类中会保存{@link #getShareCircleClient ShareCircleClient}和{@link #getShareCircleServer ShareCircleServer}的唯一实例。
 * 调用{@link #createShareCircle createShareCircle}方法会创建{@link #getShareCircleClient ShareCircleClient}和{@link #getShareCircleServer ShareCircleServer}的实例。
 * 调用{@link #createShareCircleClient createShareCircleClient}方法会创建{@link #getShareCircleClient ShareCircleClient}的实例。
 * 
 * @author 富林，Rivers
 */
public final class DefaultServiceManager implements ServiceManager {
	/**
	 * android上下文
	 */
	private Context context;
	/**
	 * 唯一实例
	 */
	private static DefaultServiceManager instance;
	/**
	 * 单例锁
	 */
	private static final Object LOCK = new Object();

	private ServiceManagerOpMode opMode;
	private WifiDirectManager wifiDirectManager;
	private WifiApManager wifiApManager;
	private WifiConnectManager wifiConnectManager;
	private final ExecutorService executorService;
	private final DefaultWifiApNameCodec apNameCodec = new DefaultWifiApNameCodec();
	private final Handler handler;
	private Map<String, ScanResult> queriedScanResults;
	private Map<String, ShareCircleInfo> validShareCircles;
	private BroadcastReceiver searchReceiver = null;
	private boolean isServer = false;
	private String serverIp = null;

	/**
	 * 私有构造函数, 单例
	 * @param context
	 */
	private DefaultServiceManager(Context context) {
		this.context = context.getApplicationContext();
		DefaultWifiApManager manager = new DefaultWifiApManager(context);
		this.wifiApManager = manager;
		try{
			this.wifiDirectManager = new DefaultWifiDirectManager(context);
		} catch (Throwable e){
			e.printStackTrace();
		}
		this.wifiConnectManager = manager;
		this.executorService = Executors.newCachedThreadPool();
		this.queriedScanResults = new ConcurrentHashMap<String, ScanResult>();
		this.validShareCircles = new ConcurrentHashMap<String, ShareCircleInfo>();
		this.handler = new Handler(context.getMainLooper());
		this.opMode = ServiceManagerOpMode.AS_CLIENT;
	}

	/**
	 * 获取接口实现单例，第一次使用时调用
	 * @param context android上下文
	 * @return 接口实现实例
	 */
	public static ServiceManager getInstance(Context context) {
		if (instance == null) {
			synchronized (LOCK) {
				if (instance == null) {
					instance = new DefaultServiceManager(context);
				}
			}
		}
		return instance;
	}

	/**
	 * 获取接口实现单例
	 * @return 接口实现实例
	 */
	public static ServiceManager getInstance() {
		return instance;
	}

	@Override
	public List<ShareCircleType> getSupportShareCircleType() {
		List<ShareCircleType> list = new ArrayList<ShareCircleType>();
		list.add(ShareCircleType.WIFIAP);
		if(wifiDirectManager != null){
			list.add(ShareCircleType.WIFIDIRECT);
		}
		return list;
	}
	
	@Override
	public void setOperationMode(ServiceManagerOpMode mode) {
		if (!this.opMode.equals(mode)) {
			this.opMode = mode;
		}
	}

	@Override
	public void createShareCircle(final String shareCircleName, final ShareCircleType shareCircleType,
			final ShareCircleAppInfo appInfo, final ClientInfo selfInfo, final int maxClients,
			final CreateShareCircleCallback createCallback) throws WrongServiceModeException {
		if (!this.opMode.equals(ServiceManagerOpMode.AS_SERVER)) {
			throw new WrongServiceModeException("Cannot create ShareCircle in client mode.");
		}
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				if (shareCircleType.equals(ShareCircleType.WIFIAP)) {
					final WifiApShareCircleInfo waci = apNameCodec.encodeWifiApName(shareCircleName, appInfo);
					WifiConfiguration config = wifiApManager.createWifiApConfig(waci.getSSID(), waci.getShareKey());
					Log.d("ShareService", "Create SSID " + waci.getSSID() + " with sharedKey " + waci.getShareKey());
					wifiApManager.openWifiAp(config,
							new WifiApManager.WifiApOpenListener() {

								@Override
								public void onStartSuccessed() {
									try {
										String ip = wifiApManager.getApLocalIp();
										selfInfo.setIp(ip);
										waci.setServerIP(ip);
										final ShareCircleServer server = new DefaultShareCircleServer(waci, selfInfo, maxClients);
										final ShareCircleClient client = new DefaultShareCircleClient(selfInfo, handler);
										handler.post(new Runnable() {
											@Override
											public void run() {
												createCallback.onShareCircleCreateSuccess(waci, server, client);
											}
										});
									} catch (Exception e) {
										Log.i("mytest", "start service failed", e);
										handler.post(new Runnable() {

											@Override
											public void run() {
												createCallback.onShareCircleCreateFailed();
											}
										});
									}
								}

								@Override
								public void onStartFailed() {
									handler.post(new Runnable() {

										@Override
										public void run() {
											createCallback.onShareCircleCreateFailed();
										}
									});
								}

							});
				} else if (shareCircleType.equals(ShareCircleType.WIFIDIRECT)) {
					// 直连里没有什么分享圈要创建，但想作为服务端的要调用这方法，标记它作为服务端
					isServer = true;
				}
			}
		});
	}

	@Override
	public void searchShareCircle(final ShareCircleAppInfo appInfo, final SearchShareCircleCallback callback) throws WrongServiceModeException {
		if (!this.opMode.equals(ServiceManagerOpMode.AS_CLIENT)) {
			throw new WrongServiceModeException("Cannot search ShareCircle in server mode.");
		}
		this.queriedScanResults.clear();
		this.validShareCircles.clear();
		if (searchReceiver == null) {
			searchReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive(Context context, Intent intent) {
					Log.d("ShareService", "On received broadcast...");
					WifiManager wifiManager = (WifiManager) context
							.getSystemService(Context.WIFI_SERVICE);
					List<ScanResult> wifiAps = wifiManager.getScanResults();
					for (ScanResult sr : wifiAps) {
						if (!queriedScanResults.containsKey(sr.SSID)) {
							Log.d("ShareService", "Found useable SSID: " + sr.SSID);
							final WifiApShareCircleInfo waci = apNameCodec.decodeWifiApName(sr.SSID, appInfo);
							if (waci != null) {
								Log.d("ShareService", "Found usable wifiAP: " + waci.getName());
								queriedScanResults.put(sr.SSID, sr);
								validShareCircles.put(sr.SSID, waci);
								callback.onFoundShareCircle(waci);
							}
						}
					}
				}
			};
			context.registerReceiver(searchReceiver, new IntentFilter(
					WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					Log.d("ShareService", "Start searching...");
					wifiConnectManager.searchWifiAp();
//					for (int i = 0; i < 5; i++) {
//						try {
//							Thread.sleep(5000);
//							if (validShareCircles.size() == 0) {
//								wifiConnectManager.searchWifiAp();
//							} else {
//								break;
//							}
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//					}
					try {
						Thread.sleep(4000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (validShareCircles.isEmpty()) {
						handler.post(new Runnable() {
							@Override
							public void run() {
								callback.onSearchTimeout();
							}
						});
					} else {
						handler.post(new Runnable() {
							@Override
							public void run() {
								callback.onSearchSucceed(validShareCircles.values());
							}
						});
					}
					context.unregisterReceiver(searchReceiver);
					searchReceiver = null;
				}
			});
		}
		// 下面是搜索 WifiDirect 设备的
		if (wifiDirectManager != null){
			if (wifiDirectManager.check()) {
				wifiDirectManager.setPeerListListener(new PeerListListener() {
					@Override
					public void onPeersAvailable(WifiP2pDeviceList peers) {
						List<WifiP2pDevice> peersList = (List<WifiP2pDevice>) peers
								.getDeviceList();
						if (peersList.size() > 0) {
							for (WifiP2pDevice device : peersList) {
								WifiDirectShareCircleInfo shareCircleInfo = new WifiDirectShareCircleInfo("", appInfo);
								shareCircleInfo.setDevice(device);
								validShareCircles.put(shareCircleInfo.getDevice().deviceAddress, shareCircleInfo);
							}
						}
					}
				});
				wifiDirectManager.discoverPeers();
			}
		}
	}
	
	@Override
	public void createShareCircleClient(final ShareCircleInfo circleInfo,
			final ClientInfo selfInfo,
			final CreateShareCircleClientCallback callback) throws WrongServiceModeException {
		if (!this.opMode.equals(ServiceManagerOpMode.AS_CLIENT)) {
			throw new WrongServiceModeException("Cannot create more client in server mode.");
		}
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				createShareCircleClientInBackground(circleInfo, selfInfo, callback);
			}
		});
	}
	
	private void createShareCircleClientInBackground(final ShareCircleInfo circleInfo,
			final ClientInfo selfInfo,
			final CreateShareCircleClientCallback callback) {
		if (circleInfo.getShareCircleType().equals(ShareCircleType.WIFIAP)) {
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					ScanResult scanResult = queriedScanResults.get(circleInfo.getSSID());
					WifiApShareCircleInfo waci = (WifiApShareCircleInfo)circleInfo;
					wifiConnectManager.connectWifi(scanResult, waci.getShareKey(),
							new WifiConnectManager.ConnectCallback() {

								@Override
								public void onConnectSuccess(String ssid, String gatewayIp, String localIp) {
									Log.d("ShareService", "On connect success...");
									circleInfo.setServerIP(gatewayIp);
									selfInfo.setIp(localIp);
									final ShareCircleClient client = new DefaultShareCircleClient(selfInfo, handler);
									handler.post(new Runnable() {
										public void run() {
											callback.onSuccess(circleInfo, client);
										}
									});
								}

								@Override
								public void onConnectFailed(String errorMessage) {
									Log.d("ShareService", errorMessage);
									handler.post(new Runnable() {
										public void run() {
											callback.onFailure();
										}
									});
								}
							});
				}
			});
		} else if(circleInfo.getShareCircleType().equals(ShareCircleType.WIFIDIRECT)) {
			if(wifiDirectManager != null){
				final WifiP2pDevice device = ((WifiDirectShareCircleInfo) circleInfo)
						.getDevice();
				wifiDirectManager.setConnectionInfoListener(new ConnectionInfoListener() {
					
					@Override
					public void onConnectionInfoAvailable(WifiP2pInfo info) {
						// 自己是不是 GroupOwner，若是返回 true
						boolean isGroupOwner = info.isGroupOwner; 
						// 无论自己是不是 GroupOwner，都可以得到 GroupOwner 的地址
						String groupOwnerIp = info.groupOwnerAddress.getHostAddress(); 
						final ShareCircleClient client;
						// 客户端要知道服务端的ip地址，要知道被连接的那段地址，在直连里，无论哪段申请连接哪段，
						// 两端都会回调到这里的方法
						if(info.groupFormed) {
							// 如果想作为服务端，但没有ip，先创建Socket
							if(isServer && !isGroupOwner) {
								createSocket(groupOwnerIp);
							}
							// 想作为客户端的,却有ip，创建ServerSocket，得到对方ip
							if(!isServer && isGroupOwner) {
								createServerSocket();
								client = new DefaultShareCircleClient(selfInfo, handler);
								circleInfo.setServerIP(serverIp);
							} else if(!isServer && !isGroupOwner) {
								client = new DefaultShareCircleClient(selfInfo, handler);
								circleInfo.setServerIP(groupOwnerIp);
							} else {
								client = null;
							}
							wifiDirectManager.connect(device, new WifiDirectConnectCallback() {

								@Override
								public void onSuccess() {
									handler.post(new Runnable() {
										public void run() {
											callback.onSuccess(circleInfo, client);
										}
									});
								}

								@Override
								public void onFailure() {
									handler.post(new Runnable() {
										public void run() {
											callback.onFailure();
										}
									});
								}
								
							});
						}
					}
				});
			}
		}
	}

	@Override
	public void saveWifiState() {
		wifiApManager.saveWifiState();
	}

	@Override
	public void restoreWifiState() {
		wifiApManager.restoreWifiState();
	}

	@Override
	public void registerWifiDirect() {
		if(wifiDirectManager != null){
			wifiDirectManager.registerReceiver();
		}
	}

	@Override
	public void unRegisterWifiDirect() {
		if(wifiDirectManager != null){
			wifiDirectManager.unregisterReceiver();
		}
	}
	
	private void createSocket(final String groupOwnerIp) {
		new Thread() {
			public void run() {
				boolean succ = false; // 是否连接成功
				Socket socket = new Socket();
				while(!succ) {
					try {
						socket.bind(null);
						socket.connect((new InetSocketAddress(groupOwnerIp, 8989)), 5000);
						BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
						if("ok".equals(br.readLine())) {
							succ = true;
						}
						br.close();
					} catch (IOException e) {
						try {
							socket.close();
						} catch (IOException e1) {
							e1.printStackTrace();
						}
						socket = new Socket();
						e.printStackTrace();
					} finally {
						if(succ) {
							if (socket != null) {
								if (socket.isConnected()) {
									try {
										socket.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		}.start();
	}

	private void createServerSocket() {
		new Thread() {
			public void run() {
				try {
					ServerSocket serverSocket = new ServerSocket(8989);
					Socket client = serverSocket.accept();
					serverIp = client.getInetAddress().getHostAddress();
					client.getOutputStream().write("ok".getBytes());
					client.getOutputStream().close();
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

}
