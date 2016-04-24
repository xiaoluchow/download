package com.xiaolu.download;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MonitorThread implements Runnable{

	private DownloadContext context;
	
	@Override
	public void run() {
		Map<Integer, ConcurrentHashMap<Long, InputStream>> downloadStream = context.getMap();
		while(true){
			if(!downloadStream.isEmpty()){
				Iterator<Integer> keysIterator = downloadStream.keySet().iterator();
				while(keysIterator.hasNext()){
					Integer key = keysIterator.next();
					Map<Long, InputStream> stream = downloadStream.get(key);
					Iterator<Long> startIterator = stream.keySet().iterator();
					String file = context.getFileNameCache().getFileName(key);
					while(startIterator.hasNext()){
						Long start = startIterator.next();
						InputStream is = stream.get(start);
						WriteFileThread thread = new WriteFileThread(is, file, start);
					}
				}
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
	}
	public DownloadContext getContext() {
		return context;
	}
	public void setContext(DownloadContext context) {
		this.context = context;
	}

}
