package br.com.demo.chunkedupload.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class LocalFileSystemRepository extends FileRepository {

    String ROOT = "./files_store";

    @Override
    public void persist(String id, int chunkNumber, byte[] buffer) throws IOException {
        Path chunkDestinationPath = Paths.get(ROOT, id);

        if (!Files.exists(chunkDestinationPath)) {
            Files.createDirectories(chunkDestinationPath);
        }

        Path path = Paths.get(ROOT, id, String.valueOf(chunkNumber));
        Files.write(path, buffer);
    }

    @Override
    public byte[] read(String id, int chunkNumber) throws IOException {
        Path targetPath = Paths.get(ROOT, id, String.valueOf(chunkNumber));
        return Files.readAllBytes(targetPath);
    }

}
