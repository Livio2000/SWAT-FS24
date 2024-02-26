package ch.hslu.swda.g06.article.repository;

import ch.hslu.swda.g06.article.model.Article;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IArticleRepository extends MongoRepository<Article, String> {
    @Query("{_id: '?0'}")
    public Article getArticleByArticleID(String id);

    @Query("{'mainWarehouseArticleId': ?0, 'storeId': ?1}")
    public Article getArticleByMainWarehouseArticleIdAndStoreId(Integer mainWarehouseArticleId, String storeId);

    @Query("{storeId: '?0'}")
    public List<Article> getArticlesBystoreID(String storeId);
}