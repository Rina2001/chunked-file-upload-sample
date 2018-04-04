package br.com.demo.chunkedupload.data;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class SessionTest {

    @Mock
    private FileInformation fileInfo;

    @Test
    public void sessionExpiration_noTimeout() {
	Long user = 1L;
	Long timeout = 100000L;

	Session session = new Session(user, fileInfo, timeout);

	assertThat(session.isExpired(), equalTo(false));
    }

    @Test
    public void sessionExpiration_timeout() {
	Long user = 1L;
	Long timeout = 0L;

	Session session = new Session(user, fileInfo, timeout);

	assertThat(session.isExpired(), equalTo(true));
    }

    @Test
    public void conclusion_withTotalNumberOfChunksDownloaded_shouldReturnConcluded() {
	Long user = 1L;
	Long timeout = 10000L;
	when(fileInfo.getAlreadyPersistedChunks()).thenReturn(new Integer[1]);
	when(fileInfo.getTotalNumberOfChunks()).thenReturn(1);

	Session session = new Session(user, fileInfo, timeout);

	assertThat(session.isConcluded(), equalTo(true));
    }

    @Test
    public void conclusion_withPartialNumberOfChunksDownloaded_shouldReturnConcluded() {
	Long user = 1L;
	Long timeout = 10000L;
	when(fileInfo.getAlreadyPersistedChunks()).thenReturn(new Integer[0]);
	when(fileInfo.getTotalNumberOfChunks()).thenReturn(1);

	Session session = new Session(user, fileInfo, timeout);

	assertThat(session.isConcluded(), equalTo(false));
    }
}
