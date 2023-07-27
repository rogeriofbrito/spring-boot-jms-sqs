package com.github.rogeriofbrito.springsqs.listener;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.amazonaws.services.sqs.model.SendMessageResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rogeriofbrito.springsqs.SpringSqsApplication;
import com.github.rogeriofbrito.springsqs.config.AppPropertiesConfig;
import com.github.rogeriofbrito.springsqs.listener.model.ProductMessage;
import com.github.rogeriofbrito.springsqs.service.ProductService;
import com.github.rogeriofbrito.springsqs.service.model.ProcessProductRequest;
import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SpringSqsApplication.class)
@ExtendWith(SpringExtension.class)
@Log4j2
public class ProductListenerIntegrationTest {

    private final AmazonSQS amazonSQS;
    private final ObjectMapper objectMapper;
    private final AppPropertiesConfig appPropertiesConfig;

    @Autowired
    public ProductListenerIntegrationTest(AmazonSQS amazonSQS, ObjectMapper objectMapper,
                                          AppPropertiesConfig appPropertiesConfig) {
        this.amazonSQS = amazonSQS;
        this.objectMapper = objectMapper;
        this.appPropertiesConfig = appPropertiesConfig;
    }

    @SpyBean
    private ProductListener productListener;

    @SpyBean
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;

    @Captor
    private ArgumentCaptor<ProcessProductRequest> processProductRequestArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        amazonSQS.purgeQueue(new PurgeQueueRequest(appPropertiesConfig.getProductQueueUrl()));
        amazonSQS.purgeQueue(new PurgeQueueRequest(appPropertiesConfig.getProductDeadLetterQueueUrl()));
    }

    @AfterEach
    void afterEach() {
        amazonSQS.purgeQueue(new PurgeQueueRequest(appPropertiesConfig.getProductQueueUrl()));
        amazonSQS.purgeQueue(new PurgeQueueRequest(appPropertiesConfig.getProductDeadLetterQueueUrl()));
    }

    @Test
    void givenAInvalidMessageShouldFailWhenReceive() throws JMSException {
        // given
        final String messageBody = "invalid message";

        // when
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(
                appPropertiesConfig.getProductQueueUrl(),
                messageBody);
        final SendMessageResult sendMessageResult = amazonSQS.sendMessage(sendMessageRequest);

        // then
        verify(productListener, timeout(10000).times(1)).onMessage(messageArgumentCaptor.capture());
        verify(productService, never()).processProduct(processProductRequestArgumentCaptor.capture());

        assertNotNull(sendMessageResult);
        assertEquals(messageBody, ((TextMessage) messageArgumentCaptor.getValue()).getText());

        /*
        After 5 seconds (pollDelay), given that product_queue has a VisibleTimeout of 5 seconds and a maxReceiveCount of 1,
        the following conditions are expected:
        product_queue: ApproximateNumberOfMessages = 0 and ApproximateNumberOfMessagesNotVisible = 0 (no message to be processed)
        product_queue_dead_letter: ApproximateNumberOfMessages = 1 and ApproximateNumberOfMessagesNotVisible = 0 (one message ready to be processed)
         */
        await()
                .timeout(15, TimeUnit.SECONDS)
                .pollDelay(5, TimeUnit.SECONDS)
                .until(() -> {

                    final Map<String, String> queueAttributes =
                            getQueueAttributes(appPropertiesConfig.getProductQueueUrl());
                    Map<String, String> deadLetterQueueAttributes =
                            getQueueAttributes(appPropertiesConfig.getProductDeadLetterQueueUrl());

                    return "0".equals(queueAttributes.get("ApproximateNumberOfMessages"))
                            && "0".equals(queueAttributes.get("ApproximateNumberOfMessagesNotVisible"))
                            && "1".equals(deadLetterQueueAttributes.get("ApproximateNumberOfMessages"))
                            && "0".equals(deadLetterQueueAttributes.get("ApproximateNumberOfMessagesNotVisible"));
                });
    }

    @Test
    void shouldCallProcessProductWhenReceiveValidMessage() throws JMSException, JsonProcessingException {
        // given
        final ProductMessage productMessage = ProductMessage
                .builder()
                .id(1)
                .title("Nike Court")
                .brand("Nike")
                .build();
        final String messageBody = objectMapper.writeValueAsString(productMessage);

        //when
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(
                appPropertiesConfig.getProductQueueUrl(),
                messageBody);
        final SendMessageResult sendMessageResult = amazonSQS.sendMessage(sendMessageRequest);

        // then
        final ProcessProductRequest expectedProcessProductRequest = ProcessProductRequest.builder()
                .id(productMessage.getId())
                .title(productMessage.getTitle())
                .brand(productMessage.getBrand())
                .build();

        verify(productListener, timeout(100).times(1)).onMessage(messageArgumentCaptor.capture());
        verify(productService, only()).processProduct(processProductRequestArgumentCaptor.capture());
        assertNotNull(sendMessageResult.getMessageId());
        assertEquals(messageBody, ((TextMessage) messageArgumentCaptor.getValue()).getText());
        assertEquals(expectedProcessProductRequest, processProductRequestArgumentCaptor.getValue());

        /*
        After 1 seconds (pollDelay), given that product_queue has a VisibleTimeout of 5 seconds and a maxReceiveCount of 1,
        the following conditions are expected:
        product_queue: ApproximateNumberOfMessages = 0 and ApproximateNumberOfMessagesNotVisible = 0 (no message to be processed)
        product_queue_dead_letter: ApproximateNumberOfMessages = 0 and ApproximateNumberOfMessagesNotVisible = 0 (one message ready to be processed)
         */
        await()
                .timeout(5, TimeUnit.SECONDS)
                .pollDelay(1, TimeUnit.SECONDS)
                .until(() -> {

                    final Map<String, String> queueAttributes =
                            getQueueAttributes(appPropertiesConfig.getProductQueueUrl());
                    Map<String, String> deadLetterQueueAttributes =
                            getQueueAttributes(appPropertiesConfig.getProductDeadLetterQueueUrl());

                    return "0".equals(queueAttributes.get("ApproximateNumberOfMessages"))
                            && "0".equals(queueAttributes.get("ApproximateNumberOfMessagesNotVisible"))
                            && "0".equals(deadLetterQueueAttributes.get("ApproximateNumberOfMessages"))
                            && "0".equals(deadLetterQueueAttributes.get("ApproximateNumberOfMessagesNotVisible"));
                });
    }

    private Map<String, String> getQueueAttributes(String queueUrl) {
        final GetQueueAttributesResult getQueueAttributesResult = amazonSQS.getQueueAttributes(
                queueUrl,
                List.of("All"));

        log.info("{}: {}", queueUrl, getQueueAttributesResult.getAttributes().toString());

        return getQueueAttributesResult.getAttributes();
    }
}
