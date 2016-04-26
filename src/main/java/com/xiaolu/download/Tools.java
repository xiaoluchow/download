package com.xiaolu.download;

import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;

public class Tools {
	
	public static void writeRandomAccessFile(InputStream is, String file, Long start) throws IOException{
		assert is != null;
		assert file != null;
		assert start != null;
		int count = 0;
		byte [] buffer = new byte[1024];
		RandomAccessFile outputStream = new RandomAccessFile(file, "rw");
		outputStream.seek(start);
		while((count = is.read(buffer, 0, buffer.length)) != -1){
			outputStream.write(buffer, 0, count);
		}
		IOUtils.closeQuietly(is);
		outputStream.close();
	}
	
	public static Long getFileLength(HttpUriRequest request) throws ClientProtocolException, IOException{
		HttpClient client = new DefaultHttpClient();
		HttpResponse resp = client.execute(request);
		if(resp.getStatusLine().getStatusCode() == 200){
			Header[] headers = resp.getHeaders("Content-Length");
			if(headers.length > 0){
				String length = headers[0].getValue();
				return Long.parseLong(length);
			}
		}
		return 0l;
	}
	
	public static String getFileName(String url){
		assert url != null;
		int index = url.lastIndexOf("/");
		if(index != -1){
			return "e://"+url.substring(index);
		}
		return "e://"+System.currentTimeMillis();
	}
	
	public static List<PartFile> divide(Long length, int factor){
		if(length == 0){
			return null;
		}
		Long partLength = Math.floorDiv(length, factor);
		List<PartFile> list =  new ArrayList<PartFile>();
		for(int i = 0; i < factor; i++){
			PartFile part = null;
			if(i != factor -1){
				part = new PartFile(i*partLength, (i+1) * partLength -1);
				
			}else{
				part = new PartFile(i*partLength, partLength -1);
			}
			list.add(part);
		}
		return list;
	}
	
	public static boolean isSupportRange(String URL, Long length, HttpUriRequest request) throws ClientProtocolException, IOException{
		HttpClient client = new DefaultHttpClient();
		/*HttpHead head = new HttpHead(URL);
		head.addHeader("Range", "bytes=0-"+(length-1));*/
		HttpResponse resp = client.execute(request);
		if(resp.getStatusLine().getStatusCode() == 206){
			return true;
		}
		return false;
	}
	
	public static InputStream download(String URL, HttpUriRequest request) throws IllegalStateException, IOException{
		HttpClient client = new DefaultHttpClient();
		HttpResponse resp = client.execute(request);
		if(resp == null){
			return null;
		}
		HttpEntity entity = resp.getEntity();
		InputStream is = null;
		if(entity == null){
			return null;
		}
		is = entity.getContent();
		return is;
	}
	
	public static InputStream download(Long start, Long end, String URL) throws IllegalStateException, IOException{
		HttpClient client = new DefaultHttpClient();
		HttpGet get = new HttpGet(URL);
		get.addHeader("Range", "bytes="+start+"-"+end);
		HttpResponse resp = client.execute(get);
		if(resp == null){
			return null;
		}
		HttpEntity entity = resp.getEntity();
		InputStream is = null;
		if(entity == null){
			return null;
		}
		is = entity.getContent();
		return is;
	}
}
