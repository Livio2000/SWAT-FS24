package ch.hslu.swda.g06.article.factory;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.model.ReOrder;
import ch.hslu.swda.g06.article.model.ReOrderArticle;
import ch.hslu.swda.g06.article.model.ReOrderState;
import ch.hslu.swda.g06.article.repository.IArticleRepository;
import ch.hslu.swda.g06.article.repository.IReOrderRepository;

@Component
public class ReOrderFactory {
    private final IReOrderRepository reOrderRepository;

    private final IArticleRepository articleRepository;

    private final MainWarehouseFactory mainWarehouseFactory;

    public ReOrderFactory(IReOrderRepository reOrderRepository, IArticleRepository articleRepository, MainWarehouseFactory mainWarehouseFactory) {
        this.reOrderRepository = reOrderRepository;
        this.articleRepository = articleRepository;
        this.mainWarehouseFactory = mainWarehouseFactory;
    }

    public void reOrder(Map<Integer, Integer> articlesToOrder, String forStoreId) {
        ReOrder reOrderToOrder = new ReOrder(
                UUID.randomUUID().toString(),
                ReOrderState.Ordered,
                forStoreId,
                articlesToOrder.entrySet().stream()
                        .map(entry -> new ReOrderArticle(entry.getKey(), entry.getValue()))
                        .collect(Collectors.toList()));

        List<String> warnings = mainWarehouseFactory.checkOrder(reOrderToOrder);
        if (!warnings.isEmpty()) {
            // ToDo: Send log message
            return;
        }

        boolean result = mainWarehouseFactory.createOrder(reOrderToOrder);
        if (!result) {
            // ToDo: Send log message
            return;
        }

        reOrderRepository.save(reOrderToOrder);
    }

    public void processCompletedReOrder(ReOrder reOrder) {
        if (reOrder.getReOrderState() == ReOrderState.Complete) {
            return;
        }
        boolean setOrderToComplete = true;
        for (ReOrderArticle reOrderArticle : reOrder.getReOrderItems()) {
            Article article = articleRepository.getArticleByMainWarehouseArticleIdAndStoreId(
                    reOrderArticle.getMainWarehouseArticleId(), reOrder.getStoreId());
            if (article == null) {
                setOrderToComplete = false;
            }

            article.setAmount(article.getAmount() + reOrderArticle.getAmount());
            article.setCurrentEtag();
            articleRepository.save(article);
        }

        if (setOrderToComplete) {
            reOrder.setReOrderState(ReOrderState.Complete);
            reOrder.setCurrentEtag();
            reOrderRepository.save(reOrder);
        }
    }
}
