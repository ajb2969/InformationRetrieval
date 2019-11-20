package retrieval;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

public class TfIdf extends Models {
    final File f = new File("indicies/vectorspace.txt");
    public TfIdf() {
        super();
    }

    private static double termFrequency(String term,
                                        String document) {
        int totalTerms = getFileTermSize().get(document);
        int occurrences =
                get_doc_indicies().get(term).getFileOccurrences().stream()
                        .filter(e -> e.getFilename().equals(document))
                        .findFirst()
                        .orElse(new FileOccurrence("", 0))
                        .getOccurrences();

        return (double) occurrences / totalTerms;
    }

    private static double inverseDocumentFrequency(String term) {
        int totalDocuments = fileTermSize.keySet().size();
        int documentsWithTerm = documents.get(term).getSize();
        return Math.log((double) totalDocuments / documentsWithTerm);
    }

    public static double tfidf(String term, String document) {
        return occursInDocuments(term) ?
                termFrequency(term, document) * inverseDocumentFrequency(term) : 0;
    }

    private double[][] documentVectorizor(HashMap<String, Entry> index,
                                          String document,
                                          double[][] vectorSpace,
                                          int i, int j) {
        ArrayList<String> terms = new ArrayList<>(index.keySet());
        Collections.sort(terms);
        double denominator = 0;
        for (String term : terms) {
            double numerator =
                    occursInDocuments(term)? (Math.log(termFrequency(term, document) + 1)) * inverseDocumentFrequency(term) : 0;
            denominator += Math.pow(numerator, 2);
        }
        denominator = Math.sqrt(denominator);

        for (String term : terms) {
            double numerator =
                    occursInDocuments(term)? (Math.log(termFrequency(term, document) + 1)) * inverseDocumentFrequency(term) : 0;
            vectorSpace[i][j] = numerator / denominator;
            j += 1;
        }

        return vectorSpace;
    }

    @Override
    public ArrayList<Similarity> retrieve(String query) {
        HashMap<String, Entry> index = super.get_doc_indicies();
        ArrayList<String> documents = super.getDocumentList();

        double[][] vectorSpace = new double[documents.size()][index.size()];
        if(!f.exists()) {
            // space is documents x terms in index
            int i = 0;
            //go through each document
            for (String document : documents) {
                int j = 0;
                //go through every element in the index, columns
                vectorSpace = documentVectorizor(index, document, vectorSpace, i,
                        j);
                i += 1;
            }

            writeMatrix(vectorSpace);
        } else {
            try {
                vectorSpace = readMatrix();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        query = query.toLowerCase();
        double[] vector_query = new double[index.size()];
        int position = 0;
        double denominator = 0;
        ArrayList<String> terms = new ArrayList<>(index.keySet());
        Collections.sort(terms);
        for (String term : terms) {
            double numerator = query.contains(term)? (Math.log(termQueryOccur(term, query) + 1) * inverseDocumentFrequency(term)) : 0;
            denominator += Math.pow(numerator, 2);
        }
        denominator = Math.sqrt(denominator);

        for (String term : terms) {
            double numerator = query.contains(term)? (Math.log(termQueryOccur(term, query) + 1) * inverseDocumentFrequency(term)) : 0;
            vector_query[position] = numerator / denominator;
            position += 1;
        }

        ArrayList<Similarity> docSimilarities = new ArrayList<>();
        for (int i = 0; i < vectorSpace.length; i++) {
            docSimilarities.add(new Similarity(documents.get(i),
                    cosineSimilarity(vectorSpace[i], vector_query), 0));
        }
        Collections.sort(docSimilarities);

        return (ArrayList<Similarity>) docSimilarities.stream().filter(e -> !Double.isNaN(e.getSimilarity())).limit(Models.DOCUMENTSRETURNED).collect(Collectors.toList());
    }

    private double termQueryOccur(String term, String query) {
        double count = 0;
        for(String s: extractTerms(query)) {
            if(term.toLowerCase().equals(s.toLowerCase())) {
                count += 1;
            }
        }
        return count;
    }


    private void writeMatrix(double[][] vectorspace) {
        if (!f.exists()) {
            try {
                ObjectOutputStream bw =
                        new ObjectOutputStream(new FileOutputStream(f));
                bw.writeObject(vectorspace);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double[][] readMatrix() throws IOException {
        if (f.exists()) {
            try {
                ObjectInputStream bw =
                        new ObjectInputStream(new FileInputStream(f));
                return (double[][]) bw.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            throw new FileNotFoundException(f.getCanonicalPath() + " was not " +
                    "found");
        }

        return null;
    }

    private double cosineSimilarity(double[] vectorSpace, double[] vector_query) {
        double numerator = 0;
        for (int position = 0; position < vectorSpace.length; position++) {
            numerator += (vectorSpace[position] * vector_query[position]);
        }

        double spaceSum = 0;
        double querySum = 0;
        for (int position = 0; position < vectorSpace.length; position++) {
            spaceSum += Math.pow(vectorSpace[position], 2);
            querySum += Math.pow(vector_query[position], 2);
        }
        double denom = Math.sqrt(spaceSum * querySum);
        if (spaceSum * querySum == 0) {
            return 0;
        }
        return numerator / (denom);
    }


}
