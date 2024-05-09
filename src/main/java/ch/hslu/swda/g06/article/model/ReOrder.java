package ch.hslu.swda.g06.article.model;

import java.io.Serializable;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "reorders")
public class ReOrder extends BaseDBObject implements Serializable {
    @Id
    private String reOrderId;
    private ReOrderState reOrderState;
    private String storeId;
    private List<ReOrderArticle> reOrderItems;

    public ReOrder(String reOrderId, ReOrderState reOrderState, String storeId, List<ReOrderArticle> reOrderItems) {
        this.reOrderId = reOrderId;
        this.reOrderState = reOrderState;
        this.storeId = storeId;
        this.reOrderItems = reOrderItems;
        this.setCurrentEtag();
    }

    public ReOrder() {
    }

    public String getReOrderId() {
        return reOrderId;
    }

    public void setReOrderId(String reOrderId) {
        this.reOrderId = reOrderId;
    }

    public ReOrderState getReOrderState() {
        return reOrderState;
    }

    public void setReOrderState(ReOrderState reOrderState) {
        this.reOrderState = reOrderState;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public List<ReOrderArticle> getReOrderItems() {
        return reOrderItems;
    }

    public void setReOrderItems(List<ReOrderArticle> reOrderItems) {
        this.reOrderItems = reOrderItems;
    }
}
