package ch.hslu.swda.g06.article.logging;

import ch.hslu.swda.g06.article.model.Article;
import com.google.gson.Gson;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.util.List;

public class MessageReceiverUtils {

    public static void sendArticleResponse(Article article, MessageProperties properties, final Gson GSON, final AmqpTemplate amqpTemplate) {
        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);
        sendMessage(article, returnMessageProperties, properties.getReplyTo(), GSON, amqpTemplate);
    }

    public static void sendArticleResponse(List<Article> orders, MessageProperties properties, final Gson GSON,
            final AmqpTemplate amqpTemplate) {
        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);
        sendMessage(orders, returnMessageProperties, properties.getReplyTo(), GSON, amqpTemplate);
    }

    public static void sendMessage(final Object messageBody, final MessageProperties properties,
            final String queueName, final Gson GSON, final AmqpTemplate amqpTemplate) {
        String messageBodyJson = GSON.toJson(messageBody);
        Message message = new Message(messageBodyJson.getBytes(), properties);
        amqpTemplate.send(queueName, message);
    }

    private static MessageProperties createReturnMessageProperties(MessageProperties properties) {
        MessageProperties returnMessageProperties = new MessageProperties();
        returnMessageProperties.setCorrelationId(properties.getCorrelationId());
        returnMessageProperties.setContentType("application/json");
        return returnMessageProperties;
    }

    public static void sendDeleteResponse(final boolean isDeleted, final MessageProperties properties, final Gson GSON,
            final AmqpTemplate amqpTemplate) {
        String deleteResult = GSON.toJson(isDeleted);

        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);

        Message returnMessage = new Message(deleteResult.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }
}
