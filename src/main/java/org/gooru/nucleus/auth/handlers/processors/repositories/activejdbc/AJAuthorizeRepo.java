package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.command.executor.authorize.AuthorizeExecutorFactory;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthorizeRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;

public class AJAuthorizeRepo implements AuthorizeRepo {
  private final MessageContext messageContext;

  public AJAuthorizeRepo(MessageContext messageContext) {
    this.messageContext = messageContext;
  }
  
  @Override
  public MessageResponse authorize() {
    return TransactionExecutor.executeTransaction(AuthorizeExecutorFactory.AuthorizeUser(messageContext));
  }
}
