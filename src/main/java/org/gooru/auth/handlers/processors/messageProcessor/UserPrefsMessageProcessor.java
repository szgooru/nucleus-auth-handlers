package org.gooru.auth.handlers.processors.messageProcessor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.command.executor.userPrefs.FetchUserPrefsExecutor;
import org.gooru.auth.handlers.processors.command.executor.userPrefs.UpdateUserPrefsExecutor;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserPrefsMessageProcessor implements MessageProcessorHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UserPrefsMessageProcessor.class);

  @Override
  public MessageResponse process(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.UPDATE_USER_PREFERENCE:
      result = new UpdateUserPrefsExecutor().execute(messageContext);
      break;
    case CommandConstants.GET_USER_PREFERENCE:
      result = new FetchUserPrefsExecutor().execute(messageContext);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }
}
