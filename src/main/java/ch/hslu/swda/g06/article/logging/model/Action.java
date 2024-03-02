package ch.hslu.swda.g06.article.logging.model;

public class Action {
    private String operation;
    private String entityName;
    private String entityId;

    public Action(String operation, String entityName, String entityId) {
        this.operation = operation;
        this.entityName = entityName;
        this.entityId = entityId;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
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
