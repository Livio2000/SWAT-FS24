package ch.hslu.swda.g06.article.listener;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import ch.hslu.swda.g06.article.model.Store;
import ch.hslu.swda.g06.article.repository.IStoreRepository;

@Component
public class StoreMessageReceiver {
    private static final Gson GSON = new Gson();

    private final IStoreRepository storeRepository;

    private final AmqpTemplate amqpTemplate;

    public StoreMessageReceiver(IStoreRepository storeRepository, AmqpTemplate amqpTemplate) {
        this.storeRepository = storeRepository;
        this.amqpTemplate = amqpTemplate;
    }

    @RabbitListener(queues = "store.post")
    public void createStore(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String storeJson = new String(messageBody, StandardCharsets.UTF_8);
        Store newStore = GSON.fromJson(storeJson, Store.class);
        newStore.setStoreId(UUID.randomUUID().toString());
        newStore.setCurrentEtag();

        Store createdStore = storeRepository.save(newStore);

        sendStoreResponse(createdStore, properties);
    }

    @RabbitListener(queues = "store.get")
    public void getStore(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String id = new String(messageBody, StandardCharsets.UTF_8);

        Store store = storeRepository.findById(id).orElse(null);

        sendStoreResponse(store, properties);
    }

    @RabbitListener(queues = "store.put")
    public void updateStore(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String storeJson = new String(messageBody, StandardCharsets.UTF_8);
        Store storeToUpdate = GSON.fromJson(storeJson, Store.class);

        Store dbStore = storeRepository.findById(storeToUpdate.getStoreId()).orElse(null);
        if (dbStore != null && dbStore.canEdit(storeToUpdate.getEtag())) {
            storeToUpdate.setCurrentEtag();
            Store updatedStoreResult = storeRepository.save(storeToUpdate);
            sendStoreResponse(updatedStoreResult, properties);
        } else {
            sendStoreResponse(null, properties);
        }
    }

    private void sendStoreResponse(Store store, MessageProperties properties) {
        String storeJson = GSON.toJson(store);

        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);

        Message returnMessage = new Message(storeJson.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private MessageProperties createReturnMessageProperties(MessageProperties properties) {
        MessageProperties returnMessageProperties = new MessageProperties();
        returnMessageProperties.setCorrelationId(properties.getCorrelationId());
        returnMessageProperties.setContentType("application/json");
        return returnMessageProperties;
    }
}
