package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.command.executor.authclient.AuthClientExecutorFactory;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;

public class AJAuthClientRepo implements AuthClientRepo {
    private final MessageContext messageContext;

    public AJAuthClientRepo(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public MessageResponse createAuthClient() {
        return TransactionExecutor.executeTransaction(AuthClientExecutorFactory.CreateAuthClient(messageContext));
    }
}
