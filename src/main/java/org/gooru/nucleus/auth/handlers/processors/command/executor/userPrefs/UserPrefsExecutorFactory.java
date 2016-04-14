package org.gooru.nucleus.auth.handlers.processors.command.executor.userPrefs;

import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class UserPrefsExecutorFactory {

    public static DBExecutor updateUserPrefs(MessageContext messageContext) {
        return new UpdateUserPrefsExecutor(messageContext);
    }

    public static DBExecutor fetchUserPrefs(MessageContext messageContext) {
        return new FetchUserPrefsExecutor(messageContext);
    }
}
