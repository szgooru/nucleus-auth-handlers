package org.gooru.auth.handlers.processors.command.executor.authenticationGLA;

import java.util.HashMap;
import java.util.Map;

import org.gooru.auth.handlers.processors.command.executor.Executor;

public class AuthenticationGLAExecutorFactory {

  private static final Map<Class<?>, Executor> instances = new HashMap<>();

  public static Executor getInstance(Class<?> classz) {
    Executor executor = instances.get(classz);
    if (executor == null) {
      synchronized (AuthenticationGLAExecutorFactory.class) {
        if (classz.equals(CreateGLAAnonymousAccessTokenExecutor.class)) {
          executor = new CreateGLAAnonymousAccessTokenExecutor();
        } else if (classz.equals(CreateGLABasicAuthAccessTokenExecutor.class)) {
          executor = new CreateGLABasicAuthAccessTokenExecutor();
        }
      }
      instances.put(classz, executor);
    }

    return executor;

  }
}
