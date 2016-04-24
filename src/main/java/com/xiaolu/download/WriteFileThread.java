package com.xiaolu.download;

import java.io.IOException;
import java.io.InputStream;

public class WriteFileThread implements Runnable{
	private String file;
	private InputStream is;
	private Long start;
	
	public WriteFileThread(InputStream is, String file, Long start) {
		super();
		this.is = is;
		this.file = file;
		this.start = start;
	}
    @Override
	public void run() {
		try {
			Tools.writeRandomAccessFile(is, file, start);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
