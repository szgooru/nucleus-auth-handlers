package org.gooru.auth.handlers.processors.messageProcessor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.processors.command.executor.ExecutorType;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.command.executor.user.UserExecutorFactory;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserMessageProcessor implements MessageProcessorHandler {

  private static final Logger LOG = LoggerFactory.getLogger(UserMessageProcessor.class);

  @Override
  public MessageResponse process(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.CREATE_USER:
      result = UserExecutorFactory.getInstance(ExecutorType.User.CREATE_USER).execute(messageContext);
      break;
    case CommandConstants.UPDATE_USER:
      result = UserExecutorFactory.getInstance(ExecutorType.User.UPDATE_USER).execute(messageContext);
      break;
    case CommandConstants.GET_USER:
      result = UserExecutorFactory.getInstance(ExecutorType.User.FETCH_USER).execute(messageContext);
      break;
    case CommandConstants.GET_USER_FIND:
      result = UserExecutorFactory.getInstance(ExecutorType.User.FIND_USER).execute(messageContext);
      break;
    case CommandConstants.RESET_PASSWORD:
      result = UserExecutorFactory.getInstance(ExecutorType.User.RESET_PASSWORD).execute(messageContext);
      break;
    case CommandConstants.UPDATE_PASSWORD:
      if (messageContext.user().getUserId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
        result = UserExecutorFactory.getInstance(ExecutorType.User.RESET_UNAUTHENTICATE_USER_PASSWORD).execute(messageContext);
      } else {
        result = UserExecutorFactory.getInstance(ExecutorType.User.RESET_AUTHENTICATE_USER_PASSWORD).execute(messageContext);
      }
      break;
    case CommandConstants.RESET_EMAIL_ADDRESS:
      result = UserExecutorFactory.getInstance(ExecutorType.User.UPDATE_USER_EMAIL).execute(messageContext);
      break;
    case CommandConstants.RESEND_CONFIRMATION_EMAIL:
      result = UserExecutorFactory.getInstance(ExecutorType.User.RESEND_CONFIRMATION_MAIL).execute(messageContext);
      break;
    case CommandConstants.CONFIRMATION_EMAIL:
      result = UserExecutorFactory.getInstance(ExecutorType.User.CONFIRM_USER_EMAIL).execute(messageContext);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }
}
