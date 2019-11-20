package retrieval;

public class Similarity implements Comparable<Similarity> {
    private String document_name;
    private String preview;
    private int season;
    private double similarity;

    Similarity(String document, double similarity, int season) {
        this.document_name = document;
        this.similarity = similarity;
        this.preview = "";
    }

    public String getDocument_name() {
        return document_name.replace(".txt", "");
    }

    public String getDocumentLink() {
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

    public int getSeason() {
        return season;
    }

    public void setSeason(int season) {
        this.season = season;
    }
}
