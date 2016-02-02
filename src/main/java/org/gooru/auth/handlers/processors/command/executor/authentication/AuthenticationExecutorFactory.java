package org.gooru.auth.handlers.processors.command.executor.authentication;

import java.util.HashMap;
import java.util.Map;

import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.ExecutorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticationExecutorFactory {

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationExecutorFactory.class);

  private static final Map<ExecutorType.Authentication, Executor> instances = new HashMap<>();

  public static Executor getInstance(ExecutorType.Authentication executorType) {
    Executor executor = instances.get(executorType);
    if (executor == null) {
      synchronized (AuthenticationExecutorFactory.class) {
        if (executorType.equals(ExecutorType.Authentication.CREATE_ANONYMOUS_ACCESS_TOKEN)) {
          executor = new CreateAnonymousAccessTokenExecutor();
        } else if (executorType.equals(ExecutorType.Authentication.CREATE_AUTHENTICATE_ACCESS_TOKEN)) {
          executor = new CreateBasicAuthAccessTokenExecutor();
        } else if (executorType.equals(ExecutorType.Authentication.DELETE_ACCESS_TOKEN)) {
          executor = new DeleteAccessTokenExecutor();
        } else if (executorType.equals(ExecutorType.Authentication.FETCH_ACCESS_TOKEN)) {
          executor = new FetchAccessTokenExecutor();
        } else {
          LOG.debug("None of the authentication executor matched, looks like invalid executor type.");
        } 
      }
      instances.put(executorType, executor);
    }

    return executor;

  }
}
