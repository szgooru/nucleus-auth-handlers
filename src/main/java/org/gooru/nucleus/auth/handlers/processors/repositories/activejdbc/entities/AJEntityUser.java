package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;

import java.util.Date;
import java.util.UUID;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_DEMOGRAPHIC)
@IdName(SchemaConstants.ID)
public class AJEntityUser extends Model {

    public static final String GET_USER = "id = ?::uuid";
    public static final String FIND_USER =
        "select u.*, ui.username, ui.login_type, ui.provision_type from user_demographic  u   inner join user_identity ui    on (u.id = ui.user_id)  where u.id = ?::uuid";
    public static final String FIND_USER_USING_EMAIL =
        "select u.*, ui.username, ui.login_type, ui.provision_type from user_demographic  u   inner join user_identity ui    on (u.id = ui.user_id)  where ui.email_id = ?";
    public static final String FIND_USER_USING_USERNAME =
        "select u.*, ui.username, ui.login_type, ui.provision_type from user_demographic  u   inner join user_identity ui    on (u.id = ui.user_id)  where ui.username = ?";

    public static final String FIND_USERS =
        "select u.firstname, u.lastname, u.id, u.thumbnail_path, ui.username from user_demographic  u   inner join user_identity ui    on (u.id = ui.user_id)  where u.id in (";

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
        set(ParameterConstants.PARAM_USER_PARENT_USER_ID, UUID.fromString(parentUserId));
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

    public void setSchoolId(Object schoolId) {
        set(ParameterConstants.PARAM_USER_SCHOOL_ID, schoolId);
    }

    public String getSchoolDistrictId() {
        return getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID);
    }

    public void setSchoolDistrictId(Object schoolDistrictId) {
        set(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID, schoolDistrictId);
    }

    public String getEmailId() {
        return getString(ParameterConstants.PARAM_USER_EMAIL_ID);
    }

    public void setEmailId(String emailId) {
        set(ParameterConstants.PARAM_USER_EMAIL_ID, emailId);
    }

    public String getCountryId() {
        return getString(ParameterConstants.PARAM_USER_COUNTRY_ID);
    }

    public void setCountryId(String countryId) {
        set(ParameterConstants.PARAM_USER_COUNTRY_ID, UUID.fromString(countryId));
    }

    public String getStateId() {
        return getString(ParameterConstants.PARAM_USER_STATE_ID);
    }

    public void setStateId(Object stateId) {
        set(ParameterConstants.PARAM_USER_STATE_ID, stateId);
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

    public void setCountry(String country) {
        setString(ParameterConstants.PARAM_USER_COUNTRY, country);
    }

    public String getCountry() {
        return getString(ParameterConstants.PARAM_USER_COUNTRY);
    }

    public void setState(String state) {
        setString(ParameterConstants.PARAM_USER_STATE, state);
    }

    public String getState() {
        return getString(ParameterConstants.PARAM_USER_STATE);
    }

    public void setSchool(String school) {
        setString(ParameterConstants.PARAM_USER_SCHOOL, school);
    }

    public String getSchool() {
        return getString(ParameterConstants.PARAM_USER_SCHOOL);
    }

    public void setSchoolDistrict(String schoolDistrict) {
        setString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT, schoolDistrict);
    }

    public String getSchoolDistrict() {
        return getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT);
    }
}
