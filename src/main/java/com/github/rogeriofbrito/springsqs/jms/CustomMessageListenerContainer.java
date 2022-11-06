package com.github.rogeriofbrito.springsqs.jms;

import org.springframework.jms.listener.DefaultMessageListenerContainer;

import javax.jms.Session;

public class CustomMessageListenerContainer extends DefaultMessageListenerContainer {

    public CustomMessageListenerContainer() {
        super();
    }

    @Override
    protected void rollbackOnExceptionIfNecessary(Session session, Throwable ex) {
    }
}
