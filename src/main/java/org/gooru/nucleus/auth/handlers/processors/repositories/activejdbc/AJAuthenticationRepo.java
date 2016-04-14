package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.command.executor.authentication.AuthenticationExecutorFactory;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthenticationRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;

public class AJAuthenticationRepo implements AuthenticationRepo {
    private final MessageContext messageContext;

    public AJAuthenticationRepo(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public MessageResponse createAnonymousAccessToken() {
        return TransactionExecutor.executeTransaction(AuthenticationExecutorFactory
            .createAnonymousAccessToken(messageContext));
    }

    @Override
    public MessageResponse deleteAccessToken() {
        return TransactionExecutor.executeTransaction(AuthenticationExecutorFactory.deleteAccessToken(messageContext));
    }

    @Override
    public MessageResponse fetchAccessToken() {
        return TransactionExecutor.executeTransaction(AuthenticationExecutorFactory.fetchAccessToken(messageContext));
    }

    @Override
    public MessageResponse createBasicAuthAccessToken() {
        return TransactionExecutor.executeTransaction(AuthenticationExecutorFactory
            .createBasicAuthAccessToken(messageContext));
    }
}
