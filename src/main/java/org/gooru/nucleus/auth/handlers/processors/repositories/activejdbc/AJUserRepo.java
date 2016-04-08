package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.command.executor.user.UserExecutorFactory;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions.TransactionExecutor;

public class AJUserRepo implements UserRepo {
  private final MessageContext messageContext;

  public AJUserRepo(MessageContext messageContext) {
    this.messageContext = messageContext;
  }

  @Override
  public MessageResponse createUser() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.createUser(messageContext));
  }

  @Override
  public MessageResponse updateUser() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.updateUser(messageContext));
  }

  @Override
  public MessageResponse findUser() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.findUser(messageContext));
  }

  @Override
  public MessageResponse findUsers() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.findUsers(messageContext));
  }

  @Override
  public MessageResponse fetchUser() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.fetchUser(messageContext));
  }

  @Override
  public MessageResponse resetAuthenticateUserPassword() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.resetAuthenticateUserPassword(messageContext));
  }

  @Override
  public MessageResponse resetPassword() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.resetPassword(messageContext));
  }

  @Override
  public MessageResponse resetUnAuthenticateUserPassword() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.resetUnAuthenticateUserPassword(messageContext));
  }

  @Override
  public MessageResponse updateUserEmail() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.updateUserEmail(messageContext));
  }

  @Override
  public MessageResponse confirmUserEmail() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.confirmUserEmail(messageContext));
  }

  @Override
  public MessageResponse resendConfirmationEmail() {
    return TransactionExecutor.executeTransaction(UserExecutorFactory.resendConfirmationEmail(messageContext));
  }

}
