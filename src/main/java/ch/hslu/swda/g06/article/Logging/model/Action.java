package ch.hslu.swda.g06.article.Logging.model;

public class Action {
    private String action;
    private String entityName;
    private String entityId;

    public Action(String action, String entityName, String entityId) {
        this.action = action;
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }
}
