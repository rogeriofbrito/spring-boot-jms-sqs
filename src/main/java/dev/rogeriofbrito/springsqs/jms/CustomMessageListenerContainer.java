package dev.rogeriofbrito.springsqs.jms;

import jakarta.jms.Session;
import org.springframework.jms.listener.DefaultMessageListenerContainer;

public class CustomMessageListenerContainer extends DefaultMessageListenerContainer {

    public CustomMessageListenerContainer() {
        super();
    }

    @Override
    protected void rollbackOnExceptionIfNecessary(Session session, Throwable ex) {
    }
}
