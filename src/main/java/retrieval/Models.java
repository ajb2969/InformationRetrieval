package retrieval;

import indexer.Index;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;

abstract public class Models {
    private static final String indicies_path = Index.output_dir;
    private HashMap<String, Entry> documents;

    Models() {
        try {
            this.documents = parse_doc_indicies();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public HashMap<String, Entry> get_doc_indicies() {
        return documents;
    }

    public abstract ArrayList<String> retrieve(String query);

    public HashMap<String, Entry> parse_doc_indicies() throws IOException {
        //TODO update for parsing document-level TSV, will need to be
        // over-written for other indicies (i.e. window)
        HashMap<String, Entry> terms = new HashMap<>();
        File index = new File(indicies_path);
        BufferedReader br = new BufferedReader(new FileReader(index));
        String line;

        while((line = br.readLine()) != null) {
            String [] parsed_line = line.split("\t");
            String term = parsed_line[0];
            int quantity = Integer.parseInt(parsed_line[1]);
            ArrayList<String> documents = new ArrayList<>();
            for(int i = 2; i < parsed_line.length; i++) {
                documents.add(parsed_line[i]);
            }
            terms.put(term, new Entry(quantity, documents));
        }

        return terms;
    }

    class Entry {
        private int size;
        private ArrayList<String> documents;

        Entry(int size, ArrayList<String> documents) {
            this.size = size;
            this.documents = documents;
        }


        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public ArrayList<String> getDocuments() {
            return documents;
        }

        public void setDocuments(ArrayList<String> documents) {
            this.documents = documents;
        }
    }
}
