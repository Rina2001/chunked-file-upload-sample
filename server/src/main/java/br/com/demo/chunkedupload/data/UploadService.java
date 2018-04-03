package br.com.demo.chunkedupload.data;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.lang3.StringUtils;

import br.com.demo.chunkedupload.exception.ApiException;
import br.com.demo.chunkedupload.exception.BadRequestException;
import br.com.demo.chunkedupload.exception.InvalidOperationException;
import br.com.demo.chunkedupload.exception.NotFoundException;

public class UploadService {

	private static final int CHUNK_LIMIT = 1024 * 1024;

	Map<String, Session> sessions;
	FileRepository fileStorage;

	public UploadService(FileRepository storage) {
		this.fileStorage = storage;
		sessions = Collections.synchronizedMap(new ConcurrentHashMap<String, Session>());
	}

	/**
	 * Creates a new session
	 * 
	 * @param user
	 * @param fileName
	 * @param chunkSize
	 * @param fileSize
	 * @return
	 * @throws ApiException
	 */
	public Session createSession(Long user, String fileName, int chunkSize, Long fileSize) throws ApiException {

		if (StringUtils.isEmpty(fileName))
			throw new BadRequestException("File name missing");

		if (user == null)
			throw new BadRequestException("User ID missing");

		if (chunkSize > CHUNK_LIMIT)
			throw new BadRequestException(String.format("Maximum chunk size is {} bytes", CHUNK_LIMIT));

		if (chunkSize < 1)
			throw new BadRequestException("Chunk size must be greater than zero");

		if (chunkSize < 1)
			throw new BadRequestException("Total size must be greater than zero");

		Session session = new Session(user, new FileInformation(fileSize, fileName, chunkSize));
		sessions.put(session.getId(), session);

		return session;
	}

	public Session getSession(String id) {
		return sessions.get(id);
	}

	public List<Session> getAllSessions() {
		List<Session> list = new ArrayList<Session>();
		for (Session s : sessions.values()) {
			list.add(s);
		}
		return list;
	}

	public void persistBlock(String sessionId, Long userId, int chunkNumber, byte[] buffer)
			throws ApiException, IOException {
		Session session = getSession(sessionId);

		try {
			if (session == null) {
				throw new NotFoundException("Session not found");
			}

			fileStorage.persist(sessionId, chunkNumber, buffer);

			session.getFileInfo().markChunkAsPersisted(chunkNumber);
		} catch (Exception e) {
			if (session != null)
				session.maskAsFailed();

			throw e;
		}
	}

	public StreamingOutput getContentStream(Session session)
			throws IOException, InvalidOperationException, NotFoundException {
		if (!session.isConcluded())
			throw new InvalidOperationException("Upload is not yet finished");

		if (session.hasFailed())
			throw new NotFoundException("File not found");

		return fileStorage.getContentStream(session);
	}
}
