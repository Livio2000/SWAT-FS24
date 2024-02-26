package ch.hslu.swda.g06.article.repository;

import ch.hslu.swda.g06.article.model.Store;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface IStoreRepository extends MongoRepository<Store, String> {
    @Query("{_id: '?0'}")
    public Store getStoreByStoreId(String id);
}
