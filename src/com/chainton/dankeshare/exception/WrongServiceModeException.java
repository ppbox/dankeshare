/**
 * 
 */
package com.chainton.dankeshare.exception;

/**
 * 分享圈服务模式错误异常
 * @author Rivers
 *
 */
public class WrongServiceModeException extends Exception {

	private static final long serialVersionUID = -1876512758899275980L;
	
	public WrongServiceModeException(String errorMsg) {
		super(errorMsg);
	}

}
