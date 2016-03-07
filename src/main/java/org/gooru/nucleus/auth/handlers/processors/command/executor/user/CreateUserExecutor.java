package org.gooru.nucleus.auth.handlers.processors.command.executor.user;

import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.addValidator;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.addValidatorIfNullOrEmptyError;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectError;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNull;
import static org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility.rejectIfNullOrEmpty;
import io.vertx.core.json.JsonObject;

import java.util.Date;

import org.gooru.nucleus.auth.handlers.constants.HelperConstants;
import org.gooru.nucleus.auth.handlers.constants.HttpConstants;
import org.gooru.nucleus.auth.handlers.constants.MailTemplateConstants;
import org.gooru.nucleus.auth.handlers.constants.MessageCodeConstants;
import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.gooru.nucleus.auth.handlers.infra.ConfigRegistry;
import org.gooru.nucleus.auth.handlers.infra.RedisClient;
import org.gooru.nucleus.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.nucleus.auth.handlers.processors.command.executor.ActionResponseDTO;
import org.gooru.nucleus.auth.handlers.processors.command.executor.Executor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.nucleus.auth.handlers.processors.email.notify.MailNotifyBuilder;
import org.gooru.nucleus.auth.handlers.processors.event.Event;
import org.gooru.nucleus.auth.handlers.processors.event.EventBuilder;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.nucleus.auth.handlers.processors.messageProcessor.UserContext;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;

