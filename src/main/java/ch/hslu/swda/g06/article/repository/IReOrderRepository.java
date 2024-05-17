package ch.hslu.swda.g06.article.repository;

import ch.hslu.swda.g06.article.model.ReOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IReOrderRepository extends MongoRepository<ReOrder, String> {
    @Query("{storeId: '?0'}")
    List<ReOrder> getReOrdersByStoreID(String storeId);
}
