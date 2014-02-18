/**
 * 
 */
package com.chainton.dankeshare;



/**
 * 创建热点http 服务文件接口
 * @author Soar
 *
 */
public interface HttpFileResult{
	

	 /**
     * 返回添加一个文件时的回调函数
     * @param url
     */
	void onFileUrlGen(String url);
}
