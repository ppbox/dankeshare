package com.chainton.dankeshare;

import com.chainton.dankeshare.data.ShareCircleAppInfo;
import com.chainton.dankeshare.data.WifiApShareCircleInfo;

/**
 * 分享圈名称编解码接口
 * @author 富林，Rivers
 *
 */
public interface WifiApNameCodec {
	
	/**
	 * 根据分享圈配置信息编码SSID并创建分享圈配置信息实例
	 * @param shareCircleName 分享圈名称
	 * @param appInfo 分享圈应用程序信息
	 * @return 分享圈配置信息
	 */
	WifiApShareCircleInfo encodeWifiApName(CharSequence shareCircleName, ShareCircleAppInfo appInfo);
	
	/**
	 * 解码ssid获取分享圈配置信息
	 * @param ssid 分享圈热点的SSID
	 * @param appInfo 分享圈应用程序信息
	 * @return 分享圈配置信息
	 */
	WifiApShareCircleInfo decodeWifiApName(String ssid, ShareCircleAppInfo appInfo);
}
