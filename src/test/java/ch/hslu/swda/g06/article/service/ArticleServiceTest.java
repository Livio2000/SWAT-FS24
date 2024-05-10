package ch.hslu.swda.g06.article.service;

import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.repository.IArticleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ArticleServiceTest {
    private ArticleService articleService;
    private IArticleRepository articleRepository;

    @BeforeEach
    public void setUp() {
        articleRepository = mock(IArticleRepository.class);
        articleService = new ArticleService(articleRepository);
    }

    @Test
    void articleExistsInStoreById_NullWarehouseId_NullStoreId() {
        ArticleService articleService = new ArticleService(null);
        assertFalse(articleService.articleExistsInStoreById(null, null));
    }

    @Test
    void articleExistsInStoreById_NullWarehouseId() {
        boolean exists = articleService.articleExistsInStoreById(null, "store123");

        assertFalse(exists);
    }

    @Test
    void articleExistsInStoreById_NullStoreId() {
        boolean exists = articleService.articleExistsInStoreById(1, null);

        assertFalse(exists);
    }

    @Test
    void articleExistsInStoreById_BothParametersNotNull_ArticleExists() {
        Integer mainWarehouseArticleId = 1;
        String storeId = "store123";
        Article mockArticle = new Article("test", 1, 1.0, 1, mainWarehouseArticleId, "store123");
        when(articleRepository.getArticleByMainWarehouseArticleIdAndStoreId(mainWarehouseArticleId, storeId)).thenReturn(mockArticle);

        boolean exists = articleService.articleExistsInStoreById(mainWarehouseArticleId, storeId);

        assertTrue(exists);
    }

    @Test
    void articleExistsInStoreById_BothParametersNotNull_ArticleDoesNotExist() {
        Integer mainWarehouseArticleId = 2;
        String storeId = "store456";
        when(articleRepository.getArticleByMainWarehouseArticleIdAndStoreId(mainWarehouseArticleId, storeId)).thenReturn(null);

        boolean exists = articleService.articleExistsInStoreById(mainWarehouseArticleId, storeId);

        assertFalse(exists);
    }
}