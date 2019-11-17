package retrieval;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TfIdf extends Models {

    public TfIdf() {
        super();
    }

    private static double termFrequency(String term,
                                        String document) {

        int totalTerms = getFileTermSize().get(document);
        int occurences = get_doc_indicies().get(term).getFileOccurrences().stream()
                .filter(e -> e.getFilename().equals(document))
                .findFirst()
                .orElse(new FileOccurrence("", 0))
                .getOccurrences();

        return (double) occurences / totalTerms;
    }

    private static double inverseDocumentFrequency(String term,
                                                   String document) {
        int totalDocuments = fileTermSize.keySet().size();
        int documentsWithTerm = documents.get(term).getSize();
        return Math.log((double) totalDocuments / documentsWithTerm);
    }

    public static double tfidf(String term, String document) {
        return termFrequency(term, document) * inverseDocumentFrequency(term,
                document);
    }

    private double[][] documentVectorizor(HashMap<String, Entry> index,
                                          String document,
                                          double[][] vectorSpace,
                                          int i, int j) {
        ArrayList<String> terms = new ArrayList<>(index.keySet());
        Collections.sort(terms);
        for (String term : terms) {
            vectorSpace[i][j] = tfidf(term, document);
            j += 1;
        }

        return vectorSpace;
    }

    @Override
    public ArrayList<Similarity> retrieve(String query) {
        HashMap<String, Entry> index = super.get_doc_indicies();
        ArrayList<String> documents = super.getDocumentList();
        String[] query_elements = super.extractTerms(query);
        // space is documents x terms in index
        double[][] vectorSpace = new double[documents.size()][index.size()];
        int i = 0;
        //go through each document
        for (String document : documents) {
            int j = 0;
            //go through every element in the index, columns
            vectorSpace = documentVectorizor(index, document, vectorSpace, i,
                    j);
            i += 1;
        }
        int[] vector_query = new int[index.size()];
        int position = 0;
        for (String term : index.keySet()) {
            if (Arrays.asList(query_elements).contains(term)) {
                vector_query[position] =
                        Collections.frequency(Arrays.asList(query_elements),
                                term);
            } else {
                vector_query[position] = 0;
            }
            position += 1;
        }
        ArrayList<Similarity> docSimilarities = new ArrayList<>();
        for (i = 0; i < vectorSpace.length; i++) {
            docSimilarities.add(new Similarity(documents.get(i),
                    cosineSimilarity(vectorSpace[i], vector_query)));
        }
        Collections.sort(docSimilarities);

        return (ArrayList<Similarity>) docSimilarities.stream().limit(15).collect(Collectors.toList());
    }

    private double cosineSimilarity(double[] vectorSpace, int[] vector_query) {
        double numerator = 0.0;
        for (int position = 0; position < vectorSpace.length; position++) {
            numerator += (vectorSpace[position] * vector_query[position]);
        }

        double spaceSum = 0.0;
        double querySum = 0.0;
        for (int position = 0; position < vectorSpace.length; position++) {
            spaceSum += Math.pow(vectorSpace[position], 2);
            querySum += Math.pow(vector_query[position], 2);
        }
        double denom = Math.sqrt(spaceSum * querySum);
        if (spaceSum * querySum == 0.0) {
            return 0;
        }
        return numerator / (denom);
    }


}
