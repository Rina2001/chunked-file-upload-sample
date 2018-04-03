package br.com.demo.chunkedupload.data;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import br.com.demo.chunkedupload.exception.ApiException;
import br.com.demo.chunkedupload.exception.InvalidOperationException;
import br.com.demo.chunkedupload.exception.SessionAlreadyBeingCreatedException;

@RunWith(MockitoJUnitRunner.class)
public class UploadManagerTest {
	private static Long VALID_USER = 1L;
	private static String VALID_FILE_NAME = "valid_file_name";
	private static int CHUNK_SIZE = 1024;
	private static Long TOTAL_SIZE = 2097152L;

	UploadService uploadService = null;

	@Mock
	FileRepository storage;

	@Before
	public void setUp() {
		uploadService = new UploadService(storage);
	}

	@Test
	public void create_withValidUserIdAndFileName_shouldCreateSession() throws ApiException {
		uploadService.createSession(VALID_USER, VALID_FILE_NAME, CHUNK_SIZE, TOTAL_SIZE);
	}

	@Test
	public void getSession_withValidUserAndFileName_shouldReturnSession() throws ApiException {
		Session session = uploadService.createSession(VALID_USER, VALID_FILE_NAME, CHUNK_SIZE, TOTAL_SIZE);
		session = uploadService.getSession(session.getId());
		assertNotNull(session);
	}

	@Test
	public void getSession_withNullParameters_shouldReturnNull()
			throws InvalidOperationException, SessionAlreadyBeingCreatedException {
		Session session = uploadService.getSession(UUID.randomUUID().toString());
		assertNull(session);
	}
}
