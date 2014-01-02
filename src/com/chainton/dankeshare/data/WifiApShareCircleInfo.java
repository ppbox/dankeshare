package com.chainton.dankeshare.data;

import com.chainton.dankeshare.data.enums.ShareCircleType;

/**
 * 热点型分享圈信息类
 * @author 富林
 *
 */
public class WifiApShareCircleInfo extends LanShareCircleInfo {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1204510835699430919L;
	
	private String shareKey;

	public WifiApShareCircleInfo(String name, ShareCircleAppInfo appInfo) {
		super(name, ShareCircleType.WIFIAP, appInfo);
	}

	public String getShareKey() {
		return shareKey;
	}

	public void setShareKey(String shareKey) {
		this.shareKey = shareKey;
	}

	@Override
	public int hashCode() {
		if(ssid != null){
			return ssid.hashCode();
		}
		return super.hashCode();
	}
	
}
