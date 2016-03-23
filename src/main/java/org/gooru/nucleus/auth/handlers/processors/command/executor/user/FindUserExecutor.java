package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import java.util.Map;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.exceptions.BadRequestException;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;

public final class FindUserExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;
  private UserRepo userRepo;
  
  public FindUserExecutor() {
     this.userIdentityRepo = UserIdentityRepo.instance();
     this.userRepo = UserRepo.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String username = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_USERNAME);
    final String email = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_EMAIL);
    return findUser(username, email);
  }

  private MessageResponse findUser(String username, String email) {
    AJEntityUserIdentity userIdentity = null;
    if (username != null) {
      userIdentity = getUserIdentityRepo().getUserIdentityByUsername(username);
    } else if (email != null) {
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(email);
    } else {
      throw new BadRequestException("Invalid param type passed");
    }
    final Map<String, Object> user = getUserRepo().findUser(userIdentity.getUserId());
    return new MessageResponse.Builder().setResponseBody(AJResponseJsonTransformer.transform(user)).setContentTypeJson().setStatusOkay().successful().build();
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }

  public void setUserRepo(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

}
