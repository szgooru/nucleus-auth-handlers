package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("user_demograph")
public class User extends Model {

  public String getUserId() {
    return getString("user_id");
  }

  public void setUserId(String userId) {
    set("user_id", userId);
  }

  public String getFirstname() {
    return getString("firstname");
  }

  public void setFirstname(String firstname) {
    set("firstname", firstname);
  }

  public String getLastname() {
    return getString("lastname");
  }

  public void setLastname(String lastname) {
    set("lastname", lastname);
  }

  public String getGender() {
    return getString("gender");
  }

  public void setGender(String gender) {
    set("gender", gender);
  }

  public String getUserCategory() {
    return getString("user_category");
  }

  public void setUserCategory(String userCategory) {
    set("user_category", userCategory);
  }

  public String getBirthDate() {
    return getString("birth_date");
  }

  public void setBirthDate(String birthDate) {
    set("birth_date", birthDate);
  }

  public String getModifiedBy() {
    return getString("modified_by");
  }

  public void setModifiedBy(String modifiedBy) {
    set("modified_by", modifiedBy);
  }

  public String getParentUserId() {
    return getString("parent_user_id");
  }

  public void setParentUserId(String parentUserId) {
    set("parent_user_id", parentUserId);
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
    set("grade", grade);
  }

  public String getAboutMe() {
    return getString("about_me");
  }

  public void setAboutMe(String aboutMe) {
    set("about_me", aboutMe);
  }

  public String getSchoolId() {
    return getString("school_id");
  }

  public void setSchoolId(String schoolId) {
    set("school_id", schoolId);
  }

  public String getSchoolDistrictId() {
    return getString("school_district_id");
  }

  public void setSchoolDistrictId(String schoolDistrictId) {
    set("school_district_id", schoolDistrictId);
  }

  public String getEmail() {
    return getString("email");
  }

  public void setEmail(String email) {
    set("email", email);
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
    set("state_id", stateId);
  }

  public JsonArray getCourse() {
    String json = getString("course");
    JsonArray course = null;
    if (json != null) {
      course = new JsonArray(json);
    }
    return course;
  }

  public void setCourse(JsonArray course) {
    set("course", course);
  }
}
