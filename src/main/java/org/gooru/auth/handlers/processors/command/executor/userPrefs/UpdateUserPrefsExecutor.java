package org.gooru.auth.handlers.processors.command.executor.userPrefs;

import static org.gooru.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.gooru.auth.handlers.infra.ConfigRegistry;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.data.transform.model.UserPrefsDTO;
import org.gooru.auth.handlers.processors.event.Event;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;

public final class UpdateUserPrefsExecutor extends Executor {

  private UserPreferenceRepo userPreferenceRepo;

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  public UpdateUserPrefsExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
    setUserPreferenceRepo(UserPreferenceRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
    if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
      userId = messageContext.user().getUserId();
    }
    UserPrefsDTO userPrefsDTO = new UserPrefsDTO(messageContext.requestBody());
    String accessToken = messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN);

    return userPrefs(accessToken, userId, userPrefsDTO);
  }

  private MessageResponse userPrefs(String token, String userId, UserPrefsDTO userPrefsDTO) {
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userId);
    boolean isNew = false;
    if (userPreference == null) {
      userPreference = new AJEntityUserPreference();
      userPreference.setUserId(userId);
      if (userPrefsDTO.getStandardPreference() == null) {
        userPreference.setStandardPreference(ConfigRegistry.instance().getDefaultUserStandardPrefs());
      }
      isNew = true;
    }
    if (userPrefsDTO.getStandardPreference() != null) {
      userPreference.setStandardPreference(userPrefsDTO.getStandardPreference());
      JsonObject accessToken = getRedisClient().getJsonObject(token);
      accessToken.put(ParameterConstants.PARAM_TAXONOMY, userPreference.getStandardPreference());
      getRedisClient().set(token, accessToken.toString());
    }

    if (userPrefsDTO.getProfileVisibility() != null) {
      userPreference.setProfileVisiblity(userPrefsDTO.getProfileVisibility());
    }
    if (isNew) {
      getUserPreferenceRepo().createPreference(userPreference);
    } else {
      getUserPreferenceRepo().updatePreference(userPreference);
    }
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.putPayLoadObject(SchemaConstants.USER_PREFERENCE,
            AJResponseJsonTransformer.transform(userPreference.toJson(false), HelperConstants.USERS_PREFS_JSON_FIELDS)).setEventName(
            Event.UPDATE_USER_PREFS.getName());
    return new MessageResponse.Builder().setContentTypeJson().setEventData(eventBuilder.build()).setStatusNoOutput().successful().build();
  }

  public UserPreferenceRepo getUserPreferenceRepo() {
    return userPreferenceRepo;
  }

  public void setUserPreferenceRepo(UserPreferenceRepo userPreferenceRepo) {
    this.userPreferenceRepo = userPreferenceRepo;
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }
}
