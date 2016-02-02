package org.gooru.auth.handlers.processors.messageProcessor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.processors.command.executor.ExecutorType;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.command.executor.authentication.AuthenticationExecutorFactory;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticationMessageProcessor implements MessageProcessorHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationMessageProcessor.class);

  @Override
  public MessageResponse process(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.ANONYMOUS_CREATE_ACCESS_TOKEN:
      result = AuthenticationExecutorFactory.getInstance(ExecutorType.Authentication.CREATE_ANONYMOUS_ACCESS_TOKEN).execute(messageContext);
      break;
    case CommandConstants.CREATE_ACCESS_TOKEN:
      result = AuthenticationExecutorFactory.getInstance(ExecutorType.Authentication.CREATE_AUTHENTICATE_ACCESS_TOKEN).execute(messageContext);
      break;
    case CommandConstants.DELETE_ACCESS_TOKEN:
      result = AuthenticationExecutorFactory.getInstance(ExecutorType.Authentication.DELETE_ACCESS_TOKEN).execute(messageContext);
      break;
    case CommandConstants.GET_ACCESS_TOKEN:
      result = AuthenticationExecutorFactory.getInstance(ExecutorType.Authentication.FETCH_ACCESS_TOKEN).execute(messageContext);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

}
