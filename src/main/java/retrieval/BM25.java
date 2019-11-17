package retrieval;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.*;
import java.util.stream.Collectors;

public class BM25 extends Models {
    private static final double K1 = 1.2;
    private static final double K2 = 500; // typical values range from 0 to 1000
    private static final double B = 0.75; // normalizes the tf componenet of the doc frequencies

    public BM25() {
        super();
    }

    @Override
    public ArrayList<TfIdf.Similarity> retrieve(String query) {
        ArrayList<String> keywords = Lists.newArrayList(super.extractTerms(query));
        Set<String> relevantDocuments = getRelevantDocuments(keywords);
        ArrayList<String> documentCollection = getDocumentList();

        Map<String, Double> scoredDocuments = Maps.newHashMap();
        documentCollection.parallelStream()
            .forEach(document -> scoredDocuments.put(document, getScore(keywords, document, relevantDocuments)));

        System.out.println("Executed BM25");
        return null;
    }

    private Set<String> getRelevantDocuments(List<String> keywords) {
        TfIdf tfidfModel = new TfIdf();
        double threshold = keywords.parallelStream()
            .mapToDouble(term -> getAverageTfIdf(term, tfidfModel))
            .average()
            .orElse(1); // highest threshold if no keywords

        return getDocumentList().parallelStream()
            .filter(document -> getAverageTfIdfForTerms(keywords, document) > threshold)
            .collect(Collectors.toSet());
    }

    private double getAverageTfIdfForTerms(List<String> keywords, String document) {
        return keywords.parallelStream()
            .mapToDouble(term -> getTfIdf(term, document))
            .average()
            .orElse(0);
    }

    private double getTfIdf(String term, String document) {
        return 0.0;
    }

    private double getAverageTfIdf(String term, TfIdf tfIdfModel) {
        if (!get_doc_indicies().containsKey(term)) {
            return 0; // if term isnt in collection, term frequency = 0
        }
        Entry filesContainingTerm = get_doc_indicies().get(term);
        double tfidf = tfIdfModel.tfidf(term, filesContainingTerm);
        double numberOfDocuments = getNumberOfDocuments();
        return tfidf / numberOfDocuments;
    }

    private double getScore(List<String> keywords, String filename, Set<String> relevantDocuments) {
        double sum = keywords.parallelStream()
            .mapToDouble(word -> getScore(word, filename, relevantDocuments, keywords))
            .sum();
        return sum;
    }

    private double getScore(String term, String filename, Set<String> relevantDocuments, List<String> keywords) {
        return relevance(term, relevantDocuments) * documentFrequency(term, filename) * queryFrequency(term, keywords);
    }

    private double relevance(String term, Set<String> relevantDocuments) {
        double N = getNumberOfDocuments();
        double ni = getNumberOfDocuments(term);
        double R = relevantDocuments.size();
        double ri = getNumberOfDocumentsContainingTerm(term, relevantDocuments);

        double numerator = (ri + 0.5) / (R - ri + 0.5);
        double denomerator = (ni - ri + 0.5) / (N - ni - R + ri + 0.5);

        return Math.log(numerator / denomerator);
    }

    private double documentFrequency(String term, String filename) {
        double fileFreq = getOccurrencesInFile(term, filename);

        return ((K1 + 1) * fileFreq) / (k(filename) + fileFreq);
    }

    private double queryFrequency(String term, List<String> keywords) {
        long queryFreq = keywords.stream()
            .filter(word -> word.equals(term))
            .count();

        return ((K2 + 1) * queryFreq) / (K2 + queryFreq);
    }

    private double k(String filename) {
        return K1 * ((1 - B) + (B * getDocLength(filename) / averageDocLength()));
    }

    private double getDocLength(String filename) {
        return (double) getFileTermSize().get(filename);
    }

    private double averageDocLength() {
        return getFileTermSize().values().stream()
            .mapToInt(Integer::intValue)
            .average()
            .getAsDouble();
    }

    private double getNumberOfDocuments() {
        return getFileTermSize().size();
    }

    private double getNumberOfDocuments(String term) {
        return get_doc_indicies().get(term).getSize();
    }

    private double getNumberOfDocumentsContainingTerm(String term, Set<String> documents) {
        return get_doc_indicies().get(term).getFileOccurrences().parallelStream()
            .map(FileOccurrence::getFilename)
            .filter(documents::contains)
            .count();
    }
}
