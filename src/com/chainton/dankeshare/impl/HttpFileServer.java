/**
 * 
 */
package com.chainton.dankeshare.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import android.os.Handler;

import com.chainton.dankeshare.util.DigestUtil;

/**
 * @author Administrator
 * 
 */
public class HttpFileServer extends NanoHTTPD {

	private final Map<String, File> fileServiceMap;
	private static final HttpFileServer server = new HttpFileServer();
	private static boolean running = true;
	private static final int HTTP_PORT = 8080;

	/**
	 * 主线程Handler实例
	 */
	protected Handler handler;


	
	private HttpFileServer() {
		super(HTTP_PORT);
		fileServiceMap = new HashMap<String, File>();
	}
	
	private HttpFileServer(int port) {
		super(port);
		fileServiceMap = new HashMap<String, File>();
	}
	
	/**
	 * get singleton instance 
	 * @return
	 */
	public static HttpFileServer getInstance() {
		return server;
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
		
		String uri = "/" + md5+extName;
		String url = "http://" + localIp + ":" + HTTP_PORT + uri;
		System.out.println("url: "+url);
		
		fileServiceMap.put(uri, file);
		return url;
	}



	private static final Response NOT_FOUND_RESPONSE = new Response(
			Response.Status.NOT_FOUND, "text/html", "not found");

	@Override
	public Response serve(IHTTPSession session) {
		
		//String type ="application/vnd.android.package-archive";
		String type = "application/octet-stream";
		String uri = session.getUri();
		
		if (fileServiceMap.containsKey(uri)) {
			return serveFile(uri, session.getHeaders(),
					fileServiceMap.get(uri), type);
		} 
		
		return NOT_FOUND_RESPONSE;
	}

	/**
	 * response info or files to the client
	 * @param uri
	 * @param header
	 * @param file
	 * @param mime
	 * @return
	 */
	Response serveFile(String uri, Map<String, String> header, File file,
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

	private Response createResponse(Response.Status status, String mimeType,
			InputStream message) {
		Response res = new Response(status, mimeType, message);
		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

	// Announce that the file server accepts partial content requests
	private Response createResponse(Response.Status status, String mimeType,
			String message) {
		Response res = new Response(status, mimeType, message);
		res.addHeader("Accept-Ranges", "bytes");
		return res;
	}

	/**
	 * use a thread start the server;
	 */
	public void startServer() {

		new Thread() {
			public void run() {
				try {
					server.start();
					System.out.println("Server started, Hit Enter to stop.\n");
					while (running) {
					}
					System.out.println("Server stopped.\n");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}
	
	/**
	 * stop this http server
	 */
	public void stopServer() {
		running = false;
		server.stop();
	}

    /**
     * simple test
     * @param args
     * @throws InterruptedException
     */
	public static void main(String[] args) throws InterruptedException {

		final HttpFileServer httpFileServer = HttpFileServer.getInstance();

		String localIp = "localhost";
		String md5 = "k";
		File file = new File("D:\\1.apk");
		httpFileServer.addFile(localIp, md5, file);
		httpFileServer.startServer();

		Thread.sleep(2000);
		//httpFileServer.stopServer();
	}
}
