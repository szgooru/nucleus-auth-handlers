package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import java.util.UUID;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.SCHOOL)
@IdName(SchemaConstants.ID)
public class AJEntitySchool extends Model {

    public static final String GET_SCHOOL_BY_NAME = "name = ?";
    public static final String GET_SCHOOL_BY_ID = "id = ?::uuid";

    public String getName() {
        return getString(ParameterConstants.PARAM_NAME);
    }

    public void setName(String name) {
        set(ParameterConstants.PARAM_NAME, name);
    }

    public String getCode() {
        return getString(ParameterConstants.PARAM_CODE);
    }

    public void setCode(String code) {
        set(ParameterConstants.PARAM_CODE, code);
    }

    public String getCreatorId() {
        return getString(ParameterConstants.PARAM_CREATOR_ID);
    }

    public void setCreatorId(Object creatorId) {
        set(ParameterConstants.PARAM_CREATOR_ID, creatorId);
    }

    public String getSchoolDistrictId() {
        return getString(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID);
    }

    public void setSchoolDistrictId(String schoolDistrictId) {
        set(ParameterConstants.PARAM_USER_SCHOOL_DISTRICT_ID, UUID.fromString(schoolDistrictId));
    }

}
