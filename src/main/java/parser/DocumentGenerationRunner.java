package parser;

import org.jsoup.nodes.Document;

public class DocumentGenerationRunner {
    public static void main(String[] args) {
        HTMLParser parser = new HTMLParser();
        Document transcriptsDocument = parser.getTranscriptsDocument();
        parser.getEpisodeTranscripts(transcriptsDocument);
    }
}
