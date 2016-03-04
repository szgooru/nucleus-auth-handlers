package org.gooru.nucleus.auth.handlers.processors.command.executor.userPrefs;

import java.util.HashMap;
import java.util.Map;

import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.ExecutorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class UserPrefsExecutorFactory {

  private static final Map<ExecutorType.UserPrefs, Executor> instances = new HashMap<>();

  private static final Logger LOG = LoggerFactory.getLogger(UserPrefsExecutorFactory.class);

  public static Executor getInstance(ExecutorType.UserPrefs executorType) {
    Executor executor = instances.get(executorType);
    if (executor == null) {
      synchronized (UserPrefsExecutorFactory.class) {
        if (executorType.equals(ExecutorType.UserPrefs.UPDATE_USER_PREFS)) {
          executor = new UpdateUserPrefsExecutor();
        } else if (executorType.equals(ExecutorType.UserPrefs.FETCH_USER_PREFS)) {
          executor = new FetchUserPrefsExecutor();
        } else {
          LOG.debug("None of the user executor matched, looks like invalid executor type.");
        }
      }
      instances.put(executorType, executor);
    }

    return executor;

  }
}
