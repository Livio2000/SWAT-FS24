package ch.hslu.swda.g06.article.integrationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.model.Store;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.google.gson.Gson;

@SpringBootTest
@Testcontainers
class GetArticleIT {
    private static final Gson GSON = new Gson();

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private AmqpAdmin amqpAdmin;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Container
    private static final MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:4.2.5");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Container
    static final RabbitMQContainer rabbitMQContainer = new RabbitMQContainer("rabbitmq:management")
            .withExposedPorts(5672, 15672);

    @DynamicPropertySource
    static void rabbitMQProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.rabbitmq.host", (rabbitMQContainer::getHost));
        registry.add("spring.rabbitmq.port", rabbitMQContainer::getAmqpPort);
        registry.add("spring.rabbitmq.username", () -> "guest");
        registry.add("spring.rabbitmq.password", () -> "guest");
        registry.add("spring.rabbitmq.exchange", () -> "swda");
    }

    @BeforeEach
    void setupSwdaExchange() {
        amqpAdmin.declareExchange(new TopicExchange("swda", true, false, Map.of("alternate-exchange", "swda.orphan")));
    }

    @BeforeEach
    void setupQueues() {
        amqpAdmin.declareQueue(new Queue("article.getAll", false));
        amqpAdmin.declareQueue(new Queue("article.get", false));
        amqpAdmin.declareQueue(new Queue("article.response", false));
    }

    @AfterEach
    void resetDatabase() {
        mongoTemplate.getDb().drop();
    }

    @AfterEach
    void resetQueues() {
        amqpAdmin.purgeQueue("article.getAll", false);
        amqpAdmin.purgeQueue("article.get", false);
        amqpAdmin.purgeQueue("article.response", false);
    }

    @Test
    void GetArticleNull() {
        String articleId = "notExistingArticleId";
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(articleId.getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.get", message);

        Message getArticleMessage = rabbitTemplate.receive("article.response", 5000);
        assert getArticleMessage != null;
        Article receivedArticle = GSON.fromJson(new String(getArticleMessage.getBody()), Article.class);

        assertNotNull(getArticleMessage);
        assertNull(receivedArticle);
    }

    @Test
    void GetArticleExistingArticle() {
        Article article = new Article("articleName", 5, 10.0, 5, 12345, "store1");
        mongoTemplate.save(article);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(article.getArticleId().getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.get", message);

        Message getArticleMessage = rabbitTemplate.receive("article.response", 5000);
        assert getArticleMessage != null;
        Article receivedArticle = GSON.fromJson(new String(getArticleMessage.getBody()), Article.class);

        assertNotNull(receivedArticle);
        assertEquals(article.getArticleId(), receivedArticle.getArticleId(), "Article Id should match");
        assertEquals(article.getName(), receivedArticle.getName(), "Name should match");
        assertEquals(article.getAmount(), receivedArticle.getAmount(), "Amount should match");
        assertEquals(article.getPrice(), receivedArticle.getPrice(), "Price should match");
        assertEquals(article.getMinimalQuantity(), receivedArticle.getMinimalQuantity(), "Minimal Quantity should match");
        assertEquals(article.getMainWarehouseArticleId(), receivedArticle.getMainWarehouseArticleId(), "MainWarehouseArticleId should match");
        assertEquals(article.getStoreId(), receivedArticle.getStoreId(), "Store Id should match");
    }

    @Test
    void GetAllArticlesForStore_StoreDoesntExist() {
        String storeId = "notExistingStoreId";
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(storeId.getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.getAll", message);

        Message getAllArticlesMessage = rabbitTemplate.receive("article.response", 5000);
        assert getAllArticlesMessage != null;
        Type articleListType = new TypeToken<List<Article>>() { }.getType();
        List<Article> receivedArticles = GSON.fromJson(new String(getAllArticlesMessage.getBody()), articleListType);

        assertNotNull(receivedArticles);
        assertEquals(0, receivedArticles.size(), "No articles should be returned");
    }

    @Test
    void GetAllArticlesForStore_StoreExists_NoArticles() {
        Store store = new Store("name", new ArrayList<>());
        mongoTemplate.save(store);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(store.getStoreId().getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.getAll", message);

        Message getAllArticlesMessage = rabbitTemplate.receive("article.response", 5000);
        assert getAllArticlesMessage != null;
        Type articleListType = new TypeToken<List<Article>>() { }.getType();
        List<Article> receivedArticles = GSON.fromJson(new String(getAllArticlesMessage.getBody()), articleListType);

        assertNotNull(receivedArticles);
        assertEquals(0, receivedArticles.size(), "No articles should be returned");
    }

    @Test
    void GetAllArticlesForStore_StoreExists_ArticlesExist() {
        Store store = new Store("name", new ArrayList<>());
        Article article1 = new Article("articleName1", 5, 10.0, 5, 12345, store.getStoreId());
        Article article2 = new Article("articleName", 5, 10.0, 5, 12345, store.getStoreId());
        mongoTemplate.save(store);
        mongoTemplate.save(article1);
        mongoTemplate.save(article2);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(store.getStoreId().getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.getAll", message);

        Message getAllArticlesMessage = rabbitTemplate.receive("article.response", 5000);
        assert getAllArticlesMessage != null;
        Type articleListType = new TypeToken<List<Article>>() { }.getType();
        List<Article> receivedArticles = GSON.fromJson(new String(getAllArticlesMessage.getBody()), articleListType);

        assertNotNull(receivedArticles);
        assertEquals(2, receivedArticles.size(), "2 articles should be returned");
        assertEquals(article1.getArticleId(), receivedArticles.getFirst().getArticleId(), "Article Id should match");
        assertEquals(article1.getName(), receivedArticles.getFirst().getName(), "Name should match");
        assertEquals(article1.getAmount(), receivedArticles.getFirst().getAmount(), "Amount should match");
        assertEquals(article2.getArticleId(), receivedArticles.getLast().getArticleId(), "Article Id should match");
        assertEquals(article2.getName(), receivedArticles.getLast().getName(), "Name should match");
        assertEquals(article2.getAmount(), receivedArticles.getLast().getAmount(), "Amount should match");
    }
}
