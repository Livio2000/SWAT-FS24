package ch.hslu.swda.g06.article.model;

public class BaseDBObject {
    private long eTag;

    public BaseDBObject() {
    }

    public long getEtag() {
        return this.eTag;
    }

    public void setCurrentEtag() {
        this.eTag = System.currentTimeMillis();
    }

    public boolean canEdit(long eTag) {
        return eTag == this.eTag;
    }
}
