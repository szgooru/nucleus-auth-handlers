package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class UserExecutorFactory {

    public static DBExecutor createUser(MessageContext messageContext) {
        return new CreateUserExecutor(messageContext);
    }

    public static DBExecutor updateUser(MessageContext messageContext) {
        return new UpdateUserExecutor(messageContext);
    }

    public static DBExecutor fetchUser(MessageContext messageContext) {
        return new FetchUserExecutor(messageContext);
    }

    public static DBExecutor findUser(MessageContext messageContext) {
        return new FindUserExecutor(messageContext);
    }

    public static DBExecutor resendConfirmationEmail(MessageContext messageContext) {
        return new ResendConfirmationEmailExecutor(messageContext);
    }

    public static DBExecutor resetAuthenticateUserPassword(MessageContext messageContext) {
        return new ResetAuthenticateUserPasswordExecutor(messageContext);
    }

    public static DBExecutor resetUnAuthenticateUserPassword(MessageContext messageContext) {
        return new ResetUnAuthenticateUserPasswordExecutor(messageContext);
    }

    public static DBExecutor updateUserEmail(MessageContext messageContext) {
        return new UpdateUserEmailExecutor(messageContext);
    }

    public static DBExecutor resetPassword(MessageContext messageContext) {
        return new ResetPasswordExecutor(messageContext);
    }

    public static DBExecutor confirmUserEmail(MessageContext messageContext) {
        return new ConfirmUserEmailExecutor(messageContext);
    }

    public static DBExecutor findUsers(MessageContext messageContext) {
        return new FindUsersExecutor(messageContext);
    }
}
