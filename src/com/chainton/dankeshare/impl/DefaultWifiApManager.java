package com.chainton.dankeshare.impl;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.List;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;

import com.chainton.dankeshare.WifiApManager;
import com.chainton.dankeshare.WifiConnectManager;
import com.chainton.dankeshare.util.GlobalUtil;
import com.chainton.dankeshare.util.NetworkUtil;

/**
 * 默认WIFI热点管理及WIFI连接管理实现�?
 * @author 富林
 *
 */
public final class DefaultWifiApManager implements WifiApManager, WifiConnectManager {
	
	private static final int WIFI_AP_STATE_DISABLING = 0;
	private static final int WIFI_AP_STATE_DISABLING_VER4 = 10;
	private static final int WIFI_AP_STATE_DISABLED = 1;
	private static final int WIFI_AP_STATE_DISABLED_VER4 = 11;
	private static final int WIFI_AP_STATE_ENABLING = 2;
	private static final int WIFI_AP_STATE_ENABLING_VER4 = 12;
	private static final int WIFI_AP_STATE_ENABLED = 3;
	private static final int WIFI_AP_STATE_ENABLED_VER4 = 13;
	private static final int WIFI_AP_STATE_FAILED = 4;
	private static final int WIFI_AP_STATE_FAILED_VER4 = 14;
	
	/**
	 * 打开WIFI热点超时时间（秒�?
	 */
	private static final int OPEN_WIFI_AP_TIMEOUT = 15;
	/**
	 * 关闭WIFI热点超时时间（秒�?
	 */
	private static final int CLOSE_WIFI_AP_TIMEOUT = 4;
	/**
	 * �?��WIFI连接超时时间（秒�?
	 */
	private static final int VALIDATE_WIFI_CONNECTION_TIMEOUT = 10;
	
	/**
	 * android上下�?
	 */
	private Context context;
	private WifiManager wifiManager;
	
	/**
	 * 当前热点状�?
	 */
	private WifiApStatus currentStatus;
	
	/**
	 * 已保存的当前热点信息
	 */
	private OldNetInfo oldNetInfo;
	
	/**
	 * 热点状�?枚举
	 * @author 富林
	 *
	 */
	public static enum WifiApStatus{
		ENABLING, ENABLED, DISABLING, DISABLED, UNKNOWN
	}
	
	/**
	 * android内置热点状�?整数值与热点状�?的映射关�?
	 */
	public static final SparseArray<WifiApStatus> statusArray;
	
	/**
	 * 初始化映射关�?
	 */
	static{
		statusArray = new SparseArray<WifiApStatus>();
		statusArray.put(WIFI_AP_STATE_DISABLING, WifiApStatus.DISABLING);
		statusArray.put(WIFI_AP_STATE_DISABLING_VER4, WifiApStatus.DISABLING);
		statusArray.put(WIFI_AP_STATE_DISABLED, WifiApStatus.DISABLED);
		statusArray.put(WIFI_AP_STATE_DISABLED_VER4, WifiApStatus.DISABLED);
		statusArray.put(WIFI_AP_STATE_ENABLING, WifiApStatus.ENABLING);
		statusArray.put(WIFI_AP_STATE_ENABLING_VER4, WifiApStatus.ENABLING);
		statusArray.put(WIFI_AP_STATE_ENABLED, WifiApStatus.ENABLED);
		statusArray.put(WIFI_AP_STATE_ENABLED_VER4, WifiApStatus.ENABLED);
		statusArray.put(WIFI_AP_STATE_FAILED, WifiApStatus.UNKNOWN);
		statusArray.put(WIFI_AP_STATE_FAILED_VER4, WifiApStatus.UNKNOWN);
	}
	
	public DefaultWifiApManager(Context context){
		this.context = context;
		this.wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
	}
	
	@Override
	public WifiConfiguration createWifiApConfig(String ssid, String shareKey) {
		return createConfig(ssid, shareKey);
	}

