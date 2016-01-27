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
      result = UserExecutorFactory.getInstance(CreateUserExecutor.class).execute(messageContext);
      break;
    case CommandConstants.UPDATE_USER:
      result = UserExecutorFactory.getInstance(UpdateUserExecutor.class).execute(messageContext);
      break;
    case CommandConstants.GET_USER:
      result = UserExecutorFactory.getInstance(FetchUserExecutor.class).execute(messageContext);
      break;
    case CommandConstants.GET_USER_FIND:
      result = UserExecutorFactory.getInstance(FindUserExecutor.class).execute(messageContext);
      break;
    case CommandConstants.RESET_PASSWORD:
      result = UserExecutorFactory.getInstance(ResetPasswordExecutor.class).execute(messageContext);
      break;
    case CommandConstants.UPDATE_PASSWORD:
      if (messageContext.user().getUserId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
        result = UserExecutorFactory.getInstance(ResetUnAuthenticateUserPasswordExecutor.class).execute(messageContext);
      } else {
        result = UserExecutorFactory.getInstance(ResetAuthenticateUserPasswordExecutor.class).execute(messageContext);
      }
      break;
    case CommandConstants.RESET_EMAIL_ADDRESS:
      result = UserExecutorFactory.getInstance(UpdateUserEmailExecutor.class).execute(messageContext);
      break;
    case CommandConstants.RESEND_CONFIRMATION_EMAIL:
      result = UserExecutorFactory.getInstance(ResendConfirmationEmailExecutor.class).execute(messageContext);
      break;
    case CommandConstants.CONFIRMATION_EMAIL:
      result = UserExecutorFactory.getInstance(ConfirmUserEmailExecutor.class).execute(messageContext);
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }
}
