package br.com.demo.chunkedupload.data;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import br.com.demo.chunkedupload.exception.InvalidOperationException;
import br.com.demo.chunkedupload.exception.SessionAlreadyBeingCreatedException;

public class SessionStore {

    Map<String, Session> sessions;
    Set<String> sessionLocks;

    public SessionStore() {
	sessions = Collections.synchronizedMap(new ConcurrentHashMap<>());
	sessionLocks = Collections.synchronizedSet(new HashSet<>());
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
	    throws InvalidOperationException, SessionAlreadyBeingCreatedException {

	System.out
		.println("Requested to create session for user " + user + " and file " + fileName + " on SessionStore");

	String key = buildKey(user, fileName);

	// avoids a race condition on session creation...
	if (!sessionLocks.contains(key)) {
	    sessionLocks.add(key);

	    if (!sessions.containsKey(key)) {
		sessions.put(key, new Session(user, fileName, chunkSize, fileSize));
		return sessions.get(key);
	    }
	}

	// ... but doesn't wait for the session to be created
	// throws the problem on the wind and let someone else handle it :-)
	if (sessions.get(key) == null) {
	    throw new SessionAlreadyBeingCreatedException("Session is still being created by another request");
	}

	return sessions.get(key);

    }

    public Session getSession(Long user, String fileName) {
	return sessions.get(buildKey(user, fileName));
    }

    public Session getSession(String id) {
	// Java 8 filter predicate causes a bug on Jersey 1.x, so we'll have to go the
	// verbose way here
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
