package retrieval;

import java.util.ArrayList;
import java.util.HashMap;

public class BM25 extends Models {
    public BM25() {
        super();
    }

    @Override
    public ArrayList<TfIdf.Similarity> retrieve(String query) {
        System.out.println("Executed BM25");
        HashMap<String, Entry> index = super.get_doc_indicies();
        String[] keywords = super.extractTerms(query);
        return null;
    }
}
