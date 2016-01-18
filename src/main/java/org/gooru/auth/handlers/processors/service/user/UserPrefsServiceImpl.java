package org.gooru.auth.handlers.processors.service.user;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.HelperConstants;
import org.gooru.auth.handlers.constants.HttpConstants;
import org.gooru.auth.handlers.constants.MessageCodeConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.data.transform.model.UserPrefsDTO;
import org.gooru.auth.handlers.processors.repositories.UserIdentityRepo;
import org.gooru.auth.handlers.processors.repositories.UserPreferenceRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserIdentity;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUserPreference;
import org.gooru.auth.handlers.processors.service.AJResponseJsonTransformer;
import org.gooru.auth.handlers.processors.service.MessageResponse;
import org.gooru.auth.handlers.utils.ServerValidatorUtility;

public class UserPrefsServiceImpl extends ServerValidatorUtility implements UserPrefsService {

  private UserPreferenceRepo userPreferenceRepo;

  private UserIdentityRepo userIdentityRepo;

  public UserPrefsServiceImpl() {
    setUserPreferenceRepo(UserPreferenceRepo.instance());
    setUserIdentityRepo(UserIdentityRepo.instance());
  }

  @Override
  public MessageResponse updateUserPreference(String userId, UserPrefsDTO userPrefsDTO) {
    final AJEntityUserIdentity userIdentity = getUserIdentityRepo().getUserIdentityById(userId);
    rejectIfNull(userIdentity, MessageCodeConstants.AU0026, HttpConstants.HttpStatus.NOT_FOUND.getCode(), ParameterConstants.PARAM_USER);
    reject(userIdentity.getStatus().equalsIgnoreCase(ParameterConstants.PARAM_STATUS_DEACTIVTED), MessageCodeConstants.AU0009,
            HttpConstants.HttpStatus.FORBIDDEN.getCode());
    AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userId);
    boolean isNew = false;
    if (userPreference == null) {
      userPreference = new AJEntityUserPreference();
      userPreference.setUserId(userId);
      isNew = true;
    }
    if (userPrefsDTO.getStandardPreference() != null) {
      userPreference.setStandardPreference(userPrefsDTO.getStandardPreference());
    }

    if (userPrefsDTO.getProfileVisiblity() != null) {
      userPreference.setProfileVisiblity(userPrefsDTO.getProfileVisiblity());
    }
    if (isNew) {
      getUserPreferenceRepo().createPreference(userPreference);
    } else {
      getUserPreferenceRepo().updatePreference(userPreference);
    }
    return new MessageResponse.Builder().setContentTypeJson().setStatusNoOutput().successful().build();
  }

  @Override
  public MessageResponse getUserPreference(String userId) {
    AJEntityUserPreference userPreference = getUserPreferenceRepo().getUserPreference(userId);
    JsonObject result = AJResponseJsonTransformer.transform(userPreference.toJson(false), HelperConstants.USERS_PREFS_JSON_FIELDS, true);
    return new MessageResponse.Builder().setResponseBody(result).setContentTypeJson().setStatusOkay().successful().build();
  }

  public UserPreferenceRepo getUserPreferenceRepo() {
    return userPreferenceRepo;
  }

  public void setUserPreferenceRepo(UserPreferenceRepo userPreferenceRepo) {
    this.userPreferenceRepo = userPreferenceRepo;
  }

  public UserIdentityRepo getUserIdentityRepo() {
    return userIdentityRepo;
  }

  public void setUserIdentityRepo(UserIdentityRepo userIdentityRepo) {
    this.userIdentityRepo = userIdentityRepo;
  }

}
