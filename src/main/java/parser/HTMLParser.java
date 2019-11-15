package parser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

class HTMLParser {
    private static String HTML_LOCATION = System.getProperty("user.dir") + "/data/transcripts.html";
    private static String HTML_TRANSCRIPT_ID = "mw-content-text";
    private static Set<String> IGNORED_TAGS = new HashSet<>();

    Document getTranscriptsDocument() {
        File transcriptsFile = new File(HTML_LOCATION);
        try {
            return Jsoup.parse(transcriptsFile, "UTF-8");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    void getEpisodeTranscripts(Document transciptsDoc) {
        String title = transciptsDoc.title();
        Element mainContent = transciptsDoc.getElementById(HTML_TRANSCRIPT_ID);
        Elements contentList = mainContent.children();

        for (Element partOfTranscript : contentList) {
            String tagName = partOfTranscript.tagName();
            // p,div,noscript, h3,h4
            if (!tagName.equals("dl") && !tagName.equals("h2"))
                System.out.println(partOfTranscript);
        }
    }
}
