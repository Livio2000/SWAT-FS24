package ch.hslu.swda.g06.article.listener;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import ch.hslu.swda.g06.article.logging.SendLog;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ch.hslu.swda.g06.article.factory.ArticleFactory;
import ch.hslu.swda.g06.article.factory.MainWarehouseFactory;
import ch.hslu.swda.g06.article.factory.ReOrderFactory;
import ch.hslu.swda.g06.article.factory.StoreFactory;
import ch.hslu.swda.g06.article.model.Article;
import ch.hslu.swda.g06.article.model.DeleteArticleDto;
import ch.hslu.swda.g06.article.model.OrderArticleDto;
import ch.hslu.swda.g06.article.model.Reason;
import ch.hslu.swda.g06.article.model.VerifyPropertyDto;
import ch.hslu.swda.g06.article.repository.IArticleRepository;

@Component
public class ArticleMessageReceiver {
    private static final Gson GSON = new Gson();
    private static final Integer AMOUNTTOREORDER = 20;

    private final IArticleRepository articleRepository;

    private final AmqpTemplate amqpTemplate;

    private final StoreFactory storeFactory;

    private final ArticleFactory articleFactory;

    private final MainWarehouseFactory mainWarehouseFactory;

    private final ReOrderFactory reOrderFactory;

    public ArticleMessageReceiver(IArticleRepository articleRepository, AmqpTemplate amqpTemplate, StoreFactory storeFactory, ArticleFactory articleFactory, MainWarehouseFactory mainWarehouseFactory, ReOrderFactory reOrderFactory) {
        this.articleRepository = articleRepository;
        this.amqpTemplate = amqpTemplate;
        this.storeFactory = storeFactory;
        this.articleFactory = articleFactory;
        this.mainWarehouseFactory = mainWarehouseFactory;
        this.reOrderFactory = reOrderFactory;
    }

    @RabbitListener(queues = "article.post")
    public void createArticle(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String articleJson = new String(messageBody, StandardCharsets.UTF_8);
        Article newArticle = GSON.fromJson(articleJson, Article.class);
        newArticle.setArticleId(UUID.randomUUID().toString());
        newArticle.setCurrentEtag();

        if (!mainWarehouseFactory.getStockMap().containsKey(newArticle.getMainWarehouseArticleId())) {
            sendBadRequestResponse(properties);
            return;
        }

        if (!storeFactory.storeExistsById(newArticle.getStoreId())) {
            sendBadRequestResponse(properties);
            return;
        }

        if (articleFactory.articleExistsInStoreById(newArticle.getMainWarehouseArticleId(), newArticle.getStoreId())) {
            sendBadRequestResponse(properties);
            return;
        }

        Article createdArticle = articleRepository.save(newArticle);

        sendArticleResponse(createdArticle, properties);
        // ToDo: Send log message
        SendLog.sendArticleCreatedLog(newArticle, properties.getCorrelationId(), GSON, amqpTemplate);
    }

    @RabbitListener(queues = "article.get")
    public void getArticle(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String id = new String(messageBody, StandardCharsets.UTF_8);

        Article article = articleRepository.findById(id).orElse(null);

        sendArticleResponse(article, properties);
    }

    @RabbitListener(queues = "article.getAll")
    public void getAllArticles(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String storeId = new String(messageBody, StandardCharsets.UTF_8);
        List<Article> articles = articleRepository.getArticlesBystoreID(storeId);
        sendArticlesResponse(articles, properties);
    }

    @RabbitListener(queues = "article.put")
    public void updateArticle(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String articleJson = new String(messageBody, StandardCharsets.UTF_8);
        Article articleToUpdate = GSON.fromJson(articleJson, Article.class);

        if (!storeFactory.storeExistsById(articleToUpdate.getStoreId())) {
            sendBadRequestResponse(properties);
            return;
        }

        if (!articleFactory.articleExistsInStoreById(articleToUpdate.getMainWarehouseArticleId(),
                articleToUpdate.getStoreId())) {
            sendBadRequestResponse(properties);
            return;
        }

        if (articleToUpdate.getAmount() < 0) {
            sendBadRequestResponse(properties);
            return;
        }

        Article existingArticle = articleRepository.findById(articleToUpdate.getArticleId()).orElse(null);
        if (existingArticle == null || !existingArticle.canEdit(articleToUpdate.getEtag())) {
            sendBadRequestResponse(properties);
            return;
        }

        if (articleToUpdate.getAmount() < articleToUpdate.getMinimalQuantity()) {
            reOrderFactory.reOrder(Map.of(articleToUpdate.getMainWarehouseArticleId(), AMOUNTTOREORDER),
                    articleToUpdate.getStoreId());
        }

        articleToUpdate.setCurrentEtag();
        Article updatedArticleResult = articleRepository.save(articleToUpdate);
        sendArticleResponse(updatedArticleResult, properties);
        // ToDo: Send log message
        SendLog.sendArticleUpdatedLog(articleToUpdate, properties.getCorrelationId(), GSON, amqpTemplate);
    }

