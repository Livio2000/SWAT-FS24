package ch.hslu.swda.g06.article.model;

import java.io.Serializable;

public class OrderArticleDto implements Serializable {
    private String articleId;
    private float unitPrice;
    private int amount;

    public OrderArticleDto(final String articleId, final float unitPrice, final int amount) {
        this.articleId = articleId;
        this.unitPrice = unitPrice;
        this.amount = amount;
    }

    public String getArticleId() {
        return this.articleId;
    }

    public float getUnitPrice() {
        return this.unitPrice;
    }

    public int getAmount() {
        return this.amount;
    }
}