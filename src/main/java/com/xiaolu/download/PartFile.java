package com.xiaolu.download;

public class PartFile {
	private Long start;
	private Long end;
	
	public PartFile(Long start, Long end) {
		super();
		this.start = start;
		this.end = end;
	}
	public Long getStart() {
		return start;
	}
	public void setStart(Long start) {
		this.start = start;
	}
	public Long getEnd() {
		return end;
	}
	public void setEnd(Long end) {
		this.end = end;
	}
	
}
