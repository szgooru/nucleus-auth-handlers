package org.gooru.nucleus.auth.handlers.processors.command.executor.authentication;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants.GrantType;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;

public final class CreateBasicAuthAccessTokenExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;
  private UserPreferenceRepo userPreferenceRepo;
  private RedisClient redisClient;
  private AuthClientRepo authClientRepo;
  private UserRepo userRepo;

  public CreateBasicAuthAccessTokenExecutor() {
    this.authClientRepo = AuthClientRepo.instance();
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.userPreferenceRepo = UserPreferenceRepo.instance();
    this.redisClient = RedisClient.instance();
    this.userRepo = UserRepo.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String basicAuthCredentials = messageContext.headers().get(MessageConstants.MSG_HEADER_BASIC_AUTH);
    String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
    AuthClientDTO authClientDTO = new AuthClientDTO(messageContext.requestBody());
    return createAccessToken(authClientDTO, requestDomain, basicAuthCredentials);
  }

  private MessageResponse createAccessToken(AuthClientDTO authClientDTO, String requestDomain, String basicAuthCredentials) {
    reject(!(GrantType.CREDENTIAL.getType().equalsIgnoreCase(authClientDTO.getGrantType())), MessageCodeConstants.AU0003,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    rejectIfNullOrEmpty(basicAuthCredentials, MessageCodeConstants.AU0006, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AJEntityAuthClient authClient =
            validateAuthClient(authClientDTO.getClientId(), InternalHelper.encryptClientKey(authClientDTO.getClientKey()),
                    authClientDTO.getGrantType());
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    final String credentials[] = InternalHelper.getUsernameAndPassword(basicAuthCredentials);
    final String username = credentials[0];
    final String password = InternalHelper.encryptPassword(credentials[1]);
    AJEntityUserIdentity userIdentity = null;
    if (username.indexOf("@") > 1) {
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailIdAndPassword(username, password);
    } else {
      userIdentity = getUserIdentityRepo().getUserIdentityByUsernameAndPassword(username, password);
    }
    rejectIfNull(userIdentity, MessageCodeConstants.AU0008, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVATED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());

    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    final String token = InternalHelper.generateToken(authClient.getClientId(), userIdentity.getUserId());
    final AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userIdentity.getUserId());
    JsonObject prefs = new JsonObject();
    if (userPreference != null) {
      prefs.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, userPreference.getStandardPreference());
    } else {
      prefs.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, ConfigRegistry.instance().getDefaultUserStandardPrefs());
    }
    prefs.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
    accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    AJEntityUser user = getUserRepo().getUser(userIdentity.getUserId());
    if (user.getFirstname() != null) {
      accessToken.put(ParameterConstants.PARAM_USER_FIRSTNAME, user.getFirstname());
    }
    if (user.getLastname() != null) {       
      accessToken.put(ParameterConstants.PARAM_USER_LASTNAME, user.getLastname());
    }
    if (user.getUserCategory() != null) { 
      accessToken.put(ParameterConstants.PARAM_USER_CATEGORY, user.getUserCategory());
    }
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.AUTHENTICATION_USER.getName()).putPayLoadObject(ParameterConstants.PARAM_ACCESS_TOKEN, token)
            .putPayLoadObject(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId())
            .putPayLoadObject(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId())
            .putPayLoadObject(ParameterConstants.PARAM_GRANT_TYPE, authClientDTO.getGrantType());
    return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build()).setContentTypeJson().setStatusOkay()
            .successful().build();

  }

  private AJEntityAuthClient validateAuthClient(String clientId, String clientKey, String grantType) {
    rejectIfNullOrEmpty(clientId, MessageCodeConstants.AU0001, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    rejectIfNullOrEmpty(clientKey, MessageCodeConstants.AU0002, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    AJEntityAuthClient authClient = getAuthClientRepo().getAuthClient(clientId, clientKey);
    rejectIfNull(authClient, MessageCodeConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    reject((authClient.getGrantTypes() == null || !authClient.getGrantTypes().contains(grantType)), MessageCodeConstants.AU0005,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
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

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }
  
  public UserPreferenceRepo getUserPreferenceRepo() {
    return userPreferenceRepo;
  } 

  public AuthClientRepo getAuthClientRepo() {
    return authClientRepo;
  }  

  public RedisClient getRedisClient() {
    return redisClient;
  }

}
