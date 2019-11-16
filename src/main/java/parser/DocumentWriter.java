package parser;

import com.google.common.collect.Lists;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

public class DocumentWriter {
    private static final String DOC_DIR = System.getProperty("user.dir") + "/documents";

    public static void writeDocumentToFile(EpisodeDocument document) {
        new File(DOC_DIR).mkdir();
        ArrayList<String> lines = Lists.newArrayList(document.getBody().split("\n"));
        Path path = Paths.get(DOC_DIR + "/" + document.getTitle() + ".txt");
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            System.err.println("Error when writing to file: " + document.getTitle());
            throw new RuntimeException();
        }
    }
}
