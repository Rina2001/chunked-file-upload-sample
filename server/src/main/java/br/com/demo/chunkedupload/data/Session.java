package br.com.demo.chunkedupload.data;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import br.com.demo.chunkedupload.exception.InvalidOperationException;

public class Session {
    private Set<Integer> alreadyPersistedBlocks;

    private int chunkSize;
    private LocalDateTime createdDate;

    private String fileName;
    private Long fileSize;
    private String id;

    private LocalDateTime lastUpdate;
    private StorageMedium storage;

    private Long TIMEOUT_IN_SECONDS = 3600L;

    private Long user;

    public Session(Long user, String fileName, int chunkSize, Long fileSize) {

	id = UUID.randomUUID().toString();

	createdDate = LocalDateTime.now();
	lastUpdate = createdDate;
	this.user = user;
	this.fileName = fileName;
	this.chunkSize = chunkSize;
	this.fileSize = fileSize;

	storage = new LocalFileSystemStorage();
	alreadyPersistedBlocks = Collections.synchronizedSet(new HashSet<Integer>());
    }

    /**
     * Gets the byte array of a file chunk
     * 
     * @param chunkIndex
     *            (starts from zero)
     * @return
     * @throws IndexOutOfBoundsException
     * @throws IOException
     */
    public byte[] getChunkContent(int chunkIndex) throws IndexOutOfBoundsException, IOException {
	if (chunkIndex > this.getTotalNumberOfChunks())
	    throw new IndexOutOfBoundsException();

	return storage.read(id, chunkIndex);

    }

    /**
     * @return the chunkSize
     */
    public int getChunkSize() {
	return chunkSize;
    }

    /**
     * @return the createdDate
     */
    public String getCreatedDate() {
	return createdDate.toString();
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
	return fileName;
    }

    public Long getFileSize() {
	return fileSize;
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
	if (getTotalNumberOfChunks() == 0)
	    return 0;

	return getSuccessfulChunks() / (getTotalNumberOfChunks() * 1f);
    }

    /**
     * @return the alreadyPersistedBlocks
     */
    public int getSuccessfulChunks() {
	return alreadyPersistedBlocks.size();
    }

    /**
     * @return the totalNumberOfChunks
     */
    public int getTotalNumberOfChunks() {
	return (int) Math.ceil(fileSize / chunkSize);
    }

    /**
     * @return the user
     */
    public Long getUser() {
	return user;
    }

    public boolean isBlockPersisted(int chunkNumber) {
	return alreadyPersistedBlocks.contains(chunkNumber);
    }

    public boolean isConcluded() {
	return getTotalNumberOfChunks() == alreadyPersistedBlocks.size();
    }

    public boolean isExpired() {
	return Duration.between(lastUpdate, LocalDateTime.now()).getSeconds() >= TIMEOUT_IN_SECONDS;
    }

    public void persistBlock(int chunkNumber, byte[] buffer) throws InvalidOperationException {
	if (chunkSize != buffer.length && chunkNumber < getTotalNumberOfChunks()) {
	    throw new InvalidOperationException("Wrong chunk size");
	}

	if (!isBlockPersisted(chunkNumber)) {
	    try {
		storage.persist(id, chunkNumber, buffer);
	    } catch (IOException e) {
		throw new InvalidOperationException(e.getMessage());
	    }
	    alreadyPersistedBlocks.add(chunkNumber);
	}

	this.lastUpdate = LocalDateTime.now();
    }

}