package org.gooru.auth.handlers.processors.command.executor.user;

import io.vertx.core.json.JsonObject;

import java.util.Date;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.gooru.auth.handlers.infra.ConfigRegistry;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.command.executor.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.command.executor.ActionResponseDTO;
import org.gooru.auth.handlers.processors.command.executor.Executor;
import org.gooru.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.event.Event;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.gooru.auth.handlers.processors.messageProcessor.MessageContext;
import org.gooru.auth.handlers.processors.messageProcessor.UserContext;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.utils.InternalHelper;

public final class CreateUserExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private RedisClient redisClient;

  interface Create {
    MessageResponse user(UserDTO userDTO, UserContext userContext);
  }

  public CreateUserExecutor() {
    setUserIdentityRepo(UserIdentityRepo.instance());
    setUserRepo(UserRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse execute(MessageContext messageContext) {
    UserDTO userDTO = new UserDTO(messageContext.requestBody().getMap());
    return create.user(userDTO, messageContext.user());
  }

  Create create = (UserDTO userDTO, UserContext userContext) -> {
    rejectIfNullOrEmpty(userDTO.getBirthDate(), MessageCodeConstants.AU0015, 400, ParameterConstants.PARAM_USER_BIRTH_DATE);
    Date dob = InternalHelper.isValidDate(userDTO.getBirthDate());
    rejectIfNull(dob, MessageCodeConstants.AU0022, 400, ParameterConstants.PARAM_USER_BIRTH_DATE);
    int age = InternalHelper.getAge(dob);
    ActionResponseDTO<AJEntityUserIdentity> responseDTO = null;
    if (age < 13) {
      responseDTO = createChildUser(userDTO, userContext.getClientId(), dob);
    } else {
      responseDTO = createUser(userDTO, userContext.getClientId(), dob);
    }
    AJEntityUserIdentity userIdentity = responseDTO.getModel();
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, userContext.getClientId());
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, userContext.getCdnUrls());
    JsonObject prefs = new JsonObject();
    prefs.put(ParameterConstants.PARAM_TAXONOMY, ConfigRegistry.instance().getDefaultUserStandardPrefs());
    accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    final String token = InternalHelper.generateToken(userIdentity.getUserId());
    saveAccessToken(token, accessToken, userContext.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    StringBuilder uri = new StringBuilder(HelperConstants.USER_ENTITY_URI).append(userIdentity.getUserId());
    EventBuilder eventBuilder = responseDTO.getEventBuilder().setEventName(Event.CREATE_USER.getName());
    return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build())
            .setHeader(HelperConstants.LOCATION, uri.toString()).setContentTypeJson().setStatusCreated().successful().build();
  };

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
    return new ActionResponseDTO<AJEntityUserIdentity>(userIdentity, eventBuilder);
  }

  private ActionResponseDTO<AJEntityUserIdentity> createChildUser(final UserDTO userDTO, final String clientId, final Date dob) {
    ActionResponseDTO<AJEntityUser> userValidator = createChildUserValidator(userDTO);
    userValidator.getModel().setBirthDate(dob);
    rejectError(userValidator.getErrors(), HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    AJEntityUser user = getUserRepo().create(userValidator.getModel());
    AJEntityUserIdentity userIdentity = createUserIdentityValue(userDTO, userValidator.getModel(), clientId);
    getUserIdentityRepo().createOrUpdate(userIdentity);
    final EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
            AJResponseJsonTransformer.transform(user.toJson(false), HelperConstants.USERS_JSON_FIELDS));
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    return new ActionResponseDTO<AJEntityUserIdentity>(userIdentity, eventBuilder);
  }

  private ActionResponseDTO<AJEntityUser> createChildUserValidator(UserDTO userDTO) {
    final String username = userDTO.getUsername();
    final String userCategory = userDTO.getUserCategory();
    final String parentUserEmailId = userDTO.getParentEmailId();
    final Errors errors = new Errors();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_PARENT_EMAIL_ID, parentUserEmailId, MessageCodeConstants.AU0031);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_USERNAME, username, MessageCodeConstants.AU0013);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_CATEGORY, userCategory, MessageCodeConstants.AU0016);
    String password = userDTO.getPassword();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_PASSWORD, password, MessageCodeConstants.AU0016);
    if (username != null) {
      addValidator(errors, !(username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0017);
      addValidator(errors, ((username.length() < 4 || username.length() > 20)), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0018,
              ParameterConstants.PARAM_USER_USERNAME, "4", "20");
      AJEntityUserIdentity userIdentityUsername = getUserIdentityRepo().getUserIdentityByUsername(username);
      addValidator(errors, !(userIdentityUsername == null), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0023, username,
              ParameterConstants.PARAM_USER_USERNAME);
    }
    if (password != null) {
      addValidator(errors, ((password.length() < 5 || password.length() > 14)), ParameterConstants.PARAM_USER_PASSWORD, MessageCodeConstants.AU0018,
              ParameterConstants.PARAM_USER_PASSWORD, "5", "14");

    }
    final AJEntityUser user = new AJEntityUser();
    user.setUserCategory(userCategory);
    if (parentUserEmailId != null) {
      AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(parentUserEmailId);
      addValidator(errors, (userIdentity == null), ParameterConstants.PARAM_USER_PARENT_EMAIL_ID, MessageCodeConstants.AU0032);
      user.setParentUserId(userIdentity.getUserId());
    }

    return new ActionResponseDTO<AJEntityUser>(user, errors);
  }

  private ActionResponseDTO<AJEntityUser> createUserValidator(final UserDTO userDTO) {
    final Errors errors = new Errors();
    final AJEntityUser user = new AJEntityUser();
    final String firstname = userDTO.getFirstname();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_FIRSTNAME, firstname, MessageCodeConstants.AU0011);
    if (firstname != null) {
      addValidator(errors, !(firstname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME, MessageCodeConstants.AU0021);
    }
    final String lastname = userDTO.getLastname();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_LASTNAME, lastname, MessageCodeConstants.AU0012);
    if (lastname != null) {
      addValidator(errors, !(lastname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME, MessageCodeConstants.AU0021);
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

    return new ActionResponseDTO<AJEntityUser>(user, errors);
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

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

  public UserRepo getUserRepo() {
    return userRepo;
  }

  public void setUserRepo(UserRepo userRepo) {
    this.userRepo = userRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }
}
