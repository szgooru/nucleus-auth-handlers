package org.gooru.nucleus.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

public class UserPrefsDTO {

    private JsonObject requestBody;

    public UserPrefsDTO(JsonObject requestBody) {
        this.requestBody = requestBody;
    }


    public Boolean getProfileVisibility() {
        return this.requestBody.getBoolean(ParameterConstants.PARAM_PROFILE_VISIBILITY);
    }
}
