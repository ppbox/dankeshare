package com.chainton.dankeshare.impl;

import java.nio.CharBuffer;

import com.chainton.dankeshare.WifiApNameCodec;
import com.chainton.dankeshare.data.ShareCircleAppInfo;
import com.chainton.dankeshare.data.WifiApShareCircleInfo;
import com.chainton.dankeshare.util.DigestUtils;

/**
 * 默认WIFI热点ssid编解码实现
 * @author 富林
 *
 */
public final class DefaultWifiApNameCodec implements WifiApNameCodec {
	
	private static final int SHARE_CIRCLE_NAME_MAX_LENGTH = 6; 

	@Override
	public WifiApShareCircleInfo encodeWifiApName(CharSequence shareCircleName, ShareCircleAppInfo appInfo) {
		WifiApShareCircleInfo circleInfo = new WifiApShareCircleInfo(shareCircleName, appInfo);
		circleInfo.setSSID(createSSID(appInfo, circleInfo.getName()));
		circleInfo.setShareKey(createShareKey(circleInfo.getName(), appInfo));
		return circleInfo;
	}

	@Override
	public WifiApShareCircleInfo decodeWifiApName(String ssid, ShareCircleAppInfo appInfo) {
		CharSequence shareCircleName = decodeSSID(ssid, appInfo);
		if (shareCircleName == "") {
			return null;
		} else {
			WifiApShareCircleInfo circleInfo = new WifiApShareCircleInfo(shareCircleName, appInfo);
			circleInfo.setSSID(ssid);
			circleInfo.setShareKey(createShareKey(circleInfo.getName(), appInfo));
			return circleInfo;
		}
	}

	private static String createShareKey(CharSequence shareCircleName, ShareCircleAppInfo appInfo){
		StringBuilder sb = new StringBuilder();
		sb.append(appInfo.appId);
		sb.append(appInfo.name);
		sb.append(appInfo.version);
//		if (shareCircleName.length() > SHARE_CIRCLE_NAME_MAX_LENGTH) {
//			sb.append(shareCircleName.subSequence(0, SHARE_CIRCLE_NAME_MAX_LENGTH));
//		} else {
//			sb.append(shareCircleName);
//		}
		return DigestUtils.md5Hex(sb.toString().getBytes()).substring(0, 13);
	}
	
//	private static boolean isMobileWLANApSSID(String ssid){
//		boolean ret = false;
//		if(ssid != null && ssid.matches("^[0-9A-Z]{6,32}$")){
//			try{
//				short lm = Short.valueOf(ssid.substring(0, 4), 16);
//				int i = 0;
//				int len = 4;
//				while(lm > 1){
//					i = lm % 2;
//					if(i == 0){
//						len += 2;
//					} else {
//						len += 4;
//					}
//					lm /= 2;
//				}
//				if(len == ssid.length()){
//					return true;
//				}
//			}catch(Throwable e){
//				
//			}
//		} 
//		return ret;
//	}
	
//	private static String decodeName(String ssid){
//		StringBuilder sb = new StringBuilder();
//		try{
//			short lm = Short.valueOf(ssid.substring(0, 4), 16);
//			int i = 0;
//			char c;
//			int pos = ssid.length();
//			while(lm > 1){
//				i = lm % 2;
//				if(i == 0){
//					short d = Integer.valueOf(ssid.substring(pos - 2, pos), 16).shortValue();
//					c = (char)d;
//					sb.insert(0, c);
//					pos -= 2;
//				} else {
//					short d = Integer.valueOf(ssid.substring(pos - 4, pos), 16).shortValue();
//					c = (char)d;
//					sb.insert(0, c);
//					pos -= 4;
//				}
//				lm /= 2;
//			}
//		}catch(Throwable e){
//			e.printStackTrace();
//		}
//		return sb.toString();
//	}
	
	private static CharSequence decodeSSID(String ssid, ShareCircleAppInfo appInfo) {
		if (ssid.matches("^[0-9a-f]+$")) {
			try {
				byte[] bytes = hexStringToBytes(ssid);
				byte[] header = new byte[2];
				byte[] body = new byte[bytes.length - header.length];
				System.arraycopy(bytes, 0, header, 0, header.length);
				System.arraycopy(bytes, header.length, body, 0, body.length);
				if (byteArrayToInt(header) != appInfo.appId) {
					return "";
				} else {
					return byteArrayToCharSequence(body);
				}
			} catch (Exception e) {
				e.printStackTrace();
				return "";
			}
		} else {
			return "";
		}
	}
	
