package org.gooru.auth.handlers.processors.messageProcessor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.command.executor.authentication.AuthenticationExecutorFactory;
import org.gooru.auth.handlers.processors.command.executor.authentication.CreateAnonymousAccessTokenExecutor;
import org.gooru.auth.handlers.processors.command.executor.authentication.CreateBasicAuthAccessTokenExecutor;
import org.gooru.auth.handlers.processors.command.executor.authentication.DeleteAccessTokenExecutor;
import org.gooru.auth.handlers.processors.command.executor.authentication.FetchAccessTokenExecutor;
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
      result = AuthenticationExecutorFactory.getInstance(CreateAnonymousAccessTokenExecutor.class).execute(messageContext);
      break;
    case CommandConstants.CREATE_ACCESS_TOKEN:
      result = AuthenticationExecutorFactory.getInstance(CreateBasicAuthAccessTokenExecutor.class).execute(messageContext);
      break;
    case CommandConstants.DELETE_ACCESS_TOKEN:
      result = AuthenticationExecutorFactory.getInstance(DeleteAccessTokenExecutor.class).execute(messageContext);
      break;
    case CommandConstants.GET_ACCESS_TOKEN:
      result = AuthenticationExecutorFactory.getInstance(FetchAccessTokenExecutor.class).execute(messageContext);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

}
