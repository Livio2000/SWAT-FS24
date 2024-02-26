package ch.hslu.swda.g06.article.factory;

import ch.hslu.swda.g06.article.model.ReOrder;
import ch.hslu.swda.g06.article.model.ReOrderArticle;
import ch.hslu.swda.stock.api.*;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Component
public class MainWarehouseFactory {
    private final Map<Integer, Integer> stockMap = new HashMap();
    private Stock MAINSTOCK = StockFactory.getStock();

    public MainWarehouseFactory() {
        /*
        this creates 100 random products and add's them to the stockMap.
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

    public int createArticle(int articleId) {
        int stock = MAINSTOCK.getItemCount(articleId);
        this.stockMap.put(articleId, stock);
        return articleId;
    }

    public List<String> checkOrder(ReOrder reOrder) {
        List<String> warnings = new ArrayList<String>();
        for (ReOrderArticle article : reOrder.getReOrderItems())
        {
            if(MAINSTOCK.getItemCount(article.getMainWarehouseArticleId()) < article.getAmount()){
                warnings.add("Artikel " + article.getMainWarehouseArticleId() + " kann nicht nachbestellt werden da im Zentrallager nurnoch " + MAINSTOCK.getItemCount(article.getMainWarehouseArticleId()) + " Stück verfügbar sind");
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
        int remainingStock = MAINSTOCK.getItemCount(articleId) - amount;
        int stock = MAINSTOCK.orderItem(articleId, remainingStock);
        this.stockMap.put(articleId, stock);
        return stock > 0;
    }

    private static int generateRandomNumber() {
        return ThreadLocalRandom.current().nextInt(100000, 1000000);
    }
}
