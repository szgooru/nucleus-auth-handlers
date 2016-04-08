package org.gooru.nucleus.auth.handlers.processors.command.executor.userPrefs;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.UserPrefsDTO;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.javalite.activejdbc.LazyList;

public final class UpdateUserPrefsExecutor implements DBExecutor {

  private RedisClient redisClient;
  private final MessageContext messageContext;
  private String userId;
  private UserPrefsDTO userPrefsDTO;
  private AJEntityUserPreference userPreference;

  public UpdateUserPrefsExecutor(MessageContext messageContext) {
    this.redisClient = RedisClient.instance();
    this.messageContext = messageContext;
  }

  @Override
  public void checkSanity() {
    userId = messageContext.requestParams().getString(MessageConstants.MSG_USER_ID);
    if (userId.equalsIgnoreCase(ParameterConstants.PARAM_ME)) {
      userId = messageContext.user().getUserId();
    }
    userPrefsDTO = new UserPrefsDTO(messageContext.requestBody());
  }

  @Override
  public void validateRequest() {
    LazyList<AJEntityUserIdentity> results = AJEntityUserIdentity.where(AJEntityUserIdentity.GET_BY_USER_ID, userId);
    final AJEntityUserIdentity userIdentity = results.size() > 0 ? results.get(0) : null;
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED), MessageCodeConstants.AU0009,
        HttpConstants.HttpStatus.FORBIDDEN.getCode());
    LazyList<AJEntityUserPreference> userPreferences = AJEntityUserPreference.where(AJEntityUserPreference.GET_USER_PREFERENCE, userId);
    userPreference = userPreferences.size() > 0 ? userPreferences.get(0) : null;
  }

  @Override
  public MessageResponse executeRequest() {
    String token = messageContext.headers().get(MessageConstants.MSG_HEADER_TOKEN);
    if (userPreference == null) {
      userPreference = new AJEntityUserPreference();
      userPreference.setUserId(UUID.fromString(userId));
      if (userPrefsDTO.getStandardPreference() == null) {
        userPreference.setStandardPreference(ConfigRegistry.instance().getDefaultUserStandardPrefs());
      }
    }
    if (userPrefsDTO.getStandardPreference() != null) {
      userPreference.setStandardPreference(userPrefsDTO.getStandardPreference());
      JsonObject accessToken = this.redisClient.getJsonObject(token);
      accessToken.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, userPreference.getStandardPreference());
      this.redisClient.set(token, accessToken.toString());
    }

    if (userPrefsDTO.getProfileVisibility() != null) {
      userPreference.setProfileVisiblity(userPrefsDTO.getProfileVisibility());
    }
    userPreference.saveIt();
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.putPayLoadObject(SchemaConstants.USER_PREFERENCE,
        AJResponseJsonTransformer.transform(userPreference.toJson(false), HelperConstants.USERS_PREFS_JSON_FIELDS)).setEventName(
        Event.UPDATE_USER_PREFS.getName());
    return new MessageResponse.Builder().setContentTypeJson().setEventData(eventBuilder.build()).setStatusNoOutput().successful().build();

  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

}
