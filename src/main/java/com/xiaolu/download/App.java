package com.xiaolu.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.DefaultHttpClient;


/**
 *
 */
public class App {
	private final static String URL = "https://dl.pandaidea.com/jarfiles/o/org.springframework.core/org.springframework.core-3.0.0.RELEASE.jar.zip";
	
	private final static String FILE = "e:\\download.jar";
	
	private static Map<Long, InputStream> fileStream = new HashMap<>();
	
	private static final int PARTS_COUNT = 3;
	
	private volatile static int complete_count = 0;
	
	private static final Object lock = new Object();
	
	private static final Object lock1 = new Object();
	
	public static void download(Long start, Long end, Long length) throws IllegalStateException, IOException{
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL);
		if(isSupportRange(client, length)){
			get.addHeader("Range", "bytes="+start+"-"+end);
		}
		HttpResponse resp = client.execute(get);
		if(resp == null){
			return;
		}
		HttpEntity entity = resp.getEntity();
		InputStream is = null;
		if(entity == null){
			return;
		}
		is = entity.getContent();
		synchronized(lock){
			fileStream.put(start, is);
		}
		return;
	}
	public static void writeFile(InputStream is, Long start) throws IOException{
		int count = 0;
		byte [] buffer = new byte[1024];
		RandomAccessFile outputStream = new RandomAccessFile(FILE, "rw");
		outputStream.seek(start);
		while((count = is.read(buffer, 0, buffer.length)) != -1){
			outputStream.write(buffer, 0, count);
		}
		IOUtils.closeQuietly(is);
		outputStream.close();
		synchronized(lock1){
			complete_count += 1;
			System.out.println("写文件+1，当前计数："+complete_count);
		}
	}
	public static Long getFileLength(HttpClient client) throws ClientProtocolException, IOException{
		HttpHead head = new HttpHead(URL);
		HttpResponse resp = client.execute(head);
		if(resp.getStatusLine().getStatusCode() == 200){
			Header[] headers = resp.getHeaders("Content-Length");
			if(headers.length > 0){
				String length = headers[0].getValue();
				return Long.parseLong(length);
			}
		}
		return 0l;
	}
	public static boolean isSupportRange(HttpClient client, Long length) throws ClientProtocolException, IOException{
		HttpHead head = new HttpHead(URL);
		head.addHeader("Range", "bytes=0-"+(length-1));
		HttpResponse resp = client.execute(head);
		if(resp.getStatusLine().getStatusCode() == 206){
			return true;
		}
		return false;
	}
	
	public static void multiThreadDownload() throws ClientProtocolException, IOException{
		HttpClient client = new DefaultHttpClient();
		Long length = getFileLength(client);
		if(length == 0){
			return;
		}
		if(isSupportRange(client, length)){
			Long first = Math.floorDiv(length, PARTS_COUNT);
			Long [] downloadBytes = new Long[4];
			downloadBytes[0] = 0l;
			downloadBytes[1] = first;
			downloadBytes[2] = 2 * first;
			downloadBytes[3] =  length;
			for(int i = 0; i< 3;i++){
				DownLoadThread thread = new DownLoadThread(downloadBytes[i], downloadBytes[i+1]-1, length);
				thread.run();
			}
		}
	}
	static class DownLoadThread implements Runnable{
		private Long start;
		private Long end;
		private Long length;
		
		public DownLoadThread(Long start, Long end, Long length) {
			super();
			this.start = start;
			this.end = end;
			this.length = length;
		}

		@Override
		public void run() {
			try {
				download(start, end, length);
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	static class WriteFileThread implements Runnable{
		private InputStream is;
		private Long start;
		
		public WriteFileThread(InputStream is, Long start) {
			super();
			this.is = is;
			this.start = start;
		}

		public void run() {
			try {
				writeFile(is, start);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	static class MonitorThread implements Runnable{
		@Override
		public void run() {
			while(true){
				if(fileStream.size() > 0){
					synchronized (lock) {
						Long key = fileStream.keySet().iterator().next();
						InputStream is = fileStream.get(key);
						fileStream.remove(key);
						new WriteFileThread(is, key).run();
					
					}
				}
				
				if(complete_count == PARTS_COUNT){
					break;
				}
			}
		}
	}
    public static void main( String[] args ){
    	try {
    		multiThreadDownload();
    		new MonitorThread().run();
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
}
