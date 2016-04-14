package org.gooru.nucleus.auth.handlers.processors.messageProcessor;

import org.gooru.nucleus.auth.handlers.constants.CommandConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserMessageProcessor implements MessageProcessorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(UserMessageProcessor.class);

    @Override
    public MessageResponse process(MessageContext messageContext) {
        MessageResponse result = null;
        switch (messageContext.command()) {
        case CommandConstants.CREATE_USER:
            result = RepoFactory.getUserRepo(messageContext).createUser();
            break;
        case CommandConstants.UPDATE_USER:
            result = RepoFactory.getUserRepo(messageContext).updateUser();
            break;
        case CommandConstants.GET_USER:
            result = RepoFactory.getUserRepo(messageContext).fetchUser();
            break;
        case CommandConstants.GET_USER_FIND:
            result = RepoFactory.getUserRepo(messageContext).findUser();
            break;
        case CommandConstants.RESET_PASSWORD:
            result = RepoFactory.getUserRepo(messageContext).resetPassword();
            break;
        case CommandConstants.UPDATE_PASSWORD:
            if (messageContext.user().getUserId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
                result = RepoFactory.getUserRepo(messageContext).resetUnAuthenticateUserPassword();
            } else {
                result = RepoFactory.getUserRepo(messageContext).resetAuthenticateUserPassword();
            }
            break;
        case CommandConstants.RESET_EMAIL_ADDRESS:
            result = RepoFactory.getUserRepo(messageContext).updateUserEmail();
            break;
        case CommandConstants.RESEND_CONFIRMATION_EMAIL:
            result = RepoFactory.getUserRepo(messageContext).resendConfirmationEmail();
            break;
        case CommandConstants.CONFIRMATION_EMAIL:
            result = RepoFactory.getUserRepo(messageContext).confirmUserEmail();
            break;
        case CommandConstants.GET_USERS_FIND:
            result = RepoFactory.getUserRepo(messageContext).findUsers();
            break;
        default:
            LOG.error("Invalid command type passed in, not able to handle");
            throw new InvalidRequestException();
        }
        return result;
    }
}
