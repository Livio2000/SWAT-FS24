package ch.hslu.swda.g06.article.factory;

import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.repository.IArticleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ArticleFactory {
    @Autowired
    private IArticleRepository articleRepository;

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
