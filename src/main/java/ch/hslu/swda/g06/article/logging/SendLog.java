package ch.hslu.swda.g06.article.logging;


import ch.hslu.swda.g06.article.logging.model.Action;
import ch.hslu.swda.g06.article.logging.model.Log;
import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.model.ReOrder;
import com.google.gson.Gson;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.stereotype.Component;

@Component
public class SendLog {
    private final Gson gson = new Gson();
    private final AmqpTemplate amqpTemplate;

    private static final String LOG_POST = "log.post";
    private static final String CONTENT_TYPE = "application/json";

    public SendLog(AmqpTemplate amqpTemplate) {
        this.amqpTemplate = amqpTemplate;
    }

    public void sendArticleCreatedLog(Article article, String correlationId) {
        Action action = new Action(String.format("Article created: '%s'",article), article.getClass().getName(), article.getArticleId());
        Log log = new Log(action, article.getStoreId());

        sendMessage(log, correlationId, LOG_POST);
    }

    public void sendArticleUpdatedLog(Article article, String correlationId) {
        Action action = new Action(String.format("Article: '%s' updated",article), article.getClass().getName(), article.getArticleId());
        Log log = new Log(action, article.getStoreId());

        sendMessage(log, correlationId, LOG_POST);
    }

    public void sendArticleDeletedLog(Article article, String correlationId) {
        Action action = new Action(String.format("Article: '%s' deleted",article), article.getClass().getName(), article.getArticleId());
        Log log = new Log(action, article.getStoreId());

        sendMessage(log, correlationId, LOG_POST);
    }

    public void sendReOrderErrorLog(ReOrder reOrder, String message, String correlationId) {
        Action action = new Action(message, reOrder.getClass().getName(), reOrder.getReOrderId());
        Log log = new Log(action, reOrder.getStoreId());

        sendMessage(log, correlationId, LOG_POST);
    }

    private void sendMessage(Log log, String correlationId, String routingKey){
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId(correlationId);
        messageProperties.setContentType(CONTENT_TYPE);
        MessageReceiverUtils.sendMessage(log, messageProperties, routingKey, gson, amqpTemplate);
    }
}
