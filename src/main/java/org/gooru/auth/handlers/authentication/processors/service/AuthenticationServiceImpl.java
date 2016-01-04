package org.gooru.auth.handlers.authentication.processors.service;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.authentication.constants.HttpConstants;
import org.gooru.auth.handlers.authentication.constants.MessageConstants;
import org.gooru.auth.handlers.authentication.constants.ParameterConstants;
import org.gooru.auth.handlers.authentication.constants.ServerMessageConstants;
import org.gooru.auth.handlers.authentication.infra.RedisClient;
import org.gooru.auth.handlers.authentication.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.AuthClient;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserIdentity;
import org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities.UserPreference;
import org.gooru.auth.handlers.authentication.utils.InternalHelper;
import org.gooru.auth.handlers.authentication.utils.ServerValidationUtility;

import redis.clients.jedis.Jedis;

public class AuthenticationServiceImpl implements AuthenticationService {

  private AuthClientRepo authClientRepo;

  private UserIdentityRepo userIdentityRepo;

  private UserPreferenceRepo userPreferenceRepo;

  private Jedis jedis;

  public AuthenticationServiceImpl() {
    setAuthClientRepo(AuthClientRepo.getInstance());
    setUserIdentityRepo(UserIdentityRepo.getInstance());
    setUserPreferenceRepo(UserPreferenceRepo.getInstance());
    setJedis(RedisClient.getInstance().getJedis());
  }

  @Override
  public JsonObject createAnonymousAccessToken(String clientId, String clientKey, String grantType, String requestDomain) {

    final AuthClient authClient = validateAuthClient(clientId, clientKey, grantType);

    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, MessageConstants.MSG_USER_ANONYMOUS);
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    final String token = InternalHelper.generateToken(MessageConstants.MSG_USER_ANONYMOUS);
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    return accessToken;
  }

  @Override
  public JsonObject
          createBasicAuthAccessToken(String clientId, String clientKey, String grantType, String requestDomain, String basicAuthCredentials) {
    ServerValidationUtility.rejectIfNull(basicAuthCredentials, ServerMessageConstants.AU0006, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AuthClient authClient = validateAuthClient(clientId, clientKey, grantType);
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    final String credentials[] = InternalHelper.getUsernameAndPassword(basicAuthCredentials);
    final String username = credentials[0];
    final String password = InternalHelper.encryptPassword(credentials[1]);
    UserIdentity userIdentity = null;
    if (username.indexOf("@") > -1) {
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailIdAndPassword(username, password);
    } else {
      userIdentity = getUserIdentityRepo().getUserIdentityByUsernameAndPassword(username, password);
    }
    ServerValidationUtility.rejectIfNull(userIdentity, ServerMessageConstants.AU0008, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    ServerValidationUtility.reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVTED),
            ServerMessageConstants.AU0009, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    final String token = InternalHelper.generateToken(userIdentity.getUserId());
    final UserPreference userPreference = getUserPreferenceRepo().getUserPreference(userIdentity.getUserId());
    if (userPreference != null && userPreference.getStandardPreference() != null) {
      accessToken.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, userPreference.getStandardPreference());
    }
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    return accessToken;
  }

  @Override
  public JsonObject getAccessToken(String token) {
    JsonObject accessToken = new JsonObject(getJedis().get(token));
    if (accessToken != null) {
      int expireAtInSeconds = accessToken.getInteger(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
      getJedis().expire(token, expireAtInSeconds);
    }
    return accessToken;
  }

  @Override
  public boolean deleteAccessToken(String token) {
    getJedis().del(token);
    return true;
  }

  private AuthClient validateAuthClient(String clientId, String clientKey, String grantType) {
    ServerValidationUtility.rejectIfNull(clientId, ServerMessageConstants.AU0001, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    ServerValidationUtility.rejectIfNull(clientKey, ServerMessageConstants.AU0002, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    ServerValidationUtility.rejectIfNull(grantType, ServerMessageConstants.AU0003, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    AuthClient authClient = getAuthClientRepo().getAuthClient(clientId, clientKey);
    ServerValidationUtility.rejectIfNull(authClient, ServerMessageConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    ServerValidationUtility.reject((authClient.getGrantTypes() == null || !authClient.getGrantTypes().contains(grantType)),
            ServerMessageConstants.AU0005, HttpConstants.HttpStatus.FORBIDDEN.getCode());
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
      ServerValidationUtility.reject(!isValidReferrer, ServerMessageConstants.AU0009, HttpConstants.HttpStatus.FORBIDDEN.getCode());
    }
  }

  private void saveAccessToken(String token, JsonObject accessToken, Integer expireAtInSeconds) {
    JsonObject data = new JsonObject(accessToken.toString());
    data.put(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY, expireAtInSeconds);
    getJedis().set(token, data.toString());
    getJedis().expire(token, expireAtInSeconds);
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

  public Jedis getJedis() {
    return jedis;
  }

  public void setJedis(Jedis jedis) {
    this.jedis = jedis;
  }

}
