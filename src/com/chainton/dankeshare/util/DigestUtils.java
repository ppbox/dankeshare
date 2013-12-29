package com.chainton.dankeshare.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 信息摘要工具类
 * @author 富林
 *
 */
public class DigestUtils {

	private static final int STREAM_BUFFER_LENGTH = 1024;
	private static final char[] DIGITS_LOWER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

	private static final char[] DIGITS_UPPER = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F' };

	/**
	 * 从输入流中获取md5值
	 * @param data
	 * @return 输入流的md5值
	 * @throws IOException
	 */
	public static String md5Hex(InputStream data) throws IOException {
		return encodeHexString(md5(data));
	}
	
	/**
	 * 从文件获取MD5值
	 * @param file
	 * @return 文件的MD5值
	 */
	public static String md5Hex(File file){
		String result = null;
		try {
			FileInputStream fis = new FileInputStream(file);
			result = md5Hex(fis);
			fis.close();
		} catch (Throwable e) {
			e.printStackTrace();
		} 
		return result;
	}
	
	/**
	 * 从byte数组中获取MD5值
	 * @param datas
	 * @return byte数组的md5值
	 */
	public static String md5Hex(byte[] datas){
		return encodeHexString(digest(getMd5Digest(), datas));
	}

	private static byte[] md5(InputStream data) throws IOException {
		return digest(getMd5Digest(), data);
	}

	private static MessageDigest getMd5Digest() {
		return getDigest("MD5");
	}

	static MessageDigest getDigest(String algorithm) {
		try {
			return MessageDigest.getInstance(algorithm);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e.getMessage());
		}
	}

	private static byte[] digest(MessageDigest digest, InputStream data) throws IOException {
		byte[] buffer = new byte[STREAM_BUFFER_LENGTH];
		int read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);

		while (read > -1) {
			digest.update(buffer, 0, read);
			read = data.read(buffer, 0, STREAM_BUFFER_LENGTH);
		}

		return digest.digest();
	}
	
	private static byte[] digest(MessageDigest digest, byte[] datas){
		return digest.digest(datas);
	}

	private static String encodeHexString(byte[] data) {
		return new String(encodeHex(data));
	}

	private static char[] encodeHex(byte[] data) {
		return encodeHex(data, true);
	}

	private static char[] encodeHex(byte[] data, boolean toLowerCase) {
		return encodeHex(data, toLowerCase ? DIGITS_LOWER : DIGITS_UPPER);
	}

	private static char[] encodeHex(byte[] data, char[] toDigits) {
		int l = data.length;
		char[] out = new char[l << 1];
		// two characters form the hex value.
		for (int i = 0, j = 0; i < l; i++) {
			out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
			out[j++] = toDigits[0x0F & data[i]];
		}
		return out;
	}
	
}
