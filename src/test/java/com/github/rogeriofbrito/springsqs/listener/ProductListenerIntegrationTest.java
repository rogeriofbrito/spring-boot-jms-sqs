package com.github.rogeriofbrito.springsqs.listener;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.PurgeQueueRequest;
import com.amazonaws.services.sqs.model.SendMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.rogeriofbrito.springsqs.SpringSqsApplication;
import com.github.rogeriofbrito.springsqs.config.AppPropertiesConfig;
import com.github.rogeriofbrito.springsqs.listener.model.ProductMessage;
import com.github.rogeriofbrito.springsqs.service.ProductService;
import com.github.rogeriofbrito.springsqs.service.model.ProcessProductRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

@SpringBootTest(classes = SpringSqsApplication.class)
@ExtendWith(SpringExtension.class)
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

    @BeforeEach
    void beforeEach() {
        amazonSQS.purgeQueue(new PurgeQueueRequest(appPropertiesConfig.getProductQueueUrl()));
    }

    @AfterEach
    void afterEach() {
        amazonSQS.purgeQueue(new PurgeQueueRequest(appPropertiesConfig.getProductQueueUrl()));
    }

    @Test
    void shouldFailWhenReceiveInvalidMessage() throws JMSException {
        String messageBody = "invalid message";

        amazonSQS.sendMessage(new SendMessageRequest(appPropertiesConfig.getProductQueueUrl(), messageBody));

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<ProcessProductRequest> processProductRequestArgumentCaptor =
                ArgumentCaptor.forClass(ProcessProductRequest.class);
        Mockito.verify(productListener, Mockito.timeout(100).times(1)).onMessage(messageArgumentCaptor.capture());
        Mockito.verify(productService, Mockito.never()).processProduct(processProductRequestArgumentCaptor.capture());

        Assertions.assertEquals(messageBody, ((TextMessage) messageArgumentCaptor.getValue()).getText());
    }

    @Test
    void shouldCallProcessProductWhenReceiveValidMessage() throws JMSException, JsonProcessingException {
        ProductMessage productMessage = ProductMessage
                .builder()
                .id(1)
                .title("Nike Court")
                .brand("Nike")
                .build();

        String messageBody = objectMapper.writeValueAsString(productMessage);

        amazonSQS.sendMessage(new SendMessageRequest(appPropertiesConfig.getProductQueueUrl(), messageBody));

        ArgumentCaptor<Message> messageArgumentCaptor = ArgumentCaptor.forClass(Message.class);
        ArgumentCaptor<ProcessProductRequest> processProductRequestArgumentCaptor =
                ArgumentCaptor.forClass(ProcessProductRequest.class);
        Mockito.verify(productListener, Mockito.timeout(100).times(1)).onMessage(messageArgumentCaptor.capture());
        Mockito.verify(productService, Mockito.only()).processProduct(processProductRequestArgumentCaptor.capture());

        Assertions.assertEquals(messageBody, ((TextMessage) messageArgumentCaptor.getValue()).getText());
        ProcessProductRequest expectedProcessProductRequest = ProcessProductRequest
                .builder()
                .id(productMessage.getId())
                .title(productMessage.getTitle())
                .brand(productMessage.getBrand())
                .build();
        Assertions.assertEquals(expectedProcessProductRequest, processProductRequestArgumentCaptor.getValue());
    }
}
