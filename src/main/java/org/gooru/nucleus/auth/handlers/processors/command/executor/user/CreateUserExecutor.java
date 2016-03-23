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
import org.gooru.nucleus.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.SchoolDistrictRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.SchoolRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.StateRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.UserRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchool;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntitySchoolDistrict;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.nucleus.auth.handlers.utils.InternalHelper;

public final class CreateUserExecutor extends Executor {

  private UserIdentityRepo userIdentityRepo;
  private UserRepo userRepo;
  private RedisClient redisClient;
  private CountryRepo countryRepo;
  private StateRepo stateRepo;
  private SchoolRepo schoolRepo;
  private SchoolDistrictRepo schoolDistrictRepo;

  public CreateUserExecutor() {
    this.userIdentityRepo = UserIdentityRepo.instance();
    this.redisClient = RedisClient.instance();
    this.userRepo = UserRepo.instance();
    this.countryRepo = CountryRepo.instance();
    this.stateRepo = StateRepo.instance();
    this.schoolRepo = SchoolRepo.instance();
    this.schoolDistrictRepo = SchoolDistrictRepo.instance();
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
    prefs.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
    accessToken.put(ParameterConstants.PARAM_USER_PREFERENCE, prefs);
    final String token = InternalHelper.generateToken(userContext.getClientId(), userIdentity.getUserId());
    saveAccessToken(token, accessToken, userContext.getAccessTokenValidity());
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    EventBuilder eventBuilder = responseDTO.getEventBuilder().setEventName(Event.CREATE_USER.getName());
    // build the mail notify for welcome email.
    MailNotifyBuilder mailNotifyBuilder = new MailNotifyBuilder();
    mailNotifyBuilder.setTemplateName(MailTemplateConstants.WELCOME_MAIL).addToAddress(userIdentity.getEmailId());
    // generate email confirmation token and build the mail notify.
    final String emailToken = InternalHelper.generateEmailConfirmToken(userIdentity.getUserId());
    JsonObject tokenData = new JsonObject();
    tokenData.put(ParameterConstants.PARAM_USER_EMAIL_ID, userIdentity.getEmailId());
    tokenData.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    getRedisClient().set(emailToken, tokenData.toString(), HelperConstants.EXPIRE_IN_SECONDS);
    MailNotifyBuilder mailConfirmNotifyBuilder = new MailNotifyBuilder();
    mailConfirmNotifyBuilder.setTemplateName(MailTemplateConstants.USER_REGISTARTION_CONFIRMATION).addToAddress(userIdentity.getEmailId())
        .putContext(ParameterConstants.MAIL_TOKEN, emailToken);
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
    if (firstname != null) {
      addValidator(errors, !(firstname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME, MessageCodeConstants.AU0021,
          ParameterConstants.PARAM_USER_FIRSTNAME);
      user.setFirstname(firstname);
    }
    final String lastname = userDTO.getLastname();
    if (lastname != null) {
      addValidator(errors, !(lastname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME, MessageCodeConstants.AU0021,
          ParameterConstants.PARAM_USER_LASTNAME);
      user.setLastname(lastname);
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
    if (userCategory != null) {
      addValidator(errors, (HelperConstants.USER_CATEGORY.get(userCategory) == null), ParameterConstants.PARAM_USER_CATEGORY,
          MessageCodeConstants.AU0025);
      user.setUserCategory(userCategory);

    }
    final String password = userDTO.getPassword();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_PASSWORD, password, MessageCodeConstants.AU0016);
    if (password != null) {
      addValidator(errors, ((password.length() < 5 || password.length() > 14)), ParameterConstants.PARAM_USER_PASSWORD, MessageCodeConstants.AU0018,
          ParameterConstants.PARAM_USER_PASSWORD, "5", "14");

    }

    user.setEmailId(emailId);
    if (userDTO.getGrade() != null) {
      user.setGrade(userDTO.getGrade());
    }
    if (user.getGender() != null) {
      addValidator(errors, (HelperConstants.USER_GENDER.get(user.getGender()) == null), ParameterConstants.PARAM_USER_GENDER,
          MessageCodeConstants.AU0024);
      user.setGender(user.getGender());
    }
    if (userDTO.getCountryId() != null) {
      AJEntityCountry country = getCountryRepo().getCountry(userDTO.getCountryId());
      addValidator(errors, (country == null), ParameterConstants.PARAM_USER_COUNTRY_ID, MessageCodeConstants.AU0027,
          ParameterConstants.PARAM_USER_COUNTRY);
      user.setCountryId(country.getId());
      user.setCountry(country.getName());
    } else if (userDTO.getCountry() != null) {
      user.setCountry(userDTO.getCountry());
    }

    if (userDTO.getStateId() != null) {
      AJEntityState state = getStateRepo().getStateById(userDTO.getStateId());
      addValidator(errors, (state == null), ParameterConstants.PARAM_USER_STATE_ID, MessageCodeConstants.AU0027, ParameterConstants.PARAM_USER_STATE);
      user.setStateId(state.getId());
      user.setState(state.getName());
    } else if (userDTO.getState() != null) {
      user.setState(userDTO.getState());
    }

    if (userDTO.getSchoolDistrictId() != null) {
      AJEntitySchoolDistrict schoolDistrict = getSchoolDistrictRepo().getSchoolDistrictById(userDTO.getSchoolDistrictId());
      addValidator(errors, (schoolDistrict == null), ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID, MessageCodeConstants.AU0027,
          ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
      user.setSchoolDistrictId(schoolDistrict.getId());
      user.setSchoolDistrict(schoolDistrict.getName());
    } else if (userDTO.getSchoolDistrict() != null) {
      user.setSchoolDistrict(userDTO.getSchoolDistrict());
    }

    if (userDTO.getSchoolId() != null) {
      AJEntitySchool school = getSchoolRepo().getSchoolById(userDTO.getSchoolId());
      addValidator(errors, (school == null), ParameterConstants.PARAM_USER_SCHOOL_ID, MessageCodeConstants.AU0027,
          ParameterConstants.PARAM_USER_SCHOOL);
      user.setSchoolId(school.getId());
      user.setSchool(school.getName());
    } else if (userDTO.getSchool() != null) {
      user.setSchool(userDTO.getSchool());
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
  

  public CountryRepo getCountryRepo() {
    return countryRepo;
  }

  public StateRepo getStateRepo() {
    return stateRepo;
  }

  public SchoolRepo getSchoolRepo() {
    return schoolRepo;
  }

  public SchoolDistrictRepo getSchoolDistrictRepo() {
    return schoolDistrictRepo;
  }
}
