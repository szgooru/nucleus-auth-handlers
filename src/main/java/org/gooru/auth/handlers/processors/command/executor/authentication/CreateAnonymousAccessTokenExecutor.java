package org.gooru.auth.handlers.processors.command.executor.authentication;

import static org.gooru.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;
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
import org.gooru.auth.handlers.processors.data.transform.model.AuthClientDTO;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.auth.handlers.utils.InternalHelper;

public final class CreateAnonymousAccessTokenExecutor extends Executor {

  private RedisClient redisClient;

  private AuthClientRepo authClientRepo;

  public CreateAnonymousAccessTokenExecutor() {
    setAuthClientRepo(AuthClientRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
    AuthClientDTO authClientDTO = new AuthClientDTO(messageContext.requestBody());
    return createAccessToken(authClientDTO, requestDomain);
  }

  private MessageResponse createAccessToken(AuthClientDTO authClientDTO, String requestDomain) {
    reject(!(GrantType.ANONYMOUS.getType().equalsIgnoreCase(authClientDTO.getGrantType())), MessageCodeConstants.AU0003,
            HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AJEntityAuthClient authClient =
            validateAuthClient(authClientDTO.getClientId(), InternalHelper.encryptClientKey(authClientDTO.getClientKey()),
                    authClientDTO.getGrantType());
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, MessageConstants.MSG_USER_ANONYMOUS);
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
    JsonObject prefs = new JsonObject();
    prefs.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, ConfigRegistry.instance().getDefaultUserStandardPrefs());
    accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    final String token = InternalHelper.generateToken(MessageConstants.MSG_USER_ANONYMOUS);
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusOkay().successful().build();
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

  private AJEntityAuthClient validateAuthClient(String clientId, String clientKey, String grantType) {
    rejectIfNullOrEmpty(clientId, MessageCodeConstants.AU0001, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    rejectIfNullOrEmpty(clientKey, MessageCodeConstants.AU0002, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    AJEntityAuthClient authClient = getAuthClientRepo().getAuthClient(clientId, clientKey);
    rejectIfNull(authClient, MessageCodeConstants.AU0004, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    reject((authClient.getGrantTypes() == null || !authClient.getGrantTypes().contains(grantType)), MessageCodeConstants.AU0005,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    return authClient;
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

}
