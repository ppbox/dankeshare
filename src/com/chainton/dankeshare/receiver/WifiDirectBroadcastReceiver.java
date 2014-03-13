package com.chainton.dankeshare.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.Channel;

import com.chainton.dankeshare.WifiDirectManager;

/**
 * 广播接收者，接收和处理  Wifi Direct 过程中发送的各种系统广播
 * @author Silas
 *
 */
public class WifiDirectBroadcastReceiver extends BroadcastReceiver {

    private WifiP2pManager manager;
    private Channel channel;
    private WifiDirectManager wifiDirectManager;

    public WifiDirectBroadcastReceiver(WifiP2pManager manager, Channel channel,
    		WifiDirectManager wifiDirectManager) {
        super();
        this.manager = manager;
        this.channel = channel;
        this.wifiDirectManager = wifiDirectManager;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
            	wifiDirectManager.setIsWifiP2pEnabled(true);
            } else {
            	wifiDirectManager.setIsWifiP2pEnabled(false);
                wifiDirectManager.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            if (manager != null) {
            	manager.requestPeers(channel, wifiDirectManager.getPeerListListener());
            }
        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            if (manager == null) {
                return;
            }

            NetworkInfo networkInfo = (NetworkInfo) intent
                    .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

            if (networkInfo.isConnected()) { // 连接设备成功
            	manager.requestConnectionInfo(channel, wifiDirectManager.getConnectionInfoListener());
            } else {
//            	wifiDirectManager.resetData();
            }
        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
        	WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(
                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
        	wifiDirectManager.updateThisDevice(device);

        }
    }
}