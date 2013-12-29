package com.chainton.dankeshare.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.chainton.dankeshare.HttpDownloadFile;
import com.chainton.dankeshare.ReceiveShareListener;

/**
 * 默认http方式下载资源实现类
 * @author 马志军
 *
 */
public final class DefaultHttpDownloadFile implements HttpDownloadFile {

	private int length = 0; // 要下载文件的大小（byte)
	private File hasDownloadFile = null; // 断点续传用的记录下载大小的文件
	private File downloadFile = null; // 下载到本地的文件
	private File urlAndLengthFile = null; // 记录文件url和长度
	
	private static final ExecutorService resourceDownloadPool = Executors.newFixedThreadPool(3);
	private static final ExecutorService thumbDownloadPool = Executors.newCachedThreadPool();

	@Override
	public void downloadShare(final String url, final String destPath,
			final ReceiveShareListener listener) {
		resourceDownloadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				doDownload(url, destPath, listener);
			}
		});
	}
	
	@Override
	public void downloadThumb(final String url, final String destPath, final ReceiveShareListener listener) {
		thumbDownloadPool.execute(new Runnable() {
			
			@Override
			public void run() {
				doDownload(url, destPath, listener);
			}
		});
	}

	private void doDownload(final String url, final String destPath, final ReceiveShareListener listener){
		RandomAccessFile raf = null;
		try {
			HttpURLConnection conn = getServerConnection(url);
			length = conn.getContentLength(); // 文件大小

			downloadFile = makeFile(destPath);
			hasDownloadFile = makeFile(destPath + ".dl");
			urlAndLengthFile = makeFile(destPath + ".note");

			if (urlAndLengthFile.exists() && urlAndLengthFile.length() > 0) {
				FileInputStream fis = new FileInputStream(urlAndLengthFile);
				BufferedReader br = new BufferedReader(new InputStreamReader(
						fis));
				String str = br.readLine();
				String[] strs = null;
				if(str != null)
					strs = str.split(",");
				String urlSaved = strs[0];
				String lengthStr = strs[1];
				int lengthSaved = Integer.parseInt(lengthStr);
				fis.close();
				if (urlSaved.equals(url) && lengthSaved != length) {
					// 如果下载路径相同，大小不一样，认为是另一个文件，把记录和文件都删掉
					if (downloadFile.exists() && downloadFile.length() > 0) {
						downloadFile.delete();
					}
					if (hasDownloadFile.exists()
							&& hasDownloadFile.length() > 0) {
						hasDownloadFile.delete();
					}
				}
			} else if (downloadFile.exists() && downloadFile.length() > 0) {
				listener.onFinish(downloadFile);
				return;
			}

			raf = new RandomAccessFile(downloadFile, "rwd");
			if (length > 0)
				raf.setLength(length);

			gotoDownload(listener, url, destPath);
		} catch (IOException e) {
			e.printStackTrace();
			listener.onFailed();
		} finally {
			if (raf != null) {
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void gotoDownload(ReceiveShareListener listener, String url,
			String destPath) {

		InputStream is = null;
		RandomAccessFile raf = null;
		try {
			FileOutputStream fos2 = new FileOutputStream(urlAndLengthFile);
			fos2.write((url + "," + String.valueOf(length)).getBytes());
			fos2.close();

			int startLocation = updateStartLocation(); // 更新下载进度
			HttpURLConnection conn = getServerConnection(url);
			conn.setRequestProperty("Range", "bytes=" + startLocation + "-"
					+ length);
			is = conn.getInputStream();

			downloadFile = makeFile(destPath);
			raf = new RandomAccessFile(downloadFile, "rwd");
			raf.seek(startLocation);

			byte[] buffer = new byte[1024 * 4];
			int len = 0;
			int total = 0; // 记录这一次已经下载的长度.

			while ((total + startLocation) < length
					&& (len = is.read(buffer)) != -1) {
				raf.write(buffer, 0, len);
				total += len; // 修改已经下载的长度
				// 将已经下载的长度保存到记录文件
				FileOutputStream fos = new FileOutputStream(hasDownloadFile);
				fos.write(String.valueOf(total + startLocation).getBytes());
				fos.close();
				listener.onUpdateProgress((float) (total + startLocation)
						/ length);
			}
			hasDownloadFile.delete(); // 下载完成后删除这个记录
			urlAndLengthFile.delete();
			listener.onFinish(downloadFile);
		} catch (Exception e) {
			e.printStackTrace();
			listener.onFailed();
		} finally {
			try {
				if (is != null)
					is.close();
				if (raf != null)
					raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private int updateStartLocation() throws IOException {
		int startLocation = 0;
		if (hasDownloadFile.exists() && hasDownloadFile.length() > 0) { // 要续传，不重头下载
			int savedStartLocation = 0;
			FileInputStream fis = new FileInputStream(hasDownloadFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(fis));
			String str = br.readLine();
			savedStartLocation = Integer.parseInt(str);
			fis.close();
			if (savedStartLocation > 0) {
				// 更改开始下载的位置，从上次结束那里开始下
				startLocation = savedStartLocation;
			}
		}
		return startLocation;
	}

	private File makeFile(String downloadPath) {
		File file = new File(downloadPath);
		if (!file.exists()) {
			File parentFile = file.getParentFile();
			parentFile.mkdirs();
		}
		return file;
	}

	private HttpURLConnection getServerConnection(String url)
			throws IOException {
		HttpURLConnection conn = (HttpURLConnection) new URL(url)
				.openConnection();
		conn.setConnectTimeout(5000);
		conn.setRequestMethod("GET");
		return conn;
	}

}
