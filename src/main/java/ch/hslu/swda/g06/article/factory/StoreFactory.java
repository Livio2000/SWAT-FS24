package ch.hslu.swda.g06.article.factory;

import ch.hslu.swda.g06.article.model.Store;
import ch.hslu.swda.g06.article.repository.IStoreRepository;
import org.springframework.stereotype.Component;

@Component
public class StoreFactory {
    private final IStoreRepository storeRepository;

    public StoreFactory(IStoreRepository storeRepository){
        this.storeRepository = storeRepository;
    }

    public boolean storeExistsById(String storeId) {
        if(storeId == null){
            return false;
        }
        Store store = storeRepository.findById(storeId).orElse(null);
        return store != null;
    }
}