    @RabbitListener(queues = "article.delete")
    public void deleteArticle(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        String deleteArticleJson = new String(messageBody, StandardCharsets.UTF_8);
        DeleteArticleDto deleteArticleDto = GSON.fromJson(deleteArticleJson, DeleteArticleDto.class);
        Article article = articleRepository.findById(deleteArticleDto.getArticleId()).orElse(null);

        if (article != null && article.canEdit(deleteArticleDto.getETag())) {
            articleRepository.deleteById(deleteArticleDto.getArticleId());
            sendDeleteResponse(true, properties);
        } else {
            sendDeleteResponse(false, properties);
        }

        // ToDo: Send log message
        SendLog.sendArticleDeletedLog(article, properties.getCorrelationId(), GSON, amqpTemplate);
    }

    @RabbitListener(queues = "article.verify")
    public void verifyArticle(Message message) {
        MessageProperties properties = message.getMessageProperties();
        byte[] messageBody = message.getBody();
        VerifyPropertyDto<List<OrderArticleDto>> verifyOrderArticles = GSON.fromJson(new String(messageBody),
                new TypeToken<VerifyPropertyDto<List<OrderArticleDto>>>() {
                }.getType());

        for (OrderArticleDto dto : verifyOrderArticles.getPropertyValue()) {
            Article articleToVerify = articleRepository.findById(dto.getArticleId()).orElse(null);
            if (articleToVerify == null) {
                VerifyPropertyDto<OrderArticleDto> doesntExist = new VerifyPropertyDto<>(
                        verifyOrderArticles.getOrderId(), dto, false, Reason.NOT_FOUND);
                sendVerifyResponse(doesntExist, properties);
                continue;
            }

            if ((articleToVerify.getAmount() < dto.getAmount())) {
                VerifyPropertyDto<OrderArticleDto> insufficientAmount = new VerifyPropertyDto<>(
                        verifyOrderArticles.getOrderId(), dto, false, Reason.INSUFFICIENT_AMOUNT);
                sendVerifyResponse(insufficientAmount, properties);
                continue;
            }

            articleToVerify.setAmount(articleToVerify.getAmount() - dto.getAmount());
            if (articleToVerify.getMinimalQuantity() > articleToVerify.getAmount()) {
                reOrderFactory.reOrder(Map.of(articleToVerify.getMainWarehouseArticleId(), AMOUNTTOREORDER),
                        articleToVerify.getStoreId());
            }
            articleToVerify.setCurrentEtag();
            articleRepository.save(articleToVerify);
            VerifyPropertyDto<OrderArticleDto> verified = new VerifyPropertyDto<>(verifyOrderArticles.getOrderId(), dto,
                    true);
            sendVerifyResponse(verified, properties);
        }
    }

    @RabbitListener(queues = "article.orderDeleted")
    public void orderDeletedArticle(Message message) {
        byte[] messageBody = message.getBody();
        List<OrderArticleDto> cancelledOrderArticles = GSON.fromJson(new String(messageBody),
                new TypeToken<List<OrderArticleDto>>() {
                }.getType());
        for (OrderArticleDto dto : cancelledOrderArticles) {
            Article dbArticle = articleRepository.findById(dto.getArticleId()).orElse(null);
            if (dbArticle != null) {
                dbArticle.setAmount(dbArticle.getAmount() + dto.getAmount());
                dbArticle.setCurrentEtag();
                articleRepository.save(dbArticle);
            }

        }
    }

    private void sendArticleResponse(Article article, MessageProperties properties) {
        String ArticleJson = GSON.toJson(article);

        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);

        Message returnMessage = new Message(ArticleJson.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private void sendArticlesResponse(Collection<Article> Articles, MessageProperties properties) {
        String ArticlesJson = GSON.toJson(Articles);

        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);

        Message returnMessage = new Message(ArticlesJson.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private void sendDeleteResponse(boolean isDeleted, MessageProperties properties) {
        String deleteResult = GSON.toJson(isDeleted);

        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);

        Message returnMessage = new Message(deleteResult.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private void sendBadRequestResponse(MessageProperties properties) {
        String badRequest = "Bad Request";
        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);
        Message returnMessage = new Message(badRequest.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private void sendVerifyResponse(VerifyPropertyDto<OrderArticleDto> verifyPropertyDto,
            MessageProperties properties) {
        String verificationResult = GSON.toJson(verifyPropertyDto);
        MessageProperties returnMessageProperties = createReturnMessageProperties(properties);
        Message returnMessage = new Message(verificationResult.getBytes(), returnMessageProperties);
        amqpTemplate.send(properties.getReplyTo(), returnMessage);
    }

    private MessageProperties createReturnMessageProperties(MessageProperties properties) {
        MessageProperties returnMessageProperties = new MessageProperties();
        returnMessageProperties.setCorrelationId(properties.getCorrelationId());
        returnMessageProperties.setContentType("application/json");
        return returnMessageProperties;
    }

}