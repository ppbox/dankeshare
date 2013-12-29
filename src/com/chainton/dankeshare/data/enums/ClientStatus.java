/**
 * 
 */
package com.chainton.dankeshare.data.enums;

/**
 * 分享圈客户端状态枚举
 * @author Rivers
 *
 */
public enum ClientStatus {
	
	/**
	 * 客户端未连接到任何服务器
	 */
	UNCONNECTED("ClientStatus", "unconnected"),
	/**
	 * 客户端已连接
	 */
	CONNECTED("ClientStatus", "connected"),
	/**
	 * 客户端已进入分享圈
	 */
	JOINED("ClientStatus", "joined"),
	/**
	 * 客户端被拒绝进入分享圈
	 */
	REJECTED("ClientStatus", "rejected");

	private String typeText;
	private String valueText;
	
	private ClientStatus(String type, String value) {
		this.typeText = type;
		this.valueText = value;
	}

	public String typeText() {
		return this.typeText;
	}
	
	public String valueText() {
		return this.valueText;
	}
	
}
