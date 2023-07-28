package com.github.rogeriofbrito.springsqs.listener;

import com.amazonaws.services.sqs.AmazonSQS;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SpringSqsApplication.class)
@ExtendWith(SpringExtension.class)
@Log4j2
public class ProductListenerIntegrationTest {

    @Autowired
    private AmazonSQS amazonSQS;
    @SpyBean
    private ObjectMapper objectMapper;
    @Autowired
    private AppPropertiesConfig appPropertiesConfig;

    @SpyBean
    private ProductListener productListener;

    @SpyBean
    private ProductService productService;

    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;

    @Captor
    private ArgumentCaptor<String> messageStrArgumentCaptor;

    @Captor
    private ArgumentCaptor<Class<ProductMessage>> classArgumentCaptor;

    @Captor
    private ArgumentCaptor<ProcessProductRequest> processProductRequestArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        amazonSQS.purgeQueue(new PurgeQueueRequest(appPropertiesConfig.getProductQueueUrl()));
    }

    @AfterEach
    void afterEach() {
        amazonSQS.purgeQueue(new PurgeQueueRequest(appPropertiesConfig.getProductQueueUrl()));
    }

    @Test
    void givenAInvalidMessageShouldFailWhenReceive() throws JMSException, JsonProcessingException {
        // given
        final String messageBody = "invalid message";

        // when
        final SendMessageRequest sendMessageRequest = new SendMessageRequest(
                appPropertiesConfig.getProductQueueUrl(),
                messageBody);
        amazonSQS.sendMessage(sendMessageRequest);

        // then
        verify(productListener, timeout(30000).times(1)).onMessage(messageArgumentCaptor.capture());
        verify(objectMapper, after(2000).times(1)).readValue("invalid message", ProductMessage.class); // necessary to verify internal calls to onMessage method that aren't checked with verify in onMessage
        verify(productService, never()).processProduct(processProductRequestArgumentCaptor.capture());

        assertEquals(messageBody, ((TextMessage) messageArgumentCaptor.getValue()).getText());
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
    }
}
