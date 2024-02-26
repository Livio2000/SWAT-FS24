package ch.hslu.swda.g06.article.model;

public class DeleteArticleDto {
    private String articleId;
    private long eTag;

    public DeleteArticleDto(String articleId, long eTag) {
        this.articleId = articleId;
        this.eTag = eTag;
    }

    public String getArticleId() {
        return articleId;
    }

    public long getETag() {
        return eTag;
    }
}
