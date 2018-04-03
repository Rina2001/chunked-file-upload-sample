package br.com.demo.chunkedupload.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MemoryRepository extends FileRepository {

	private Map<String, Map<Integer, byte[]>> internalStorage;

	public MemoryRepository() {
		internalStorage = new HashMap<String, Map<Integer, byte[]>>();
	}

	@Override
	public void persist(String id, int chunkNumber, byte[] buffer) throws IOException {
		if (!internalStorage.containsKey(id))
			internalStorage.put(id, new HashMap<Integer, byte[]>());

		Map<Integer, byte[]> blocks = internalStorage.get(id);
		blocks.put(chunkNumber, buffer);
	}

	@Override
	public byte[] read(String id, int chunkNumber) throws IOException {
		if (!internalStorage.containsKey(id)) {
			throw new IOException("Session not found on internalStorage");
		}

		return internalStorage.get(id).get(chunkNumber);
	}

}
