package retrieval;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class BM25 extends Models {
    private static final double K1 = 1.2;
    private static final double K2 = 500; // typical values range from 0 to 1000
    private static final double B = 0.75; // normalizes the tf componenet of the doc frequencies

    public BM25() {
        super();
    }

    @Override
    public ArrayList<TfIdf.Similarity> retrieve(String query) {
        System.out.println("Executed BM25");
        ArrayList<String> keywords = Lists.newArrayList(super.extractTerms(query));
        return null;
    }

    private void labelDocuments(List<String> keywords) {
        // compute tfidf threshold that document has to be above to be considered relevant
        //
    }

    private double getScore(List<String> keywords, String filename) {
        double sum;
        sum = keywords.stream()
            .mapToDouble(word -> getScore(word, filename))
            .sum();
        return sum;

    }

    private double getScore(String term, String filename) {
        return 0.0;
    }

    private double relevance() {
        return 0.0;
    }

    private double documentFrequency(String term, String filename) {
        int fileFreq = getOccurrencesInFile(term, filename);

        return ((K1 + 1) * fileFreq) / (K(filename) + fileFreq);
    }

    private double queryFrequency(String term, List<String> keywords) {
        long queryFreq = keywords.stream()
            .filter(word -> word.equals(term))
            .count();

        return ((K2 + 1) * queryFreq) / (K2 + queryFreq);
    }

    private double K(String filename) {
        return K1 * ((1 - B) + (B * getDocLength(filename) / averageDocLength()));
    }

    private int getDocLength(String filename) {
        return this.getFileTermSize().get(filename);
    }

    private double averageDocLength() {
        return this.getFileTermSize().values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .getAsDouble();
    }
}
