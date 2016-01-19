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
import org.gooru.auth.handlers.processors.data.transform.model.AuthorizeDTO;
import org.gooru.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.auth.handlers.processors.service.MessageResponse;
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
  public MessageResponse authorize(AuthorizeDTO authorizeDTO, String requestDomain) {
    reject((HelperConstants.SSO_CONNECT_GRANT_TYPES.get(authorizeDTO.getGrantType()) == null), MessageCodeConstants.AU0003,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AJEntityAuthClient authClient =
            validateAuthClient(authorizeDTO.getClientId(), InternalHelper.encryptClientKey(authorizeDTO.getClientKey()), authorizeDTO.getGrantType());
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    String identityId = authorizeDTO.getUser().getIdentityId();
    rejectIfNullOrEmpty(identityId, MessageCodeConstants.AU0033, 400);
    boolean isEmailIdentity = false;
    AJEntityUserIdentity userIdentity = null;
    if (identityId.indexOf("@") > 1) {
      isEmailIdentity = true;
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(identityId);
    } else {
      userIdentity = getUserIdentityRepo().getUserIdentityByReferenceId(identityId);
    }
    if (userIdentity == null) {
      userIdentity = createUserWithIdentity(authorizeDTO.getUser(), authorizeDTO.getGrantType(), authorizeDTO.getClientId(), isEmailIdentity);
    }

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
    return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusOkay().successful().build();
  }

  private AJEntityUserIdentity createUserWithIdentity(UserDTO userDTO, String grantType, String clientId, boolean isEmailIdentity) {
    final AJEntityUser user = new AJEntityUser();
    user.setFirstname(userDTO.getFirstname());
    if (userDTO.getLastname() != null) {
      user.setLastname(userDTO.getLastname());
    }

    user.setId(UUID.randomUUID().toString());
    getUserRepo().create(user);
    final AJEntityUserIdentity userIdentity = createUserIdentityValue(grantType, user, clientId);
    if (isEmailIdentity) {
      userIdentity.setEmailId(userDTO.getIdentityId());
    } else {
      userIdentity.setReferenceId(userDTO.getIdentityId());
    }
    if (userDTO.getUsername() == null) {
      StringBuilder username = new StringBuilder(userDTO.getFirstname().replaceAll("\\s+", ""));
      if (userDTO.getLastname() != null && userDTO.getLastname().length() > 0 && username.length() < 14) {
        username.append(userDTO.getLastname().substring(0, 5));
      }
      AJEntityUserIdentity identityUsername = getUserIdentityRepo().getUserIdentityByUsername(username.toString());
      if (identityUsername != null) {
        final Random randomNumber = new Random();
        username.append(randomNumber.nextInt(1000));
      }
      userIdentity.setUsername(username.toString());
    } else {
      userIdentity.setUsername(userDTO.getUsername());
    }
    getUserIdentityRepo().createOrUpdate(userIdentity);
    return userIdentity;
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

  private AJEntityUserIdentity createUserIdentityValue(final String userIdentityAuthorizeType, final AJEntityUser user, final String clientId) {
    final AJEntityUserIdentity userIdentity = new AJEntityUserIdentity();
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
