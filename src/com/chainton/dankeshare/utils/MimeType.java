/**
 * 
 */
package com.chainton.dankeshare.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Administrator
 *
 */
public class MimeType {
	public static Map mapping = new HashMap<String,String>();
	static{
		mapping.put("apk" , "application/vnd.android.package-archive");
    }
}
