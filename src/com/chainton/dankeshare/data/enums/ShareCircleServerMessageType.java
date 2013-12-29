package com.chainton.dankeshare.data.enums;

/**
 * 分享圈逻辑服务器端数据包类型定义
 * @author Rivers
 *
 */
public enum ShareCircleServerMessageType {
	/**
	 * 返回分享圈信息
	 */
	RETURN_SHARE_CIRCLE_INFO("RETURN_SHARE_CIRCLE_INFO", 1),
	/**
	 * 同意客户端加入
	 */
	ACCEPT_JOIN("ACCEPT_JOIN", 2),
	/**
	 * 拒绝客户端加入
	 */
	REJECT_JOIN("REJECT_JOIN", 3),
	/**
	 * 有分享资源加入
	 */
	RESOURCE_ADDED("RESOURCE_ADDED", 4),
	/**
	 * 有分享资源被移除
	 */
	RESOURCE_REMOVED("RESOURCE_REMOVED", 5),
	/**
	 * 有新客户端加入
	 */
	CLIENT_JOINED("CLIENT_JOINED", 6),
	/**
	 * 有客户端退出
	 */
	CLIENT_EXITED("CLIENT_EXITED", 7),
	/**
	 * 分享圈逻辑服务器退出
	 */
	SERVER_EXITED("SERVER_EXITED", 8),
	/**
	 * 踢出逻辑客户端
	 */
	KICK_OFF("KICK_OFF", 9),
	/**
	 * 自定义数据包
	 */
	DATA_PACKET("DATA_PACKET", 99),
	/**
	 * 接受客户端注册
	 */
	ACCEPT_REGISTER("ACCEPT_REGISTER", 200),
	/**
	 * 未知类型
	 */
	UNKNOWN("UNKNOWN", 255);

	private int intValue;
	private String valueText;
	
	private ShareCircleServerMessageType(String valueText, int intValue) {
		this.intValue = intValue;
		this.valueText = valueText;
	}

	public int intValue() {
		return this.intValue;
	}
	
	public String valueText() {
		return this.valueText;
	}
	
	public static ShareCircleServerMessageType parseFromInt(int intValue) {
		for (ShareCircleServerMessageType type : ShareCircleServerMessageType.values()) {
			if (type.intValue() == intValue) {
				return type;
			}
		}
		return ShareCircleServerMessageType.UNKNOWN;
	}
	
}
