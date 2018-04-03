package br.com.demo.chunkedupload.data;

import java.util.HashSet;
import java.util.Set;

public class FileInformation {
	private int chunkSize;
	private String fileName;
	private Long fileSize;

	private Set<Integer> alreadyPersistedChunks;

	public FileInformation(Long fileSize, String fileName, int chunkSize) {
		this.fileSize = fileSize;
		this.fileName = fileName;
		this.chunkSize = chunkSize;

		alreadyPersistedChunks = new HashSet<Integer>();
	}

	public int getTotalNumberOfChunks() {
		return (int) Math.ceil(fileSize / (chunkSize * 1F));
	}

	public int getChunkSize() {
		return chunkSize;
	}

	public void setChunkSize(int chunkSize) {
		this.chunkSize = chunkSize;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public Long getFileSize() {
		return fileSize;
	}

	public void setFileSize(Long fileSize) {
		this.fileSize = fileSize;
	}

	public void markChunkAsPersisted(int chunkNumber) {
		alreadyPersistedChunks.add(chunkNumber);
	}

	public Integer[] getAlreadyPersistedChunks() {
		return alreadyPersistedChunks.toArray(new Integer[alreadyPersistedChunks.size()]);
	}

}
