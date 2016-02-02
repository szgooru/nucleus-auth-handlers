package org.gooru.auth.handlers.processors.command.executor.authenticationGLA;

import java.util.HashMap;
import java.util.Map;

import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.ExecutorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AuthenticationGLAExecutorFactory {

  private static final Map<ExecutorType.AuthenticationGLAVersion, Executor> instances = new HashMap<>();

  private static final Logger LOG = LoggerFactory.getLogger(AuthenticationGLAExecutorFactory.class);
  
  public static Executor getInstance(ExecutorType.AuthenticationGLAVersion executorType) {
    Executor executor = instances.get(executorType);
    if (executor == null) {
      synchronized (AuthenticationGLAExecutorFactory.class) {
        if (executorType.equals(ExecutorType.AuthenticationGLAVersion.CREATE_ANONYMOUS_ACCESS_TOKEN)) {
          executor = new CreateGLAAnonymousAccessTokenExecutor();
        } else if (executorType.equals(ExecutorType.AuthenticationGLAVersion.CREATE_AUTHENTICATE_ACCESS_TOKEN)) {
          executor = new CreateGLABasicAuthAccessTokenExecutor();
        } else {
          LOG.debug("None of the authentication GLA version executor matched, looks like invalid executor type.");
        }
      }
      instances.put(executorType, executor);
    }

    return executor;

  }
}
