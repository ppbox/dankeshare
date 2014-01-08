/**
 * 
 */
package com.chainton.dankeshare.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Rivers
 *
 */
public class GlobalUtil {
	
	public static String LOG_TAG = "ShareService"; 
	
	private static ExecutorService executorService = null;
	
	public static ExecutorService threadExecutor() {
		if (executorService == null) {
			executorService = Executors.newCachedThreadPool();
		}
		return executorService;
	}

}
