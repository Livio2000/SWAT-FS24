package ch.hslu.swda.g06.article.factory;

import ch.hslu.swda.g06.article.model.Store;
import ch.hslu.swda.g06.article.repository.IStoreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class StoreFactory {
    @Autowired
    private IStoreRepository storeRepository;

    public boolean storeExistsById(String storeId) {
        if(storeId == null){
            return false;
        }
        Store store = storeRepository.findById(storeId).orElse(null);
        return store != null;
    }
}
