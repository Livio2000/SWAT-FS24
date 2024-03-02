package ch.hslu.swda.g06.article.factory;

import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.repository.IArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArticleFactoryTest {
    private ArticleFactory articleFactory;
    private IArticleRepository articleRepository;

    @BeforeEach
    public void setUp() {
        articleRepository = mock(IArticleRepository.class);
        articleFactory = new ArticleFactory(articleRepository);
    }

    @Test
    void articleExistsInStoreById_NullWarehouseId_NullStoreId() {
        ArticleFactory articleFactory = new ArticleFactory(null);
        assertFalse(articleFactory.articleExistsInStoreById(null, null));
    }

    @Test
    void articleExistsInStoreById_NullWarehouseId() {
        boolean exists = articleFactory.articleExistsInStoreById(null, "store123");

        assertFalse(exists);
    }

    @Test
    void articleExistsInStoreById_NullStoreId() {
        boolean exists = articleFactory.articleExistsInStoreById(1, null);

        assertFalse(exists);
    }

    @Test
    void articleExistsInStoreById_BothParametersNotNull_ArticleExists() {
        Integer mainWarehouseArticleId = 1;
        String storeId = "store123";
        Article mockArticle = new Article("test", 1, 1.0, 1, mainWarehouseArticleId, "store123");
        when(articleRepository.getArticleByMainWarehouseArticleIdAndStoreId(mainWarehouseArticleId, storeId)).thenReturn(mockArticle);

        boolean exists = articleFactory.articleExistsInStoreById(mainWarehouseArticleId, storeId);

        assertTrue(exists);
    }

    @Test
    void articleExistsInStoreById_BothParametersNotNull_ArticleDoesNotExist() {
        Integer mainWarehouseArticleId = 2;
        String storeId = "store456";
        when(articleRepository.getArticleByMainWarehouseArticleIdAndStoreId(mainWarehouseArticleId, storeId)).thenReturn(null);

        boolean exists = articleFactory.articleExistsInStoreById(mainWarehouseArticleId, storeId);

        assertFalse(exists);
    }
}