package org.gooru.nucleus.auth.handlers.processors.messageProcessor;

import org.gooru.nucleus.auth.handlers.constants.CommandConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.nucleus.auth.handlers.processors.repositories.RepoFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InternalMessageProcessor implements MessageProcessorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(InternalMessageProcessor.class);
    
    @Override
    public MessageResponse process(MessageContext messageContext) {
        MessageResponse result = null;
        switch (messageContext.command()) {
        case CommandConstants.USER_DETAILS:
            result = RepoFactory.getInternalRepo(messageContext).userDetails();
            break;
            
        case CommandConstants.LOGIN_AS_USER:
            result = RepoFactory.getInternalRepo(messageContext).loginAsUser();
            break;
            
        default:
            LOGGER.error("Invalid command type passed in, not able to handle");
            throw new InvalidRequestException();
        }
        return result;
    }

}
