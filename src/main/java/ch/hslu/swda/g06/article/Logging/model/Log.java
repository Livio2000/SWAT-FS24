package ch.hslu.swda.g06.article.Logging.model;

public class Log {
    private Action action;
    private String storeId;

    public Log(final Action action, String storeId) {
        this.action = action;
        this.storeId = storeId;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }
}
