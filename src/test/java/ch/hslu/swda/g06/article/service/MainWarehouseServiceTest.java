package ch.hslu.swda.g06.article.service;

import ch.hslu.swda.g06.article.model.ReOrder;
import ch.hslu.swda.g06.article.model.ReOrderArticle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MainWarehouseServiceTest {
    private MainWarehouseService mainWarehouseService;

    @BeforeEach
    public void setUp() {
        mainWarehouseService = new MainWarehouseService();
    }

    @Test
    void mainWarehouseFactoryConstructor(){
        assertEquals(100, mainWarehouseService.getStockMap().size());
    }

    @Test
    void testCreateOrderAllArticlesCanBeOrdered() {
        ReOrder reOrder = mock(ReOrder.class);
        List<ReOrderArticle> reOrderItems = new ArrayList<>();
        ReOrderArticle reOrderArticle = mock(ReOrderArticle.class);
        var someArticle = mainWarehouseService.getStockMap().keySet().iterator().next();
        when(reOrderArticle.getMainWarehouseArticleId()).thenReturn(someArticle);
        when(reOrderArticle.getAmount()).thenReturn(50);
        reOrderItems.add(reOrderArticle);
        when(reOrder.getReOrderItems()).thenReturn(reOrderItems);

        assertTrue(mainWarehouseService.createOrder(reOrder));
    }

    @Test
    void testCreateOrderNotAllArticlesCanBeOrdered() {
        ReOrder reOrder = mock(ReOrder.class);
        List<ReOrderArticle> reOrderItems = new ArrayList<>();
        ReOrderArticle reOrderArticle = mock(ReOrderArticle.class);
        var someArticle = mainWarehouseService.getStockMap().keySet().iterator().next();
        when(reOrderArticle.getMainWarehouseArticleId()).thenReturn(someArticle);
        when(reOrderArticle.getAmount()).thenReturn(5000); // This amount is too large
        reOrderItems.add(reOrderArticle);
        when(reOrder.getReOrderItems()).thenReturn(reOrderItems);

        assertFalse(mainWarehouseService.createOrder(reOrder));
    }

    @Test
    void testCheckOrderAllArticlesCanBeOrdered() {
        ReOrder reOrder = mock(ReOrder.class);
        List<ReOrderArticle> reOrderItems = new ArrayList<>();
        ReOrderArticle reOrderArticle = mock(ReOrderArticle.class);
        var someArticle = mainWarehouseService.getStockMap().keySet().iterator().next();
        when(reOrderArticle.getMainWarehouseArticleId()).thenReturn(someArticle);
        when(reOrderArticle.getAmount()).thenReturn(50);
        reOrderItems.add(reOrderArticle);
        when(reOrder.getReOrderItems()).thenReturn(reOrderItems);

        assertTrue(mainWarehouseService.checkOrder(reOrder).isEmpty());
    }

    @Test
    void testCheckOrderNotAllArticlesCanBeOrdered() {
        ReOrder reOrder = mock(ReOrder.class);
        List<ReOrderArticle> reOrderItems = new ArrayList<>();
        ReOrderArticle reOrderArticle = mock(ReOrderArticle.class);
        var someArticle = mainWarehouseService.getStockMap().keySet().iterator().next();
        when(reOrderArticle.getMainWarehouseArticleId()).thenReturn(someArticle);
        when(reOrderArticle.getAmount()).thenReturn(5000); // This amount is too large
        reOrderItems.add(reOrderArticle);
        when(reOrder.getReOrderItems()).thenReturn(reOrderItems);

        assertFalse(mainWarehouseService.checkOrder(reOrder).isEmpty());
        assertEquals(mainWarehouseService.checkOrder(reOrder).getFirst(),
              "Artikel " + someArticle + " kann nicht nachbestellt werden da im Zentrallager nurnoch "
                    + mainWarehouseService.getStockMap().get(someArticle) + " Stück verfügbar sind");
    }
}
