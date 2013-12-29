package com.chainton.dankeshare.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.chainton.dankeshare.util.DigestUtils;

/**
 * http文件服务器
 * @author 富林
 *
 */
public class ShareServiceFileServer extends NanoHTTPD {
	private static final int HTTP_PORT = 9998;
	private static final ShareServiceFileServer fileServer = new ShareServiceFileServer(HTTP_PORT);
	private final AtomicBoolean isRun = new AtomicBoolean(false);
	private final Map<String, File> fileServiceMap;
	private final Map<String, File> imageServiceMap;
	
	private ShareServiceFileServer(int port) {
		super(port);
		fileServiceMap = new HashMap<String, File>();
		imageServiceMap = new HashMap<String, File>();
	}
	
	public static ShareServiceFileServer getInstance(){
		return fileServer;
	}
	
	@Override
	public void start() throws IOException {
		if(!isRun.getAndSet(true)){
			super.start();
		}
	}

	@Override
	public void stop() {
		super.stop();
		fileServiceMap.clear();
		isRun.set(false);
	}
	
	/**
	 * 将一个文件映射到http服务上
	 * @param localIp 本地ip地址
	 * @param md5	文件md5值
	 * @param file	源文件
	 * @return	映射的url
	 */
	public String addFile(String localIp, String md5, File file){
		String uri = "/" + md5;
		String url = "http://" + localIp + ":" + HTTP_PORT + uri;
		fileServiceMap.put(uri, file);
		return url;
	}
	
	/**
	 * 将一个缩略图映射到http服务
	 * @param localIp 本地ip地址
	 * @param file 源文件
	 * @return 映射的url
	 */
	public String addImage(String localIp, File file){
		String md5 = DigestUtils.md5Hex(file);
		String uri = "/" + md5;
		String url = "http://" + localIp + ":" + HTTP_PORT + uri;
		imageServiceMap.put(uri, file);
		return url;
	}

	private static final Response NOT_FOUND_RESPONSE = new Response(Response.Status.NOT_FOUND, "text/html", "not found");
	@Override
	public Response serve(IHTTPSession session) {
		String uri = session.getUri();
		if(fileServiceMap.containsKey(uri)){
			return serveFile(uri, session.getHeaders(), fileServiceMap.get(uri), "application/octet-stream");
		} else if(imageServiceMap.containsKey(uri)){
			return serveFile(uri, session.getHeaders(), imageServiceMap.get(uri), "image/jpeg");
		}
		return NOT_FOUND_RESPONSE;
	}
	
	Response serveFile(String uri, Map<String, String> header, File file, String mime) {
        Response res;
        try {
            // Calculate etag
            String etag = Integer.toHexString((file.getAbsolutePath() + file.lastModified() + "" + file.length()).hashCode());

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
                            startFrom = Long.parseLong(range.substring(0, minus));
                            endAt = Long.parseLong(range.substring(minus + 1));
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }

            // Change return code and add Content-Range header when skipping is requested
            long fileLen = file.length();
            if (range != null && startFrom >= 0) {
                if (startFrom >= fileLen) {
                    res = createResponse(Response.Status.RANGE_NOT_SATISFIABLE, NanoHTTPD.MIME_PLAINTEXT, "");
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

                    res = createResponse(Response.Status.PARTIAL_CONTENT, mime, fis);
                    res.addHeader("Content-Length", "" + dataLen);
                    res.addHeader("Content-Range", "bytes " + startFrom + "-" + endAt + "/" + fileLen);
                    res.addHeader("ETag", etag);
                }
            } else {
                if (etag.equals(header.get("if-none-match")))
                    res = createResponse(Response.Status.NOT_MODIFIED, mime, "");
                else {
                    res = createResponse(Response.Status.OK, mime, new FileInputStream(file));
                    res.addHeader("Content-Length", "" + fileLen);
                    res.addHeader("ETag", etag);
                }
            }
        } catch (IOException ioe) {
            res = createResponse(Response.Status.FORBIDDEN, NanoHTTPD.MIME_PLAINTEXT, "FORBIDDEN: Reading file failed.");
        }

        return res;
    }
	
	private Response createResponse(Response.Status status, String mimeType, InputStream message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }

    // Announce that the file server accepts partial content requests
    private Response createResponse(Response.Status status, String mimeType, String message) {
        Response res = new Response(status, mimeType, message);
        res.addHeader("Accept-Ranges", "bytes");
        return res;
    }
}
