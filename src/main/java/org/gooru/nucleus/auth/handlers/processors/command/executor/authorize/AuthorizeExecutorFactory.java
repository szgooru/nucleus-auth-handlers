package org.gooru.nucleus.auth.handlers.processors.command.executor.authorize;

import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class AuthorizeExecutorFactory {

    public static DBExecutor AuthorizeUser(MessageContext messageContext) {
        return new AuthorizeUserExecutor(messageContext);
    }

}
