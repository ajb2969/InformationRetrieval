package retrieval;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BM25 extends Models {

    private Map<String, Entry> index;

    public BM25() {
        super();
        this.index = super.get_doc_indicies();
    }

    @Override
    public ArrayList<String> retrieve(String query) {
        System.out.println("Executed BM25");
        ArrayList<String> keywords = Lists.newArrayList(super.extractTerms(query));
        return null;

    }

    private double getScore(List<String> keywords, String filename) {
        double sum = 0;
        sum = keywords.stream()
            .mapToDouble(this::getScore)
            .sum();
        return sum;

    }

    private double getScore(String term) {
        return 0.0;
    }
}