	private static int byteArrayToInt(byte[] bytes) {  
		int number = 0;  
		if (bytes.length == 2) {
			number = bytes[0] * 256 + bytes[1];
		}
		return number;
	}  

//	private static String createSSID(String name){
//		short lm = 1;
//		int i = 0;
//		char[] cs = name.toCharArray();
//		StringBuilder sb = new StringBuilder();
//		for(char c : cs){
//			String s = getFormatChar(c);
//			if(s.length() == 2 && i <= 26 && (lm & 0x8000) == 0){
//				lm <<= 1;
//				i += 2;
//			} else if(s.length() == 4 && i <= 24 && (lm & 0x8000) == 0){
//				lm <<= 1;
//				lm++;
//				i += 4;
//			} else {
//				break;
//			}
//			sb.append(s);
//		}
//		sb.insert(0, getFormatShort(lm));
//		return sb.toString();
//	}
	
	private static String createSSID(ShareCircleAppInfo appInfo, CharSequence shareCircleName) {
		try {
			byte[] header = intToByteArray(appInfo.appId);
			byte[] body;
			if (shareCircleName.length() > SHARE_CIRCLE_NAME_MAX_LENGTH) {
				body = charSequenceToByteArray(shareCircleName.subSequence(0, SHARE_CIRCLE_NAME_MAX_LENGTH));
			} else {
				body = charSequenceToByteArray(shareCircleName);
			}
			byte[] bytes = new byte[header.length + body.length];
			System.arraycopy(header, 0, bytes, 0, header.length);
			System.arraycopy(body, 0, bytes, header.length, body.length);
			return bytesToHexString(bytes);
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	private static byte[] intToByteArray(int number) {  
	    byte[] byteArray = new byte[2];   
	
	    int shiftNum = 0;  
	    for(int i = 0; i < 2; i++)  
	    {  
	        shiftNum = (1 - i) * 8;  
	        byteArray[i] = (byte)((number >> shiftNum) & 0xFF);  
	    }  
	    return byteArray;  
	}  

	private static String bytesToHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		String hex;
		for (int i = 0; i < bytes.length; i++) {
			hex = Integer.toHexString(bytes[i] & 0xFF);
			if (hex.length() == 1) {
				sb.append("0");
			}
			sb.append(hex);
		}
		return sb.toString();
	}
	
	private static byte[] hexStringToBytes(String hex) {
		int byteValue;
		int bytesLen = hex.length() / 2;
		byte[] bytes = new byte[bytesLen];
		for (int i = 0; i < bytesLen - 1; i++) {
			byteValue = Integer.parseInt(hex.substring(0 + i * 2, 2 + i * 2), 16);
			bytes[i] = (byte) (byteValue % 0xFF00);
		}
		return bytes;
	}
	
	private static byte[] charSequenceToByteArray(CharSequence cs) {
		byte[] bytes = new byte[cs.length() * 2];
		char ch;
		for (int i = 0; i < cs.length(); i++) {
			ch = cs.charAt(i);
	        bytes[2 * i] = (byte)((ch >> 4) & 0xFF);
	        bytes[2 * i + 1] = (byte)(ch & 0xFF);
		}
		return bytes;
	}
	
	private static CharSequence byteArrayToCharSequence(byte[] bytes) {
		char ch;
		CharBuffer cb = CharBuffer.allocate(bytes.length / 2);
		for (int i = 0; i < cb.length(); i++) {
	        ch = (char) (bytes[2 * i] * 256 + bytes[2 * i + 1]);
	        cb.append(ch);
		}
		return cb;
	}
	
//	private static String getFormatChar(char c){
//		short s = (short)c;
//		if((s & 0xFF00) == 0){
//			String ret = String.format("00%X", s);
//			return ret.substring(ret.length() - 2);
//		} else {
//			return getFormatShort(s);
//		}
//	}
	
//	private static String getFormatShort(short i){
//		String ret = String.format("0000%X", i);
//		return ret.substring(ret.length() - 4);
//	}

}
