package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Date;
import java.util.UUID;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.infra.RedisClient;
import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.auth.handlers.processors.repositories.SchoolDistrictRepo;
import org.gooru.auth.handlers.processors.repositories.SchoolRepo;
import org.gooru.auth.handlers.processors.repositories.StateRepo;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.Country;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.School;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.SchoolDistrict;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.State;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.User;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserIdentity;
import org.gooru.auth.handlers.processors.service.Validator;
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
    setUserIdentityRepo(UserIdentityRepo.getInstance());
    setUserRepo(UserRepo.instance());
    setCountryRepo(CountryRepo.instance());
    setStateRepo(StateRepo.instance());
    setSchoolRepo(SchoolRepo.instance());
    setSchoolDistrictRepo(SchoolDistrictRepo.instance());
    setRedisClient(RedisClient.instance());
  }

  @Override
  public JsonObject createUser(final JsonObject userJson, final String clientId, final JsonObject cdnUrls, final Integer expireAtInSeconds) {
    Validator<User> userValidator = createUserValidator(userJson);
    rejectError(userValidator.getErrors(), HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    getUserRepo().create(userValidator.getModel());
    UserIdentity userIdentity = createUserIdenityValue(userJson, userValidator.getModel(), clientId);
    getUserIdentityRepo().saveOrUpdate(userIdentity);
    final JsonObject accessToken = new JsonObject();
    accessToken.put(ParameterConstants.PARAM_USER_ID, userIdentity.getUserId());
    accessToken.put(ParameterConstants.PARAM_USER_USERNAME, userIdentity.getUsername());
    accessToken.put(ParameterConstants.PARAM_CLIENT_ID, clientId);
    accessToken.put(ParameterConstants.PARAM_PROVIDED_AT, System.currentTimeMillis());
    final String token = InternalHelper.generateToken(userIdentity.getUserId());
    saveAccessToken(token, accessToken, expireAtInSeconds);
    accessToken.put(ParameterConstants.PARAM_ACCESS_TOKEN, token);
    accessToken.put(ParameterConstants.PARAM_CDN_URLS, cdnUrls);
    return accessToken;
  }

  @Override
  public JsonObject updateUser(final String userId, final JsonObject userJson) {
    Validator<User> userValidator = updateUserValidator(userId, userJson);
    rejectError(userValidator.getErrors(), HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    final User user = getUserRepo().update(userValidator.getModel());
    final String username = userJson.getString(ParameterConstants.PARAM_USER_USERNAME);
    if (username != null) {
      final UserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
      userIdentity.setUsername(username);
      getUserIdentityRepo().saveOrUpdate(userIdentity);
    }
    return new JsonObject(user.toMap());
  }

  @Override
  public JsonObject getUser(final String userId) {
    final User user = getUserRepo().getUser(userId);
    rejectIfNull(user, MessageCodeConstants.AU0026, 404, ParameterConstants.PARAM_USER);
    return new JsonObject(user.toMap());
  }

  @Override
  public JsonObject resetAuthenticateUserPassword(final String userId, final String oldPassword, final String newPassword) {
    final UserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByIdAndPassword(userId, InternalHelper.encryptPassword(oldPassword));
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    userIdentity.setPassword(InternalHelper.encryptPassword(newPassword));
    getUserIdentityRepo().saveOrUpdate(userIdentity);
    return new JsonObject();
  }

  @Override
  public JsonObject resetPassword(final String emailId) {
    final UserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final String token = InternalHelper.generateToken(InternalHelper.RESET_PASSWORD_TOKEN);
    getRedisClient().set(token, userIdentity.getEmailId(), EXPIRE_IN_SECONDS);
    // TO-DO send mail notification
    return new JsonObject();
  }

  @Override
  public JsonObject resetUnAuthenticateUserPassword(String token, String password) {
    String emailId = getRedisClient().get(token);
    rejectIfNull(emailId, MessageCodeConstants.AU0028, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    UserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    userIdentity.setPassword(InternalHelper.encryptPassword(password));
    getUserIdentityRepo().saveOrUpdate(userIdentity);
    getRedisClient().del(token);
    return new JsonObject();
  }

  private Validator<User> createUserValidator(final JsonObject userJson) {
    final String firstname = userJson.getString(ParameterConstants.PARAM_USER_FIRSTNAME);
    final String lastname = userJson.getString(ParameterConstants.PARAM_USER_LASTNAME);
    final String username = userJson.getString(ParameterConstants.PARAM_USER_USERNAME);
    final String emailId = userJson.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    final String birthDate = userJson.getString(ParameterConstants.PARAM_USER_BIRTH_DATE);
    final String userCategory = userJson.getString(ParameterConstants.PARAM_USER_CATEGORY);
    final String gender = userJson.getString(ParameterConstants.PARAM_USER_GENDER);

    final Errors errors = new Errors();
    final User user = new User();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_FIRSTNAME, firstname, MessageCodeConstants.AU0011);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_LASTNAME, lastname, MessageCodeConstants.AU0012);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_USERNAME, username, MessageCodeConstants.AU0013);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_EMAIL_ID, emailId, MessageCodeConstants.AU0014);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_BIRTH_DATE, birthDate, MessageCodeConstants.AU0015);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_CATEGORY, userCategory, MessageCodeConstants.AU0016);
    String password = userJson.getString(ParameterConstants.PARAM_USER_PASSWORD);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_PASSWORD, password, MessageCodeConstants.AU0016);
    addValidator(errors, !(username != null && username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0017);
    addValidator(errors, (username != null && (username.length() < 4 || username.length() > 20)), ParameterConstants.PARAM_USER_USERNAME,
            MessageCodeConstants.AU0018, ParameterConstants.PARAM_USER_USERNAME, "4", "20");
    addValidator(errors, (password != null && (password.length() < 5 || password.length() > 14)), ParameterConstants.PARAM_USER_PASSWORD,
            MessageCodeConstants.AU0018, ParameterConstants.PARAM_USER_PASSWORD, "5", "14");

    addValidator(errors, !(emailId != null && emailId.indexOf("@") > 1), ParameterConstants.PARAM_USER_EMAIL_ID, MessageCodeConstants.AU0020);
    addValidator(errors, !(firstname != null && firstname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME,
            MessageCodeConstants.AU0021);
    addValidator(errors, !(lastname != null && lastname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME,
            MessageCodeConstants.AU0021);
    if (username != null) {
      UserIdentity userIdentityUsername = getUserIdentityRepo().getUserIdentityByUsername(username);
      addValidator(errors, !(userIdentityUsername == null), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0023, username,
              ParameterConstants.PARAM_USER_USERNAME);
    }
    if (emailId != null) {
      UserIdentity userIdentityEmail = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
      addValidator(errors, !(userIdentityEmail == null), ParameterConstants.PARAM_USER_EMAIL_ID, MessageCodeConstants.AU0023, emailId,
              ParameterConstants.EMAIL_ADDRESS);
    }
    addValidator(errors, (userCategory != null && HelperConstants.USER_CATEGORY.get(userCategory) == null), ParameterConstants.PARAM_USER_CATEGORY,
            MessageCodeConstants.AU0025);

    user.setId(UUID.randomUUID().toString());
    user.setFirstname(firstname);
    user.setLastname(lastname);
    user.setUserCategory(userCategory);
    user.setEmailId(emailId);
    user.setModifiedBy(user.getId());

    if (birthDate != null) {
      Date date = InternalHelper.isValidDate(birthDate);
      addValidator(errors, (date == null), ParameterConstants.PARAM_USER_BIRTH_DATE, MessageCodeConstants.AU0022);
      user.setBirthDate(date);
    }
    JsonArray grade = userJson.getJsonArray(ParameterConstants.PARAM_GRADE);
    if (grade != null) {
      user.setGrade(grade);
    }
    if (gender != null) {
      addValidator(errors, (HelperConstants.USER_GENDER.get(gender) == null), ParameterConstants.PARAM_USER_GENDER, MessageCodeConstants.AU0024);
      user.setGender(gender);
    }

    return new Validator<User>(user, errors);
  }

  private UserIdentity createUserIdenityValue(final JsonObject userJson, final User user, final String clientId) {
    final UserIdentity userIdentity = new UserIdentity();
    userIdentity.setUsername(userJson.getString(ParameterConstants.PARAM_USER_USERNAME));
    userIdentity.setEmailId(user.getEmailId());
    userIdentity.setUserId(user.getId());
    userIdentity.setLoginType(HelperConstants.UserIdentityLoginType.CREDENTIAL.getType());
    userIdentity.setProvisionType(HelperConstants.UserIdentityProvisionType.REGISTERED.getType());
    userIdentity.setPassword(InternalHelper.encryptPassword(userJson.getString(ParameterConstants.PARAM_USER_PASSWORD)));
    userIdentity.setClientId(clientId);
    userIdentity.setStatus(HelperConstants.UserIdentityStatus.ACTIVE.getStatus());
    return userIdentity;
  }

  private Validator<User> updateUserValidator(final String userId, final JsonObject userJson) {
    final User user = getUserRepo().getUser(userId);
    rejectIfNull(user, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final Errors errors = new Errors();
    final String firstname = userJson.getString(ParameterConstants.PARAM_USER_FIRSTNAME);
    final String lastname = userJson.getString(ParameterConstants.PARAM_USER_LASTNAME);
    final String gender = userJson.getString(ParameterConstants.PARAM_USER_GENDER);
    final String userCategory = userJson.getString(ParameterConstants.PARAM_USER_CATEGORY);
    final String aboutMe = userJson.getString(ParameterConstants.PARAM_USER_ABOUT_ME);
    final JsonArray grade = userJson.getJsonArray(ParameterConstants.PARAM_GRADE);
    final JsonObject course = userJson.getJsonObject(ParameterConstants.PARAM_COURSE);
    final String username = userJson.getString(ParameterConstants.PARAM_USER_USERNAME);
    final String schoolId = userJson.getString(ParameterConstants.PARAM_USER_SCHOOL_ID);
    final String schoolDistrictId = userJson.getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID);
    final String schoolText = userJson.getString(ParameterConstants.PARAM_USER_SCHOOL);
    final String schoolDistrictText = userJson.getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
    final Long stateId = userJson.getLong(ParameterConstants.PARAM_USER_STATE_ID);
    final Long countryId = userJson.getLong(ParameterConstants.PARAM_USER_COUNTRY_ID);
    final String stateText = userJson.getString(ParameterConstants.PARAM_USER_STATE);
    final String countryText = userJson.getString(ParameterConstants.PARAM_USER_COUNTRY);

    if (firstname != null) {
      addValidator(errors, !(firstname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME, MessageCodeConstants.AU0021);

    }
    if (lastname != null) {
      addValidator(errors, !(lastname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME, MessageCodeConstants.AU0021);

    }
    if (gender != null) {
      addValidator(errors, (HelperConstants.USER_GENDER.get(gender) == null), ParameterConstants.PARAM_USER_GENDER, MessageCodeConstants.AU0024);
      user.setGender(gender);
    }
    if (userCategory != null) {
      addValidator(errors, (HelperConstants.USER_CATEGORY.get(userCategory) == null), ParameterConstants.PARAM_USER_CATEGORY,
              MessageCodeConstants.AU0025);
    }
    if (username != null) {
      addValidator(errors, !(username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0017);
      addValidator(errors, ((username.length() < 4 || username.length() > 20)), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0018,
              ParameterConstants.PARAM_USER_USERNAME, "4", "20");
      UserIdentity userIdentityUsername = getUserIdentityRepo().getUserIdentityByUsername(username);
      addValidator(errors, !(userIdentityUsername == null), ParameterConstants.PARAM_USER_USERNAME, MessageCodeConstants.AU0023, username,
              ParameterConstants.PARAM_USER_USERNAME);
    }

    if (schoolId != null) {
      School school = getSchoolRepo().getSchoolById(schoolId);
      addValidator(errors, !(school == null), ParameterConstants.PARAM_USER_SCHOOL_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_SCHOOL);
      user.setSchoolId(schoolId);
    }
    if (schoolDistrictId != null) {
      SchoolDistrict schoolDistrict = getSchoolDistrictRepo().getSchoolDistrictById(schoolDistrictId);
      addValidator(errors, !(schoolDistrict == null), ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
      user.setSchoolDistrictId(schoolDistrictId);
    }

    if (stateId != null) {
      State state = getStateRepo().getStateById(stateId);
      addValidator(errors, !(state == null), ParameterConstants.PARAM_USER_STATE_ID, MessageCodeConstants.AU0027, ParameterConstants.PARAM_USER_STATE);
      user.setStateId(stateId);
    }

    if (countryId != null) {
      Country country = getCountryRepo().getCountry(countryId);
      addValidator(errors, !(country == null), ParameterConstants.PARAM_USER_COUNTRY_ID, MessageCodeConstants.AU0027,
              ParameterConstants.PARAM_USER_COUNTRY);
      user.setCountryId(countryId);
    }

    if (countryText != null) {
      Country country = getCountryRepo().getCountryByName(countryText);
      if (country == null) {
        country = getCountryRepo().createCountry(countryText);
      }
      user.setCountryId(country.getId());
    }

    if (stateText != null) {
      State state = getStateRepo().getStateByName(countryText);
      if (state == null) {
        state = getStateRepo().createState(state);
      }
      user.setStateId(state.getId());
    }

    if (schoolText != null) {
      School school = getSchoolRepo().getSchoolByName(schoolText);
      if (school == null) {
        school = getSchoolRepo().createSchool(school);
      }
      user.setSchoolId(school.getId());
    }

    if (schoolDistrictText != null) {
      SchoolDistrict schoolDistrict = getSchoolDistrictRepo().getSchoolDistrictByName(schoolDistrictText);
      if (schoolDistrict == null) {
        schoolDistrict = getSchoolDistrictRepo().createSchoolDistrict(schoolDistrictText);
      }
      user.setSchoolDistrictId(schoolDistrict.getId());
    }

    if (grade != null) {
      user.setGrade(grade);
    }

    if (course != null) {
      user.setCourse(course);
    }
    if (aboutMe != null) {
      user.setAboutMe(aboutMe);
    }
    return new Validator<User>(user, errors);

  }

  @Override
  public JsonObject findUser(final String username, final String email) {
    UserIdentity userIdentity = null;
    if (username != null) {
      userIdentity = getUserIdentityRepo().getUserIdentityByUsername(username);
    } else if (email != null) {
      userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(email);
    } else {
      throw new InvalidRequestException("Invalid param type passed");
    }

    JsonObject result = userIdentity != null ? new JsonObject(userIdentity.toJson(false, "user_id", "username", "email_id")) : new JsonObject();
    return result;
  }

  @Override
  public JsonObject resendConfirmationEmail(String emailId) {
    // TO-DO resend email notification
    final UserIdentity userIdentity = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    final String token = InternalHelper.generateToken(InternalHelper.EMAIL_CONFIRM_TOKEN);
    getRedisClient().set(token, userIdentity.getEmailId(), EXPIRE_IN_SECONDS);
    return new JsonObject();
  }

  @Override
  public JsonObject confirmUserEmail(String userId, String token) {
    final String emailId = getRedisClient().get(token);
    rejectIfNull(emailId, MessageCodeConstants.AU0028, HttpConstants.HttpStatus.UNAUTHORIZED.getCode());
    final UserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    if (!userIdentity.getEmailId().equalsIgnoreCase(emailId)) {       
      userIdentity.setEmailId(emailId);
    }
    userIdentity.setetEmailConfirmStatus(true);
    getUserIdentityRepo().saveOrUpdate(userIdentity);
    getRedisClient().del(token);
    return new JsonObject();
  }

  @Override
  public JsonObject updateUserEmail(String emailId) {
    if (emailId != null) {
      UserIdentity userIdentityEmail = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
      rejectIfNull(userIdentityEmail, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
      final String token = InternalHelper.generateToken(InternalHelper.EMAIL_CONFIRM_TOKEN);
      getRedisClient().set(token, emailId, EXPIRE_IN_SECONDS);
      // send email notification
    }
    return new JsonObject();
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
