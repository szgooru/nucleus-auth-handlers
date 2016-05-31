package org.gooru.nucleus.auth.handlers.processors.data.transform.model;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;

public class AuthClientDTO {

    private JsonObject requestBody;

    public AuthClientDTO(JsonObject requestBody) {
        this.requestBody = requestBody;
    }

    public String getClientId() {
        return this.requestBody.getString(ParameterConstants.PARAM_CLIENT_ID);
    }

    public String getClientKey() {
        return this.requestBody.getString(ParameterConstants.PARAM_CLIENT_KEY);
    }

    public String getGrantType() {
        return this.requestBody.getString(ParameterConstants.PARAM_GRANT_TYPE);
    }
    
    public JsonArray getGrantTypes() {
        return this.requestBody.getJsonArray(ParameterConstants.PARAM_GRANT_TYPES);
    }
    
    public String getUrl() {
        return this.requestBody.getString(ParameterConstants.PARAM_URL);
    }
    
    public String getName() {
        return this.requestBody.getString(ParameterConstants.PARAM_NAME);
    }
    
    public String getDescription() {
        return this.requestBody.getString(ParameterConstants.PARAM_DESCRIPTION);
    }
    
    public String getContactEmail() {
        return this.requestBody.getString(ParameterConstants.PARAM_CONTACT_EMAIL);
    }
    
    public JsonObject getCdnUrls() {
        return this.requestBody.getJsonObject(ParameterConstants.PARAM_CDN_URLS);
    }

}
