package ch.hslu.swda.g06.article.service;

import ch.hslu.swda.g06.article.model.Store;
import ch.hslu.swda.g06.article.repository.IStoreRepository;
import org.springframework.stereotype.Component;

@Component
public class StoreService {
    private final IStoreRepository storeRepository;

    public StoreService(IStoreRepository storeRepository){
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
