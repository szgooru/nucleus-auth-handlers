package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import java.util.HashMap;
import java.util.Map;

import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.ExecutorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserExecutorFactory {
  private static final Map<ExecutorType.User, Executor> instances = new HashMap<>();

  private static final Logger LOG = LoggerFactory.getLogger(UserExecutorFactory.class);

  public static Executor getInstance(ExecutorType.User executorType) {
    Executor executor = instances.get(executorType);
    if (executor == null) {
      synchronized (UserExecutorFactory.class) {
        if (executorType.equals(ExecutorType.User.CREATE_USER)) {
          executor = new CreateUserExecutor();
        } else if (executorType.equals(ExecutorType.User.UPDATE_USER)) {
          executor = new UpdateUserExecutor();
        } else if (executorType.equals(ExecutorType.User.FETCH_USER)) {
          executor = new FetchUserExecutor();
        } else if (executorType.equals(ExecutorType.User.FIND_USER)) {
          executor = new FindUserExecutor();
        } else if (executorType.equals(ExecutorType.User.RESEND_CONFIRMATION_MAIL)) {
          executor = new ResendConfirmationEmailExecutor();
        } else if (executorType.equals(ExecutorType.User.RESET_AUTHENTICATE_USER_PASSWORD)) {
          executor = new ResetAuthenticateUserPasswordExecutor();
        } else if (executorType.equals(ExecutorType.User.RESET_UNAUTHENTICATE_USER_PASSWORD)) {
          executor = new ResetUnAuthenticateUserPasswordExecutor();
        } else if (executorType.equals(ExecutorType.User.UPDATE_USER_EMAIL)) {
          executor = new UpdateUserEmailExecutor();
        } else if (executorType.equals(ExecutorType.User.RESET_PASSWORD)) {
          executor = new ResetPasswordExecutor();
        } else if (executorType.equals(ExecutorType.User.RESEND_CONFIRMATION_MAIL)) {
          executor = new ConfirmUserEmailExecutor();
        } else if (executorType.equals(ExecutorType.User.FIND_USERS)) {
          executor = new FindUsersExecutor();
        } else {
          LOG.debug("None of the user executor matched, looks like invalid executor type.");
        }
      }
      instances.put(executorType, executor);
    }

    return executor;

  }
}
