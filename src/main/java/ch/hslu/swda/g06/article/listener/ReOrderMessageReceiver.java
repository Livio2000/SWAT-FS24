package ch.hslu.swda.g06.article.listener;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

import ch.hslu.swda.g06.article.factory.ReOrderFactory;
import ch.hslu.swda.g06.article.model.ReOrder;
import ch.hslu.swda.g06.article.repository.IReOrderRepository;

@Component
public class ReOrderMessageReceiver {
    private static final Gson GSON = new Gson();

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private IReOrderRepository reOrderRepository;

    @Autowired
    private ReOrderFactory reOrderFactory;

    @RabbitListener(queues = "reorder.get")
    public void getReOrder(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String id = new String(messageBody, StandardCharsets.UTF_8);

        ReOrder reOrder = reOrderRepository.findById(id).orElse(null);

        sendReOrderResponse(reOrder, properties);
    }

    @RabbitListener(queues = "reorder.getAllByStoreId")
    public void getAllReordersByStoreId(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String storeId = new String(messageBody, StandardCharsets.UTF_8);
        List<ReOrder> reOrders = reOrderRepository.getReOrdersByStoreID(storeId);
        sendReOrdersResponse(reOrders, properties);
    }

    @RabbitListener(queues = "reorder.put")
    public void updateReOrder(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String reOrderJson = new String(messageBody, StandardCharsets.UTF_8);
        ReOrder reOrderToUpdate = GSON.fromJson(reOrderJson, ReOrder.class);

        ReOrder dbReOrder = reOrderRepository.findById(reOrderToUpdate.getReOrderId()).orElse(null);
        if (dbReOrder != null && dbReOrder.canEdit(reOrderToUpdate.getEtag())) {
            reOrderToUpdate.setCurrentEtag();
            ReOrder updatedReOrderResult = reOrderRepository.save(reOrderToUpdate);
            sendReOrderResponse(updatedReOrderResult, properties);
        }
    }

    @RabbitListener(queues = "reorder.complete")
    public void completeReOrder(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String reOrderJson = new String(messageBody, StandardCharsets.UTF_8);
        ReOrder reOrderToComplete = GSON.fromJson(reOrderJson, ReOrder.class);

        ReOrder dbReOrder = reOrderRepository.findById(reOrderToComplete.getReOrderId()).orElse(null);
        if (dbReOrder == null || !dbReOrder.canEdit(reOrderToComplete.getEtag())) {
            sendBadRequestResponse(properties);
            return;
        }
        if (dbReOrder != null) {
            reOrderFactory.processCompletedReOrder(dbReOrder);
            sendReOrderResponse(dbReOrder, properties);
        }
    }

    private void sendReOrderResponse(ReOrder reOrder, MessageProperties properties) {
        String reOrderJson = GSON.toJson(reOrder);

        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);

        Message returnMessage = new Message(reOrderJson.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private void sendReOrdersResponse(Collection<ReOrder> reOrder, MessageProperties properties) {
        String reOrderJson = GSON.toJson(reOrder);

        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);

        Message returnMessage = new Message(reOrderJson.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private MessageProperties createReturnMessageProperties(MessageProperties properties) {
        MessageProperties returnMessageProperties = new MessageProperties();
        returnMessageProperties.setCorrelationId(properties.getCorrelationId());
        returnMessageProperties.setContentType("application/json");
        return returnMessageProperties;
    }

    private void sendBadRequestResponse(MessageProperties properties) {
        String badRequest = "Bad Request";
        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);
        Message returnMessage = new Message(badRequest.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }
}
