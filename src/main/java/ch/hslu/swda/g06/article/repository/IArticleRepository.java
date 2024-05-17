package ch.hslu.swda.g06.article.repository;

import ch.hslu.swda.g06.article.model.Article;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IArticleRepository extends MongoRepository<Article, String> {
    @Query("{'mainWarehouseArticleId': ?0, 'storeId': ?1}")
    Article getArticleByMainWarehouseArticleIdAndStoreId(Integer mainWarehouseArticleId, String storeId);

    @Query("{storeId: '?0'}")
    List<Article> getArticlesBystoreID(String storeId);
}