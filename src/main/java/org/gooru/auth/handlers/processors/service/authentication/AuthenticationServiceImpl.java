package org.gooru.auth.handlers.processors.service.authentication;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HelperConstants.GrantType;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.HttpConstants.HttpStatus;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.gooru.auth.handlers.utils.InternalHelper;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;

public class AuthenticationServiceImpl extends ServerValidatorUtility implements AuthenticationService {

  private AuthClientRepo authClientRepo;

  private UserIdentityRepo userIdentityRepo;

  private UserPreferenceRepo userPreferenceRepo;

  private RedisClient redisClient;

  public AuthenticationServiceImpl() {
    setAuthClientRepo(AuthClientRepo.instance());
    setUserIdentityRepo(UserIdentityRepo.instance());
    setUserPreferenceRepo(UserPreferenceRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse createAnonymousAccessToken(AuthClientDTO authClientDTO, String requestDomain) {
    reject(!(GrantType.ANONYMOUS.getType().equalsIgnoreCase(authClientDTO.getGrantType())), MessageCodeConstants.AU0003,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AJEntityAuthClient authClient = validateAuthClient(authClientDTO.getClientId(), InternalHelper.encryptClientKey(authClientDTO.getClientKey()), authClientDTO.getGrantType());
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, MessageConstants.MSG_USER_ANONYMOUS);
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    final String token = InternalHelper.generateToken(MessageConstants.MSG_USER_ANONYMOUS);
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
    return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusHttpCode(HttpStatus.ACCEPTED).build();
  }

  @Override
  public MessageResponse
          createBasicAuthAccessToken(AuthClientDTO authClientDTO, String requestDomain, String basicAuthCredentials) {
    reject(!(GrantType.CREDENTIAL.getType().equalsIgnoreCase(authClientDTO.getGrantType())), MessageCodeConstants.AU0003,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    rejectIfNullOrEmpty(basicAuthCredentials, MessageCodeConstants.AU0006, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AJEntityAuthClient authClient = validateAuthClient(authClientDTO.getClientId(), InternalHelper.encryptClientKey(authClientDTO.getClientKey()), authClientDTO.getGrantType());
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
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVTED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    final String token = InternalHelper.generateToken(userIdentity.getUserId());
    final AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userIdentity.getUserId());
    if (userPreference != null) {
      JsonObject prefs = new JsonObject();
      if (userPreference.getStandardPreference() != null) {
        prefs.put(ParameterConstants.PARAM_TAXONOMY, userPreference.getStandardPreference());
      }
      accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    }
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
    return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusHttpCode(HttpStatus.ACCEPTED).build();
  }

  @Override
  public MessageResponse deleteAccessToken(String token) {
    getRedisClient().del(token);
    return new MessageResponse.Builder().setContentTypeJson().setStatusHttpCode(HttpStatus.ACCEPTED).build();
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
      ServerValidatorUtility.reject(!isValidReferrer, MessageCodeConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
    }
  }

  private void saveAccessToken(String token, JsonObject accessToken, Integer expireAtInSeconds) {
    JsonObject data = new JsonObject(accessToken.toString());
    data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
    getRedisClient().set(token, data.toString(), expireAtInSeconds);
  }

  public AuthClientRepo getAuthClientRepo() {
    return authClientRepo;
  }

  public void setAuthClientRepo(AuthClientRepo authClientRepo) {
    this.authClientRepo = authClientRepo;
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

  public UserPreferenceRepo getUserPreferenceRepo() {
    return userPreferenceRepo;
  }

  public void setUserPreferenceRepo(UserPreferenceRepo userPreferenceRepo) {
    this.userPreferenceRepo = userPreferenceRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }

}
