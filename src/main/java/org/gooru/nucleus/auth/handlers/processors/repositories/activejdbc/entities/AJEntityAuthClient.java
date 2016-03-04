package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.gooru.nucleus.auth.handlers.constants.ParameterConstants;
import org.gooru.nucleus.auth.handlers.constants.SchemaConstants;
import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.IdName;
import org.javalite.activejdbc.annotations.Table;

@Table(SchemaConstants.AUTH_CLIENT)
@IdName(SchemaConstants.CLIENT_ID)
public class AJEntityAuthClient extends Model {

  public String getClientId() {
    return getString(ParameterConstants.PARAM_CLIENT_ID);
  }

  public String getClientKey() {
    return getString(ParameterConstants.PARAM_CLIENT_KEY);
  }

  public JsonArray getGrantTypes() {
    return new JsonArray(getString(ParameterConstants.PARAM_GRANT_TYPES));
  }

  public JsonArray getRefererDomains() {
    String json = getString(ParameterConstants.PARAM_REFERER_DOMAINS);
    JsonArray refererDomains = null;
    if (json != null) {
      refererDomains = new JsonArray(json);
    }
    return refererDomains;
  }

  public int getAccessTokenValidity() {
    return getInteger(ParameterConstants.PARAM_ACCESS_TOKEN_VALIDITY);
  }

  public JsonObject getCdnUrls() {
    String json = getString(ParameterConstants.PARAM_CDN_URLS);
    JsonObject cdnUrls = null;
    if (json != null) {
      cdnUrls = new JsonObject(json);
    }
    return cdnUrls;
  }
}
