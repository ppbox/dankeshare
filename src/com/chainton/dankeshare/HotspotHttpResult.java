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
	 * 创建热点 http 服务成功时回调
	 * @param url 如果创建热点并且同时上传文件，则返回文件url
	 */
	void onSucceed(String url);
	
	/**
	 * 创建热点 http 服务 失败时回调
	 */
	void onFailed();
}
