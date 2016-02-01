package org.gooru.auth.handlers.processors.command.executor.user;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.exceptions.BadRequestException;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;

public final class FindUserExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private final static String[] RESPONSE_FIELDS = { "user_id", "username", "email_id" };

  interface Find {
    MessageResponse user(String username, String email);
  }

  public FindUserExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String username = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_USERNAME);
    final String email = messageContext.requestParams().getString(ParameterConstants.PARAM_USER_EMAIL);
    return find.user(username, email);
  }

  private final Find find = (String username, String email) -> {
    AJEntityUserIdentity userIdentity = null;
    if (username != null) {
      userIdentity = getUserIdentityRepo().getUserIdentityByUsername(username);
    } else if (email != null) {
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(email);
    } else {
      throw new BadRequestException("Invalid param type passed");
    }
    JsonObject result = userIdentity != null ? new JsonObject(userIdentity.toJson(false, RESPONSE_FIELDS)) : new JsonObject();
    return new MessageResponse.Builder().setResponseBody(result).setContentTypeJson().setStatusOkay().successful().build();
  };

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

}
