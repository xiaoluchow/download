package com.xiaolu.download;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.methods.HttpGet;

/**
 *
 */
public class DownloadContext {
	
	private volatile Map<Integer, ConcurrentHashMap<Long, InputStream>> map = new HashMap<>();
	
	private FileNameCache fileNameCache = new FileNameCache();
	
	private static DownloadContext context;
	
	
	private DownloadContext(){}
	
	public synchronized static DownloadContext getInstance(){
		if(context == null){
			context = new DownloadContext();
			return context;
		}else{
			return context;
		}
	}
	
	public void multiDownload(List<String> urls){
		for(String url : urls){
			HttpGet request = new HttpGet(url);
			fileNameCache.setFileName(url.hashCode(), Tools.getFileName(url));
			try {
				Long length = Tools.getFileLength(request);
				List<PartFile> list = Tools.divide(length, 3);
				for(PartFile part : list){
					DownloadThread download = new DownloadThread(url, part.getStart(), 
							part.getEnd(), DownloadContext.getInstance());
					System.out.println("开始下载"+url+"的部分文件");
					new Thread(download).start();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
		
	}
	
	
	public Map<Integer, ConcurrentHashMap<Long, InputStream>> getMap() {
		return map;
	}
	public static void main(String[] args) {
		DownloadContext dc = DownloadContext.getInstance();
		List<String> list = new ArrayList<>();
		String url1 = "https://dl.pandaidea.com/jarfiles/o/org.springframework.core/org.springframework.core-3.0.0.RELEASE.jar.zip";
		String url2 = "https://dl.pandaidea.com/jarfiles/o/org.springframework.context/org.springframework.context.jar.zip";
		String url3 = "https://dl.pandaidea.com/jarfiles/o/org.springframework.context/org.springframework.context.support-sources-3.0.5.RELEASE.jar.zip";
		list.add(url1);
		list.add(url2);
		list.add(url3);
		dc.multiDownload(list);
		MonitorThread monitor = new MonitorThread();
		monitor.setContext(dc);
		new Thread(monitor).start();
	}

	public FileNameCache getFileNameCache() {
		return fileNameCache;
	}

	public void setFileNameCache(FileNameCache fileNameCache) {
		this.fileNameCache = fileNameCache;
	}
	
}
