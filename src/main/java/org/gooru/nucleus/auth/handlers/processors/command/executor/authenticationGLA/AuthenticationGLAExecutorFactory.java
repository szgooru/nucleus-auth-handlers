package org.gooru.nucleus.auth.handlers.processors.command.executor.authenticationGLA;

import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class AuthenticationGLAExecutorFactory {
  
  public static DBExecutor createGLAAnonymousAccessToken(MessageContext messageContext) {
    return new CreateGLAAnonymousAccessTokenExecutor(messageContext);
  }
  
  public static DBExecutor CreateGLABasicAuthAccessToken(MessageContext messageContext) {
    return new CreateGLABasicAuthAccessTokenExecutor(messageContext);
  }
}
