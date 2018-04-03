package br.com.demo.chunkedupload.model;

import java.util.ArrayList;
import java.util.List;

import br.com.demo.chunkedupload.data.Session;

@javax.xml.bind.annotation.XmlRootElement
public class UploadStatusResponse {

	public static UploadStatusResponse fromSession(Session session) {
		UploadStatusResponse model = new UploadStatusResponse();

		model.setChunkSize(session.getFileInfo().getChunkSize());
		model.setFileName(session.getFileInfo().getFileName());
		model.setTotalNumberOfChunks(session.getFileInfo().getTotalNumberOfChunks());

		model.setConcluded(session.isConcluded());
		model.setCreatedDate(session.getCreatedDate());
		model.setExpired(session.isExpired());

		model.setLastUpdate(session.getLastUpdate());
		model.setProgress(session.getProgress());
		model.setSuccessfulChunks(session.getSuccessfulChunks());

		model.setUser(session.getUser());
		model.setId(session.getId());
		model.setStatus(session.getStatus());

		return model;
	}

	public static List<UploadStatusResponse> fromSessionList(List<Session> sessions) {
		List<UploadStatusResponse> translated = new ArrayList<UploadStatusResponse>();

		for (Session s : sessions) {
			translated.add(fromSession(s));
		}

		return translated;
	}

	private Integer chunkSize;

	private Boolean concluded;

	private String createdDate;

	private Boolean expired;

	private String fileName;

	private String id;

	private String lastUpdate;
	private Double progress;
	private String status;
	private Integer successfulChunks;
	private Integer totalNumberOfChunks;
	private Long user;

	public Integer getChunkSize() {
		return chunkSize;
	}

	public Boolean getConcluded() {
		return concluded;
	}

	public String getCreatedDate() {
		return createdDate;
	}

	public Boolean getExpired() {
		return expired;
	}

	public String getFileName() {
		return fileName;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	public String getLastUpdate() {
		return lastUpdate;
	}

	public Double getProgress() {
		return progress;
	}

	/**
	 * @return the status
	 */
	public String getStatus() {
		return status;
	}

	public Integer getSuccessfulChunks() {
		return successfulChunks;
	}

	public Integer getTotalNumberOfChunks() {
		return totalNumberOfChunks;
	}

	public Long getUser() {
		return user;
	}

	public void setChunkSize(Integer chunkSize) {
		this.chunkSize = chunkSize;
	}

	public void setConcluded(Boolean concluded) {
		this.concluded = concluded;
	}

	public void setCreatedDate(String createdDate) {
		this.createdDate = createdDate;
	}

	public void setExpired(Boolean expired) {
		this.expired = expired;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}

	public void setProgress(Double progress) {
		this.progress = progress;
	}

	private void setStatus(String status) {
		this.status = status;

	}

	public void setSuccessfulChunks(Integer successfulChunks) {
		this.successfulChunks = successfulChunks;
	}

	public void setTotalNumberOfChunks(Integer totalNumberOfChunks) {
		this.totalNumberOfChunks = totalNumberOfChunks;
	}

	public void setUser(Long user) {
		this.user = user;
	}

}