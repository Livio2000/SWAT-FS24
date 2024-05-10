package ch.hslu.swda.g06.article.service;

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

class StoreServiceTest {
    private StoreService storeService;
    private IStoreRepository storeRepository;

    @BeforeEach
    public void setUp() {
        storeRepository = mock(IStoreRepository.class);
        storeService = new StoreService(storeRepository);
    }

    @Test
    void storeExistsById_NullStoreId() {
        assertFalse(storeService.storeExistsById(null));
    }

    @Test
    void storeExistsById_FalseStoreId() {
        assertFalse(storeService.storeExistsById("store123"));
    }

    @Test
    void storeExistsById_StoreExists() {
        String storeId = "store123";
        Store store = new Store(storeId, new ArrayList<>());
        when(storeRepository.findById(storeId)).thenReturn(Optional.of(store));

        boolean exists = storeService.storeExistsById(storeId);

        assertTrue(exists);
    }
}
