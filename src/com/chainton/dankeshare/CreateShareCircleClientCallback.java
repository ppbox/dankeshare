/**
 * 
 */
package com.chainton.dankeshare;

/**
 * 创建分享圈客户端回调接口
 * @author Rivers
 *
 */
public interface CreateShareCircleClientCallback {

	/**
	 * 分享圈客户端创建成功时回调
	 * @param shareCircle 分享圈信息
	 * @param client 分享圈客户端实例
	 */
	void onSuccess(ShareCircleInfo shareCircle, ShareCircleClient client);
	
	/**
	 * 失败时回调
	 */
	void onFailure();

}
