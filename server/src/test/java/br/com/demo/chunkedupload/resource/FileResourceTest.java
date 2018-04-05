package br.com.demo.chunkedupload.resource;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import br.com.demo.chunkedupload.data.FileInformation;
import br.com.demo.chunkedupload.data.Session;
import br.com.demo.chunkedupload.exception.ApiException;
import br.com.demo.chunkedupload.exception.BadRequestException;
import br.com.demo.chunkedupload.exception.InvalidOperationException;
import br.com.demo.chunkedupload.exception.NotFoundException;
import br.com.demo.chunkedupload.model.UploadStatusResponse;
import br.com.demo.chunkedupload.service.UploadService;

@RunWith(MockitoJUnitRunner.class)
public class FileResourceTest {

    private static final int STATUS_OK = 200;
    private static final int STATUS_BAD_REQUEST = 400;
    private static final int STATUS_FORBIDDEN = 403;
    private static final int STATUS_NOT_FOUND = 404;
    private static final int STATUS_SERVER_ERROR = 500;
    private static final int STATUS_CREATED = 201;

    private FileResource fileResource;

    @Mock
    private UploadService uploadService;

    @Mock
    private Session session;

    @Mock
    private FileInformation file;

    @Before
    public void setUp() {
	fileResource = new FileResource(uploadService);

	when(session.getFileInfo()).thenReturn(file);
    }

    @Test
    public void startSession_WithValidData_shouldReturnCreatedResponse() throws ApiException {
	when(session.getFileInfo()).thenReturn(file);

	when(uploadService.createSession(any(), any(), anyInt(), any())).thenReturn(session);

	Response response = fileResource.startSession(1L, 1, 1L, UUID.randomUUID().toString());

	assertThat(response.getStatus(), equalTo(STATUS_CREATED));
    }

    @Test
    public void startSession_WithBadRequestException_shouldReturnBadRequestStatus() throws ApiException {
	when(uploadService.createSession(any(), any(), anyInt(), any())).thenThrow(BadRequestException.class);

	Response response = fileResource.startSession(null, 1, 1L, UUID.randomUUID().toString());

	assertThat(response.getStatus(), equalTo(STATUS_BAD_REQUEST));
    }

    @Test
    public void startSession_WithGenericApiException_shouldReturnForbiddenStatus() throws ApiException {
	given(uploadService.createSession(any(), any(), anyInt(), any())).willAnswer(invocation -> {
	    throw new ApiException(0, "");
	});

	Response response = fileResource.startSession(null, 1, 1L, UUID.randomUUID().toString());

	assertThat(response.getStatus(), equalTo(STATUS_FORBIDDEN));
    }

    @Test
    public void startSession_WithGenericException_shouldReturnServerErrorResponse() throws ApiException {
	given(uploadService.createSession(any(), any(), anyInt(), any())).willAnswer(invocation -> {
	    throw new Exception("");
	});

	Response response = fileResource.startSession(null, 1, 1L, UUID.randomUUID().toString());

	assertThat(response.getStatus(), equalTo(STATUS_SERVER_ERROR));
    }

    @Test
    public void uploadFileChunk_withValidData_shouldReturnSuccess() {
	InputStream inputStream = new ByteArrayInputStream("test".getBytes());

	Response response = fileResource.uploadFileChunk(1L, UUID.randomUUID().toString(), 1, inputStream);

	assertThat(response.getStatus(), equalTo(STATUS_OK));
    }

    @Test
    public void uploadFileChunk_withMissingUserId_shouldReturnBadRequest() {
	InputStream inputStream = new ByteArrayInputStream("test".getBytes());

	Response response = fileResource.uploadFileChunk(null, UUID.randomUUID().toString(), 1, inputStream);

	assertThat(response.getStatus(), equalTo(STATUS_BAD_REQUEST));
    }

    @Test
    public void uploadFileChunk_withMissingSessionId_shouldReturnBadRequest() {
	InputStream inputStream = new ByteArrayInputStream("test".getBytes());

	Response response = fileResource.uploadFileChunk(1L, null, 1, inputStream);

	assertThat(response.getStatus(), equalTo(STATUS_BAD_REQUEST));
    }

