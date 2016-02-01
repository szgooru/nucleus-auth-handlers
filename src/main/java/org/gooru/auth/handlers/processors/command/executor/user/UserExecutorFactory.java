package org.gooru.auth.handlers.processors.command.executor.user;

import java.util.HashMap;
import java.util.Map;

import org.gooru.auth.handlers.processors.command.executor.Executor;

public class UserExecutorFactory {
  private static final Map<Class<?>, Executor> instances = new HashMap<>();

  public static Executor getInstance(Class<?> classz) {
    Executor executor = instances.get(classz);
    if (executor == null) {
      synchronized (UserExecutorFactory.class) {
        if (classz.equals(CreateUserExecutor.class)) {
          executor = new CreateUserExecutor();
        } else if (classz.equals(UpdateUserExecutor.class)) {
          executor = new UpdateUserExecutor();
        } else if (classz.equals(FetchUserExecutor.class)) {
          executor = new FetchUserExecutor();
        } else if (classz.equals(FindUserExecutor.class)) {
          executor = new FindUserExecutor();
        } else if (classz.equals(ResendConfirmationEmailExecutor.class)) {
          executor = new ResendConfirmationEmailExecutor();
        } else if (classz.equals(ResetAuthenticateUserPasswordExecutor.class)) {
          executor = new ResetAuthenticateUserPasswordExecutor();
        } else if (classz.equals(ResetUnAuthenticateUserPasswordExecutor.class)) {
          executor = new ResetUnAuthenticateUserPasswordExecutor();
        } else if (classz.equals(UpdateUserEmailExecutor.class)) {
          executor = new UpdateUserEmailExecutor();
        } else if (classz.equals(ResetPasswordExecutor.class)) {
          executor = new ResetPasswordExecutor();
        } else if (classz.equals(ConfirmUserEmailExecutor.class)) {
          executor = new ConfirmUserEmailExecutor();
        }
      }
      instances.put(classz, executor);
    }

    return executor;

  }
}
