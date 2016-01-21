package org.gooru.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Map;

import org.gooru.auth.handlers.constants.ParameterConstants;

public class UserDTO extends JsonObject {

  public UserDTO() {
    super();
  }

  public UserDTO(Map<String, Object> map) {
    super(map);
  }
  
  
  public String getFirstname() {
    return getString(ParameterConstants.PARAM_USER_FIRSTNAME);
  }

  public String getLastname() {
    return getString(ParameterConstants.PARAM_USER_LASTNAME);
  }

  public String getUsername() {
    return getString(ParameterConstants.PARAM_USER_USERNAME);
  }

  public String getEmailId() {
    return getString(ParameterConstants.PARAM_USER_EMAIL_ID);
  }

  public String getUserCategory() {
    return getString(ParameterConstants.PARAM_USER_CATEGORY);
  }

  public String getBirthDate() {
    return getString(ParameterConstants.PARAM_USER_BIRTH_DATE);
  }

  public String getParentEmailId() {
    return getString(ParameterConstants.PARAM_USER_PARENT_EMAIL_ID);
  }

  public String getPassword() {
    return getString(ParameterConstants.PARAM_USER_PASSWORD);
  }

  public String getSchoolId() {
    return getString(ParameterConstants.PARAM_USER_SCHOOL_ID);
  }

  public String getSchool() {
    return getString(ParameterConstants.PARAM_USER_SCHOOL);
  }

  public String getSchoolDistrictId() {
    return getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID);
  }

  public String getSchoolDistrict() {
    return getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
  }

  public Long getStateId() {
    return getLong(ParameterConstants.PARAM_USER_STATE_ID);
  }

  public String getState() {
    return getString(ParameterConstants.PARAM_USER_STATE);
  }

  public Long getCountryId() {
    return getLong(ParameterConstants.PARAM_USER_COUNTRY_ID);
  }

  public String getCountry() {
    return getString(ParameterConstants.PARAM_USER_COUNTRY);
  }
  
  public String getThumbnailPath() {
    return getString(ParameterConstants.PARAM_USER_THUMBNAIL_PATH);
  }

  public String getGender() {
    return getString(ParameterConstants.PARAM_USER_GENDER);
  }

  public JsonArray getGrade() {
    return getJsonArray(ParameterConstants.PARAM_GRADE);
  }

  public JsonArray getCourse() {
    return getJsonArray(ParameterConstants.PARAM_COURSE);
  }

  public String getIdentityId() {
    return getString(ParameterConstants.PARAM_AUTHORIZE_IDENTITY_ID);
  }

  public String getAboutMe() {
    return getString(ParameterConstants.PARAM_USER_ABOUT_ME);
  }
  
  

}
