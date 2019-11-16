package query;

public class Querycontainer {
    public String content;
    public String selectedModel;

    Querycontainer() {
        this.content = "";
        this.selectedModel = "";
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSelectedModel() {
        return selectedModel;
    }

    public void setSelectedModel(String selectedModel) {
        this.selectedModel = selectedModel;
    }
}
