package ch.hslu.swda.g06.article.repository;

import ch.hslu.swda.g06.article.model.Store;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IStoreRepository extends MongoRepository<Store, String> {
}
