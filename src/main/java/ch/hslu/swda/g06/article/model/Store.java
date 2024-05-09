package ch.hslu.swda.g06.article.model;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "stores")
public class Store extends BaseDBObject implements Serializable {
    @Id
    private String storeId;
    private String name;
    private List<Integer> articleIds;

    public Store(String name, List<Integer> articleIds) {
        this.storeId = UUID.randomUUID().toString();
        this.name = name;
        this.articleIds = articleIds;
        setCurrentEtag();
    }

    public Store() {
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getArticleIds() {
        return articleIds;
    }

    public void setArticleIds(List<Integer> articleIds) {
        this.articleIds = articleIds;
    }

    @Override
    public String toString() {
        return "Store{" +
                "storeId='" + storeId + '\'' +
                ", name='" + name + '\'' +
                ", articleList=" + articleIds +
                '}';
    }
}
