package com.github.rogeriofbrito.springsqs.jms;

import org.springframework.jms.config.DefaultJmsListenerContainerFactory;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class CustomJmsListenerContainerFactory extends DefaultJmsListenerContainerFactory {

    @Override
    protected DefaultMessageListenerContainer createContainerInstance() {
        return new CustomMessageListenerContainer();
    }
}
