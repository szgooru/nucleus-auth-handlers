package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc.entities;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import org.javalite.activejdbc.Model;
import org.javalite.activejdbc.annotations.Table;

@Table("auth_client")
public class AuthClient extends Model {

  public String getClientId() {
    return getString("client_id");
  }

  public String getClientKey() {
    return getString("client_key");
  }

  public JsonArray getGrantTypes() {
    return new JsonArray(getString("grant_types"));
  }

  public JsonArray getRefererDomains() {
    String json = getString("referer_domains");
    JsonArray refererDomains = null;
    if (json != null) {
      refererDomains = new JsonArray(json);
    }
    return refererDomains;
  }

  public int getAccessTokenValidity() {
    return getInteger("access_token_validity");
  }

  public JsonObject getCdnUrls() {
    String json = getString("cdn_urls");
    JsonObject cdnUrls = null;
    if (json != null) {
      cdnUrls = new JsonObject(json);
    }
    return cdnUrls;
  }
}
