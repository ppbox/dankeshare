/**
 * 
 */
package com.chainton.dankeshare.data;

import java.io.Serializable;

/**
 * 应用程序信息类
 * @author Rivers
 *
 */
public class ShareCircleAppInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 5119222006605989190L;
	/**
	 * 应用程序标示ID
	 */
	public int appId;
	/**
	 * 应用程序名
	 */
	public String name;
	/**
	 * 应用程序版本
	 */
	public String version;
	/**
	 * 额外信息
	 */
	public String extraInfo;

	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(obj instanceof ShareCircleAppInfo){
			ShareCircleAppInfo other = (ShareCircleAppInfo)obj;
			if(this.appId == 0 || other.appId == 0){
				return false;
			}
			return (this.appId == other.appId);
		} else {
			return false;
		}
	}

}
