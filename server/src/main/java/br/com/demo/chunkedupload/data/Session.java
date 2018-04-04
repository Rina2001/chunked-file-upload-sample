package br.com.demo.chunkedupload.data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

public class Session {
    private LocalDateTime createdDate;

    private boolean failed = false;

    private String id;
    private LocalDateTime lastUpdate;

    private String status;

    private Long timeout;

    private static Long DEFAULT_TIMEOUT = 3600L;

    private Long user;

    private FileInformation file;

    public Session(Long user, FileInformation fileInfo) {
	this(user, fileInfo, DEFAULT_TIMEOUT);
    }

    public Session(Long user, FileInformation fileInfo, Long timeout) {
	this.id = UUID.randomUUID().toString();
	this.createdDate = LocalDateTime.now();
	this.lastUpdate = this.createdDate;
	this.user = user;
	this.file = fileInfo;
	this.timeout = timeout;
    }

    /**
     * @return the createdDate
     */
    public String getCreatedDate() {
	return createdDate.toString();
    }

    public String getId() {
	return id;
    }

    /**
     * @return the lastUpdate
     */
    public String getLastUpdate() {
	return lastUpdate.toString();
    }

    /**
     * 
     * @return the upload progress
     */
    public double getProgress() {
	if (file.getTotalNumberOfChunks() == 0)
	    return 0;

	return getSuccessfulChunks() / (file.getTotalNumberOfChunks() * 1f);
    }

    public String getStatus() {
	if (failed)
	    status = "failed";
	else if (isConcluded())
	    status = "done";
	else
	    status = "ongoing";

	return status;
    }

    public boolean isConcluded() {
	return file.getTotalNumberOfChunks() == file.getAlreadyPersistedChunks().length;
    }

    /**
     * @return the alreadyPersistedBlocks
     */
    public int getSuccessfulChunks() {
	return file.getAlreadyPersistedChunks().length;
    }

    /**
     * @return the user
     */
    public Long getUser() {
	return user;
    }

    public boolean hasFailed() {
	return failed;
    }

    public boolean isExpired() {
	return Duration.between(lastUpdate, LocalDateTime.now()).getSeconds() >= timeout;
    }

    public void maskAsFailed() {
	failed = true;
    }

    public FileInformation getFileInfo() {
	return file;
    }

    public void renewTimeout() {
	lastUpdate = LocalDateTime.now();
    }
}
