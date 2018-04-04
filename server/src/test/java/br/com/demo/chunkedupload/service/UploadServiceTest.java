package br.com.demo.chunkedupload.service;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import br.com.demo.chunkedupload.data.FileRepository;
import br.com.demo.chunkedupload.data.Session;
import br.com.demo.chunkedupload.exception.ApiException;
import br.com.demo.chunkedupload.exception.BadRequestException;
import br.com.demo.chunkedupload.exception.NotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class UploadServiceTest {
    private static final int CHUNK_LIMIT = 1024 * 1024;

    private UploadService service;

    @Mock
    private FileRepository repository;

    @Before
    public void setUp() {
	service = new UploadService(repository);
    }

    @Test
    public void createSession_withValidData_shouldCreateSessionSuccessfully() throws BadRequestException {
	Session session = service.createSession(1L, UUID.randomUUID().toString(), 1, 1L);

	assertThat(session, notNullValue());
    }

    @Test(expected = BadRequestException.class)
    public void createSession_withMissingFilename_shouldThrowBadRequestException() throws BadRequestException {
	Session session = service.createSession(1L, "", 1, 1L);

	assertThat(session, notNullValue());
    }

    @Test(expected = BadRequestException.class)
    public void createSession_withMissingUser_shouldThrowBadRequestException() throws BadRequestException {
	service.createSession(null, UUID.randomUUID().toString(), 1, 1L);
    }

    @Test(expected = BadRequestException.class)
    public void createSession_withChunkSizeGreaterThanThreshold_shouldThrowBadRequestException()
	    throws BadRequestException {
	service.createSession(1L, UUID.randomUUID().toString(), CHUNK_LIMIT + 1, 1L);
    }

    @Test(expected = BadRequestException.class)
    public void createSession_withChunkSizeLessThanOne_shouldThrowBadRequestException() throws BadRequestException {
	service.createSession(1L, UUID.randomUUID().toString(), 0, 1L);
    }

    @Test(expected = BadRequestException.class)
    public void createSession_withFileSizeLessThanOne_shouldThrowBadRequestException() throws BadRequestException {
	service.createSession(1L, UUID.randomUUID().toString(), 1, 0L);
    }

    @Test
    public void persistBlock_withValidData_shouldPersistSuccessfully() throws ApiException, IOException {
	Session session = service.createSession(1L, UUID.randomUUID().toString(), 1, 1L);

	byte[] buffer = new byte[1];
	int chunkNumber = 1;
	service.persistBlock(session.getId(), session.getUser(), chunkNumber, buffer);

	verify(repository).persist(session.getId(), chunkNumber, buffer);
    }

    @Test(expected = NotFoundException.class)
    public void persistBlock_whenSessionNotFound_shouldTriggerNotFoundException() throws ApiException, IOException {
	byte[] buffer = new byte[1];
	int chunkNumber = 1;
	String sessionId = "random_session_id";
	Long user = 1L;
	service.persistBlock(sessionId, user, chunkNumber, buffer);

	verify(repository, times(0)).persist(sessionId, chunkNumber, buffer);
    }
}
