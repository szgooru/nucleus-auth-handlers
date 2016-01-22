package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.gooru.auth.handlers.infra.ConfigRegistry;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.data.transform.model.UserPrefsDTO;
import org.gooru.auth.handlers.processors.event.Event;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.auth.handlers.processors.service.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;

public class UserPrefsServiceImpl extends ServerValidatorUtility implements UserPrefsService {

  private UserPreferenceRepo userPreferenceRepo;

  private UserIdentityRepo userIdentityRepo;

  private RedisClient redisClient;

  public UserPrefsServiceImpl() {
    setUserPreferenceRepo(UserPreferenceRepo.instance());
    setUserIdentityRepo(UserIdentityRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse updateUserPreference(String token, String userId, UserPrefsDTO userPrefsDTO) {
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVTED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userId);
    boolean isNew = false;
    if (userPreference == null) {
      userPreference = new AJEntityUserPreference();
      userPreference.setUserId(userId);
      if (userPrefsDTO.getStandardPreference() == null) {
        userPreference.setStandardPreference(ConfigRegistry.instance().getDefaultUserPrefs());
      }
      isNew = true;
    }
    if (userPrefsDTO.getStandardPreference() != null) {
      userPreference.setStandardPreference(userPrefsDTO.getStandardPreference());
      JsonObject accessToken = getRedisClient().getJsonObject(token);
      accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, userPreference.getStandardPreference());
      getRedisClient().set(token, accessToken.toString());
    }

    if (userPrefsDTO.getProfileVisiblity() != null) {
      userPreference.setProfileVisiblity(userPrefsDTO.getProfileVisiblity());
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

  @Override
  public MessageResponse getUserPreference(String userId) {
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
