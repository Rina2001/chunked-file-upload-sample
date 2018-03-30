package br.com.demo.chunkedupload.data;

import java.io.IOException;

public class MemoryStorage implements StorageMedium {
    @Override
    public void persist(String id, int chunkNumber, byte[] buffer) throws IOException {
	// TODO Auto-generated method stub

    }

    @Override
    public byte[] read(String id, int chunkNumber) throws IOException {
	// TODO Auto-generated method stub
	return null;
    }

}
