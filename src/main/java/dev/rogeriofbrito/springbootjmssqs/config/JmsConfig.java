package dev.rogeriofbrito.springbootjmssqs.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import dev.rogeriofbrito.springbootjmssqs.jms.CustomJmsListenerContainerFactory;
import jakarta.jms.Session;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;
import software.amazon.awssdk.services.sqs.SqsClient;

@Configuration
@EnableJms
public class JmsConfig {

    private final SqsClient sqsClient;

    public JmsConfig(SqsClient sqsClient) {
        this.sqsClient = sqsClient;
    }

    @Bean
    public DefaultJmsListenerContainerFactory jmsListenerContainerFactory() {
        DefaultJmsListenerContainerFactory factory = new CustomJmsListenerContainerFactory();
        factory.setConnectionFactory(getSQSConnectionFactory());
        factory.setDestinationResolver(new DynamicDestinationResolver());
        factory.setConcurrency("2");
        factory.setSessionAcknowledgeMode(Session.CLIENT_ACKNOWLEDGE);

        return factory;
    }

    private SQSConnectionFactory getSQSConnectionFactory() {
        return new SQSConnectionFactory(
                getProviderConfiguration(),
                sqsClient);
    }

    private ProviderConfiguration getProviderConfiguration() {
        return new ProviderConfiguration();
    }
}
