package parser;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.Map;

public enum TranscriptTag {
    TITLE_TAG("h2"),
    PARAGRAPH_TAG("dl"),
    TABLE_TAG("table"),
    LINK_TAG("a")
    ;

    private final String tagName;

    private static final Map<String, TranscriptTag> MAP = new HashMap<>();

    static {
        for (TranscriptTag tag : values()) MAP.put(tag.tagName, tag);
    }

    TranscriptTag(String tagName) {
        this.tagName = tagName;
    }

    public String getTagName() {
        return tagName;
    }

    public static TranscriptTag from(String tagName) {
        return MAP.get(tagName);
    }
}
