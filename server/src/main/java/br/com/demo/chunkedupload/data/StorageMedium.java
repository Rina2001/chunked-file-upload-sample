package br.com.demo.chunkedupload.data;

import java.io.IOException;

public interface StorageMedium {

    void persist(String id, int chunkNumber, byte[] buffer) throws IOException;

    byte[] read(String id, int chunkNumber) throws IOException;
}
