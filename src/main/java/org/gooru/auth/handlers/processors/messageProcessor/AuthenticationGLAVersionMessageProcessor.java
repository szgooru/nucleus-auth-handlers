package org.gooru.auth.handlers.processors.messageProcessor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.processors.command.executor.ExecutorType;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.command.executor.authenticationGLA.AuthenticationGLAExecutorFactory;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public final class AuthenticationGLAVersionMessageProcessor implements MessageProcessorHandler {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationGLAVersionMessageProcessor.class);

  @Override
  public MessageResponse process(MessageContext messageContext) {
    MessageResponse result = null;
    switch (messageContext.command()) {
    case CommandConstants.ANONYMOUS_CREATE_ACCESS_TOKEN:
      result = AuthenticationGLAExecutorFactory.getInstance(ExecutorType.AuthenticationGLAVersion.CREATE_ANONYMOUS_ACCESS_TOKEN).execute(messageContext);
      break;
    case CommandConstants.CREATE_ACCESS_TOKEN:
      result = AuthenticationGLAExecutorFactory.getInstance(ExecutorType.AuthenticationGLAVersion.CREATE_AUTHENTICATE_ACCESS_TOKEN).execute(messageContext);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

}
