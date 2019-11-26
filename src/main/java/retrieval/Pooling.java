package retrieval;

import query.QueryController;

import java.util.ArrayList;

import static retrieval.Models.DOCUMENTSRETURNED;

public class Pooling {
    private Models bm25;
    private Models tfidf;
    private String query;
    private QueryController.ModelTypes currModel;

    public Pooling(String query, QueryController.ModelTypes model) {
        this.bm25 = new BM25();
        this.tfidf = new TfIdf();
        this.query = query;
        this.currModel = model;
    }


    public ArrayList<Similarity> retrieve() {
        ArrayList<Similarity> tfidfResults = this.tfidf.retrieve(this.query);
        ArrayList<Similarity> bm25Results = this.bm25.retrieve(this.query);
        ArrayList<Similarity> returnedResults = new ArrayList<>();
        for (int i = 0; i < DOCUMENTSRETURNED; ) {
            if(this.currModel == QueryController.ModelTypes.Pool || this.currModel == QueryController.ModelTypes.BM25) {
                if (i % 2 == 0 && !returnedResults.contains(bm25Results.get(i))) {
                    returnedResults.add(bm25Results.get(i));
                }
            }
            if(this.currModel == QueryController.ModelTypes.Pool || this.currModel == QueryController.ModelTypes.TFIDF) {
                if (i % 2 == 1 && !returnedResults.contains(tfidfResults.get(i))) {
                    returnedResults.add(tfidfResults.get(i));
                }
            }
            i++;
        }
        return returnedResults;
    }
}
