/**
 * 
 */
package com.chainton.dankeshare.impl;

import java.io.File;

import com.chainton.forest.core.file.CoreFileInfo;
import com.chainton.forest.core.helper.ForestFileReceiver;
import com.chainton.forest.core.helper.ForestFileReceiverEvents;

/**
 * 
 * 
 * @author leuhenry
 *
 */
public class ShareCircleFileReceiver{
	private ForestFileReceiver forestFileReceiver;
//	ForestFileReceiverEventsImpl handler = new ForestFileReceiverEventsImpl();

	public static final int DEFAULT_PORT = 9902; //
	
	private int port = DEFAULT_PORT;
	
	public ShareCircleFileReceiver(){
		forestFileReceiver = new ForestFileReceiver();
	}
	
	public ShareCircleFileReceiver(int port){
		forestFileReceiver = new ForestFileReceiver();
		
	}

	public void setPort(int port) {
		this.port = port;
	}

	/**
	 * TODO: to be implemented 
	 * Start file receiver for receiving the coming files sent by other clients
	 */
	public void start(){
		forestFileReceiver.startReceiver(this.port, new ForestFileReceiverEvents(){

			@Override
			public void onReceiverStartFailed() {
				System.out.println("receive fail");
			}

			@Override
			public File onBeginReceivingFile(CoreFileInfo fileInfo) {
				System.out.println("begin receive:"+fileInfo.fileName);
				return null;
			}

			@Override
			public void onReceivingFile(CoreFileInfo fileInfo, File localFile, int percent) {
				// TODO Auto-generated method stub
				System.out.println("received percent:"+percent);
			}

			@Override
			public void onFileReceived(CoreFileInfo fileInfo, File localFile) {
				// TODO Auto-generated method stub
				System.out.println("success:");
			}

			@Override
			public void onFileReceiveFailed(CoreFileInfo fileInfo, File localFile) {
				System.out.println(" received failed");
				
			}
			
		});
		
	}
	
	/**
	 * Send a event which trigger stopping file receiver. 
	 * the file receiver won't stop immediately because that forest will
	 * handle it in a polite way.
	 * 
	 */
	
	public void stop(){
		forestFileReceiver.startClosing();
	}

}
