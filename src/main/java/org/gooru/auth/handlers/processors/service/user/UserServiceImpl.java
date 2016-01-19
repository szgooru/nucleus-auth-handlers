package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.UUID;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.UserContext;
import org.gooru.auth.handlers.processors.data.transform.model.UserDTO;
import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.event.Event;
import org.gooru.auth.handlers.processors.event.EventBuilder;
import org.gooru.auth.handlers.processors.exceptions.BadRequestException;
import org.gooru.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.auth.handlers.processors.repositories.SchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.SchoolRepo;
import org.gooru.auth.handlers.processors.repositories.StateRepo;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchool;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchoolDistrict;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.processors.service.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.service.ActionResponseDTO;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.gooru.auth.handlers.utils.InternalHelper;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;

public class UserServiceImpl extends ServerValidatorUtility implements UserService {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  private CountryRepo countryRepo;

  private StateRepo stateRepo;

  private SchoolRepo schoolRepo;

  private SchoolDistrictRepo schoolDistrictRepo;

  private RedisClient redisClient;

  private static final int EXPIRE_IN_SECONDS = 86400;

  public UserServiceImpl() {
    setUserIdentityRepo(UserIdentityRepo.instance());
    setUserRepo(UserRepo.instance());
    setCountryRepo(CountryRepo.instance());
    setStateRepo(StateRepo.instance());
    setSchoolRepo(SchoolRepo.instance());
    setSchoolDistrictRepo(SchoolDistrictRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public MessageResponse createUserAccount(UserDTO userDTO, UserContext userContext) {
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
    final String token = InternalHelper.generateToken(userIdentity.getUserId());
    saveAccessToken(token, accessToken, userContext.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    StringBuilder uri = new StringBuilder(HelperConstants.USER_ENTITY_URI).append(userIdentity.getUserId());
    EventBuilder eventBuilder = responseDTO.getEventBuilder().setEventName(Event.CREATE_USER.getName());
    return new MessageResponse.Builder().setResponseBody(accessToken).setEventData(eventBuilder.build())
            .setHeader(HelperConstants.LOCATION, uri.toString()).setContentTypeJson().setStatusCreated().successful().build();
  }

  @Override
  public MessageResponse updateUser(final String userId, final UserDTO userDTO) {
    ActionResponseDTO<AJEntityUser> userValidator = updateUserValidator(userId, userDTO);
    rejectError(userValidator.getErrors(), HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    AJEntityUser user = userValidator.getModel();
    user = getUserRepo().update(user);
    EventBuilder eventBuilder = userValidator.getEventBuilder().setEventName(Event.UPDATE_USER.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC,
            AJResponseJsonTransformer.transform(user.toJson(false), HelperConstants.USERS_JSON_FIELDS));
    if (userDTO.getUsername() != null) {
      final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
      userIdentity.setUsername(userDTO.getUsername());
      getUserIdentityRepo().createOrUpdate(userIdentity);
      eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    }
    return new MessageResponse.Builder().setContentTypeJson().setEventData(eventBuilder.build()).setStatusNoOutput().successful().build();
  }

  @Override
  public MessageResponse getUser(final String userId) {
    final AJEntityUser user = getUserRepo().getUser(userId);
    rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVTED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    JsonObject result = AJResponseJsonTransformer.transform(user.toJson(false), HelperConstants.USERS_JSON_FIELDS, true);
    result.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    return new MessageResponse.Builder().setResponseBody(result).setContentTypeJson().setStatusOkay().successful().build();
  }

  @Override
  public MessageResponse findUser(final String username, final String email) {
    AJEntityUserIdentity userIdentity = null;
    if (username != null) {
      userIdentity = getUserIdentityRepo().getUserIdentityByUsername(username);
    } else if (email != null) {
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(email);
    } else {
      throw new BadRequestException("Invalid param type passed");
    }

    JsonObject result = userIdentity != null ? new JsonObject(userIdentity.toJson(false, "user_id", "username", "email_id")) : new JsonObject();
    return new MessageResponse.Builder().setResponseBody(result).setContentTypeJson().setStatusOkay().successful().build();
  }

  @Override
  public MessageResponse resetPassword(final String emailId) {
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final String token = InternalHelper.generateToken(InternalHelper.RESET_PASSWORD_TOKEN);
    getRedisClient().set(token, userIdentity.getEmailId(), EXPIRE_IN_SECONDS);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.RESET_USER_PASSWORD.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    eventBuilder.putPayLoadObject(ParameterConstants.PARAM_TOKEN, token);
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
  }

  @Override
  public MessageResponse resetUnAuthenticateUserPassword(final String token, final String password) {
    String emailId = getRedisClient().get(token);
    rejectIfNull(emailId, MessageCodeConstants.AU0028, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    userIdentity.setPassword(InternalHelper.encryptPassword(password));
    getUserIdentityRepo().createOrUpdate(userIdentity);
    getRedisClient().del(token);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.UPDATE_USER_PASSWORD.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
  }

  @Override
  public MessageResponse resetAuthenticateUserPassword(final String userId, final String oldPassword, final String newPassword) {
    final AJEntityUserIdentity userIdentity =
            getUserIdentityRepo().getUserIdentityByIdAndPassword(userId, InternalHelper.encryptPassword(oldPassword));
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    userIdentity.setPassword(InternalHelper.encryptPassword(newPassword));
    getUserIdentityRepo().createOrUpdate(userIdentity);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.UPDATE_USER_PASSWORD.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusNoOutput().successful().build();
  }

  @Override
  public MessageResponse resendConfirmationEmail(String emailId) {
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final String token = InternalHelper.generateToken(InternalHelper.EMAIL_CONFIRM_TOKEN);
    getRedisClient().set(token, userIdentity.getEmailId(), EXPIRE_IN_SECONDS);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.RESEND_CONFIRM_EMAIL.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    eventBuilder.putPayLoadObject(ParameterConstants.PARAM_USER_EMAIL_ID, emailId).putPayLoadObject(ParameterConstants.PARAM_TOKEN, token);
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusOkay().successful().build();
  }

  @Override
  public MessageResponse confirmUserEmail(String userId, String token) {
    final String emailId = getRedisClient().get(token);
    rejectIfNull(emailId, MessageCodeConstants.AU0028, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.UPDATE_USER_EMAIL_CONFIRM.getName());
    if (!userIdentity.getEmailId().equalsIgnoreCase(emailId)) {
      userIdentity.setEmailId(emailId);
      AJEntityUser user = getUserRepo().getUser(userId);
      user.setEmailId(emailId);
      getUserRepo().update(user);
      eventBuilder.put(SchemaConstants.USER_DEMOGRAPHIC, AJResponseJsonTransformer.transform(user.toJson(false), HelperConstants.USERS_JSON_FIELDS));
    }
    userIdentity.setEmailConfirmStatus(true);
    getUserIdentityRepo().createOrUpdate(userIdentity);
    getRedisClient().del(token);
    eventBuilder.put(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    return new MessageResponse.Builder().setEventData(eventBuilder.build()).setContentTypeJson().setStatusOkay().successful().build();
  }

  @Override
  public MessageResponse updateUserEmail(String userId, String emailId) {
    rejectIfNull(emailId, MessageCodeConstants.AU0014, HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_EMAIL_ID);
    AJEntityUserIdentity userIdentityEmail = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    reject(userIdentityEmail != null, MessageCodeConstants.AU0023, HttpConstants.HttpStatus.BAD_REQUEST.getCode(), emailId,
            ParameterConstants.EMAIL_ADDRESS);
    final String token = InternalHelper.generateToken(InternalHelper.EMAIL_CONFIRM_TOKEN);
    getRedisClient().set(token, emailId, EXPIRE_IN_SECONDS);
    AJEntityUser user = getUserRepo().getUser(userId);
    AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    EventBuilder eventBuilder = new EventBuilder();
    eventBuilder.setEventName(Event.UPDATE_USER_EMAIL.getName());
    eventBuilder.putPayLoadObject(SchemaConstants.USER_IDENTITY, AJResponseJsonTransformer.transform(userIdentity.toJson(false)));
    eventBuilder.putPayLoadObject(SchemaConstants.USER_DEMOGRAPHIC, AJResponseJsonTransformer.transform(user.toJson(false)));
    eventBuilder.putPayLoadObject(ParameterConstants.PARAM_USER_NEW_EMAIL_ID, emailId).putPayLoadObject(ParameterConstants.PARAM_TOKEN, token);
    return new MessageResponse.Builder().setContentTypeJson().setEventData(eventBuilder.build()).setStatusNoOutput().successful().build();
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
    user.setId(UUID.randomUUID().toString());
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

    user.setId(UUID.randomUUID().toString());
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

  private ActionResponseDTO<AJEntityUser> updateUserValidator(final String userId, final UserDTO userDTO) {
    final AJEntityUser user = getUserRepo().getUser(userId);
    rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVTED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    final Errors errors = new Errors();
    final String username = userDTO.getUsername();
    final EventBuilder eventBuilder = new EventBuilder();

    if (userDTO.getFirstname() != null) {
      addValidator(errors, !(userDTO.getFirstname().matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME, MessageCodeConstants.AU0021);
      user.setFirstname(userDTO.getFirstname());
    }
    if (userDTO.getLastname() != null) {
      addValidator(errors, !(userDTO.getLastname().matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME, MessageCodeConstants.AU0021);
      user.setLastname(userDTO.getLastname());

    }
    if (userDTO.getGender() != null) {
      addValidator(errors, (HelperConstants.USER_GENDER.get(userDTO.getGender()) == null), ParameterConstants.PARAM_USER_GENDER,
              MessageCodeConstants.AU0024);
      user.setGender(userDTO.getGender());
    }
    if (userDTO.getUserCategory() != null) {
      addValidator(errors, (HelperConstants.USER_CATEGORY.get(userDTO.getUserCategory()) == null), ParameterConstants.PARAM_USER_CATEGORY,
              MessageCodeConstants.AU0025);
      user.setUserCategory(userDTO.getUserCategory());
    }
    if (username != null) {
      addValidator(errors, !(username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0017);
      addValidator(errors, ((username.length() < 4 || username.length() > 20)), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0018,
              ParameterConstants.PARAM_USER_USERNAME, "4", "20");
      AJEntityUserIdentity userIdentityUsername = getUserIdentityRepo().getUserIdentityByUsername(username);
      addValidator(errors, !(userIdentityUsername == null), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0023, username,
              ParameterConstants.PARAM_USER_USERNAME);
    }

    if (userDTO.getSchoolId() != null) {
      AJEntitySchool school = getSchoolRepo().getSchoolById(userDTO.getSchoolId());
      addValidator(errors, (school == null), ParameterConstants.PARAM_USER_SCHOOL_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_SCHOOL);
      user.setSchoolId(userDTO.getSchoolId());
    }
    if (userDTO.getSchoolDistrictId() != null) {
      AJEntitySchoolDistrict schoolDistrict = getSchoolDistrictRepo().getSchoolDistrictById(userDTO.getSchoolDistrictId());
      addValidator(errors, (schoolDistrict == null), ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
      user.setSchoolDistrictId(userDTO.getSchoolDistrictId());
    }

    if (userDTO.getStateId() != null) {
      AJEntityState state = getStateRepo().getStateById(userDTO.getStateId());
      addValidator(errors, (state == null), ParameterConstants.PARAM_USER_STATE_ID, MessageCodeConstants.AU0027, ParameterConstants.PARAM_USER_STATE);
      user.setStateId(userDTO.getStateId());
    }

    if (userDTO.getCountryId() != null) {
      AJEntityCountry country = getCountryRepo().getCountry(userDTO.getCountryId());
      addValidator(errors, (country == null), ParameterConstants.PARAM_USER_COUNTRY_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_COUNTRY);
      user.setCountryId(userDTO.getCountryId());
    }

    if (userDTO.getCountry() != null) {
      AJEntityCountry country = getCountryRepo().getCountryByName(userDTO.getCountry());
      if (country == null) {
        country = getCountryRepo().createCountry(userDTO.getCountry(), userId);
        eventBuilder.putPayLoadObject(SchemaConstants.COUNTRY, AJResponseJsonTransformer.transform(country.toJson(false)));
      }
      user.setCountryId(country.getId());
    }

    if (userDTO.getState() != null) {
      AJEntityState state = getStateRepo().getStateByName(userDTO.getState());
      if (state == null) {
        state = getStateRepo().createState(userDTO.getState(), userId);
        eventBuilder.putPayLoadObject(SchemaConstants.STATE, AJResponseJsonTransformer.transform(state.toJson(false)));
      }
      user.setStateId(state.getId());
    }

    if (userDTO.getSchool() != null) {
      AJEntitySchool school = getSchoolRepo().getSchoolByName(userDTO.getSchool());
      if (school == null) {
        school = getSchoolRepo().createSchool(userDTO.getSchool(), userId);
        eventBuilder.putPayLoadObject(SchemaConstants.SCHOOL, AJResponseJsonTransformer.transform(school.toJson(false)));
      }
      user.setSchoolId(school.getId());
    }

    if (userDTO.getSchoolDistrict() != null) {
      AJEntitySchoolDistrict schoolDistrict = getSchoolDistrictRepo().getSchoolDistrictByName(userDTO.getSchoolDistrict());
      if (schoolDistrict == null) {
        schoolDistrict = getSchoolDistrictRepo().createSchoolDistrict(userDTO.getSchoolDistrict(), userId);
        eventBuilder.putPayLoadObject(SchemaConstants.SCHOOL_DISTRICT, AJResponseJsonTransformer.transform(schoolDistrict.toJson(false)));
      }
      user.setSchoolDistrictId(schoolDistrict.getId());
    }

    if (userDTO.getGrade() != null) {
      user.setGrade(userDTO.getGrade());
    }

    if (userDTO.getCourse() != null) {
      user.setCourse(userDTO.getCourse());
    }
    if (userDTO.getAboutMe() != null) {
      user.setAboutMe(userDTO.getAboutMe());
    }
    if (userDTO.getThumbnailPath() != null) {
      user.setThumbnailPath(userDTO.getThumbnailPath());
    }
    return new ActionResponseDTO<AJEntityUser>(user, eventBuilder, errors);

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

  public CountryRepo getCountryRepo() {
    return countryRepo;
  }

  public void setCountryRepo(CountryRepo countryRepo) {
    this.countryRepo = countryRepo;
  }

  public StateRepo getStateRepo() {
    return stateRepo;
  }

  public void setStateRepo(StateRepo stateRepo) {
    this.stateRepo = stateRepo;
  }

  public SchoolRepo getSchoolRepo() {
    return schoolRepo;
  }

  public void setSchoolRepo(SchoolRepo schoolRepo) {
    this.schoolRepo = schoolRepo;
  }

  public SchoolDistrictRepo getSchoolDistrictRepo() {
    return schoolDistrictRepo;
  }

  public void setSchoolDistrictRepo(SchoolDistrictRepo schoolDistrictRepo) {
    this.schoolDistrictRepo = schoolDistrictRepo;
  }

  public RedisClient getRedisClient() {
    return redisClient;
  }

  public void setRedisClient(RedisClient redisClient) {
    this.redisClient = redisClient;
  }
}
