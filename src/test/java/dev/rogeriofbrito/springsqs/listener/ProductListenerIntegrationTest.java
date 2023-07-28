package dev.rogeriofbrito.springsqs.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rogeriofbrito.springsqs.SpringSqsApplication;
import dev.rogeriofbrito.springsqs.config.AppPropertiesConfig;
import dev.rogeriofbrito.springsqs.listener.model.ProductMessage;
import dev.rogeriofbrito.springsqs.service.ProductService;
import dev.rogeriofbrito.springsqs.service.model.ProcessProductRequest;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.TextMessage;
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
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest(classes = SpringSqsApplication.class)
@ExtendWith(SpringExtension.class)
@Log4j2
public class ProductListenerIntegrationTest {

    @Autowired
    private SqsClient sqsClient;
    @Autowired
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
        sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(appPropertiesConfig.getProductQueueUrl()).build());
    }

    @AfterEach
    void afterEach() {
        sqsClient.purgeQueue(PurgeQueueRequest.builder().queueUrl(appPropertiesConfig.getProductQueueUrl()).build());
    }

    @Test
    void givenAInvalidMessageShouldFailWhenReceive() throws JMSException {
        // given
        final String messageBody = "invalid message";

        // when
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(appPropertiesConfig.getProductQueueUrl())
                .messageBody(messageBody)
                .build());

        // then
        verify(productListener, after(7000).times(2)).onMessage(messageArgumentCaptor.capture());
        verify(productService, after(7000).never()).processProduct(processProductRequestArgumentCaptor.capture());

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
        sqsClient.sendMessage(SendMessageRequest.builder()
                .queueUrl(appPropertiesConfig.getProductQueueUrl())
                .messageBody(messageBody)
                .build());

        // then
        final ProcessProductRequest expectedProcessProductRequest = ProcessProductRequest.builder()
                .id(productMessage.getId())
                .title(productMessage.getTitle())
                .brand(productMessage.getBrand())
                .build();

        verify(productListener, after(7000).times(1)).onMessage(messageArgumentCaptor.capture());
        verify(productService, after(7000).only()).processProduct(processProductRequestArgumentCaptor.capture());
        assertEquals(messageBody, ((TextMessage) messageArgumentCaptor.getValue()).getText());
        assertEquals(expectedProcessProductRequest, processProductRequestArgumentCaptor.getValue());
    }
}
