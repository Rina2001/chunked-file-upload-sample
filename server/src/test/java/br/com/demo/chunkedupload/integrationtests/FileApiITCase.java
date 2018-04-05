package br.com.demo.chunkedupload.integrationtests;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Formatter;
import java.util.UUID;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Before;
import org.junit.Test;

import br.com.demo.chunkedupload.model.SessionCreationStatusResponse;
import br.com.demo.chunkedupload.resource.FileResource;

public class FileApiITCase {
    private static final int STATUS_OK = 200;
    private static final int STATUS_CREATED = 201;
    private static final int CHUNK_SIZE = 10;
    private static final Long USER_ID = 1L;
    private static final int NUMBER_OF_CHUNKS = 5;
    private static final int FILE_SIZE = ((NUMBER_OF_CHUNKS - 1) * CHUNK_SIZE) + (CHUNK_SIZE / 2);

    byte[] originalData;

    private FileResource api;

    @Before
    public void setup() {
        api = new FileResource();
        originalData = generateRandomArray(FILE_SIZE);
    }

    @Test
    public void testChunkedUpload() throws WebApplicationException, IOException, NoSuchAlgorithmException {

        String fileName = UUID.randomUUID().toString();

        SessionCreationStatusResponse session = createSession(FILE_SIZE, fileName, api);

        for (int i = 0; i < NUMBER_OF_CHUNKS; i++) {
            uploadChunk(session.getSessionId(), i);
        }

        byte[] downloadedData = downloadFile(session.getSessionId());

        assertThat(SHAsum(originalData), equalTo(SHAsum(downloadedData)));
    }

    private byte[] downloadFile(String sessionId) throws IOException {
        Response response = api.downloadFile(sessionId);

        assertThat(response.getStatus(), equalTo(STATUS_OK));

        StreamingOutput stream = (StreamingOutput) response.getEntity();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        stream.write(output);

        byte[] downloadedContent = output.toByteArray();
        output.close();
        return downloadedContent;
    }

    private void uploadChunk(String sessionId, int chunkIndex) {
        int start = chunkIndex * CHUNK_SIZE;
        int end = (chunkIndex == NUMBER_OF_CHUNKS - 1) ? start + (FILE_SIZE % CHUNK_SIZE)  : start + CHUNK_SIZE;
        byte[] chunkContent = Arrays.copyOfRange(originalData, start, end);
        Response response = api.uploadFileChunk(USER_ID, sessionId, (chunkIndex + 1), createStream(chunkContent));

        assertThat(response.getStatus(), equalTo(STATUS_OK));
    }

    private InputStream createStream(byte[] chunkContent) {
        return new InputStream() {
            int offset = 0;

            @Override
            public int read() throws IOException {
                if (offset == chunkContent.length)
                    return -1;

                return chunkContent[offset++];
            }
        };
    }

    private SessionCreationStatusResponse createSession(int size, String fileName, FileResource api) {
        Response response = api.startSession(USER_ID, CHUNK_SIZE, Long.valueOf(size), fileName);

        assertThat(response.getStatus(), equalTo(STATUS_CREATED));
        return (SessionCreationStatusResponse) response.getEntity();
    }

    private static byte[] generateRandomArray(int size) {
        byte[] arr = new byte[size];

        while (--size >= 0) {
            arr[size] = (byte) (65 + ((Math.random() * 100) % 26));
        }

        return arr;
    }

    private static String SHAsum(byte[] array) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        return byteArray2Hex(md.digest(array));
    }

    private static String byteArray2Hex(final byte[] hash) {
        Formatter formatter = new Formatter();
        try {
            for (byte b : hash) {
                formatter.format("%02x", b);
            }
            return formatter.toString();
        } finally {
            formatter.close();
        }
    }
}
