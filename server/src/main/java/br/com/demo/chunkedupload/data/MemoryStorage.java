package br.com.demo.chunkedupload.data;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MemoryStorage implements StorageMedium {

    static Map<String, Map<Integer, byte[]>> storage;

    static {
	storage = new HashMap<String, Map<Integer, byte[]>>();
    }

    @Override
    public void persist(String id, int chunkNumber, byte[] buffer) throws IOException {
	if (!storage.containsKey(id))
	    storage.put(id, new HashMap<Integer, byte[]>());

	Map<Integer, byte[]> blocks = storage.get(id);
	blocks.put(chunkNumber, buffer);
    }

    @Override
    public byte[] read(String id, int chunkNumber) throws IOException {
	if (!storage.containsKey(id)) {
	    throw new IOException("Session not found on storage");
	}

	Map<Integer, byte[]> blocks = storage.get(id);
	return blocks.get(chunkNumber);
    }

}
