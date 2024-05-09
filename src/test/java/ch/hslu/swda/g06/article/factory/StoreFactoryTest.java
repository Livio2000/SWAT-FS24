package ch.hslu.swda.g06.article.factory;

import ch.hslu.swda.g06.article.model.Store;
import ch.hslu.swda.g06.article.repository.IStoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class StoreFactoryTest {
    private StoreFactory storeFactory;
    private IStoreRepository storeRepository;

    @BeforeEach
    public void setUp() {
        storeRepository = mock(IStoreRepository.class);
        storeFactory = new StoreFactory(storeRepository);
    }

    @Test
    void storeExistsById_NullStoreId() {
        assertFalse(storeFactory.storeExistsById(null));
    }

    @Test
    void storeExistsById_FalseStoreId() {
        assertFalse(storeFactory.storeExistsById("store123"));
    }

    @Test
    void storeExistsById_StoreExists() {
        String storeId = "store123";
        Store store = new Store(storeId, new ArrayList<>());
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        boolean exists = storeFactory.storeExistsById(storeId);

        assertTrue(exists);
    }
}
