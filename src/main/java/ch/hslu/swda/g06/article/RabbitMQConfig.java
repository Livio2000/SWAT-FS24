package ch.hslu.swda.g06.article;

import java.util.Map;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    @Bean
    public Exchange swdaExchange() {
        return new TopicExchange("swda", true, false, Map.of("alternate-exchange", "swda.orphan"));
    }

    @Bean
    public Queue articleGetQueue() {
        return new Queue("article.get", false);
    }

    @Bean
    public Queue articlePostQueue() {
        return new Queue("article.post", false);
    }

    @Bean
    public Queue articlePutQueue() {
        return new Queue("article.put", false);
    }

    @Bean
    public Queue articleDeleteQueue() {
        return new Queue("article.delete", false);
    }

    @Bean
    public Queue articleGetAllQueue() { return new Queue("article.getAll", false); }

    @Bean
    public Queue articleVerifyQueue() { return new Queue("article.verify", false); }

    @Bean
    public Queue articleOrderDeletedQueue() { return new Queue("article.orderDeleted", false); }

    @Bean
    public Queue storePostQueue() {
        return new Queue("store.post", false);
    }

    @Bean
    public Queue storeGetQueue() {
        return new Queue("store.get", false);
    }

    @Bean
    public Queue storePutQueue() {
        return new Queue("store.put", false);
    }

    @Bean
    public Queue mainWarehouseGetAllQueue() {
        return new Queue("mainWarehouse.getAll", false);
    }

    @Bean
    public Queue reOrderGetQueue() {
        return new Queue("reorder.get", false);
    }

    @Bean
    public Queue reOrderPutQueue() {
        return new Queue("reorder.put", false);
    }

    @Bean
    public Queue reOrderGetAllByStoreIdQueue() {
        return new Queue("reorder.getAllByStoreId", false);
    }

    @Bean
    public Queue reOrderCompleteQueue() {
        return new Queue("reorder.complete", false);
    }

    @Bean
    public Binding bindArticleGetQueueToExchange(@Qualifier("articleGetQueue") Queue articleGetQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(articleGetQueue).to(swdaExchange).with("article.get").noargs();
    }

    @Bean
    public Binding bindArticlePostQueueToExchange(@Qualifier("articlePostQueue") Queue articlePostQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(articlePostQueue).to(swdaExchange).with("article.post").noargs();
    }

    @Bean
    public Binding bindArticlePutQueueToExchange(@Qualifier("articlePutQueue") Queue articlePutQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(articlePutQueue).to(swdaExchange).with("article.put").noargs();
    }

    @Bean
    public Binding bindArticleDeleteQueueToExchange(@Qualifier("articleDeleteQueue") Queue articleDeleteQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(articleDeleteQueue).to(swdaExchange).with("article.delete").noargs();
    }

    @Bean
    public Binding bindGetAllQueueToExchange(@Qualifier("articleGetAllQueue") Queue articleGetAllQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(articleGetAllQueue).to(swdaExchange).with("article.getAll").noargs();
    }

    @Bean
    public Binding bindGetVerifyQueueToExchange(@Qualifier("articleVerifyQueue") Queue articleVerifyQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(articleVerifyQueue).to(swdaExchange).with("article.verify").noargs();
    }

    @Bean
    public Binding bindOrderDeletedQueueToExchange(@Qualifier("articleOrderDeletedQueue") Queue articleOrderDeletedQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(articleOrderDeletedQueue).to(swdaExchange).with("article.orderDeleted").noargs();
    }

    @Bean
    public Binding bindStorePostQueueToExchange(@Qualifier("storePostQueue") Queue storePostQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(storePostQueue).to(swdaExchange).with("store.post").noargs();
    }

    @Bean
    public Binding bindStoreGetQueueToExchange(@Qualifier("storeGetQueue") Queue storeGetQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(storeGetQueue).to(swdaExchange).with("store.get").noargs();
    }

    @Bean
    public Binding bindStorePutQueueToExchange(@Qualifier("storePutQueue") Queue storePutQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(storePutQueue).to(swdaExchange).with("store.put").noargs();
    }

    @Bean
    public Binding bindMainWarehouseGetAllQueueToExchange(@Qualifier("mainWarehouseGetAllQueue") Queue mainWarehouseGetAllQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(mainWarehouseGetAllQueue).to(swdaExchange).with("mainWarehouse.getAll").noargs();
    }

    @Bean
    public Binding bindReOrderGetQueueToExchange(@Qualifier("reOrderGetQueue") Queue reOrderGetQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(reOrderGetQueue).to(swdaExchange).with("reorder.get").noargs();
    }

    @Bean
    public Binding bindReOrderPutQueueToExchange(@Qualifier("reOrderPutQueue") Queue reOrderPutQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(reOrderPutQueue).to(swdaExchange).with("reorder.put").noargs();
    }

    @Bean
    public Binding bindReOrderGetAllByStoreIdQueueToExchange(@Qualifier("reOrderGetAllByStoreIdQueue") Queue reOrderGetAllByStoreIdQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(reOrderGetAllByStoreIdQueue).to(swdaExchange).with("reorder.getAllByStoreId").noargs();
    }

    @Bean
    public Binding bindReOrderCompleteQueueToExchange(@Qualifier("reOrderCompleteQueue") Queue reOrderCompleteQueue, Exchange swdaExchange) {
        return BindingBuilder.bind(reOrderCompleteQueue).to(swdaExchange).with("reorder.complete").noargs();
    }
}
