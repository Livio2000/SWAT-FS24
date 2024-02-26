package ch.hslu.swda.g06.article.repository;

import ch.hslu.swda.g06.article.model.ReOrder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface IReOrderRepository extends MongoRepository<ReOrder, String> {
    @Query("{_id: '?0'}")
    public ReOrder getReOrderByReOrderId(String id);

    @Query("{storeId: '?0'}")
    public List<ReOrder> getReOrdersByStoreID(String storeId);
}
