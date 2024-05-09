package ch.hslu.swda.g06.article.listener;

import ch.hslu.swda.g06.article.factory.MainWarehouseFactory;
import com.google.gson.Gson;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class MainWarehouseMessageReceiver {
    private static final Gson GSON = new Gson();

    private final MainWarehouseFactory mainWarehouseFactory;

    private final AmqpTemplate amqpTemplate;

    public MainWarehouseMessageReceiver(MainWarehouseFactory mainWarehouseFactory, AmqpTemplate amqpTemplate) {
        this.mainWarehouseFactory = mainWarehouseFactory;
        this.amqpTemplate = amqpTemplate;
    }

    @RabbitListener(queues ="mainWarehouse.getAll")
    public void getAllMainWarehouseArticles(Message message) {
        MessageProperties properties = message.getMessageProperties();

        Map<Integer, Integer> articles = mainWarehouseFactory.getStockMap();

        sendArticlesResponse(articles, properties);
    }

    private void sendArticlesResponse(Map<Integer, Integer> articles, MessageProperties properties){
        String articlesJson = GSON.toJson(articles);

        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);

        Message returnMessage = new Message(articlesJson.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private MessageProperties createReturnMessageProperties(MessageProperties properties){
        MessageProperties returnMessageProperties = new MessageProperties();
        returnMessageProperties.setCorrelationId(properties.getCorrelationId());
        returnMessageProperties.setContentType("application/json");
        return returnMessageProperties;
    }
}