public final class CreateUserExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private RedisClient redisClient;

  public CreateUserExecutor() {
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.redisClient = RedisClient.instance();
    this.userRepo = UserRepo.instance();
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    final UserDTO userDTO = new UserDTO(messageContext.requestBody());
    return createUser(userDTO, messageContext.user());
  }

  private MessageResponse createUser(final UserDTO userDTO, final UserContext userContext) {
    rejectIfNullOrEmpty(userDTO.getBirthDate(), MessageCodeConstants.AU0015, 400, ParameterConstants.PARAM_USER_BIRTH_DATE);
    Date dob = InternalHelper.isValidDate(userDTO.getBirthDate());
    rejectIfNull(dob, MessageCodeConstants.AU0022, 400, ParameterConstants.PARAM_USER_BIRTH_DATE);
    final ActionResponseDTO<AJEntityUserIdentity> responseDTO = createUser(userDTO, userContext.getClientId(), dob);
    AJEntityUserIdentity userIdentity = responseDTO.getModel();
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, userContext.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, userContext.getCdnUrls());
    JsonObject prefs = new JsonObject();
    prefs.put(ParameterConstants.PARAM_STANDARD_PREFERENCE, ConfigRegistry.instance().getDefaultUserStandardPrefs());
    accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    final String token = InternalHelper.generateToken(userContext.getClientId(), userIdentity.getUserId());
    saveAccessToken(token, accessToken, userContext.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    EventBuilder eventBuilder = responseDTO.getEventBuilder().setEventName(Event.CREATE_USER.getName());
    // build the mail notify for welcome email.
    MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
    mailNotifyBuilder.setTemplateName(MailTemplateConstants.WELCOME_MAIL).setAuthAccessToken(token).addToAddress(userIdentity.getEmailId());
    // generate email confirmation token and build the mail notify.
    final String emailToken = InternalHelper.generateEmailConfirmToken(userIdentity.getUserId());
    JsonObject tokenData = new JsonObject();
    tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
    tokenData.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    getRedisClient().set(emailToken, tokenData.toString(), HelperConstants.EXPIRE_IN_SECONDS);
    MailNotifyBuilder mailConfirmNotifyBuilder = new MailNotifyBuilder();
    mailConfirmNotifyBuilder.setAuthAccessToken(token).setTemplateName(MailTemplateConstants.USER_REGISTARTION_CONFIRMATION).addToAddress(userIdentity.getEmailId()).putContext(ParameterConstants.MAIL_TOKEN, emailToken);
    return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build())
        .addMailNotify(mailConfirmNotifyBuilder.build()).addMailNotify(mailNotifyBuilder.build())
        .setHeader(HelperConstants.LOCATION, HelperConstants.USER_ENTITY_URI + userIdentity.getUserId()).setContentTypeJson().setStatusCreated()
        .successful().build();
  }

  private ActionResponseDTO<AJEntityUserIdentity> createUser(final UserDTO userDTO, final String clientId, final Date dob) {
    ActionResponseDTO<AJEntityUser> userValidator = createUserValidator(userDTO);
    userValidator.getModel().setBirthDate(dob);
    rejectError(userValidator.getErrors(), HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    AJEntityUser user = getUserRepo().create(userValidator.getModel());
    AJEntityUserIdentity userIdentity = createUserIdentityValue(userDTO, userValidator.getModel(), clientId);
    getUserIdentityRepo().createOrUpdate(userIdentity);
    final EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
        AJResponseJsonTransformer.transform(user.toJson(false), HelperConstants.USERS_JSON_FIELDS));
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    return new ActionResponseDTO<>(userIdentity, eventBuilder);
  }

  private ActionResponseDTO<AJEntityUser> createUserValidator(final UserDTO userDTO) {
    final JsonObject errors = new JsonObject();
    final AJEntityUser user = new AJEntityUser();
    final String firstname = userDTO.getFirstname();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_FIRSTNAME, firstname, MessageCodeConstants.AU0011);
    if (firstname != null) {
      addValidator(errors, !(firstname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME, MessageCodeConstants.AU0021,
          ParameterConstants.PARAM_USER_FIRSTNAME);
    }
    final String lastname = userDTO.getLastname();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_LASTNAME, lastname, MessageCodeConstants.AU0012);
    if (lastname != null) {
      addValidator(errors, !(lastname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME, MessageCodeConstants.AU0021,
          ParameterConstants.PARAM_USER_LASTNAME);
    }
    final String username = userDTO.getUsername();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_USERNAME, username, MessageCodeConstants.AU0013);
    if (username != null) {
      addValidator(errors, !(username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0017);
      addValidator(errors, ((username.length() < 4 || username.length() > 20)), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0018,
          ParameterConstants.PARAM_USER_USERNAME, "4", "20");
      AJEntityUserIdentity userIdentityUsername = getUserIdentityRepo().getUserIdentityByUsername(username);
      addValidator(errors, !(userIdentityUsername == null), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0023, username,
          ParameterConstants.PARAM_USER_USERNAME);
    }
    final String emailId = userDTO.getEmailId();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_EMAIL_ID, userDTO.getEmailId(), MessageCodeConstants.AU0014);
    if (emailId != null) {
      addValidator(errors, !(emailId.indexOf("@") > 1), ParameterConstants.PARAM_USER_EMAIL_ID, MessageCodeConstants.AU0020);
      AJEntityUserIdentity userIdentityEmail = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
      addValidator(errors, !(userIdentityEmail == null), ParameterConstants.PARAM_USER_EMAIL_ID, MessageCodeConstants.AU0023, emailId,
          ParameterConstants.EMAIL_ADDRESS);
    }
    final String userCategory = userDTO.getUserCategory();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_CATEGORY, userDTO.getUserCategory(), MessageCodeConstants.AU0016);
    if (userCategory != null) {
      addValidator(errors, (HelperConstants.USER_CATEGORY.get(userCategory) == null), ParameterConstants.PARAM_USER_CATEGORY,
          MessageCodeConstants.AU0025);

    }
    final String password = userDTO.getPassword();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_PASSWORD, password, MessageCodeConstants.AU0016);
    if (password != null) {
      addValidator(errors, ((password.length() < 5 || password.length() > 14)), ParameterConstants.PARAM_USER_PASSWORD, MessageCodeConstants.AU0018,
          ParameterConstants.PARAM_USER_PASSWORD, "5", "14");

    }

    user.setFirstname(firstname);
    user.setLastname(lastname);
    user.setUserCategory(userCategory);
    user.setEmailId(emailId);
    if (userDTO.getGrade() != null) {
      user.setGrade(userDTO.getGrade());
    }
    if (user.getGender() != null) {
      addValidator(errors, (HelperConstants.USER_GENDER.get(user.getGender()) == null), ParameterConstants.PARAM_USER_GENDER,
          MessageCodeConstants.AU0024);
      user.setGender(user.getGender());
    }

    return new ActionResponseDTO<>(user, errors);
  }

  private AJEntityUserIdentity createUserIdentityValue(final UserDTO userDTO, final AJEntityUser user, final String clientId) {
    final AJEntityUserIdentity userIdentity = new AJEntityUserIdentity();
    userIdentity.setUsername(userDTO.getUsername());
    if (user.getEmailId() != null) {
      userIdentity.setEmailId(user.getEmailId());
    }
    userIdentity.setUserId(user.getId());
    userIdentity.setLoginType(HelperConstants.UserIdentityLoginType.CREDENTIAL.getType());
    userIdentity.setProvisionType(HelperConstants.UserIdentityProvisionType.REGISTERED.getType());
    userIdentity.setPassword(InternalHelper.encryptPassword(userDTO.getPassword()));
    userIdentity.setClientId(clientId);
    userIdentity.setStatus(HelperConstants.UserIdentityStatus.ACTIVE.getStatus());
    return userIdentity;
  }

  private void saveAccessToken(final String token, final JsonObject accessToken, final Integer expireAtInSeconds) {
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

  public RedisClient getRedisClient() {
    return redisClient;
  }

}
