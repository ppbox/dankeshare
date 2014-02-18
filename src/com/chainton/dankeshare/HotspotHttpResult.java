/**
 * 
 */
package com.chainton.dankeshare;



/**
 * 创建热点http 服务接口
 * @author Soar
 *
 */
public interface HotspotHttpResult{
	

	/**
	 * 创建分享圈成功时回调
	 * @param sci 分享圈信息
	 */
	void onSucceed(String url);
	
	/**
	 * 创建分享圈失败时回调
	 */
	void onFailed();
}
