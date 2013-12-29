package com.chainton.dankeshare;

import java.util.Collection;

/**
 * 搜索分享圈回调接口
 * @author 富林
 *
 */
public interface SearchShareCircleCallback {
	
	/**
	 * 搜索到可用分享圈时回调
	 * @param shareCircleInfo 分享圈信息
	 */
	void onFoundShareCircle(ShareCircleInfo shareCircle);
	
	/**
	 * 搜索结束时搜索到可用分享圈时回调
	 * @param valideShareCircles 所有可用的分享圈集合
	 */
	void onSearchSucceed(Collection<ShareCircleInfo> validShareCircles);
	
	/**
	 * 搜索分享圈超时回调
	 */
	void onSearchTimeout();
}
