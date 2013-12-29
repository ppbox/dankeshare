package com.chainton.dankeshare;

import java.io.File;

/**
 * 接收分享回调接口
 * @author 富林
 *
 */
public interface ReceiveShareListener {
	/**
	 * 接收成功时回调
	 * @param file
	 */
	void onFinish(File file);
	
	/**
	 * 接收失败时回调
	 */
	void onFailed();
	/**
	 * 更新下载进度, rate(0-1)
	 * @param rate
	 */
	void onUpdateProgress(float rate);
}
