package br.com.demo.chunkedupload.data;

import java.io.IOException;
import java.io.OutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

public abstract class FileRepository {

	public abstract void persist(String id, int chunkNumber, byte[] buffer) throws IOException;

	public abstract byte[] read(String id, int chunkNumber) throws IOException;

	public StreamingOutput getContentStream(Session session) throws IOException {
		return new StreamingOutput() {
			@Override
			public void write(OutputStream out) throws IOException, WebApplicationException {

				for (int i = 1; i <= session.getFileInfo().getTotalNumberOfChunks(); i++) {
					out.write(read(session.getId(), i));
				}

				out.flush();

			}
		};
	}

}
