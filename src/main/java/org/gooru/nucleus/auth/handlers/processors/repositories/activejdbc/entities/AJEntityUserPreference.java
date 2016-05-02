package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_PREFERENCE)
@IdName(SchemaConstants.USER_ID)
public class AJEntityUserPreference extends Model {

    public static final String GET_USER_PREFERENCE = "user_id = ?::uuid";

    public String getUserId() {
        return getString(ParameterConstants.PARAM_USER_ID);
    }

    public void setUserId(Object userId) {
        setId(userId);
        set(ParameterConstants.PARAM_USER_ID, userId);
    }

    public Boolean getProfileVisiblity(Boolean profileVisiblity) {
        return getBoolean(ParameterConstants.PARAM_PROFILE_VISIBILITY);
    }

    public void setProfileVisiblity(Boolean profileVisiblity) {
        set(ParameterConstants.PARAM_PROFILE_VISIBILITY, profileVisiblity);
    }

}
