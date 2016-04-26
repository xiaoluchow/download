package com.xiaolu.download;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadThread implements Runnable{
	private String url;
	private Long start;
	private Long end;
	private DownloadContext context;
	
	public DownloadThread(String url, Long start, Long end, DownloadContext context) {
		super();
		this.url = url;
		this.start = start;
		this.end = end;
		this.context = context;
	}

	@Override
	public void run() {
		try {
			InputStream is = Tools.download(start, end, url);
			synchronized (context) {
				if(context.getMap().get(url.hashCode()) == null){
					ConcurrentHashMap<Long, InputStream> map = new ConcurrentHashMap<>();
					map.put(start, is);
					context.getMap().put(new Integer(url.hashCode()), map);
				}else{
					context.getMap().get(url.hashCode()).put(start, is);
				}
				System.out.println("下载了"+url+"部分文件，保存在map中");
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
