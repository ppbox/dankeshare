/**
 * 
 */
package com.chainton.dankeshare;

import java.io.Serializable;

import com.chainton.dankeshare.data.ShareCircleAppInfo;
import com.chainton.dankeshare.data.enums.ShareCircleType;

/**
 * 分享圈信息接口
 * @author Rivers
 *
 */
public interface ShareCircleInfo extends Serializable {

	/**
	 * 取得分享圈名字
	 * @return 分享圈名字
	 */
	CharSequence getName();

	/**
	 * 设置分享圈名字
	 * @param name 分享圈名字
	 */
	void setName(CharSequence name);

	/**
	 * 取得分享圈类型
	 * @return 分享圈类型
	 */
	ShareCircleType getShareCircleType();
	
	/**
	 * 取得分享圈热点SSID
	 */
	String getSSID();
	
	/**
	 * 设置分享圈热点SSID
	 */
	void setSSID(String ssid);

	/**
	 * 取得分享圈服务器IP
	 * @return 分享圈服务器IP
	 */
	String getServerIP();

	/**
	 * 设置分享圈服务器IP
	 * @param serverIP 分享圈服务器IP
	 */
	void setServerIP(String serverIP);
	
	/**
	 * 分享圈当前应用程序信息
	 * @return 应用程序信息
	 */
	ShareCircleAppInfo getApplicationInfo();

	/**
	 * 设置已在分享圈内的客户端数
	 * @return 分享圈内的客户端数
	 */
	void setAcceptedClients(int acceptedClients);

	/**
	 * 取得已在分享圈内的客户端数
	 * @return 分享圈内的客户端数
	 */
	int getAcceptedClients();

	/**
	 * 设置分享圈最大客户端数
	 * @param maxClients 分享圈最大客户端数
	 */
	void setMaxClients(int maxClients);
	
	/**
	 * 取得分享圈最大客户端数
	 * @return 分享圈最大客户端数
	 */
	int getMaxClients();
	
	/**
	 * 判断分享圈是否满员
	 * @return
	 */
	boolean isFull();

}
