/**
 * 
 */
package com.chainton.dankeshare.data.enums;

/**
 * 分享圈逻辑客户端数据包类型定义
 * @author Rivers
 *
 */
public enum ShareCircleClientMessageType {
	/**
	 * 查询分享圈信息
	 */
	QUERY_SHARE_CIRCLE_INFO("QUERY_SHARE_CIRCLE_INFO", 1),
	/**
	 * 请求加入分享圈
	 */
	REQUEST_ENTER("REQUEST_ENTER", 2),
	/**
	 * 离开分享圈
	 */
	EXIT_SHARE_CIRCLE("EXIT_SHARE_CIRCLE", 3),
	/**
	 * 获取所有已连接客户端信息
	 */
	GET_ALL_CLIENTS("GET_ALL_CLIENTS", 4),
	/**
	 * 获取所有以分享资源
	 */
	GET_ALL_SHARED_RESOURCES("GET_ALL_SHARED_RESOURCES", 5),
	/**
	 * 发送分享资源信息
	 */
	ADD_SHARE_RESOURCE("ADD_SHARE_RESOURCE", 6),
	/**
	 * 取消已分享的信息
	 */
	REMOVE_SHARED_RESOURCE("REMOVE_SHARED_RESOURCE", 7),
	/**
	 * 请求同步分享圈数据
	 */
	REQUEST_SYNCHRONIZE("REQUEST_SYNCHRONIZE", 8),
	/**
	 * 广播数据包
	 */
	AUTO_DISTRIBUTE_DATA("AUTO_DISTRIBUTE_DATA", 9),
	/**
	 * 给其他客户端发送数据包
	 */
	DISTRIBUTE_DATA_TO_OTHERS("DISTRIBUTE_DATA_TO_OTHERS", 10),
	/**
	 * 自定义数据包
	 */
	DATA_PACKET("DATA_PACKET", 99),
	/**
	 * 注册客户端信息
	 */
	REGISTER_CLIENT("REGISTER_CLIENT", 200),
	/**
	 * 未知类型
	 */
	UNKNOWN("UNKNOWN", 255);
	
	private int intValue;
	private String valueText;
	
	private ShareCircleClientMessageType(String valueText, int intValue) {
		this.intValue = intValue;
		this.valueText = valueText;
	}

	public int intValue() {
		return this.intValue;
	}
	
	public String valueText() {
		return this.valueText;
	}
	
	public static ShareCircleClientMessageType parseFromInt(int intValue) {
		for (ShareCircleClientMessageType type : ShareCircleClientMessageType.values()) {
			if (type.intValue() == intValue) {
				return type;
			}
		}
		return ShareCircleClientMessageType.UNKNOWN;
	}
	
}
