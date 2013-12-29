package com.chainton.dankeshare;

import java.util.List;

import com.chainton.dankeshare.data.ClientInfo;
import com.chainton.dankeshare.data.ServiceManagerOpMode;
import com.chainton.dankeshare.data.ShareCircleAppInfo;
import com.chainton.dankeshare.data.enums.ShareCircleType;
import com.chainton.dankeshare.exception.WrongServiceModeException;

/**
 * 分享圈服务包装接口
 * @author 富林
 *
 */
public interface ServiceManager {

	/**
	 * 获取本地支持的分享圈类型
	 * @return 分享圈类型列表
	 */
	List<ShareCircleType> getSupportShareCircleType();
	
	/**
	 * 设置分享圈服务实例操作模式
	 * 缺省为客户端模式
	 * @param mode 分享圈服务实例操作模式
	 */
	void setOperationMode(ServiceManagerOpMode mode);
	
	/**
	 * 创建分享圈
	 * @param shareCircleName 分享圈名字
	 * @param shareCircleType 分享圈类型
	 * @param appInfo 创建分享圈的应用程序信息
	 * @param selfInfo 本地的客户端信息
	 * @param maxClients 分享圈允许接入的最大客户端数
	 * @param createCallback 创建操作回调
	 */
	void createShareCircle(CharSequence shareCircleName, ShareCircleType shareCircleType, ShareCircleAppInfo appInfo, ClientInfo selfInfo,
			int maxClients, CreateShareCircleCallback createCallback) throws WrongServiceModeException; 
	
	/**
	 * 搜索分享圈
	 * @param appInfo 创建分享圈的应用程序信息
	 * @param callback 搜索分享圈回调实例
	 */
	void searchShareCircle(ShareCircleAppInfo appInfo, SearchShareCircleCallback callback)  throws WrongServiceModeException;
	
	/**
	 * 创建分享圈客户端
	 * @param circleInfo 分享圈的信息
	 * @param selfInfo 客户端信息
	 * @param createCallback 创建操作回调
	 */
	void createShareCircleClient(ShareCircleInfo circleInfo, ClientInfo selfInfo,
			CreateShareCircleClientCallback createCallback) throws WrongServiceModeException;
	
	/**
	 * 保存当前的WIFI配置
	 */
	void saveWifiState();
	
	/**
	 * 恢复之前保存的WIFI配置
	 */
	void restoreWifiState();
	
	/**
	 * 注册wifi direct的广播
	 */
	void registerWifiDirect(); 
	
	/**
	 * 反注册wifi direct的广播
	 */
	void unRegisterWifiDirect();
	
}
