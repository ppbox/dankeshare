package com.chainton.dankeshare.data;

import java.io.Serializable;
import java.util.UUID;

/**
 * 客户端信息类, 可扩展
 * @author 富林
 *
 */
public class ClientInfo implements Serializable {
	private static final long serialVersionUID = 2019222536772760899L;
	
	private String uid;
	private String name;
	private String ip;
	private int headIconIndex;

	public ClientInfo(){
		this.uid = UUID.randomUUID().toString();
	}
	
	public int getHeadIconIndex() {
		return headIconIndex;
	}

	public void setHeadIconIndex(int headIconIndex) {
		this.headIconIndex = headIconIndex;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
	public ClientInfo getCopy() {
		ClientInfo ci = new ClientInfo();
		ci.setHeadIconIndex(this.headIconIndex);
		ci.setUid(this.uid);
		ci.setName(this.name);
		ci.setIp(this.ip);
		return ci;
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj){
			return true;
		}
		if(obj == null){
			return false;
		}
		if(obj instanceof ClientInfo){
			ClientInfo other = (ClientInfo)obj;
			if(this.ip == null || other.ip == null){
				return false;
			}
			return this.ip.equals(other.ip);
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return uid.hashCode();
	}
	
}
