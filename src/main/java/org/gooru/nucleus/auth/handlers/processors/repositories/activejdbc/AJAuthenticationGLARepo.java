package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.command.executor.authenticationGLA.AuthenticationGLAExecutorFactory;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthenticationGLARepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;

public class AJAuthenticationGLARepo implements AuthenticationGLARepo {
  private final MessageContext messageContext;

  public AJAuthenticationGLARepo(MessageContext messageContext) {
    this.messageContext = messageContext;
  }

  @Override
  public MessageResponse createGLAAnonymousAccessToken() {
    return TransactionExecutor.executeTransaction(AuthenticationGLAExecutorFactory.createGLAAnonymousAccessToken(messageContext));
  }

  @Override
  public MessageResponse createGLABasicAuthAccessToken() {
    return TransactionExecutor.executeTransaction(AuthenticationGLAExecutorFactory.CreateGLABasicAuthAccessToken(messageContext));
  }
}
