package br.com.demo.chunkedupload.data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import br.com.demo.chunkedupload.exception.InvalidOperationException;
import br.com.demo.chunkedupload.exception.SessionAlreadyBeingCreatedException;

public class SessionStore {

    Map<String, Session> sessions;

    public SessionStore() {
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
     * @throws InvalidOperationException
     */
    public Session createSession(Long user, String fileName, int chunkSize, Long fileSize)
	    throws InvalidOperationException, SessionAlreadyBeingCreatedException {
	Session session = new Session(user, fileName, chunkSize, fileSize);
	sessions.put(session.getId(), session);
	return session;
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
	List<Session> list = new ArrayList<Session>();
	for (Session s : sessions.values()) {
	    list.add(s);
	}
	return list;
    }
}
