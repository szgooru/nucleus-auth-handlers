package org.gooru.nucleus.auth.handlers.processors.command.executor.authclient;

import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class AuthClientExecutorFactory {
    public static DBExecutor CreateAuthClient(MessageContext messageContext) {
        return new CreateAuthClientExecutor(messageContext);
    }
}
