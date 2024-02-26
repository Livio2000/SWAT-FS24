package ch.hslu.swda.g06.article.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "articles")
public class Article extends BaseDBObject implements Serializable, Comparable<Article> {
    @Id
    private String articleId;
    private String name;
    private Integer amount;
    private Double price;
    private Integer minimalQuantity;
    private Integer mainWarehouseArticleId;
    private String storeId;

    public Article(String name, Integer amount, Double price, Integer minimalQuantity,
            Integer mainWarehouseArticleId, String storeId) {
        this.articleId = UUID.randomUUID().toString();
        this.name = name;
        this.amount = amount;
        this.price = price;
        this.minimalQuantity = minimalQuantity;
        this.mainWarehouseArticleId = mainWarehouseArticleId;
        this.storeId = storeId;
        this.setCurrentEtag();
    }

    public String getArticleId() {
        return articleId;
    }

    public void setArticleId(String articleId) {
        this.articleId = articleId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getMinimalQuantity() {
        return minimalQuantity;
    }

    public void setMinimalQuantity(Integer minimalQuantity) {
        this.minimalQuantity = minimalQuantity;
    }

    public int getMainWarehouseArticleId() {
        return mainWarehouseArticleId;
    }

    public void setMainWarehouseArticleId(int mainWarehouseArticleId) {
        this.mainWarehouseArticleId = mainWarehouseArticleId;
    }

    public String getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = storeId;
    }

    @Override
    public String toString() {
        return "Article{" +
                "articleId='" + articleId + '\'' +
                ", name='" + name + '\'' +
                ", amount=" + amount +
                ", price=" + price +
                ", minimalQuantity=" + minimalQuantity +
                ", mainWarehouseId=" + mainWarehouseArticleId +
                ", storeId='" + storeId + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Article article = (Article) o;

        return Objects.equals(articleId, article.articleId) &&
                Objects.equals(name, article.name) &&
                Objects.equals(amount, article.amount) &&
                Objects.equals(price, article.price) &&
                Objects.equals(minimalQuantity, article.minimalQuantity) &&
                Objects.equals(mainWarehouseArticleId, article.mainWarehouseArticleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId, name, amount, price, minimalQuantity, mainWarehouseArticleId);
    }

    @Override
    public int compareTo(Article otherArticle) {
        int result = this.name.compareTo(otherArticle.name);
        if (result == 0) {
            result = this.amount.compareTo(otherArticle.amount);
        }
        if (result == 0) {
            result = this.price.compareTo(otherArticle.price);
        }
        if (result == 0) {
            result = this.minimalQuantity.compareTo(otherArticle.minimalQuantity);
        }
        if (result == 0) {
            result = this.mainWarehouseArticleId.compareTo(otherArticle.mainWarehouseArticleId);
        }
        return result;
    }
}