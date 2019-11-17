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

    private double termFrequency(String term, Entry index) {

        return 0.0;
    }

    private double inverseDocumentFrequency(String term, Entry index) {

        return 0.0;
    }

    public double tfidf(String term, Entry index) {
        return termFrequency(term, index) * inverseDocumentFrequency(term, index);
    }

    private int[][] documentVectorizor(HashMap<String, Entry> index,
                                       String document, int[][] vectorSpace,
                                       int i, int j) {
        for (String term : index.keySet()) {
            ArrayList<FileOccurrence> file_occur =
                    index.get(term).getFileOccurrences();
            boolean skip = false;
            for (FileOccurrence fo : file_occur) {
                if (fo.getFilename().equals(document)) {
                    vectorSpace[i][j] = fo.getOccurrences();
                    skip = true;
                    break;
                }
            }
            if (!skip) {
                vectorSpace[i][j] = 0;
            }
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
        int[][] vectorSpace = new int[documents.size()][index.size()];
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
            docSimilarities.add(new Similarity(documents.get(i), cosineSimilarity(vectorSpace[i], vector_query)));
        }
        Collections.sort(docSimilarities);

        return (ArrayList<Similarity>) docSimilarities.stream().limit(15).collect(Collectors.toList());
    }

    private double cosineSimilarity(int[] vectorSpace, int[] vector_query) {
        double numerator = 0.0;
        for(int position = 0; position < vectorSpace.length; position++) {
            numerator += (vectorSpace[position] * vector_query[position]);
        }

        double spaceSum = 0.0;
        double querySum = 0.0;
        for(int position = 0; position < vectorSpace.length; position++) {
            spaceSum += Math.pow(vectorSpace[position], 2);
            querySum += Math.pow(vector_query[position], 2);
        }
        double denom = Math.sqrt(spaceSum * querySum);
        if(spaceSum * querySum == 0.0) {
            return 0;
        }
        return numerator/(denom);
    }


    public class Similarity implements Comparable<Similarity> {
        private String document_name;
        private String preview;
        private double similarity;

        Similarity(String document, double similarity) {
            this.document_name = document;
            this.similarity = similarity;
            this.preview = "";
        }

        public String getDocument_name() {
            return document_name;
        }

        public double getSimilarity() {
            return similarity;
        }

        @Override
        public int compareTo(Similarity o) {
            return Double.compare(this.getSimilarity(), o.getSimilarity());
        }

        public String getPreview() {
            return preview;
        }

        public void setPreview(String preview) {
            this.preview = preview;
        }
    }
}
