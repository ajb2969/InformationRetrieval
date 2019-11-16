package retrieval;

import java.util.ArrayList;
import java.util.HashMap;

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

    @Override
    public ArrayList<String> retrieve(String query) {
        System.out.println("Executed TfIdf");
        HashMap<String, Entry> index = super.get_doc_indicies();
        System.out.println("Terms in the query are:");
        for(String term: super.extractTerms(query)) {
            termFrequency(term, index.get(term));
            inverseDocumentFrequency(term, index.get(term));
            // map term -> document, term freq, inverse doc freq.
        }


        return null;
    }
}
