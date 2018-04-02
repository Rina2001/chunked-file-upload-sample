package br.com.demo.chunkedupload.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

import br.com.demo.chunkedupload.exception.InvalidOperationException;
import br.com.demo.chunkedupload.exception.SessionAlreadyBeingCreatedException;

public class SessionStoreTest {
    private static Long VALID_USER = 1L;
    private static String VALID_FILE_NAME = "valid_file_name";
    private static int CHUNK_SIZE = 1024;
    private static Long TOTAL_SIZE = 2097152L;

    SessionStore sessionStore = null;

    @Before
    public void setUp() {
	sessionStore = new SessionStore();
    }

    @Test
    public void create_withValidUserIdAndFileName_shouldCreateSession()
	    throws InvalidOperationException, SessionAlreadyBeingCreatedException {
	sessionStore.createSession(VALID_USER, VALID_FILE_NAME, CHUNK_SIZE, TOTAL_SIZE);
    }

    @Test
    public void getSession_withValidUserAndFileName_shouldReturnSession()
	    throws InvalidOperationException, SessionAlreadyBeingCreatedException {
	Session session = sessionStore.createSession(VALID_USER, VALID_FILE_NAME, CHUNK_SIZE, TOTAL_SIZE);
	session = sessionStore.getSession(session.getId());
	assertNotNull(session);
    }

    @Test
    public void getSession_withNullParameters_shouldReturnNull()
	    throws InvalidOperationException, SessionAlreadyBeingCreatedException {
	Session session = sessionStore.getSession(UUID.randomUUID().toString());
	assertNull(session);
    }
}
