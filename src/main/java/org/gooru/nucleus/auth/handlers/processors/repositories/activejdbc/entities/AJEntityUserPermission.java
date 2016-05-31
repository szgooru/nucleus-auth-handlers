package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.USER_PERMISSION)
@IdName(SchemaConstants.USER_ID)
public class AJEntityUserPermission extends Model {

    public static final String GET_USER_PERMISSION = "user_id = ?::uuid";

    public String getUserId() {
        return getString(ParameterConstants.PARAM_USER_ID);
    }

    public void setUserId(Object userId) {
        setId(userId);
        set(ParameterConstants.PARAM_USER_ID, userId);
    }

    public JsonObject getPermission() {
        String json = getString(ParameterConstants.PARAM_PERMISSION);
        JsonObject permissions = null;
        if (json != null) {
            permissions = new JsonObject(json);
        }
        return permissions;
    }

}
