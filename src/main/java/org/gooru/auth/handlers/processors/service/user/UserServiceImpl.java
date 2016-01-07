package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.UUID;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.ServerMessageConstants;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.User;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.UserIdentity;
import org.gooru.auth.handlers.utils.InternalHelper;
import org.gooru.auth.handlers.utils.ServerValidationUtility;

public class UserServiceImpl implements UserService {

  private UserIdentityRepo userIdentityRepo;

  private UserRepo userRepo;


  public UserServiceImpl() {
    setUserIdentityRepo(UserIdentityRepo.getInstance());
    setUserRepo(UserRepo.instance());
  }

  @Override
  public JsonObject createUser(JsonObject userJson, String clientId) {
    User user = validateUserAndSetValue(userJson);
    getUserRepo().create(user);
    UserIdentity userIdentity = setUserIdenityValue(userJson, user, clientId);
    getUserIdentityRepo().saveOrUpdate(userIdentity);
    return new JsonObject(user.toMap());
  }

  @Override
  public JsonObject updateUser(String userId, JsonObject userJson) {
    User user = getUserRepo().getUser(userId);
    ServerValidationUtility.rejectIfNull(user, ServerMessageConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(),
            ParameterConstants.PARAM_USER);
    User newUser = new User();
    newUser.fromMap(userJson.getMap());
    if (newUser.getFirstname() != null) {
      ServerValidationUtility.reject(!newUser.getFirstname().matches("[a-zA-Z0-9 ]+"), ServerMessageConstants.AU0021,
              HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_LASTNAME);
      user.setFirstname(newUser.getFirstname());

    }
    if (newUser.getLastname() != null) {
      ServerValidationUtility.reject(!newUser.getFirstname().matches("[a-zA-Z0-9 ]+"), ServerMessageConstants.AU0021,
              HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_FIRSTNAME);
      user.setLastname(newUser.getLastname());
    }
    if (newUser.getGender() != null) {
      ServerValidationUtility.reject((HelperConstants.USER_GENDER.get(newUser.getGender()) == null), ServerMessageConstants.AU0024,
              HttpConstants.HttpStatus.BAD_REQUEST.getCode());
      user.setGender(newUser.getGender());
    }

    if (newUser.getUserCategory() != null) {
      ServerValidationUtility.reject((HelperConstants.USER_CATEGORY.get(newUser.getUserCategory()) == null), ServerMessageConstants.AU0025,
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

  private User validateUserAndSetValue(JsonObject userJson) {
    String firstname = userJson.getString(ParameterConstants.PARAM_USER_FIRSTNAME);
    ServerValidationUtility.rejectIfNullOrEmpty(firstname, ServerMessageConstants.AU0011, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    String lastname = userJson.getString(ParameterConstants.PARAM_USER_LASTNAME);
    ServerValidationUtility.rejectIfNullOrEmpty(lastname, ServerMessageConstants.AU0012, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    String username = userJson.getString(ParameterConstants.PARAM_USER_USERNAME);
    ServerValidationUtility.rejectIfNullOrEmpty(username, ServerMessageConstants.AU0013, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    String email = userJson.getString(ParameterConstants.PARAM_USER_EMAIL);
    ServerValidationUtility.rejectIfNullOrEmpty(email, ServerMessageConstants.AU0014, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    String dob = userJson.getString(ParameterConstants.PARAM_USER_BIRTH_DATE);
    ServerValidationUtility.rejectIfNullOrEmpty(dob, ServerMessageConstants.AU0015, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    String category = userJson.getString(ParameterConstants.PARAM_USER_CATEGORY);
    ServerValidationUtility.rejectIfNullOrEmpty(category, ServerMessageConstants.AU0016, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    String password = userJson.getString(ParameterConstants.PARAM_USER_PASSWORD);
    ServerValidationUtility.rejectIfNullOrEmpty(password, ServerMessageConstants.AU0019, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    ServerValidationUtility.reject(!username.matches("[a-zA-Z0-9]+"), ServerMessageConstants.AU0017, HttpConstants.HttpStatus.BAD_REQUEST.getCode(),
            ParameterConstants.PARAM_USER_USERNAME);
    ServerValidationUtility.reject((username.length() < 4 && username.length() > 20), ServerMessageConstants.AU0018,
            HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_USERNAME, "4", "20");
    ServerValidationUtility.reject((password.length() < 5 && password.length() > 14), ServerMessageConstants.AU0018,
            HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_USERNAME, "5", "14");
    ServerValidationUtility.reject(!(email.indexOf("@") > 1), ServerMessageConstants.AU0020, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    ServerValidationUtility.reject(!firstname.matches("[a-zA-Z0-9 ]+"), ServerMessageConstants.AU0021,
            HttpConstants.HttpStatus.BAD_REQUEST.getCode(), ParameterConstants.PARAM_USER_FIRSTNAME);
    ServerValidationUtility.reject(!lastname.matches("[a-zA-Z0-9 ]+"), ServerMessageConstants.AU0021, HttpConstants.HttpStatus.BAD_REQUEST.getCode(),
            ParameterConstants.PARAM_USER_LASTNAME);
    ServerValidationUtility.reject(!(InternalHelper.isValidDate(dob)), ServerMessageConstants.AU0022, HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    UserIdentity userIdentityUsername = getUserIdentityRepo().getUserIdentityByUsername(username);
    ServerValidationUtility.reject(!(userIdentityUsername == null), ServerMessageConstants.AU0023, HttpConstants.HttpStatus.BAD_REQUEST.getCode(),
            username, ParameterConstants.PARAM_USER_USERNAME);
    UserIdentity userIdentityEmail = getUserIdentityRepo().getUserIdentityByEmailId(email);
    ServerValidationUtility.reject(!(userIdentityEmail == null), ServerMessageConstants.AU0023, HttpConstants.HttpStatus.BAD_REQUEST.getCode(),
            email, ParameterConstants.PARAM_USER_EMAIL);
    String gender = userJson.getString(ParameterConstants.PARAM_USER_GENDER);
    String userCategory = userJson.getString(ParameterConstants.PARAM_USER_CATEGORY);
    ServerValidationUtility.reject((HelperConstants.USER_CATEGORY.get(userCategory) == null), ServerMessageConstants.AU0025,
            HttpConstants.HttpStatus.BAD_REQUEST.getCode());
    User user = new User();
    if (gender != null) {
      ServerValidationUtility.reject((HelperConstants.USER_GENDER.get(gender) == null), ServerMessageConstants.AU0024,
              HttpConstants.HttpStatus.BAD_REQUEST.getCode());
      user.setGender(gender);
    }
    String aboutMe = userJson.getString(ParameterConstants.PARAM_USER_ABOUT_ME);

    String userId = UUID.randomUUID().toString();
    user.setUserId(userId);
    user.setFirstname(firstname);
    user.setLastname(lastname);
    user.setUserCategory(userCategory);
    user.setEmailId(email);
    user.setModifiedBy(userId);

    JsonArray grade = userJson.getJsonArray(ParameterConstants.PARAM_GRADE);
    if (grade != null) {
      user.setGrade(grade);
    }
    if (aboutMe != null) {
      user.setAboutMe(aboutMe);
    }
    // TO-DO Added validation
    /*
     * school school district country state set values in state, school, school
     * district provide support for set parent user id
     */
    return user;
  }

  private UserIdentity setUserIdenityValue(JsonObject userJson, User user, String clientId) {
    UserIdentity userIdentity = new UserIdentity();
    userIdentity.setUsername(userJson.getString(ParameterConstants.PARAM_USER_USERNAME));
    userIdentity.setEmailId(user.getEmailId());
    userIdentity.setUserId(user.getUserId());
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
}
