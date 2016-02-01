package org.gooru.auth.handlers.processors.command.executor.authentication;

import java.util.HashMap;
import java.util.Map;

import org.gooru.auth.handlers.processors.command.executor.Executor;

public class AuthenticationExecutorFactory {

  private static final Map<Class<?>, Executor> instances = new HashMap<>();

  public static Executor getInstance(Class<?> classz) {
    Executor executor = instances.get(classz);
    if (executor == null) {
      synchronized (AuthenticationExecutorFactory.class) {
        if (classz.equals(CreateAnonymousAccessTokenExecutor.class)) {
          executor = new CreateAnonymousAccessTokenExecutor();
        } else if (classz.equals(CreateBasicAuthAccessTokenExecutor.class)) {
          executor = new CreateBasicAuthAccessTokenExecutor();
        } else if (classz.equals(DeleteAccessTokenExecutor.class)) {
          executor = new DeleteAccessTokenExecutor();
        } else if (classz.equals(FetchAccessTokenExecutor.class)) {
          executor = new FetchAccessTokenExecutor();
        }

      }
      instances.put(classz, executor);
    }

    return executor;

  }
}
