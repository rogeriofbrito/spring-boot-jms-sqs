package dev.rogeriofbrito.springsqs.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.rogeriofbrito.springsqs.listener.model.ProductMessage;
import dev.rogeriofbrito.springsqs.service.ProductService;
import dev.rogeriofbrito.springsqs.service.model.ProcessProductRequest;
import dev.rogeriofbrito.springsqs.service.model.ProcessProductResponse;
import jakarta.jms.JMSException;
import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import jakarta.jms.TextMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

@Component
@Log4j2
public class ProductListener implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ProductService productService;

    public ProductListener(ObjectMapper objectMapper, ProductService productService) {
        this.objectMapper = objectMapper;
        this.productService = productService;
    }

    @JmsListener(destination = "${app.queue.product.name}")
    @Override
    public void onMessage(Message message) {
        log.info("message received in product queue");

        ProductMessage productMessage;
        try {
            String messageText = ((TextMessage) message).getText();
            log.debug("message text: {}", messageText);
            productMessage = objectMapper.readValue(messageText, ProductMessage.class);
        } catch (JMSException | JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        log.debug("product: {}", productMessage);

        ProcessProductRequest processProductRequest = ProcessProductRequest
                .builder()
                .id(productMessage.getId())
                .title(productMessage.getTitle())
                .brand(productMessage.getBrand())
                .build();
        log.debug("process product request: {}", processProductRequest);

        ProcessProductResponse processProductResponse = productService.processProduct(processProductRequest);
        log.debug("process product response: {}", processProductResponse);
    }
}