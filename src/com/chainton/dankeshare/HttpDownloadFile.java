package com.chainton.dankeshare;


/**
 * Http 下载接口
 *
 */
public interface HttpDownloadFile {
	/**
	 * 下载文件
	 * @param url 要下载的文件路径
	 * @param destPath 保存到本地的路径
	 * @param listener 下载成功、失败和进度更新的回调接口
	 */
	void downloadShare(String url, String destPath, ReceiveShareListener listener);
	
	/**
	 * 下载文件用到的缩略图
	 * @param url 要下载的文件路径
	 * @param destPath 保存到本地的路径
	 * @param listener 下载成功、失败和进度更新的回调接口
	 */
	void downloadThumb(String url, String destPath, ReceiveShareListener listener);
}
