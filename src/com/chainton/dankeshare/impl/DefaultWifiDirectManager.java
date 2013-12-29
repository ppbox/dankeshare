package com.chainton.dankeshare.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.widget.Toast;

import com.chainton.dankeshare.WifiDirectConnectCallback;
import com.chainton.dankeshare.WifiDirectManager;
import com.chainton.dankeshare.data.WifiDirectShareCircleInfo;
import com.chainton.dankeshare.receiver.WifiDirectBroadcastReceiver;

/**
 * 默认WIFI直连管理接口实现
 * @author 马志军
 *
 */
public final class DefaultWifiDirectManager implements WifiDirectManager {

	private WifiP2pManager manager;
	private Channel channel;
	private IntentFilter intentFilter = new IntentFilter();
	private BroadcastReceiver receiver = null;
	private Context context;
	
	private PeerListListener peerListListener;
	private ConnectionInfoListener connectionInfoListener;
	
	private WifiDirectShareCircleInfo wifiDirectInfo;
	
	private boolean isWifiP2pEnabled = false;
	
	public DefaultWifiDirectManager(Context context) {
		this.context = context;
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter
				.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) context
				.getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(context, context.getMainLooper(), null);
	}
	
	@Override	
	public void setIsWifiP2pEnabled(boolean isWifiP2pEnabled) {
		this.isWifiP2pEnabled = isWifiP2pEnabled;
	}
	
	@Override
	public void registerReceiver() {
		receiver = new WifiDirectBroadcastReceiver(manager, channel, this);
		context.registerReceiver(receiver, intentFilter);
	}

	@Override
	public void unregisterReceiver() {
		context.unregisterReceiver(receiver);
	}

	@Override
	public void resetData() {
		// TODO Auto-generated method stub
	}

	@Override
	public void discoverPeers() {
		manager.discoverPeers(channel, new ActionListener() {

			@Override
			public void onSuccess() {
				
			}

			@Override
			public void onFailure(int reason) {
				Toast.makeText(context, "搜索设备失败，错误代码：" + reason,
						Toast.LENGTH_SHORT).show();
			}
			
		});
		
	}

	@Override
	public PeerListListener getPeerListListener() {
		return peerListListener;
	}

	@Override
	public void setPeerListListener(PeerListListener peerListListener) {
		this.peerListListener = peerListListener;
	}
	

	@Override
	public ConnectionInfoListener getConnectionInfoListener() {
		return connectionInfoListener;
	}

	@Override
	public void setConnectionInfoListener(
			ConnectionInfoListener connectionInfoListener) {
		this.connectionInfoListener = connectionInfoListener;
	}

	@Override
	public void connect(WifiP2pDevice device, final WifiDirectConnectCallback callback) {
		WifiP2pConfig config = new WifiP2pConfig();
		config.deviceAddress = device.deviceAddress;
		config.wps.setup = WpsInfo.PBC;
		
		manager.connect(channel, config, new ActionListener() {
			
			@Override
			public void onSuccess() {
				callback.onSuccess();
			}
			
			@Override
			public void onFailure(int reason) {
				callback.onFailure();
			}
		});
	}

	@Override
	public void updateThisDevice(WifiP2pDevice device) {
		wifiDirectInfo.setDevice(device);
	}

	@Override
	public boolean check() {
		if (android.os.Build.VERSION.SDK_INT < 14) {
//			Toast.makeText(context, "您的系统版本不支持 Wifi Direct 功能",
//					Toast.LENGTH_SHORT).show();
			return false;
		} else if (!isWifiP2pEnabled) {
//			Toast.makeText(
//					context,
//					"请先确认您的硬件支持 Wifi Direct 功能，如果硬件支持请先去设置中打开 Wifi",
//					Toast.LENGTH_SHORT).show();
			return false;
		} else {
			return true;
		}
	}
}
