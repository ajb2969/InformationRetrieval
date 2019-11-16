package retrieval;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

abstract public class Models {
    final String documents_path = "documents/";
    private ArrayList<String> documents;

    Models() {
        this.documents = parse_documents();
    }

    public ArrayList<String> getDocuments() {
        return documents;
    }

    public abstract ArrayList<Integer> retrieve(String query);

    public ArrayList<String> parse_documents() {
        ArrayList<String> documents = new ArrayList<>();
        File[] files = new File(this.documents_path).listFiles();
        for (File f : files) {
            try {
                BufferedReader br = new BufferedReader(new FileReader(f));
                String line;
                String document = "";
                while ((line = br.readLine()) != null) {
                    document += line;
                }
                documents.add(document);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return documents;
    }
}
