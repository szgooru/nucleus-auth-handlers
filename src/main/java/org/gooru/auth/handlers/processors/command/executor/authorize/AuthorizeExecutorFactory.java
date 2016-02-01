package org.gooru.auth.handlers.processors.command.executor.authorize;

import java.util.HashMap;
import java.util.Map;

import org.gooru.auth.handlers.processors.command.executor.Executor;

public class AuthorizeExecutorFactory {

  private static final Map<Class<?>, Executor> instances = new HashMap<>();

  public static Executor getInstance(Class<?> classz) {
    Executor executor = instances.get(classz);
    if (executor == null) {
      synchronized (AuthorizeExecutorFactory.class) {
        if (classz.equals(AuthorizeUserExecutor.class)) {
          executor = new AuthorizeUserExecutor();
        }
      }
      instances.put(classz, executor);
    }

    return executor;

  }
}
