package org.gooru.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.util.Date;

import org.gooru.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_DEMOGRAPHIC)
@IdName(SchemaConstants.ID)
public class AJEntityUser extends Model {

  public String getId() {
    return getString("id");
  }

  public String getFirstname() {
    return getString("firstname");
  }

  public void setFirstname(String firstname) {
    setString("firstname", firstname);
  }

  public String getLastname() {
    return getString("lastname");
  }

  public void setLastname(String lastname) {
    setString("lastname", lastname);
  }

  public String getGender() {
    return getString("gender");
  }

  public void setGender(String gender) {
    set("gender", DBEnums.userGenderType(gender));
  }

  public String getUserCategory() {
    return getString("user_category");
  }

  public void setUserCategory(String userCategory) {
    set("user_category", DBEnums.userCategoryType(userCategory));
  }

  public Date getBirthDate() {
    return getDate("birth_date");
  }

  public void setBirthDate(Date birthDate) {
    setDate("birth_date", birthDate);
  }

  public String getParentUserId() {
    return getString("parent_user_id");
  }

  public void setParentUserId(String parentUserId) {
    setString("parent_user_id", parentUserId);
  }

  public JsonArray getGrade() {
    String json = getString("grade");
    JsonArray grade = null;
    if (json != null) {
      grade = new JsonArray(json);
    }
    return grade;
  }

  public void setGrade(JsonArray grade) {
    set("grade", DBEnums.jsonArray(grade));
  }

  public String getAboutMe() {
    return getString("about_me");
  }

  public void setAboutMe(String aboutMe) {
    setString("about_me", aboutMe);
  }

  public String getSchoolId() {
    return getString("school_id");
  }

  public void setSchoolId(String schoolId) {
    setString("school_id", schoolId);
  }

  public String getSchoolDistrictId() {
    return getString("school_district_id");
  }

  public void setSchoolDistrictId(String schoolDistrictId) {
    setString("school_district_id", schoolDistrictId);
  }

  public String getEmailId() {
    return getString("email_id");
  }

  public void setEmailId(String emailId) {
    set("email_id", emailId);
  }

  public Long getCountryId() {
    return getLong("country_id");
  }

  public void setCountryId(Long countryId) {
    set("country_id", countryId);
  }

  public Long getStateId() {
    return getLong("state_id");
  }

  public void setStateId(Long stateId) {
    setLong("state_id", stateId);
  }

  public JsonObject getCourse() {
    String json = getString("course");
    JsonObject course = null;
    if (json != null) {
      course = new JsonObject(json);
    }
    return course;
  }

  public void setCourse(JsonObject course) {
    set("course", DBEnums.jsonObject(course));
  }

  public String getThumbnailPath() {
    return getString("thumbnail_path");
  }

  public void setThumbnailPath(String thumbnailPath) {
    setString("thumbnail_path", thumbnailPath);
  }
}
