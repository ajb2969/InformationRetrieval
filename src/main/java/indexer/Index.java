package indexer;

import com.google.common.collect.Maps;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;

public class Index {
    public static String docs_file_path = "documents/";
    public static String output_dir = "indicies/doc-index.tsv";
    public static String docSize = "indicies/doc-size-index.tsv";

    private static void document_level() {
        // Map of Filename -> <word, occurrences in file>
        HashMap<String, Map<String, Integer>> doc_index = new HashMap<>();
        HashMap<String, Integer> totalTokens = new HashMap<>();
        File[] files = new File(docs_file_path).listFiles();
        //brackets, parenthesis, colons, periods, commas, html tags, split on
        // spaces, remove new-line
        for (File f : files) {
            try {
                AtomicInteger sum = new AtomicInteger();
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                Map<String, Integer> wordOccurrences = Maps.newHashMap();
                while ((line = br.readLine()) != null) {
                    //String [] split = line.split("[.\\[\\]!.:\"?,\s]");

                    line = line.replaceAll("<[^>]*>", "");
                    String[] split = line.split(" ");
                    Arrays.stream(split)
                            .filter(token -> !token.isEmpty() || !token.equals(""))
                            .map(token -> token.replaceAll("[.,!?:\\[\\]]", ""))
                            .map(String::toLowerCase)
                            .forEach(token -> {
                                sum.addAndGet(1);
                                addOccurrence(token, wordOccurrences);
                            });
                }
                doc_index.put(f.getName(), wordOccurrences);
                totalTokens.put(f.getName(), sum.intValue());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //map of token -> files it exists in
        HashMap<String, ArrayList<String>> index = new HashMap<>();
        for (String file_name : doc_index.keySet()) {
            for (Entry<String, Integer> wordOccurrenceEntry : doc_index.get(file_name).entrySet()) {
                String token = wordOccurrenceEntry.getKey();
                String filenameWithOccurrences = file_name + ":" + String.valueOf(wordOccurrenceEntry.getValue());
                if (!token.equals("")) {
                    if (index.containsKey(token) && !index.get(token).contains(file_name)) {
                        ArrayList<String> documents = index.get(token);
                        documents.add(filenameWithOccurrences);
                        index.put(token, documents);
                    } else {
                        ArrayList<String> documents = new ArrayList<>();
                        documents.add(filenameWithOccurrences);
                        index.put(token, documents);
                    }
                }
            }
        }

        try {
            write_index(index);
            writeSizeIndex(totalTokens);
        } catch (IOException e) {
            System.err.println("Unable to create index file");
        }
    }

    private static void addOccurrence(String word, Map<String, Integer> wordOccurrences) {
        if (wordOccurrences.containsKey(word)) {
            Integer occurrences = wordOccurrences.get(word);
            wordOccurrences.put(word, occurrences + 1);
        } else {
            wordOccurrences.put(word, 1);
        }
    }

    private static void write_index(HashMap<String, ArrayList<String>> index) throws IOException {
        File output_file = new File(output_dir);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));
        for (String term : index.keySet()) {
            bw.write(term + "\t" + index.get(term).size() + "\t");
            for (String file : index.get(term)) {
                bw.write(file + "\t");
            }
            bw.write("\n");
            bw.flush();
        }
    }

    private static void writeSizeIndex(HashMap<String, Integer> index) throws IOException {
        File output_file = new File(docSize);
        BufferedWriter bw = new BufferedWriter(new FileWriter(output_file));
        for (String file : index.keySet()) {
            bw.write(file + "\t" + index.get(file));
            bw.write("\n");
            bw.flush();
        }
    }




    public static void main(String[] args) {
        document_level();
    }

}
