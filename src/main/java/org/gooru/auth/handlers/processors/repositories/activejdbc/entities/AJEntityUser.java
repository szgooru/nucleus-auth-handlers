package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;

import java.util.Date;

import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_DEMOGRAPHIC)
@IdName(SchemaConstants.ID)
public class AJEntityUser extends Model {

  public String getId() {
    return getString(ParameterConstants.PARAM_ID);
  }

  public String getFirstname() {
    return getString(ParameterConstants.PARAM_USER_FIRSTNAME);
  }

  public void setFirstname(String firstname) {
    setString(ParameterConstants.PARAM_USER_FIRSTNAME, firstname);
  }

  public String getLastname() {
    return getString(ParameterConstants.PARAM_USER_LASTNAME);
  }

  public void setLastname(String lastname) {
    setString(ParameterConstants.PARAM_USER_LASTNAME, lastname);
  }

  public String getGender() {
    return getString(ParameterConstants.PARAM_USER_GENDER);
  }

  public void setGender(String gender) {
    set(ParameterConstants.PARAM_USER_GENDER, DBEnums.userGenderType(gender));
  }

  public String getUserCategory() {
    return getString(ParameterConstants.PARAM_USER_CATEGORY);
  }

  public void setUserCategory(String userCategory) {
    set(ParameterConstants.PARAM_USER_CATEGORY, DBEnums.userCategoryType(userCategory));
  }

  public Date getBirthDate() {
    return getDate(ParameterConstants.PARAM_USER_BIRTH_DATE);
  }

  public void setBirthDate(Date birthDate) {
    setDate(ParameterConstants.PARAM_USER_BIRTH_DATE, birthDate);
  }

  public String getParentUserId() {
    return getString(ParameterConstants.PARAM_USER_PARENT_USER_ID);
  }

  public void setParentUserId(String parentUserId) {
    setString(ParameterConstants.PARAM_USER_PARENT_USER_ID, parentUserId);
  }

  public JsonArray getGrade() {
    String json = getString(ParameterConstants.PARAM_GRADE);
    JsonArray grade = null;
    if (json != null) {
      grade = new JsonArray(json);
    }
    return grade;
  }

  public void setGrade(JsonArray grade) {
    set(ParameterConstants.PARAM_GRADE, DBEnums.jsonArray(grade));
  }

  public String getAboutMe() {
    return getString(ParameterConstants.PARAM_USER_ABOUT_ME);
  }

  public void setAboutMe(String aboutMe) {
    setString(ParameterConstants.PARAM_USER_ABOUT_ME, aboutMe);
  }

  public String getSchoolId() {
    return getString(ParameterConstants.PARAM_USER_SCHOOL_ID);
  }

  public void setSchoolId(String schoolId) {
    setString(ParameterConstants.PARAM_USER_SCHOOL_ID, schoolId);
  }

  public String getSchoolDistrictId() {
    return getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID);
  }

  public void setSchoolDistrictId(String schoolDistrictId) {
    setString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID, schoolDistrictId);
  }

  public String getEmailId() {
    return getString(ParameterConstants.PARAM_USER_EMAIL_ID);
  }

  public void setEmailId(String emailId) {
    set(ParameterConstants.PARAM_USER_EMAIL_ID, emailId);
  }

  public Long getCountryId() {
    return getLong(ParameterConstants.PARAM_USER_COUNTRY_ID);
  }

  public void setCountryId(Long countryId) {
    set(ParameterConstants.PARAM_USER_COUNTRY_ID, countryId);
  }

  public Long getStateId() {
    return getLong(ParameterConstants.PARAM_USER_STATE_ID);
  }

  public void setStateId(Long stateId) {
    setLong(ParameterConstants.PARAM_USER_STATE_ID, stateId);
  }

  public JsonArray getCourse() {
    String json = getString(ParameterConstants.PARAM_COURSE);
    JsonArray course = null;
    if (json != null) {
      course = new JsonArray(json);
    }
    return course;
  }

  public void setCourse(JsonArray course) {
    set(ParameterConstants.PARAM_COURSE, DBEnums.jsonArray(course));
  }

  public String getThumbnailPath() {
    return getString(ParameterConstants.PARAM_USER_THUMBNAIL_PATH);
  }

  public void setThumbnailPath(String thumbnailPath) {
    setString(ParameterConstants.PARAM_USER_THUMBNAIL_PATH, thumbnailPath);
  }
}
