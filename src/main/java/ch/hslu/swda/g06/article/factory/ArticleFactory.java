package ch.hslu.swda.g06.article.factory;

import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.repository.IArticleRepository;
import org.springframework.stereotype.Component;

@Component
public class ArticleFactory {
    private final IArticleRepository articleRepository;

    public ArticleFactory(IArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    public boolean articleExistsInStoreById(Integer mainWarehouseArticleId, String storeId) {
        if(mainWarehouseArticleId == null){
            return false;
        }
        if(storeId == null){
            return false;
        }
        Article article = articleRepository.getArticleByMainWarehouseArticleIdAndStoreId(mainWarehouseArticleId, storeId);
        return article != null;
    }
}
