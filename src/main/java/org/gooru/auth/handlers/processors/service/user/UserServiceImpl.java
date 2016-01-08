package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Base64;
import java.util.UUID;

import org.gooru.auth.handlers.constants.ErrorConstants;
import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.ServerMessageConstants;
import org.gooru.auth.handlers.processors.error.Errors;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.User;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserIdentity;
import org.gooru.auth.handlers.processors.service.Validator;
import org.gooru.auth.handlers.utils.InternalHelper;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;

public class UserServiceImpl extends ServerValidatorUtility implements UserService {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;

  public UserServiceImpl() {
    setUserIdentityRepo(UserIdentityRepo.getInstance());
    setUserRepo(UserRepo.instance());
  }

  @Override
  public JsonObject createUser(JsonObject userJson, String clientId) {
    Validator<User> userValidator = createUserValidator(userJson);
    rejectError(userValidator.getErrors(), HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ErrorConstants.VALIDATION_ERROR);
    User user = getUserRepo().create(userValidator.getModel());
    UserIdentity userIdentity = setUserIdenityValue(userJson, userValidator.getModel(), clientId);
    getUserIdentityRepo().saveOrUpdate(userIdentity);
    return new JsonObject(user.toMap());
  }

  @Override
  public JsonObject updateUser(String userId, JsonObject userJson) {

    User user = getUserRepo().getUser(userId);
    ServerValidatorUtility.rejectIfNull(user, ServerMessageConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
    User newUser = new User();
    newUser.fromMap(userJson.getMap());
    if (newUser.getFirstname() != null) {
      ServerValidatorUtility.reject(!newUser.getFirstname().matches("[a-zA-Z0-9 ]+"), ServerMessageConstants.AU0021,
              HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_LASTNAME);
      user.setFirstname(newUser.getFirstname());

    }
    if (newUser.getLastname() != null) {
      ServerValidatorUtility.reject(!newUser.getFirstname().matches("[a-zA-Z0-9 ]+"), ServerMessageConstants.AU0021,
              HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_FIRSTNAME);
      user.setLastname(newUser.getLastname());
    }
    if (newUser.getGender() != null) {
      ServerValidatorUtility.reject((HelperConstants.USER_GENDER.get(newUser.getGender()) == null), ServerMessageConstants.AU0024,
              HttpConstants.HttpStatus.BAD_REQUEST.getCode());
      user.setGender(newUser.getGender());
    }

    if (newUser.getUserCategory() != null) {
      ServerValidatorUtility.reject((HelperConstants.USER_CATEGORY.get(newUser.getUserCategory()) == null), ServerMessageConstants.AU0025,
              HttpConstants.HttpStatus.BAD_REQUEST.getCode());
      user.setUserCategory(newUser.getUserCategory());
    }

    if (newUser.getGrade() != null) {
      user.setGrade(newUser.getGrade());
    }
    if (newUser.getCourse() != null) {
      user.setCourse(newUser.getCourse());
    }

    return new JsonObject(user.toMap());
  }

  @Override
  public JsonObject getUser(String userId) {
    User user = getUserRepo().getUser(userId);
    return new JsonObject(user.toMap());
  }

  private Validator<User> createUserValidator(JsonObject userJson) {
    String firstname = userJson.getString(ParameterConstants.PARAM_USER_FIRSTNAME);
    String lastname = userJson.getString(ParameterConstants.PARAM_USER_LASTNAME);
    String username = userJson.getString(ParameterConstants.PARAM_USER_USERNAME);
    String emailId = userJson.getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    String birthDate = userJson.getString(ParameterConstants.PARAM_USER_BIRTH_DATE);
    String userCategory = userJson.getString(ParameterConstants.PARAM_USER_CATEGORY);
    String gender = userJson.getString(ParameterConstants.PARAM_USER_GENDER);
    String aboutMe = userJson.getString(ParameterConstants.PARAM_USER_ABOUT_ME);

    Errors errors = new Errors();
    User user = new User();
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_FIRSTNAME, firstname, ServerMessageConstants.AU0011);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_LASTNAME, lastname, ServerMessageConstants.AU0012);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_USERNAME, username, ServerMessageConstants.AU0013);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_EMAIL_ID, emailId, ServerMessageConstants.AU0014);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_BIRTH_DATE, birthDate, ServerMessageConstants.AU0015);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_CATEGORY, userCategory, ServerMessageConstants.AU0016);
    String password = userJson.getString(ParameterConstants.PARAM_USER_PASSWORD);
    addValidatorIfNullOrEmptyError(errors, ParameterConstants.PARAM_USER_PASSWORD, password, ServerMessageConstants.AU0016);
    addValidator(errors, !(username != null && username.matches("[a-zA-Z0-9]+")), ParameterConstants.PARAM_USER_USERNAME,
            ServerMessageConstants.AU0017);
    addValidator(errors, (username != null && (username.length() < 4 || username.length() > 20)), ParameterConstants.PARAM_USER_USERNAME,
            ServerMessageConstants.AU0018, ParameterConstants.PARAM_USER_USERNAME, "4", "20");
    addValidator(errors, (password != null && (password.length() < 5 || password.length() > 14)), ParameterConstants.PARAM_USER_PASSWORD,
            ServerMessageConstants.AU0018, ParameterConstants.PARAM_USER_PASSWORD, "5", "14");

    addValidator(errors, !(emailId != null && emailId.indexOf("@") > 1), ParameterConstants.PARAM_USER_EMAIL_ID, ServerMessageConstants.AU0020);
    addValidator(errors, !(firstname != null && firstname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_FIRSTNAME,
            ServerMessageConstants.AU0021);
    addValidator(errors, !(lastname != null && lastname.matches("[a-zA-Z0-9 ]+")), ParameterConstants.PARAM_USER_LASTNAME,
            ServerMessageConstants.AU0021);
    addValidator(errors, !(birthDate != null && InternalHelper.isValidDate(birthDate)), ParameterConstants.PARAM_USER_BIRTH_DATE,
            ServerMessageConstants.AU0022);
    if (username != null) {
      UserIdentity userIdentityUsername = getUserIdentityRepo().getUserIdentityByUsername(username);
      addValidator(errors, !(userIdentityUsername == null), ParameterConstants.PARAM_USER_USERNAME, ServerMessageConstants.AU0023, username,
              ParameterConstants.PARAM_USER_USERNAME);
    }
    if (emailId != null) {
      UserIdentity userIdentityEmail = getUserIdentityRepo().getUserIdentityByEmailId(emailId);
      addValidator(errors, !(userIdentityEmail == null), ParameterConstants.PARAM_USER_EMAIL_ID, ServerMessageConstants.AU0023, emailId,
              ParameterConstants.PARAM_USER_EMAIL_ID);
    }
    addValidator(errors, (userCategory != null && HelperConstants.USER_CATEGORY.get(userCategory) == null), ParameterConstants.PARAM_USER_CATEGORY,
            ServerMessageConstants.AU0025);

    if (gender != null) {
      addValidator(errors, (HelperConstants.USER_GENDER.get(gender) == null), ParameterConstants.PARAM_USER_GENDER, ServerMessageConstants.AU0024);
    }
    if (aboutMe != null) {
      user.setAboutMe(aboutMe);
    }
    user.setId(UUID.randomUUID().toString());
    user.setFirstname(firstname);
    user.setLastname(lastname);
    user.setUserCategory(userCategory);
    user.setEmailId(emailId);
    user.setModifiedBy(user.getId());

    JsonArray grade = userJson.getJsonArray(ParameterConstants.PARAM_GRADE);
    if (grade != null) {
      user.setGrade(grade);
    }

    // TO-DO Added validation
    /*
     * school school district country state set values in state, school, school
     * district provide support for set parent user id
     */
    return new Validator<User>(user, errors);
  }

  private UserIdentity setUserIdenityValue(JsonObject userJson, User user, String clientId) {
    UserIdentity userIdentity = new UserIdentity();
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

  public static void main(String a[]) {
    System.out.println(Base64.getEncoder().encodeToString("sheeban@gooru.org:sheeban".getBytes()));
    System.out.println(InternalHelper.encryptPassword("sheeban"));
  }
}