	@Override
	public void openWifiAp(WifiConfiguration config, final WifiApOpenListener openListener) {
		WifiConfiguration currentConfig = getConfig();
		if (getCurrentStatus() == WifiApStatus.DISABLED || (currentConfig != null && !isEqualsSSID(config.SSID, currentConfig.SSID))) {
			if (setWifiApEnabled(config, true)) {
				GlobalUtil.threadExecutor().execute(new Runnable() {
					@Override
					public void run() {
						int timeoutCount = 0;
						boolean openSucceed = false;
						int state = -1;
						while (timeoutCount < OPEN_WIFI_AP_TIMEOUT) {
							state = getWifiApState();
							if (statusArray.get(state) == WifiApStatus.ENABLED) {
								openSucceed = true;
								break;
							} else {
								try {
									Thread.sleep(1000);
								} catch(Exception e) {
									e.printStackTrace();
								}
								timeoutCount++;
							}
						}
						if (openListener != null) {
							if (openSucceed) {
								openListener.onStartSucceed();
							} else {
								openListener.onStartFailed();
							}
						}
					}
				});
			} else {
				if (openListener != null) {
					openListener.onStartFailed();
				}
			}
		} else {
			if (openListener != null) {
				openListener.onStartSucceed();
			}
		}
	}

	@Override
	public void closeWifiAp(final WifiApCloseListener closeListener) {
		GlobalUtil.threadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				boolean closeSucceed = false;
				int timeoutCount = 0;
				int state = -1;
				while (timeoutCount < CLOSE_WIFI_AP_TIMEOUT) {
					state = getWifiApState();
					if (statusArray.get(state) == WifiApStatus.DISABLED) {
						closeSucceed = true;
						break;
					} else {
						setWifiApEnabled(null, false);
						try {
							Thread.sleep(1000);
						} catch(Exception e) {
							e.printStackTrace();
						}
						timeoutCount++;
					}
				}
				if (closeListener != null) {
					if (closeSucceed) {
						closeListener.onCloseSucceed();
					} else {
						closeListener.onCloseFailed();
					}
				}
			}
		});
	}
	
	/**
	 * 校验连接
	 * @param wi
	 * @param callback
	 * @return 是否有效连接
	 */
	private boolean validIp(WifiInfo wi, InternalIpInfo ipInfo){
		long ipint = wi.getIpAddress();
		if(ipint != 0){
			ipInfo.ip = NetworkUtil.long2ip(ipint);
			ipInfo.gatewayIp = NetworkUtil.long2ip((ipint | 0x01000000) & 0x01FFFFFF);
			if (ipInfo.ip != null && ipInfo.ip.length() > 0) {
				return true;
			}
		}
		Log.d(GlobalUtil.LOG_TAG, "Cannot get valid IP from " + wi.getSSID() + " on " + wi.getMacAddress());
		return false;
	}
	
	/**
	 * IP信息�?
	 * @author Rivers
	 *
	 */
	private class InternalIpInfo {
		/**
		 * 网关IP
		 */
		public String gatewayIp;
		/**
		 * IP地址
		 */
		public String ip;
	}
	
	@Override
	public void connectWifi(final ScanResult scanResult, String shareKey, final ConnectCallback callback) {
		final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
		Log.d(GlobalUtil.LOG_TAG, "Start connect to " + scanResult.SSID + " shareKey: " + shareKey);
		wifiManager.setWifiEnabled(true);
		WifiInfo wi = wifiManager.getConnectionInfo();
		InternalIpInfo ipInfo = new InternalIpInfo();
		if (wi != null && wi.getSSID() != null) {
			if (isEqualsSSID(scanResult.SSID, wi.getSSID())) {
				if (validIp(wi, ipInfo)) {
					callback.onConnectSuccess(wi.getSSID(), ipInfo.gatewayIp, ipInfo.ip);
					return;
				}
			} else {
				wifiManager.disableNetwork(wi.getNetworkId());
			}
		}
		
		List<WifiConfiguration> wcs = wifiManager.getConfiguredNetworks();
		WifiConfiguration targetWc = null;
		String ssidyh = "\"" + scanResult.SSID + "\"";
		if (wcs != null) {
			for (WifiConfiguration wc : wcs) {
				if (isEqualsSSID(wc.SSID, ssidyh)) {
					targetWc = wc;
					break;
				}
			}
		}
		Log.d(GlobalUtil.LOG_TAG, "Connect to " + scanResult.SSID + ", step2");
		if (targetWc != null && shareKey == null) {
			wifiManager.enableNetwork(targetWc.networkId, true);
		} else {
			if(targetWc != null){
				wifiManager.removeNetwork(targetWc.networkId);
			}
			Log.d(GlobalUtil.LOG_TAG, "Connect to " + scanResult.SSID + ", step3");
			WifiConfiguration wcg = new WifiConfiguration();
			wcg.SSID = ssidyh;
			if (shareKey != null && scanResult.capabilities.contains("PSK")) {
				String shareKeyYH = "\"" + shareKey + "\"";
				wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
				wcg.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
				wcg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
				wcg.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
				wcg.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
				wcg.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
				wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
				wcg.preSharedKey = shareKeyYH;
			} else {
				wcg.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
			}
			
			wcg.networkId = wifiManager.addNetwork(wcg);
			if (wcg.networkId != -1) {
				Log.d(GlobalUtil.LOG_TAG, "Enabling connection to " + scanResult.SSID);
				wifiManager.enableNetwork(wcg.networkId, true);
			} else {
				callback.onConnectFailed("Cannot connect to network " + wcg.SSID);
				return;
			}
		}
		/**
		 * 异步线程, 等待连接的状态变�? 超时判断以及校验连接的有效�?,并调用响应的回调方法
		 */
		GlobalUtil.threadExecutor().execute(new Runnable() {
			@Override
			public void run() {
				int timeoutCount = 0;
				InternalIpInfo ipInfo = new InternalIpInfo();
				while (timeoutCount < VALIDATE_WIFI_CONNECTION_TIMEOUT) {
					if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
						WifiInfo wi = wifiManager.getConnectionInfo();
						if (wi != null && wi.getSSID() != null) {
							if (isEqualsSSID(scanResult.SSID, wi.getSSID())) {
								if (validIp(wi, ipInfo)) {
									callback.onConnectSuccess(wi.getSSID(), ipInfo.gatewayIp, ipInfo.ip);
									return;
								}
							}
						}
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					timeoutCount++;
				}
				callback.onConnectFailed("Cannot connect to network " + scanResult.SSID);
			}
		});
	}

	/**
	 * 当前热点是否已打�?
	 * @return true:热点�?��状�?, false: 热点关闭状�?
	 */
	private boolean isWifiApEnabled(){
		return (getCurrentStatus() == WifiApStatus.ENABLED);
	}
	
	
	@Override
	public String getApLocalIp() {
		try {
			for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
				NetworkInterface intf = en.nextElement();
				for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address)) {
						return inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException ex) {
			Log.i("mytest", "getApLocalIp Error", ex);
		}
		return null;
	}


	/**
	 * 保存WIFI连接信息
	 * @author 富林
	 *
	 */
	private class OldNetInfo{
		/**
		 * 2g/3g数据是否打开
		 */
		boolean mobileDataisEnabled;
		
		/**
		 * 热点是否打开
		 */
		boolean wifiApIsEnabled;
		
		/**
		 * WIFI连接是否打开
		 */
		boolean wifiIsEnabled;
		
		/**
		 * 如果WIFI是连接的, 当前的WIFI的id
		 */
		int wifiId;
		
		/**
		 * 如果热点是打�?���? 当前的热点配置信�?
		 */
		WifiConfiguration apConfig;
		
		void saveState(){
			mobileDataisEnabled = getMobileDataStatus();
			wifiApIsEnabled = isWifiApEnabled();
			apConfig = getConfig();
			wifiIsEnabled = isWifiConnected();
			if (wifiIsEnabled) {
				wifiId = getCurrentWifiId();
			}
		}
		
		void restore(final RestoreWifiStateResult result){
			if (mobileDataisEnabled) {
				setMobileDataStatus(mobileDataisEnabled);
			}
			setConfig(apConfig);
			if (wifiApIsEnabled) {
				openWifiAp(apConfig, new WifiApOpenListener() {
					@Override
					public void onStartSucceed() {
						result.onSucceed();
					}
					@Override
					public void onStartFailed() {
						result.onFailed();
					}
				});
			} else {
				closeWifiAp(new WifiApCloseListener() {
					@Override
					public void onCloseSucceed() {
						if (wifiIsEnabled) {
							connectToAp(wifiId);
						}
						result.onSucceed();
					}
					@Override
					public void onCloseFailed() {
						result.onFailed();
					}
				});
			}
		}
	}

	@Override
	public void saveWifiState() {
		if(oldNetInfo == null){
			oldNetInfo = new OldNetInfo();
		}
		oldNetInfo.saveState();
		Log.i(this.getClass().getName(), " saveWifiState  oldNetInfo ssid:"+oldNetInfo.apConfig.SSID);
	}

	@Override
	public void restoreWifiState(RestoreWifiStateResult result) {
		if(oldNetInfo != null){
			oldNetInfo.restore(result);
			Log.i(this.getClass().getName(), " not null ssid:"+oldNetInfo.apConfig.SSID);
		}
	}
	
	/**
	 * 创建�?��WIFI连接配置
	 * @param ssid
	 * @param shareKey
	 * @return
	 */
	private WifiConfiguration createConfig(String ssid, String shareKey){
		WifiConfiguration config = getConfig();
		if(config == null){
			config = new WifiConfiguration();
		}
		config.SSID = ssid;
		config.status = WifiConfiguration.Status.ENABLED;
		config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
		config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
		config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
		config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
		config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
		config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
		config.preSharedKey = shareKey;
		return config;
	}

	/**
	 * 获取当前热点配置信息
	 * @return 热点配置信息
	 */
	private WifiConfiguration getConfig(){
		try {
            Method method = wifiManager.getClass().getMethod("getWifiApConfiguration");
            method.setAccessible(true);
            WifiConfiguration config = (WifiConfiguration) method.invoke(wifiManager);
            return config;
        } catch (Exception e) {
        }
        return null;
	}
	
	/**
	 * 设置WIFI热点配置
	 * @param config
	 */
	public void setConfig(WifiConfiguration config){
		configHtcWithOutSharekey(config);
		setNormalConfig(config);
	}
	
	@Override
	public void searchWifiAp() {
		wifiManager.setWifiEnabled(true);
		wifiManager.startScan();
	}

	/**
	 * 配置htc热点,无密�?
	 * @param config
	 */
	private void configHtcWithOutSharekey(WifiConfiguration config) {
		Field localField1;
        if (!Build.MODEL.contains("HTC") && !Build.MANUFACTURER.contains("HTC")) {
            return;
        }
        try {
            localField1 = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
            localField1.setAccessible(true);
            Object localObject2 = localField1.get(config);
            localField1.setAccessible(false);

            if (localObject2 != null) {
                Field localField5 = localObject2.getClass().getDeclaredField("SSID");
                localField5.setAccessible(true);
                localField5.set(localObject2, config.SSID);
                localField5.setAccessible(false);

                Field localField4 = localObject2.getClass().getDeclaredField("BSSID");
                localField4.setAccessible(true);
                localField4.set(localObject2, config.BSSID);
                localField4.setAccessible(false);

                Field localField6 = localObject2.getClass().getDeclaredField("dhcpEnable");
                localField6.setAccessible(true);
                localField6.setInt(localObject2, 1);
                localField6.setAccessible(false);
                
                Field localField2 = localObject2.getClass().getDeclaredField("secureType");
                localField2.setAccessible(true);
                localField2.set(localObject2, "open");
                localField2.setAccessible(false);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	/**
	 * 设置普�?热点配置
	 * @param config
	 */
	private void setNormalConfig(WifiConfiguration config){
		try {
            Method method1 = wifiManager.getClass().getMethod(
                    "setWifiApConfiguration", WifiConfiguration.class);
            method1.invoke(wifiManager, config);
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}
	
	/**
	 * 获取当前热点状�?枚举实例
	 * @return 热点状�?枚举实例
	 */
	private WifiApStatus getCurrentStatus(){
		int state = getWifiApState();
		currentStatus = statusArray.get(state);
		return currentStatus;
	}
	
	/**
	 * 从android系统获取的热点状态整数�?
	 * @return 热点状�?整数�?
	 */
	private int getWifiApState(){
		try {
			Method method = wifiManager.getClass().getMethod("getWifiApState");
			return (Integer) method.invoke(wifiManager);
		} catch (Throwable e) {
			return WIFI_AP_STATE_FAILED;
		}
	}
	
	/**
	 * 设置wifi热点的状�?
	 * @param config
	 * @param isEnabled
	 * @return 是否成功
	 */
	private boolean setWifiApEnabled(WifiConfiguration config, boolean isEnabled){
		if(isEnabled){
			wifiManager.setWifiEnabled(false);
		}
		try {  
			configHtcWithKey(config); //TODO why do we do this, shall we need to check phone vender before invoke it
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);  
            return (Boolean) method.invoke(wifiManager, config, isEnabled);  
        } catch (Exception e) {
        	e.printStackTrace();
            return false;  
        }
	}
	
	/**
	 * 设置htc手机的wifi热点
	 * @param cfg
	 */
	private void configHtcWithKey(WifiConfiguration cfg) {
        Field localField1;
        if (!Build.MODEL.contains("HTC") && !Build.MANUFACTURER.contains("HTC")) {
            return;
        }

        try {
            localField1 = WifiConfiguration.class.getDeclaredField("mWifiApProfile");
            localField1.setAccessible(true);
            Object localObject2 = localField1.get(cfg);
            localField1.setAccessible(false);

            if (localObject2 != null) {
                Field localField5 = localObject2.getClass().getDeclaredField("SSID");
                localField5.setAccessible(true);
                localField5.set(localObject2, cfg.SSID);
                localField5.setAccessible(false);

                Field localField4 = localObject2.getClass().getDeclaredField("BSSID");
                localField4.setAccessible(true);
                localField4.set(localObject2, cfg.BSSID);
                localField4.setAccessible(false);

                Field localField2 = localObject2.getClass().getDeclaredField("secureType");
                localField2.setAccessible(true);
                localField2.set(localObject2, "wpa2-psk");
                localField2.setAccessible(false);

                Field localField3 = localObject2.getClass().getDeclaredField("key");
                localField3.setAccessible(true);
                localField3.set(localObject2, cfg.preSharedKey);
                localField3.setAccessible(false);

                Field localField6 = localObject2.getClass().getDeclaredField("dhcpEnable");
                localField6.setAccessible(true);
                localField6.setInt(localObject2, 1);
                localField6.setAccessible(false);
            }
        } catch (Exception e) {
        	e.printStackTrace();
        }

    }
	
	/**
	 * 获取当前2g/3g数据连接是否已打�?
	 * @return 2g/3g数据连接是否已打�?
	 */
	private boolean getMobileDataStatus() {
        ConnectivityManager cm;
        cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        Class<?> cmClass = cm.getClass();
        Class<?>[] argClasses = null;
        Object[] argObject = null;
        Boolean isOpen = false;
        try {
            Method method = cmClass.getMethod("getMobileDataEnabled", argClasses);
            isOpen = (Boolean) method.invoke(cm, argObject);
        } catch (Exception e) {
            Log.d("getMobileDataStatus", e.toString());
        }

        return isOpen;
    }
	
	/**
	 * 设置2g/3g数据连接状�?
	 * @param enabled
	 * @return 是否设置成功
	 */
	private boolean setMobileDataStatus(boolean enabled) {
        ConnectivityManager conMgr = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        Class<?> conMgrClass = null;
        Field iConMgrField = null;
        Object iConMgr = null;
        Class<?> iConMgrClass = null;
        Method setMobileDataEnabledMethod = null;
        try {
            conMgrClass = Class.forName(conMgr.getClass().getName());
            iConMgrField = conMgrClass.getDeclaredField("mService");
            iConMgrField.setAccessible(true);
            iConMgr = iConMgrField.get(conMgr);
            iConMgrClass = Class.forName(iConMgr.getClass().getName());
            setMobileDataEnabledMethod = iConMgrClass.getDeclaredMethod("setMobileDataEnabled",
                    Boolean.TYPE);
            setMobileDataEnabledMethod.setAccessible(true);
            setMobileDataEnabledMethod.invoke(iConMgr, enabled);
        } catch (Exception e) {
            Log.d("getMobileDataStatus", e.toString());
            return false;
        }
        return true;
    }
	
	/**
	 * 当前是否已连接热�?
	 * @return 是否已连接热�?
	 */
	private boolean isWifiConnected() {
		if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
			WifiInfo wi = wifiManager.getConnectionInfo();
			if (wi != null && wi.getSSID() != null) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * 获取当前连接的WIFI热点的id
	 * @return 当前连接的WIFI热点的id
	 */
	private int getCurrentWifiId() {
		WifiInfo wi = wifiManager.getConnectionInfo();
		if (wi != null) {
			return wi.getNetworkId();
		}
		return 0;
	}
	
	/**
	 * 连接指定id的热�?
	 * @param netId
	 */
	private void connectToAp(int netId) {
		if (isWifiApEnabled()) {
			closeWifiAp(null);
		}
		if (wifiManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
			wifiManager.setWifiEnabled(true);
		}
		WifiInfo wi = wifiManager.getConnectionInfo();
		if (wi != null && wi.getSSID() != null) {
			if (wi.getNetworkId() != netId) {
				wifiManager.disableNetwork(wi.getNetworkId());
			}
		}
		wifiManager.enableNetwork(netId, true);
	}
	
	/**
	 * 判断两个热点ssid是否相同
	 * @param ssid1
	 * @param ssid2
	 * @return true:相同, false: 不同
	 */
	private boolean isEqualsSSID(String ssid1, String ssid2){
		if(ssid1 != null && ssid2 != null){
			return ssid1.replaceAll("\"", "").equals(ssid2.replaceAll("\"", ""));
		}
		return false;
	}

}
