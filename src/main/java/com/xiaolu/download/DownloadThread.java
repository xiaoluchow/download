package com.xiaolu.download;

import java.io.IOException;
import java.io.InputStream;

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
				context.getMap().get(url.hashCode()).put(start, is);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
