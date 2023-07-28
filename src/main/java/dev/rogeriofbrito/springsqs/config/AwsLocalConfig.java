package dev.rogeriofbrito.springsqs.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
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
        final StaticCredentialsProvider staticCredentialsProvider = StaticCredentialsProvider
                .create(AwsBasicCredentials.
                        create("fake-access-key-id", "fake-secret-access-key"));

        return SqsClient.builder()
                .region(Region.of(appPropertiesConfig.getAwsRegion()))
                .endpointOverride(new URI(appPropertiesConfig.getAwsServiceEndpoint()))
                .credentialsProvider(staticCredentialsProvider)
                .build();
    }
}
