package com.chainton.dankeshare.data;

import com.chainton.dankeshare.ShareCircleInfo;
import com.chainton.dankeshare.data.enums.ShareCircleType;

/**
 * 分享圈配置信息基类
 * @author 富林
 *
 */
public class LanShareCircleInfo implements ShareCircleInfo {

	private static final long serialVersionUID = -722385875207029976L;
	
	protected final ShareCircleType type;
	protected CharSequence name;
	protected String ssid;
	protected String serverIP;
	protected ShareCircleAppInfo appInfo;
	/**
	 * 所有加入分享圈的用户
	 */
	protected int acceptedClients;
	protected int maxClients;
	
	public LanShareCircleInfo(CharSequence shareCircleName, ShareCircleType type, ShareCircleAppInfo appInfo) {
		this.name = shareCircleName;
		this.type = type;
		this.appInfo = appInfo;
		this.acceptedClients = 0;
		this.maxClients = 0;
	}

	@Override
	public CharSequence getName() {
		return name;
	}
	
	@Override
	public void setName(CharSequence name) {
		this.name = name;
	}

	public ShareCircleType getShareCircleType() {
		return type;
	}

	public String getSSID() {
		return this.ssid;
	}

	public void setSSID(String ssid) {
		this.ssid = ssid;
	}
	
	public String getServerIP() {
		return serverIP;
	}

	public void setServerIP(String serverIP) {
		this.serverIP = serverIP;
	}
	
	@Override
	public ShareCircleAppInfo getApplicationInfo() {
		return this.appInfo;
	}
	
	public void setAcceptedClients(int acceptedClients) {
		this.acceptedClients = acceptedClients;
	}
	
	public int getAcceptedClients() {
		return this.acceptedClients;
	}
	
	public void setMaxClients(int maxClients) {
		this.maxClients = maxClients;
	}
	
	public int getMaxClients() {
		return this.maxClients;
	}
	
	public boolean isFull() {
		if (this.acceptedClients >= this.maxClients) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((serverIP == null) ? 0 : serverIP.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LanShareCircleInfo other = (LanShareCircleInfo) obj;
		if (serverIP == null) {
			if (other.serverIP != null)
				return false;
		} else if (!serverIP.equals(other.serverIP))
			return false;
		return true;
	}
	
}
