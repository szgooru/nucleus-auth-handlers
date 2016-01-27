package org.gooru.auth.handlers.processors.command.executor.userPrefs;

import java.util.HashMap;
import java.util.Map;

import org.gooru.auth.handlers.processors.command.executor.Executor;

public class UserPrefsExecutorFactory {

  private static Map<Class<?>, Executor> instances = new HashMap<>();

  public static Executor getInstance(Class<?> classz) {
    Executor executor = instances.get(classz);
    if (executor == null) {
      synchronized (UserPrefsExecutorFactory.class) {
        if (classz.equals(UpdateUserPrefsExecutor.class)) {
          executor = new UpdateUserPrefsExecutor();
        } else if (classz.equals(FetchUserPrefsExecutor.class)) {
          executor = new FetchUserPrefsExecutor();
        }
      }
      instances.put(classz, executor);
    }

    return executor;

  }
}
