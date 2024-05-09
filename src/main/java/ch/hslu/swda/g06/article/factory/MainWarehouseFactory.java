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

    /*
    * Creates 100 products with the id 100000-100100 and adds them to the stockMap.
    * This simulates a main warehouse where the stores can get their products.
    */
    public MainWarehouseFactory() {
        for (int i = 100000; i < 100100; i++) {
            createArticle(i);
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
        if(stock < 0) {
            return false;
        }

        this.stockMap.put(articleId, stock);
        return stock > 0;
    }

    private static int generateRandomNumber() {
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}
