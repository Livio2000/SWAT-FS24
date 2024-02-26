package ch.hslu.swda.g06.article.model;

public class ReOrderArticle {
    private Integer mainWarehouseArticleId;
    private Integer amount;

    public ReOrderArticle(Integer mainWarehouseArticleId, Integer amount){
        this.mainWarehouseArticleId = mainWarehouseArticleId;
        this.amount = amount;
    }

    public Integer getMainWarehouseArticleId() {
        return mainWarehouseArticleId;
    }

    public void setMainWarehouseArticleId(Integer mainWarehouseArticleId) {
        this.mainWarehouseArticleId = mainWarehouseArticleId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
