package ch.hslu.swda.g06.article.logging;


import ch.hslu.swda.g06.article.logging.model.Action;
import ch.hslu.swda.g06.article.logging.model.Log;
import ch.hslu.swda.g06.article.model.Article;
import com.google.gson.Gson;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.MessageProperties;

public class SendLog {
    public static void sendArticleCreatedLog(Article article, String correlationId, Gson gson, AmqpTemplate amqpTemplate) {
        Action action = new Action(String.format("Article created: '%s'",article), "article", article.getArticleId());
        Log log = new Log(action, article.getStoreId());

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId(correlationId);
        messageProperties.setContentType("application/json");
        MessageReceiverUtils.sendMessage(log, messageProperties, "log.post", gson, amqpTemplate);
    }

    public static void sendArticleUpdatedLog(Article article, String correlationId, Gson gson, AmqpTemplate amqpTemplate) {
        Action action = new Action(String.format("Article: '%s' updated",article), "article", article.getArticleId());
        Log log = new Log(action, article.getStoreId());

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId(correlationId);
        messageProperties.setContentType("application/json");
        MessageReceiverUtils.sendMessage(log, messageProperties, "log.post", gson, amqpTemplate);
    }

    public static void sendArticleDeletedLog(Article article, String correlationId, Gson gson, AmqpTemplate amqpTemplate) {
        Action action = new Action(String.format("Article: '%s' deleted",article), "article", article.getArticleId());
        Log log = new Log(action, article.getStoreId());

        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setCorrelationId(correlationId);
        messageProperties.setContentType("application/json");
        MessageReceiverUtils.sendMessage(log, messageProperties, "log.post", gson, amqpTemplate);
    }

}
