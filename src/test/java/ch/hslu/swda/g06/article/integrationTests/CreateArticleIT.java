package ch.hslu.swda.g06.article.integrationTests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.model.Store;
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
class CreateArticleIT {
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
        amqpAdmin.declareQueue(new Queue("article.post", false));
        amqpAdmin.declareQueue(new Queue("article.response", false));
    }

    @AfterEach
    void resetDatabase() {
        mongoTemplate.getDb().drop();
    }

    @AfterEach
    void resetQueues() {
        amqpAdmin.purgeQueue("article.post", false);
        amqpAdmin.purgeQueue("article.response", false);
    }

    @Test
    void CreateArticleSuccessful() {
        Store store = new Store("store1", new ArrayList<>());
        mongoTemplate.save(store);
        Article article = new Article("articleName", 20, 10.0, 5, 100000, store.getStoreId());

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(GSON.toJson(article).getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.post", message);

        Message getArticleMessage = rabbitTemplate.receive("article.response", 5000);
        assert getArticleMessage != null;

        List<Article> articles = mongoTemplate.findAll(Article.class, "articles");
        assertEquals(1, articles.size(), "Article should be created");
        assertEquals(article.getName(), articles.getFirst().getName(), "Article name should be the same");
        assertEquals(article.getAmount(), articles.getFirst().getAmount(), "Article amount should be the same");
        assertEquals(article.getPrice(), articles.getFirst().getPrice(), "Article price should be the same");
        assertEquals(article.getStoreId(), articles.getFirst().getStoreId(), "Article storeId should be the same");
        assertEquals(article.getMainWarehouseArticleId(), articles.getFirst().getMainWarehouseArticleId(), "Article mainWarehouseArticleId should be the same");
        assertEquals(article.getStoreId(), articles.getFirst().getStoreId(), "Article storeId should be the same");
    }

    @Test
    void CreateArticleNoStore() {
        Article article = new Article("articleName", 20, 10.0, 5, 100000, "notExistingStoreId");

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(GSON.toJson(article).getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.post", message);

        Message getArticleMessage = rabbitTemplate.receive("article.response", 5000);
        assert getArticleMessage != null;
        String returnMessage = new String(getArticleMessage.getBody());

        assertEquals("Bad Request", returnMessage);
        List<Article> articles = mongoTemplate.findAll(Article.class, "articles");
        assertEquals(0, articles.size(), "Article should not be created");
    }

    @Test
    void CreateArticleNotInMainWarehouse() {
        Store store = new Store("store1", new ArrayList<>());
        mongoTemplate.save(store);
        Article article = new Article("articleName", 20, 10.0, 5, 0, "notExistingStoreId");

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(GSON.toJson(article).getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.post", message);

        Message getArticleMessage = rabbitTemplate.receive("article.response", 5000);
        assert getArticleMessage != null;
        String returnMessage = new String(getArticleMessage.getBody());

        assertEquals("Bad Request", returnMessage);
        List<Article> articles = mongoTemplate.findAll(Article.class, "articles");
        assertEquals(0, articles.size(), "Article should not be created");
    }

    @Test
    void CreateArticleAlreadyExists() {
        Store store = new Store("store1", new ArrayList<>());
        mongoTemplate.save(store);
        Article article = new Article("articleName", 20, 10.0, 5, 0, "notExistingStoreId");
        mongoTemplate.save(article);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("article.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(GSON.toJson(article).getBytes(), messageProperties);
        rabbitTemplate.send("swda", "article.post", message);

        Message getArticleMessage = rabbitTemplate.receive("article.response", 5000);
        assert getArticleMessage != null;
        String returnMessage = new String(getArticleMessage.getBody());

        assertEquals("Bad Request", returnMessage);
        List<Article> articles = mongoTemplate.findAll(Article.class, "articles");
        assertEquals(1, articles.size(), "Article should only be once in the db");
    }
}
