/**
 * 
 */
package com.chainton.dankeshare.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.os.Handler;

import com.chainton.dankeshare.OperationResult;

/**
 * @author Administrator
 * 
 */
public class HttpFileServer extends NanoHTTPD {

	
	private static  Map<String, File> fileServiceMap;
	private static boolean running = true;
	private int port;

	private String modelStyle;
	/**
	 * 主线程Handler实例
	 */
	protected Handler handler;

	public HttpFileServer(int port,String modelStyle) {
		super(port);
		this.port =port;
		this.modelStyle = modelStyle;
		fileServiceMap = new HashMap<String, File>();
	}
	

	
	/**
	 * 将一个文件映射到http服务上
	 * 
	 * @param localIp
	 *            本地ip地址
	 * @param md5
	 *            文件md5值
	 * @param file
	 *            源文件
	 * @return 映射的url
	 */
	public String addFile(String localIp, String md5, File file) {
		
		String fileName = file.getName();
		int i = fileName.lastIndexOf('.');
		String extName = fileName.substring(i);
		
		String key = md5+extName;
		
		String uri = "/" +key;
		String url = "http://" + localIp + ":" + port + uri;
		System.out.println("put uri :  "+uri);
		fileServiceMap.put(uri, file);
		return url;
	}



	private static final Response NOT_FOUND_RESPONSE = new Response(
			Response.Status.NOT_FOUND, "text/html", "not found");

	/**
	 * 对请求的url 返回 流或普通字符串
	 */
	@Override
	public Response serve(IHTTPSession session) {
		
		String type = "application/octet-stream";
		String uri = session.getUri();
		System.out.println("session uri:  "+uri);

		if(modelStyle.equalsIgnoreCase("STANDALONE")){
			
			//currently standalone only support .apk
			type ="application/vnd.android.package-archive";
			
			System.out.println("STANDALONE Sytle:  ");
			Set<String> filesKey = fileServiceMap.keySet();
			if(!filesKey.isEmpty()){
				Iterator<String> filesIterator =filesKey.iterator();
				String key = filesIterator.next();
				System.out.println("STANDALONE download key:  "+key);
				return serveFile(key, session.getHeaders(),
						fileServiceMap.get(key), type);
			}
		}else{
			if (fileServiceMap.containsKey(uri)) {
				return serveFile(uri, session.getHeaders(),
						fileServiceMap.get(uri), type);
			} 
		}

		return NOT_FOUND_RESPONSE;
	}

	/**
	 * 对请求的url 返回 流或普通字符串
	 * @param uri  文件url
	 * @param header 请求头
	 * @param file 文件
	 * @param mime 文件类型
	 * @return
	 */
	private Response serveFile(String uri, Map<String, String> header, File file,
			String mime) {
		Response res;
		try {
			// Calculate etag
			String etag = Integer.toHexString((file.getAbsolutePath()
					+ file.lastModified() + "" + file.length()).hashCode());

			// Support (simple) skipping:
			long startFrom = 0;
			long endAt = -1;
			String range = header.get("range");
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					try {
						if (minus > 0) {
							startFrom = Long.parseLong(range
									.substring(0, minus));
							endAt = Long.parseLong(range.substring(minus + 1));
						}
					} catch (NumberFormatException ignored) {
					}
				}
			}

			// Change return code and add Content-Range header when skipping is
			// requested
			long fileLen = file.length();
			if (range != null && startFrom >= 0) {
				if (startFrom >= fileLen) {
					res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE,
							NanoHTTPD.MIME_PLAINTEXT, "");
					res.addHeader("Content-Range", "bytes 0-0/" + fileLen);
					res.addHeader("ETag", etag);
				} else {
					if (endAt < 0) {
						endAt = fileLen - 1;
					}
					long newLen = endAt - startFrom + 1;
					if (newLen < 0) {
						newLen = 0;
					}

					final long dataLen = newLen;
					FileInputStream fis = new FileInputStream(file) {
						@Override
						public int available() throws IOException {
							return (int) dataLen;
						}
					};
					fis.skip(startFrom);

					res = createResponse(Response.Status.PARTIAL_CONTENT, mime,
							fis);
					res.addHeader("Content-Length", "" + dataLen);
					res.addHeader("Content-Range", "bytes " + startFrom + "-"
							+ endAt + "/" + fileLen);
					res.addHeader("ETag", etag);
				}
			} else {
				if (etag.equals(header.get("if-none-match")))
					res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
				else {
					res = createResponse(Response.Status.OK, mime,
							new FileInputStream(file));
					res.addHeader("Content-Length", "" + fileLen);
					res.addHeader("ETag", etag);
				}
			}
		} catch (IOException ioe) {
			res = createResponse(Response.Status.FORBIDDEN,
					NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
		}

		return res;
	}

	/**
	 * 返回response 流格式
	 * @param status
	 * @param mimeType
	 * @param message
	 * @return
	 */
	private Response createResponse(Response.Status status, String mimeType,
			InputStream message) {
		Response res = new Response(status, mimeType, message);
		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

    /**
     * 返回 response 普通文本
     * @param status
     * @param mimeType
     * @param message
     * @return
     */
	private Response createResponse(Response.Status status, String mimeType,
			String message) {
		Response res = new Response(status, mimeType, message);
		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

	/**
	 * 启动http server
	 * @throws IOException
	 */
	public void startServer() throws IOException {
		super.start();
		while(running){
			
		}
	}
	
	
	/**
	 * 启动http server
	 * @throws IOException
	 */
	public void startServer(OperationResult result) throws IOException {
		try{
			super.start();
			result.onSucceed();
			while(running){}
		}catch(Exception e){
			e.printStackTrace();
			result.onFailed();
		}
	}
	
	/**
	 * 停止http server
	 */
	public void stopServer() {
		if(running){
			running = false;
			//server.stop();
		}
		System.out.println("stop success!");
	}

    /**
     * simple test
     * @param args
     * @throws InterruptedException
     */
	public static void main(String[] args) throws InterruptedException {

		final HttpFileServer httpFileServer = new  HttpFileServer(80,"STANDALONE");

		String localIp = "localhost";
		String md5 = "k";
		File file = new File("D:\\data.docx");
		httpFileServer.addFile(localIp, md5, file);
		try {
			httpFileServer.startServer();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//Thread.sleep(2000);
		//httpFileServer.stopServer();
	}
}
