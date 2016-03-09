package org.gooru.nucleus.auth.handlers.processors.command.executor.authorize;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.addValidator;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.reject;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectError;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Random;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.ActionResponseDTO;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.AuthorizeDTO;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.nucleus.auth.handlers.processors.email.notify.MailNotifyBuilder;
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

public class AuthorizeUserExecutor extends Executor {

  private AuthClientRepo authClientRepo;

  private RedisClient redisClient;

  private UserIdentityRepo userIdentityRepo;

  private UserPreferenceRepo userPreferenceRepo;

  private UserRepo userRepo;

  public AuthorizeUserExecutor() {
    this.authClientRepo = AuthClientRepo.instance();
    this.redisClient = RedisClient.instance();
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.userPreferenceRepo = UserPreferenceRepo.instance();
    this.userRepo = UserRepo.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    AuthorizeDTO authorizeDTO = new AuthorizeDTO(messageContext.requestBody());
    String requestDomain = messageContext.headers().get(MessageConstants.MSG_HEADER_REQUEST_DOMAIN);
    return authorizeUser(authorizeDTO, requestDomain);
  }

  private MessageResponse authorizeUser(AuthorizeDTO authorizeDTO, String requestDomain) {
    reject((HelperConstants.SSO_CONNECT_GRANT_TYPES.get(authorizeDTO.getGrantType()) == null), MessageCodeConstants.AU0003,
        HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AJEntityAuthClient authClient =
        validateAuthClient(authorizeDTO.getClientId(), InternalHelper.encryptClientKey(authorizeDTO.getClientKey()), authorizeDTO.getGrantType());
    verifyClientkeyDomains(requestDomain, authClient.getRefererDomains());
    authorizeValidator(authorizeDTO);
    String identityId = authorizeDTO.getUser().getIdentityId();
    boolean isEmailIdentity = false;
    AJEntityUserIdentity userIdentity = null;
    EventBuilder eventBuilder = new EventBuilder();
    MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
    if (identityId.indexOf("@") > 1) {
      isEmailIdentity = true;
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(identityId);
    } else {
      userIdentity = getUserIdentityRepo().getUserIdentityByReferenceId(identityId);
    }
    if (userIdentity == null) {
      ActionResponseDTO<AJEntityUserIdentity> responseDTO =
          createUserWithIdentity(authorizeDTO.getUser(), authorizeDTO.getGrantType(), authorizeDTO.getClientId(), isEmailIdentity, eventBuilder);
      userIdentity = responseDTO.getModel();
      eventBuilder = responseDTO.getEventBuilder();
      mailNotifyBuilder.setTemplateName(MailTemplateConstants.WELCOME_MAIL).addToAddress(userIdentity.getEmailId());
    }

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
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, authClient.getCdnUrls());
    accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    saveAccessToken(token, accessToken, authClient.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    eventBuilder.setEventName(Event.AUTHORIZE_USER.getName()).putPayLoadObject(ParameterConstants.PARAM_ACCESS_TOKEN, token)
        .putPayLoadObject(ParameterConstants.PARAM_CLIENT_ID, authClient.getClientId())
        .putPayLoadObject(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId())
        .putPayLoadObject(ParameterConstants.PARAM_GRANT_TYPE, authorizeDTO.getGrantType());
    return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build()).addMailNotify(mailNotifyBuilder.build())
        .setContentTypeJson().setStatusOkay().successful().build();
  }

  private ActionResponseDTO<AJEntityUserIdentity> createUserWithIdentity(final UserDTO userDTO, final String grantType, final String clientId,
      final boolean isEmailIdentity, final EventBuilder eventBuilder) {
    final AJEntityUser user = new AJEntityUser();
    user.setFirstname(userDTO.getFirstname());
    if (userDTO.getLastname() != null) {
      user.setLastname(userDTO.getLastname());
    }

    getUserRepo().create(user);
    eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
        AJResponseJsonTransformer.transform(user.toJson(false), HelperConstants.USERS_JSON_FIELDS));

    final AJEntityUserIdentity userIdentity = createUserIdentityValue(grantType, user, clientId);
    if (isEmailIdentity) {
      userIdentity.setEmailId(userDTO.getIdentityId());
      userIdentity.setEmailConfirmStatus(true);
    } else {
      userIdentity.setReferenceId(userDTO.getIdentityId());
    }
    if (userDTO.getUsername() == null) {
      StringBuilder username = new StringBuilder(userDTO.getFirstname().replaceAll("\\s+", ""));
      if (userDTO.getLastname() != null && userDTO.getLastname().length() > 0 && username.length() < 14) {
        final String lastname = userDTO.getLastname();
        username.append(lastname.substring(0, lastname.length() > 5 ? 5 : lastname.length()));
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
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    return new ActionResponseDTO<>(userIdentity, eventBuilder);
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

  private AJEntityUserIdentity createUserIdentityValue(final String userIdentityAuthorizeType, final AJEntityUser user, final String clientId) {
    final AJEntityUserIdentity userIdentity = new AJEntityUserIdentity();
    userIdentity.setUserId(user.getId());
    userIdentity.setLoginType(userIdentityAuthorizeType);
    userIdentity.setProvisionType(userIdentityAuthorizeType);
    userIdentity.setClientId(clientId);
    userIdentity.setStatus(HelperConstants.UserIdentityStatus.ACTIVE.getStatus());
    return userIdentity;
  }

  private void authorizeValidator(AuthorizeDTO authorizeDTO) {
    reject(authorizeDTO.getUser() == null, MessageCodeConstants.AU0038, 400);
    JsonObject errors = new JsonObject();
    addValidator(errors, authorizeDTO.getUser().getIdentityId() == null, ParameterConstants.PARAM_AUTHORIZE_IDENTITY_ID, MessageCodeConstants.AU0033);
    rejectError(errors, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
  }

  public AuthClientRepo getAuthClientRepo() {
    return authClientRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
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

}