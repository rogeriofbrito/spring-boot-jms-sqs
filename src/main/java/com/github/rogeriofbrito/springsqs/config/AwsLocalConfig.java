package com.github.rogeriofbrito.springsqs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsClient;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
public class AwsLocalConfig {

    private final AppPropertiesConfig appPropertiesConfig;

    public AwsLocalConfig(AppPropertiesConfig appPropertiesConfig) {
        this.appPropertiesConfig = appPropertiesConfig;
    }

    @Bean
    public SqsClient sqsClient() throws URISyntaxException {
        return SqsClient.builder()
                .region(Region.of(appPropertiesConfig.getAwsRegion()))
                .endpointOverride(new URI(appPropertiesConfig.getAwsServiceEndpoint()))
                .build();
    }
}
