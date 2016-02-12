package org.gooru.auth.handlers.processors.command.executor.authenticationGLA;

import static org.gooru.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.infra.ConfigRegistry;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityAuthClient;
import org.gooru.auth.handlers.utils.InternalHelper;
import static org.gooru.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;

public final class CreateGLAAnonymousAccessTokenExecutor extends Executor {

  private AuthClientRepo authClientRepo;

  private RedisClient redisClient;

  public CreateGLAAnonymousAccessTokenExecutor() {
    setAuthClientRepo(AuthClientRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    String clientKey = messageContext.headers().get(MessageConstants.MSG_HEADER_API_KEY);
    if (clientKey == null) {
      clientKey = messageContext.requestParams().getString(ParameterConstants.PARAM_API_KEY);
    }
    final String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
    return createAccessToken(clientKey, requestDomain);
  }

  private MessageResponse createAccessToken(String clientKey, String requestDomain) {
    final AJEntityAuthClient authClient = validateAuthClient(InternalHelper.encryptClientKey(clientKey));
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, MessageConstants.MSG_USER_ANONYMOUS);
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
    final String token = InternalHelper.generateToken(MessageConstants.MSG_USER_ANONYMOUS);
    JsonObject prefs = new JsonObject();
    prefs.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, ConfigRegistry.instance().getDefaultUserStandardPrefs());
    accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    return new MessageResponse.Builder().setResponseBody(accessToken).setContentTypeJson().setStatusOkay().successful().build();
  }

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

}
