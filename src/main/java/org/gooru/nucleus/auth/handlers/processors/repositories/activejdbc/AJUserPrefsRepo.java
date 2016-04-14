package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.command.executor.userPrefs.UserPrefsExecutorFactory;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserPrefsRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;

public class AJUserPrefsRepo implements UserPrefsRepo {
    private final MessageContext messageContext;

    public AJUserPrefsRepo(MessageContext messageContext) {
        this.messageContext = messageContext;
    }

    @Override
    public MessageResponse updateUserPrefs() {
        return TransactionExecutor.executeTransaction(UserPrefsExecutorFactory.updateUserPrefs(messageContext));
    }

    @Override
    public MessageResponse fetchUserPrefs() {
        return TransactionExecutor.executeTransaction(UserPrefsExecutorFactory.fetchUserPrefs(messageContext));
    }
}
