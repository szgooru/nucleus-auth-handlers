package org.gooru.auth.handlers.processors.command.executor.authenticationGLA;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HelperConstants.GrantType;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.infra.ConfigRegistry;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.event.Event;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.auth.handlers.utils.InternalHelper;

public class CreateGLABasicAuthAccessTokenExecutor extends Executor {

  private RedisClient redisClient;

  private AuthClientRepo authClientRepo;

  private UserPreferenceRepo userPreferenceRepo;

  private UserIdentityRepo userIdentityRepo;

  public CreateGLABasicAuthAccessTokenExecutor() {
    setAuthClientRepo(AuthClientRepo.instance());
    setRedisClient(RedisClient.instance());
    setUserPreferenceRepo(UserPreferenceRepo.instance());
    setUserIdentityRepo(UserIdentityRepo.instance());
  }

  interface Create {
    MessageResponse accessToken(String clientKey, String requestDomain, String username, String password);
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final String password = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_PASSWORD);
    final String username = messageContext.requestBody().getString(ParameterConstants.PARAM_USER_USERNAME);
    String clientKey = messageContext.headers().get(MessageConstants.MSG_HEADER_API_KEY);
    if (clientKey == null) {
      clientKey = messageContext.requestParams().getString(ParameterConstants.PARAM_API_KEY);
    }
    final String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
    return create.accessToken(clientKey, requestDomain, username, password);
  }

  Create create = (String clientKey, String requestDomain, String username, String password) -> {
    rejectIfNullOrEmpty(username, MessageCodeConstants.AU0036, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    rejectIfNullOrEmpty(password, MessageCodeConstants.AU0037, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AJEntityAuthClient authClient = validateAuthClient(InternalHelper.encryptClientKey(clientKey));
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    AJEntityUserIdentity userIdentity = null;
    if (username.indexOf("@") > 1) {
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailIdAndPassword(username, InternalHelper.encryptPassword(password));
    } else {
      userIdentity = getUserIdentityRepo().getUserIdentityByUsernameAndPassword(username, InternalHelper.encryptPassword(password));
    }
    rejectIfNull(userIdentity, MessageCodeConstants.AU0008, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVTED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    final String token = InternalHelper.generateToken(userIdentity.getUserId());
    final AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userIdentity.getUserId());
    JsonObject prefs = new JsonObject();
    if (userPreference != null) {
      prefs.put(ParameterConstants.PARAM_TAXONOMY, userPreference.getStandardPreference());
    } else {
      prefs.put(ParameterConstants.PARAM_TAXONOMY, ConfigRegistry.instance().getDefaultUserStandardPrefs());
    }
    accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.AUTHENTICATION_USER.getName()).putPayLoadObject(ParameterConstants.PARAM_ACCESS_TOKEN, token)
            .putPayLoadObject(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId())
            .putPayLoadObject(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId())
            .putPayLoadObject(ParameterConstants.PARAM_GRANT_TYPE, GrantType.CREDENTIAL.getType());

    return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build()).setContentTypeJson().setStatusOkay()
            .successful().build();
  };

  private AJEntityAuthClient validateAuthClient(String clientKey) {
    rejectIfNullOrEmpty(clientKey, MessageCodeConstants.AU0034, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    AJEntityAuthClient authClient = getAuthClientRepo().getAuthClient(clientKey);
    rejectIfNull(authClient, MessageCodeConstants.AU0035, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    return authClient;
  }

  private void verifyClientkeyDomains(String requestDomain, JsonArray registeredRefererDomains) {
    if (requestDomain != null && registeredRefererDomains != null) {
      boolean isValidReferrer = false;
      for (Object whitelistedDomain : registeredRefererDomains) {
        if (requestDomain.endsWith(((String) whitelistedDomain))) {
          isValidReferrer = true;
          break;
        }
      }
      reject(!isValidReferrer, MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
    }
  }

  private void saveAccessToken(String token, JsonObject accessToken, Integer expireAtInSeconds) {
    JsonObject data = new JsonObject(accessToken.toString());
    data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
    getRedisClient().set(token, data.toString(), expireAtInSeconds);
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }

  public AuthClientRepo getAuthClientRepo() {
    return authClientRepo;
  }

  public void setAuthClientRepo(AuthClientRepo authClientRepo) {
    this.authClientRepo = authClientRepo;
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

}
