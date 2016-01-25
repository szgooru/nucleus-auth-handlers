package org.gooru.auth.handlers.processors.messageProcessor;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.command.executor.user.ConfirmUserEmailExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.CreateUserExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.FetchUserExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.FindUserExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.ResendConfirmationEmailExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.ResetAuthenticateUserPasswordExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.ResetPasswordExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.ResetUnAuthenticateUserPasswordExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.UpdateUserEmailExecutor;
import org.gooru.auth.handlers.processors.command.executor.user.UpdateUserExecutor;
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
      result = new CreateUserExecutor().execute(messageContext);
      break;
    case CommandConstants.UPDATE_USER:
      result = new UpdateUserExecutor().execute(messageContext);
      break;
    case CommandConstants.GET_USER:
      result = new FetchUserExecutor().execute(messageContext);
      break;
    case CommandConstants.GET_USER_FIND:
      result = new FindUserExecutor().execute(messageContext);
      break;
    case CommandConstants.RESET_PASSWORD:
      result = new ResetPasswordExecutor().execute(messageContext);
      break;
    case CommandConstants.UPDATE_PASSWORD:
      if (messageContext.user().getUserId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
        result = new ResetUnAuthenticateUserPasswordExecutor().execute(messageContext);
      } else {
        result = new ResetAuthenticateUserPasswordExecutor().execute(messageContext);
      }
      break;
    case CommandConstants.RESET_EMAIL_ADDRESS:
      result = new UpdateUserEmailExecutor().execute(messageContext);
      break;
    case CommandConstants.RESEND_CONFIRMATION_EMAIL:
      result = new ResendConfirmationEmailExecutor().execute(messageContext);
      break;
    case CommandConstants.CONFIRMATION_EMAIL:
      result = new ConfirmUserEmailExecutor().execute(messageContext);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }
}
