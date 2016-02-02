package org.gooru.auth.handlers.processors.command.executor.authorize;

import java.util.HashMap;
import java.util.Map;

import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.ExecutorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthorizeExecutorFactory {

  private static final Map<ExecutorType.Authorize, Executor> instances = new HashMap<>();

  private static final Logger LOG = LoggerFactory.getLogger(AuthorizeExecutorFactory.class);
  
  public static Executor getInstance(ExecutorType.Authorize executorType) {
    Executor executor = instances.get(executorType);
    if (executor == null) {
      synchronized (AuthorizeExecutorFactory.class) {
        if (executorType.equals(ExecutorType.Authorize.AUTHORIZE_USER)) {
          executor = new AuthorizeUserExecutor();
        }  else {
          LOG.debug("None of the authorize executor matched, looks like invalid executor type.");
        }
      }
      instances.put(executorType, executor);
    }

    return executor;

  }
}
