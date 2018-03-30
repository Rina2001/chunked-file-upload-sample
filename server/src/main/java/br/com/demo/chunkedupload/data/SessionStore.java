package br.com.demo.chunkedupload.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import br.com.demo.chunkedupload.exception.InvalidOperationException;

public class SessionStore {

    Map<String, Session> sessions;

    public SessionStore() {
	sessions = new HashMap<>();
    }

    /**
     * Creates a new session
     * 
     * @param user
     * @param fileName
     * @param chunkSize
     * @param fileSize
     * @return
     * @throws InvalidOperationException
     */
    public Session createSession(Long user, String fileName, int chunkSize, Long fileSize)
	    throws InvalidOperationException {
	String key = buildKey(user, fileName);

	if (sessions.containsKey(key)) {
	    throw new InvalidOperationException("The given session already exists");
	}

	Session session = new Session(user, fileName, chunkSize, fileSize);
	sessions.put(key, session);

	return session;
    }

    public Session getSession(Long user, String fileName) {
	return sessions.get(buildKey(user, fileName));
    }

    public Session getSession(String id) {
	// Java 8 causes a bug on Jersey 1.x, so we'll have to go the verbose way here
	for (Session s : sessions.values()) {
	    if (id.equals(s.getId())) {
		return s;
	    }
	}

	return null;
    }

    public List<Session> getAllSessions() {
	return Collections.unmodifiableList(sessions.values().stream().collect(Collectors.toList()));
    }

    private static String buildKey(Long user, String fileName) {
	return String.join("#", String.valueOf(user), fileName);
    }

}
