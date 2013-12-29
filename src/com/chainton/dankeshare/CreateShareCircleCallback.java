package com.chainton.dankeshare;

/**
 * 创建分享圈回调接口
 * @author 富林
 *
 */
public interface CreateShareCircleCallback {

	/**
	 * 创建分享圈成功时回调
	 * @param shareCircle 分享圈信息
	 * @param shareCircleServer 分享圈服务端实例
	 * @param 分享圈客户端实例
	 */
	void onShareCircleCreateSuccess(ShareCircleInfo shareCircle,
			ShareCircleServer shareCircleServer,
			ShareCircleClient shareCircleClient);
	
	/**
	 * 创建分享圈失败时回调
	 */
	void onShareCircleCreateFailed();
}
