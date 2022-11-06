package com.github.rogeriofbrito.springsqs.config;

import com.amazon.sqs.javamessaging.ProviderConfiguration;
import com.amazon.sqs.javamessaging.SQSConnectionFactory;
import com.amazonaws.services.sqs.AmazonSQS;
import com.github.rogeriofbrito.springsqs.jms.CustomJmsListenerContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.support.destination.DynamicDestinationResolver;

import javax.jms.Session;

@Configuration
@EnableJms
public class JmsConfig {

    private final AmazonSQS amazonSQS;

    public JmsConfig(AmazonSQS amazonSQS) {
        this.amazonSQS = amazonSQS;
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
                amazonSQS);
    }

    private ProviderConfiguration getProviderConfiguration() {
        return new ProviderConfiguration();
    }
}
