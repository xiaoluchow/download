package com.xiaolu.download;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileNameCache {
	private Map<Integer, String> map = new ConcurrentHashMap<>();

	public String getFileName(Integer urlHash){
		return map.get(urlHash);
	}
	public void setFileName(Integer urlHash, String fileName){
		map.put(urlHash, fileName);
	}
}
