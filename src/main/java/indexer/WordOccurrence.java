package indexer;

public class WordOccurrence {
    private String word;
    private int occurrences;

    public WordOccurrence(String word, int occurrences) {
        this.word = word;
        this.occurrences = occurrences;
    }

    public String getWord() {
        return word;
    }

    public int getOccurrences() {
        return occurrences;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public void setOccurrences(int occurrences) {
        this.occurrences = occurrences;
    }
}
