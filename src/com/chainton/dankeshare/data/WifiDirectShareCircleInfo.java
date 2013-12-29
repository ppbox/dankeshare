package com.chainton.dankeshare.data;

import com.chainton.dankeshare.data.enums.ShareCircleType;

import android.net.wifi.p2p.WifiP2pDevice;

/**
 * WIFI直连型分享圈信息类
 * @author 马志军
 *
 */
public class WifiDirectShareCircleInfo extends LanShareCircleInfo {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8711617431802107747L;
	
	private WifiP2pDevice device;
	
	public WifiDirectShareCircleInfo(CharSequence name, ShareCircleAppInfo appInfo) {
		super(name, ShareCircleType.WIFIDIRECT, appInfo);
	}

	/**
	 * 获取参与直连并且要作为服务端的设备
	 * @return
	 */
	public WifiP2pDevice getDevice() {
		return device;
	}

	/**
	 * 设置参与直连且要作为服务端的设备
	 * @param device
	 */
	public void setDevice(WifiP2pDevice device) {
		this.device = device;
	}

}
