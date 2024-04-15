package ch.hslu.swda.g06.article.logging.model;

public class Action {
    private final String operation;
    private final String entityName;
    private final String entityId;

    private Action(ActionBuilder actionBuilder) {
        this.operation = actionBuilder.operation;
        this.entityName = actionBuilder.entityName;
        this.entityId = actionBuilder.entityId;
    }

    public String getOperation() {
        return operation;
    }

    public String getEntityName() {
        return entityName;
    }

    public String getEntityId() {
        return entityId;
    }

    public static class ActionBuilder {
        private String operation;
        private String entityName;
        private String entityId;


        public ActionBuilder setOperation(String operation) {
            this.operation = operation;
            return this;
        }

        public ActionBuilder setEntityName(Object entity) {
            this.entityName = entity.getClass().getName();
            return this;
        }

        public ActionBuilder setEntityId(String entityId) {
            this.entityId = entityId;
            return this;
        }

        public Action build(){
            return new Action(this);
        }
    }
}
