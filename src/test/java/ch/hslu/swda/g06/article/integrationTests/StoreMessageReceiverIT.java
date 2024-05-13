package ch.hslu.swda.g06.article.integrationTests;

import ch.hslu.swda.g06.article.model.Store;
import com.google.gson.Gson;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class StoreMessageReceiverIT {
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
        amqpAdmin.declareQueue(new Queue("store.post", false));
        amqpAdmin.declareQueue(new Queue("store.put", false));
        amqpAdmin.declareQueue(new Queue("store.get", false));
        amqpAdmin.declareQueue(new Queue("store.response", false));
    }

    @AfterEach
    void resetDatabase() {
        mongoTemplate.getDb().drop();
    }

    @AfterEach
    void resetQueues() {
        amqpAdmin.purgeQueue("store.post", false);
        amqpAdmin.purgeQueue("store.put", false);
        amqpAdmin.purgeQueue("store.get", false);
        amqpAdmin.purgeQueue("store.response", false);
    }

    @Test
    void GetStoreNull() {
        String storeId = "notExistingStoreId";
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("store.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(storeId.getBytes(), messageProperties);
        rabbitTemplate.send("swda", "store.get", message);

        Message getStoreMessage = rabbitTemplate.receive("store.response", 5000);
        assert getStoreMessage != null;
        Store receivedStore = GSON.fromJson(new String(getStoreMessage.getBody()), Store.class);

        assertNull(receivedStore);
    }

    @Test
    void GetStoreNotNull() {
        Store store = new Store("storeName", new ArrayList<>());
        mongoTemplate.save(store);

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("store.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(store.getStoreId().getBytes(), messageProperties);
        rabbitTemplate.send("swda", "store.get", message);

        Message getStoreMessage = rabbitTemplate.receive("store.response", 5000);
        assert getStoreMessage != null;
        Store receivedStore = GSON.fromJson(new String(getStoreMessage.getBody()), Store.class);

        assertNotNull(receivedStore);
        assertEquals(store.getStoreId(), receivedStore.getStoreId());
        assertEquals(store.getName(), receivedStore.getName());
        assertEquals(store.getArticleIds(), receivedStore.getArticleIds());
    }

    @Test
    void CreateStore() {
        Store store = new Store("storeName", new ArrayList<>());
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("store.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(GSON.toJson(store).getBytes(), messageProperties);
        rabbitTemplate.send("swda", "store.post", message);

        Message getStoreMessage = rabbitTemplate.receive("store.response", 5000);
        assert getStoreMessage != null;

        List<Store> stores = mongoTemplate.findAll(Store.class, "stores");
        assertEquals(1, stores.size(), "Store should be created");
        assertEquals(store.getName(), stores.getFirst().getName());
        assertEquals(store.getArticleIds(), stores.getFirst().getArticleIds());
    }

    @Test
    void UpdateStore() {
        Store store = new Store("storeName", new ArrayList<>());
        mongoTemplate.save(store);
        store.setName("newStoreName");

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId("correlationId");
        messageProperties.setReplyTo("store.response");
        messageProperties.setContentType("application/json");
        Message message = new Message(GSON.toJson(store).getBytes(), messageProperties);
        rabbitTemplate.send("swda", "store.put", message);

        Message getStoreMessage = rabbitTemplate.receive("store.response", 5000);
        assert getStoreMessage != null;
        Store receivedStore = GSON.fromJson(new String(getStoreMessage.getBody()), Store.class);

        List<Store> stores = mongoTemplate.findAll(Store.class, "stores");
        assertEquals(1, stores.size(), "Store should be created");
        assertEquals(store.getName(), stores.getFirst().getName());
        assertEquals(store.getArticleIds(), stores.getFirst().getArticleIds());
        assertNotNull(receivedStore);
        assertEquals(store.getName(), receivedStore.getName());
        assertEquals(store.getArticleIds(), receivedStore.getArticleIds());
    }
}
