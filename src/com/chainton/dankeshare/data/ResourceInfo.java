package com.chainton.dankeshare.data;

import java.io.Serializable;

/**
 * 分享资源信息类
 * @author 富林
 *
 */
public class ResourceInfo implements Serializable {

	private static final long serialVersionUID = 543249495660467714L;
	
	private String url;
	private String name;
	private String length;
	private String thumbUrl;
	private String md5;
	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	public String getThumbUrl() {
		return thumbUrl;
	}

	public void setThumbUrl(String thumbUrl) {
		this.thumbUrl = thumbUrl;
	}

	public String getLength() {
		return length;
	}

	public void setLength(String length) {
		this.length = length;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
