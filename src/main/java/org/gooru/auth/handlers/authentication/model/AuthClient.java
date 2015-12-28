package org.gooru.auth.handlers.authentication.model;

import io.vertx.core.json.JsonObject;

public class AuthClient {

  private final JsonObject json;

  public String getClientId() {
    return json.getString("client_id");
  }

  public AuthClient setClientId(String clientId) {
    json.put("client_id", clientId);
    return this;
  }

  public String getClientSecret() {
    return json.getString("client_secret");
  }

  public AuthClient setClientSecret(String clientSecret) {
    json.put("client_secret", clientSecret);
    return this;
  }

  public String getGrantType() {
    return json.getString("grant_type");
  }

  public AuthClient setGrantType(String grantType) {
    json.put("grant_type", grantType);
    return this;
  }

  public AuthClient() {
    json = new JsonObject();
  }

  public AuthClient(JsonObject json) {
    this.json = json.copy();
  }

  public AuthClient(AuthClient authClient) {
    this(authClient.toJSON());
  }

  public JsonObject toJSON() {
    return json;
  }

}
