package org.gooru.nucleus.auth.handlers.processors.command.executor.userPrefs;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;

public final class FetchUserPrefsExecutor extends Executor {

  private UserPreferenceRepo userPreferenceRepo;

  interface Fetch {
    MessageResponse userPrefs(String userId);
  }

  public FetchUserPrefsExecutor() {
    setUserPreferenceRepo(UserPreferenceRepo.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
    if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
      userId = messageContext.user().getUserId();
    }
    return fetchUserPrefs(userId);
  }

  private MessageResponse fetchUserPrefs(String userId) {
    final AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userId);
    JsonObject result = null;
    if (userPreference != null) {
      result = AJResponseJsonTransformer.transform(userPreference.toJson(false), HelperConstants.USERS_PREFS_JSON_FIELDS);
    } else {
      result = ConfigRegistry.instance().getDefaultUserPrefs();
    }

    return new MessageResponse.Builder().setResponseBody(result).setContentTypeJson().setStatusOkay().successful().build();
  }

  public UserPreferenceRepo getUserPreferenceRepo() {
    return userPreferenceRepo;
  }

  public void setUserPreferenceRepo(UserPreferenceRepo userPreferenceRepo) {
    this.userPreferenceRepo = userPreferenceRepo;
  }

}
