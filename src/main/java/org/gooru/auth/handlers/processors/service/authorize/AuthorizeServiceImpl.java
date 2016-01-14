package org.gooru.auth.handlers.processors.service.authorize;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Random;
import java.util.UUID;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AuthClient;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.User;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserIdentity;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserPreference;
import org.gooru.auth.handlers.utils.InternalHelper;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;

public class AuthorizeServiceImpl extends ServerValidatorUtility implements AuthorizeService {

  private AuthClientRepo authClientRepo;

  private RedisClient redisClient;

  private UserIdentityRepo userIdentityRepo;

  private UserPreferenceRepo userPreferenceRepo;

  private UserRepo userRepo;

  public AuthorizeServiceImpl() {
    setAuthClientRepo(AuthClientRepo.instance());
    setRedisClient(RedisClient.instance());
    setUserIdentityRepo(UserIdentityRepo.instance());
    setUserPreferenceRepo(UserPreferenceRepo.instance());
    setUserRepo(UserRepo.instance());
  }

  @Override
  public JsonObject authorize(JsonObject userJson, String clientId, String clientKey, String grantType, String requestDomain, String returnUrl) {
    reject((HelperConstants.SSO_CONNECT_GRANT_TYPES.get(grantType) == null), MessageCodeConstants.AU0003,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AuthClient authClient = validateAuthClient(clientId, InternalHelper.encryptClientKey(clientKey), grantType);
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    String identityId = userJson.getString(ParameterConstants.PARAM_AUTHORIZE_IDENTITY_ID);
    rejectIfNullOrEmpty(identityId, MessageCodeConstants.AU0033, 400);
    boolean isEmailIdentity = false;
    UserIdentity userIdentity = null;
    if (identityId.indexOf("@") > 1) {
      isEmailIdentity = true;
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(identityId);
    } else {
      userIdentity = getUserIdentityRepo().getUserIdentityByReferenceId(identityId);
    }
    if (userIdentity == null) {
      userIdentity = createUserWithIdentity(userJson, grantType, clientId, isEmailIdentity);
    }

    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    final String token = InternalHelper.generateToken(userIdentity.getUserId());
    final UserPreference userPreference = getUserPreferenceRepo().getUserPreference(userIdentity.getUserId());
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
    return accessToken;
  }

  private UserIdentity createUserWithIdentity(JsonObject userJson, String grantType, String clientId, boolean isEmailIdentity) {
    final String firstname = userJson.getString(ParameterConstants.PARAM_USER_FIRSTNAME);
    final String lastname = userJson.getString(ParameterConstants.PARAM_USER_LASTNAME);
    String username = userJson.getString(ParameterConstants.PARAM_USER_USERNAME);
    String identityId = userJson.getString(ParameterConstants.PARAM_AUTHORIZE_IDENTITY_ID);
    final User user = new User();
    user.setFirstname(firstname);
    if (lastname != null) {
      user.setLastname(lastname);
    }
    if (username == null) {
      username = firstname.replaceAll("\\s+", "");
      if (lastname != null && lastname.length() > 0) {
        username = username + lastname;
      }
      UserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByUsername(username);
      if (userIdentity != null) {
        final Random randomNumber = new Random();
        username = username + randomNumber.nextInt(1000);
      }
    }
    user.setId(UUID.randomUUID().toString());
    user.setModifiedBy(user.getId());
    getUserRepo().create(user);
    UserIdentity userIdentity = createUserIdentityValue(grantType, user, clientId);
    if (isEmailIdentity) {
      userIdentity.setEmailId(identityId);
    } else {
      userIdentity.setReferenceId(identityId);
    }
    userIdentity.setUsername(username);
    getUserIdentityRepo().saveOrUpdate(userIdentity);
    return userIdentity;
  }

  private AuthClient validateAuthClient(String clientId, String clientKey, String grantType) {
    rejectIfNullOrEmpty(clientId, MessageCodeConstants.AU0001, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    rejectIfNullOrEmpty(clientKey, MessageCodeConstants.AU0002, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    AuthClient authClient = getAuthClientRepo().getAuthClient(clientId, clientKey);
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

  private UserIdentity createUserIdentityValue(final String userIdentityAuthorizeType, final User user, final String clientId) {
    final UserIdentity userIdentity = new UserIdentity();
    userIdentity.setUserId(user.getId());
    userIdentity.setLoginType(userIdentityAuthorizeType);
    userIdentity.setProvisionType(userIdentityAuthorizeType);
    userIdentity.setClientId(clientId);
    userIdentity.setStatus(HelperConstants.UserIdentityStatus.ACTIVE.getStatus());
    return userIdentity;
  }

  public AuthClientRepo getAuthClientRepo() {
    return authClientRepo;
  }

  public void setAuthClientRepo(AuthClientRepo authClientRepo) {
    this.authClientRepo = authClientRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }

  public void setUserRepo(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  public UserPreferenceRepo getUserPreferenceRepo() {
    return userPreferenceRepo;
  }

  public void setUserPreferenceRepo(UserPreferenceRepo userPreferenceRepo) {
    this.userPreferenceRepo = userPreferenceRepo;
  }

}
