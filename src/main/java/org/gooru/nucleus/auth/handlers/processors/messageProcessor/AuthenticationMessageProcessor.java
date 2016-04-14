package org.gooru.nucleus.auth.handlers.processors.messageProcessor;

import org.gooru.nucleus.auth.handlers.constants.CommandConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticationMessageProcessor implements MessageProcessorHandler {

    private static final Logger LOG = LoggerFactory.getLogger(AuthenticationMessageProcessor.class);

    @Override
    public MessageResponse process(MessageContext messageContext) {
        MessageResponse result = null;
        switch (messageContext.command()) {
        case CommandConstants.ANONYMOUS_CREATE_ACCESS_TOKEN:
            result = RepoFactory.getAuthenticationRepo(messageContext).createAnonymousAccessToken();
            break;
        case CommandConstants.CREATE_ACCESS_TOKEN:
            result = RepoFactory.getAuthenticationRepo(messageContext).createBasicAuthAccessToken();
            break;
        case CommandConstants.DELETE_ACCESS_TOKEN:
            result = RepoFactory.getAuthenticationRepo(messageContext).deleteAccessToken();
            break;
        case CommandConstants.GET_ACCESS_TOKEN:
            result = RepoFactory.getAuthenticationRepo(messageContext).fetchAccessToken();
            break;
        default:
            LOG.error("Invalid command type passed in, not able to handle");
            throw new InvalidRequestException();
        }
        return result;
    }

}
