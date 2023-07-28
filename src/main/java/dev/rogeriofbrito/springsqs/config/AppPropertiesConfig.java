package dev.rogeriofbrito.springsqs.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AppPropertiesConfig {

    @Value("${aws.region}")
    private String awsRegion;

    @Value("${aws.service-endpoint}")
    private String awsServiceEndpoint;

    @Value("${app.queue.product.url}")
    private String productQueueUrl;

    @Value("${app.dead-letter-queue.product.url}")
    private String productDeadLetterQueueUrl;
}