    @Test
    public void uploadFileChunk_withInvalidChunkNumber_shouldReturnBadRequest() {
	InputStream inputStream = new ByteArrayInputStream("test".getBytes());

	Response response = fileResource.uploadFileChunk(1L, UUID.randomUUID().toString(), 0, inputStream);

	assertThat(response.getStatus(), equalTo(STATUS_BAD_REQUEST));
    }

    @Test
    public void uploadFileChunk_whenSessionNotFound_shouldReturnObjectNotFoundStatus()
	    throws ApiException, IOException {
	doThrow(new NotFoundException("")).when(uploadService).persistBlock(any(), any(), anyInt(), any());
	InputStream inputStream = new ByteArrayInputStream("test".getBytes());
	String sessionId = "some random value";

	Response response = fileResource.uploadFileChunk(1L, sessionId, 1, inputStream);

	assertThat(response.getStatus(), equalTo(STATUS_NOT_FOUND));
    }

    @Test
    public void uploadFileChunk_whenIOException_shouldReturnServerError() throws ApiException, IOException {
	doThrow(new IOException("")).when(uploadService).persistBlock(any(), any(), anyInt(), any());
	InputStream inputStream = new ByteArrayInputStream("test".getBytes());
	String sessionId = "some random value";

	Response response = fileResource.uploadFileChunk(1L, sessionId, 1, inputStream);

	assertThat(response.getStatus(), equalTo(STATUS_SERVER_ERROR));
    }

    @Test
    public void uploadFileChunk_whenGenericApiException_shouldReturnForbiddenStatus() throws ApiException, IOException {
	doThrow(new ApiException(0, "")).when(uploadService).persistBlock(any(), any(), anyInt(), any());
	InputStream inputStream = new ByteArrayInputStream("test".getBytes());
	String sessionId = "some random value";

	Response response = fileResource.uploadFileChunk(1L, sessionId, 1, inputStream);

	assertThat(response.getStatus(), equalTo(STATUS_FORBIDDEN));
    }

    @Test
    public void getUploadStatus_withExistingSession_shouldReturnOKStatus() throws ApiException, IOException {
	String validSessionId = "valid_session_id";
	when(uploadService.getSession(validSessionId)).thenReturn(session);

	Response response = fileResource.getUploadStatus(validSessionId);

	assertThat(response.getStatus(), equalTo(STATUS_OK));
	assertThat(response.getEntity(), instanceOf(UploadStatusResponse.class));
    }

    @Test
    public void getUploadStatus_withNonExistingSession_shouldReturnNotFoundStatus() throws ApiException, IOException {
	String validSessionId = "valid_session_id";
	when(uploadService.getSession(validSessionId)).thenReturn(null);

	Response response = fileResource.getUploadStatus(validSessionId);

	assertThat(response.getStatus(), equalTo(STATUS_NOT_FOUND));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void getListOfUploadStatus_withExistingSession_shouldReturnOKStatus() throws ApiException, IOException {
	when(uploadService.getAllSessions()).thenReturn(Arrays.asList(session));

	Response response = fileResource.listUploadsStatus();

	assertThat(response.getStatus(), equalTo(STATUS_OK));
	assertThat(response.getEntity(), instanceOf((Class<List<UploadStatusResponse>>) (Object) List.class));
    }

    @Test
    public void downloadFile_withExistingSession_shouldReturnFile()
	    throws InvalidOperationException, NotFoundException, IOException {
	String validSessionId = "valid_session_id";
	when(uploadService.getSession(validSessionId)).thenReturn(session);
	when(uploadService.getContentStream(session)).thenReturn(new StreamingOutput() {
	    @Override
	    public void write(OutputStream output) throws IOException, WebApplicationException {
		output.write("result".getBytes());
	    }
	});

	Response response = fileResource.downloadFile(validSessionId);

	assertThat(response.getStatus(), equalTo(STATUS_OK));
	assertThat(response.getEntity(), notNullValue());
    }

    @Test
    public void downloadFile_withNonExistingSession_shouldReturnNotFound() {
	String validSessionId = "valid_session_id";
	when(uploadService.getSession(validSessionId)).thenReturn(null);

	Response response = fileResource.downloadFile(validSessionId);

	assertThat(response.getStatus(), equalTo(STATUS_NOT_FOUND));
    }
}
