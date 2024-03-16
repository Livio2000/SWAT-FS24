package ch.hslu.swda.g06.article.factory;

import ch.hslu.swda.g06.article.model.ReOrder;
import ch.hslu.swda.g06.article.model.ReOrderArticle;
import ch.hslu.swda.stock.api.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MainWarehouseFactory {
    private final Map<Integer, Integer> stockMap = new HashMap<>();
    private final Stock mainStock = StockFactory.getStock();

    public MainWarehouseFactory() {
        /*
        this creates 100 random products and adds them to the stockMap.
        this simulates a main warehouse where the stores can get their products.
        */
        Set<Integer> articleIds = new HashSet<>();
        for (int i = 0; i < 100; i++){
            articleIds.add(generateRandomNumber());
        }

        for(Integer randomArticleId : articleIds) {
            createArticle(randomArticleId);
        }
    }

    public Map<Integer, Integer> getStockMap(){
        return this.stockMap;
    }

    public void createArticle(int articleId) {
        int stock = mainStock.getItemCount(articleId);
        this.stockMap.put(articleId, stock);
    }

    public List<String> checkOrder(ReOrder reOrder) {
        List<String> warnings = new ArrayList<>();
        for (ReOrderArticle article : reOrder.getReOrderItems())
        {
            if(mainStock.getItemCount(article.getMainWarehouseArticleId()) < article.getAmount()){
                warnings.add("Artikel " + article.getMainWarehouseArticleId() + " kann nicht nachbestellt werden da im Zentrallager nurnoch " + mainStock.getItemCount(article.getMainWarehouseArticleId()) + " Stück verfügbar sind");
            }
        }
        return warnings;
    }

    public boolean createOrder(ReOrder reOrder) {
        boolean result = true;
        for (ReOrderArticle article : reOrder.getReOrderItems())
        {
            if(!orderItem(article.getMainWarehouseArticleId(), article.getAmount())){
                result = false;
            }
        }
        return result;
    }

    private boolean orderItem(int articleId, int amount) {
        int remainingStock = mainStock.getItemCount(articleId) - amount;
        int stock = mainStock.orderItem(articleId, remainingStock);
        this.stockMap.put(articleId, stock);
        return stock > 0;
    }

    private static int generateRandomNumber() {
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}
