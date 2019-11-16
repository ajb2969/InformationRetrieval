package retrieval;

import java.util.ArrayList;
import java.util.HashMap;

public class TfIdf extends Models {
    public TfIdf() {
        super();
    }

    @Override
    public ArrayList<String> retrieve(String query) {
        System.out.println("Executed TfIdf");
        HashMap<String, Entry> index = super.get_doc_indicies();
        return null;
    }
}
