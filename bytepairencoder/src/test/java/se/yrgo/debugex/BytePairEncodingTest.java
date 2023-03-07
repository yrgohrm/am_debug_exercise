package se.yrgo.debugex;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import org.junit.jupiter.api.*;
import com.github.marschall.memoryfilesystem.*;

class BytePairEncodingTest {
    private FileSystem fileSystem;

    @BeforeEach
    public void beforeEach() throws Exception {
        fileSystem = MemoryFileSystemBuilder.newEmpty().build("default");
    }

    @AfterEach
    public void afterEach() throws Exception {
        if (fileSystem != null) {
            fileSystem.close();
        }
    }

    @Test
    void testEncodeEmptyFile() throws IOException {
        final String input = "data.txt";
        final String output = input + ".bpe";

        Path file = setupFile(input, "");
        BytePairEncoding.encodeFile(file);

        Path encodedFile = fileSystem.getPath(output);
        byte[] data = Files.readAllBytes(encodedFile);

        assertEquals(1, data.length);
    }

    private Path setupFile(String filename, String data) throws IOException {
        Path file = fileSystem.getPath(filename);
        try (var out = Files.newBufferedWriter(file)) {
            out.write(data);
        }
        return file;
    }
}
