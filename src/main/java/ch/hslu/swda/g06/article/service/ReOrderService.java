package ch.hslu.swda.g06.article.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.hslu.swda.g06.article.logging.SendLog;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.model.ReOrder;
import ch.hslu.swda.g06.article.model.ReOrderArticle;
import ch.hslu.swda.g06.article.model.ReOrderState;
import ch.hslu.swda.g06.article.repository.IArticleRepository;
import ch.hslu.swda.g06.article.repository.IReOrderRepository;

@Component
public class ReOrderService {
    private final IReOrderRepository reOrderRepository;

    private final IArticleRepository articleRepository;

    private final MainWarehouseService mainWarehouseService;

    private final SendLog logger;

    public ReOrderService(IReOrderRepository reOrderRepository,
                          IArticleRepository articleRepository,
                          MainWarehouseService mainWarehouseService,
                          SendLog sendLog) {
        this.reOrderRepository = reOrderRepository;
        this.articleRepository = articleRepository;
        this.mainWarehouseService = mainWarehouseService;
        this.logger = sendLog;
    }

    public void reOrder(Map<Integer, Integer> articlesToOrder, String forStoreId, MessageProperties properties) {
        ReOrder reOrderToOrder = new ReOrder(
                UUID.randomUUID().toString(),
                ReOrderState.ORDERED,
                forStoreId,
                articlesToOrder.entrySet().stream()
                        .map(entry -> new ReOrderArticle(entry.getKey(), entry.getValue()))
                        .toList());

        List<String> warnings = mainWarehouseService.checkOrder(reOrderToOrder);
        if (!warnings.isEmpty()) {
            logger.sendReOrderErrorLog(
                    reOrderToOrder,
                    String.join("\n", warnings),
                    properties.getCorrelationId());
            return;
        }

        boolean result = mainWarehouseService.createOrder(reOrderToOrder);
        if (!result) {
            logger.sendReOrderErrorLog(
                    reOrderToOrder,
                    "There was an error while placing the reorder.",
                    properties.getCorrelationId()
            );
            return;
        }

        reOrderRepository.save(reOrderToOrder);
    }

    public void processCompletedReOrder(ReOrder reOrder) {
        if (reOrder.getReOrderState() == ReOrderState.COMPLETE) {
            return;
        }

        if (updateAmountOfReOrderArticles(reOrder)) {
            reOrder.setReOrderState(ReOrderState.COMPLETE);
            reOrder.setCurrentEtag();
            reOrderRepository.save(reOrder);
        }
    }

    public boolean updateAmountOfReOrderArticles(ReOrder reOrder){
        boolean setOrderToComplete = true;

        for (ReOrderArticle reOrderArticle : reOrder.getReOrderItems()) {
            Article article = articleRepository.getArticleByMainWarehouseArticleIdAndStoreId(
                    reOrderArticle.getMainWarehouseArticleId(), reOrder.getStoreId());
            if (article == null) {
                setOrderToComplete = false;
                continue;
            }

            article.setAmount(article.getAmount() + reOrderArticle.getAmount());
            article.setCurrentEtag();
            articleRepository.save(article);
        }

        return setOrderToComplete;
    }
}
