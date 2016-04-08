package org.gooru.nucleus.auth.handlers.processors.command.executor.authentication;

import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;

public final class AuthenticationExecutorFactory {

  public static DBExecutor createAnonymousAccessToken(MessageContext messageContext) {
    return new CreateAnonymousAccessTokenExecutor(messageContext);
  }
  
  public static DBExecutor createBasicAuthAccessToken(MessageContext messageContext) {
    return new CreateBasicAuthAccessTokenExecutor(messageContext);
  }
  
  public static DBExecutor deleteAccessToken(MessageContext messageContext) {
    return new DeleteAccessTokenExecutor(messageContext);
  }
  
  public static DBExecutor fetchAccessToken(MessageContext messageContext) {
    return new FetchAccessTokenExecutor(messageContext);
  }
}
