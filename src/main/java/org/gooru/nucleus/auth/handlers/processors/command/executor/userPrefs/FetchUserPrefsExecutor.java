package org.gooru.nucleus.auth.handlers.processors.command.executor.userPrefs;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.javalite.activejdbc.LazyList;

public final class FetchUserPrefsExecutor implements DBExecutor {

  private final MessageContext messageContext;
  private String userId;

  public FetchUserPrefsExecutor(MessageContext messageContext) {
    this.messageContext = messageContext;
  }

  @Override
  public void checkSanity() {
    userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
    if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
      userId = messageContext.user().getUserId();
    }

  }

  @Override
  public void validateRequest() {
  }

  @Override
  public MessageResponse executeRequest() {
    LazyList<AJEntityUserPreference> results = AJEntityUserPreference.where(AJEntityUserPreference.GET_USER_PREFERENCE, userId);
    AJEntityUserPreference userPreference = results.size() > 0 ? results.get(0) : null;
    JsonObject result = null;
    if (userPreference != null) {
      result = AJResponseJsonTransformer.transform(userPreference.toJson(false), HelperConstants.USERS_PREFS_JSON_FIELDS);
    } else {
      result = ConfigRegistry.instance().getDefaultUserPrefs();
    }
    return new MessageResponse.Builder().setResponseBody(result).setContentTypeJson().setStatusOkay().successful().build();
  }

  @Override
  public boolean handlerReadOnly() {
    return true;
  }

}
