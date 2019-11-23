package query;

import java.util.ArrayList;

public class Querycontainer {
    public String content;
    public ArrayList<Integer> selectedSeason;

    Querycontainer() {
        this.content = "";
        this.selectedSeason = new ArrayList<>();
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public ArrayList<Integer> getSelectedSeason() {
        return selectedSeason;
    }

    public void addSeason(Integer season) {
        this.selectedSeason.add(season);
    }

    public void setSelectedSeason(ArrayList<Integer> selectedModel) {
        this.selectedSeason = selectedModel;
    }
}
