package org.gooru.auth.handlers.processors.command.executor.user;

import java.util.Map;

import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;

public final class FetchUserExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  public FetchUserExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
    setUserRepo(UserRepo.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
    if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
      userId = messageContext.user().getString(ParameterConstants.PARAM_USER_ID);
    }
    return fetchUser(userId);
  }

  private MessageResponse fetchUser(String userId) {
    final Map<String, Object> user = getUserRepo().findUser(userId);
    ServerValidatorUtility.rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
    return new MessageResponse.Builder().setResponseBody(AJResponseJsonTransformer.transform(user)).setContentTypeJson().setStatusOkay().successful()
            .build();
  };

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }

  public void setUserRepo(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

}
