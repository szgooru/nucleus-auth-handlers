package org.gooru.auth.handlers.authentication.service;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.MessageConstants;
import org.gooru.auth.handlers.infra.Redis;
import org.gooru.auth.handlers.processors.repositories.AuthClientRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AuthClient;
import org.gooru.auth.handlers.utils.InternalHelper;

public class AuthenticationServiceImpl implements AuthenticationService {

  private AuthClientRepo authClientRepo;

  public AuthenticationServiceImpl() {
    setAuthClientRepo(AuthClientRepo.create());
  }

  @Override
  public JsonObject createAnonymousAccessToken(String clientId, String clientKey) {
    AuthClient authClient = getAuthClientRepo().getAuthClient(clientId, clientKey);
    if (authClient == null) {
      throw new IllegalAccessError("invalid client id and key");
    }
    JsonObject access = new JsonObject();
    access.put("user_id", MessageConstants.MSG_USER_ANONYMOUS);
    access.put("client_id", authClient.get("client_id"));
    access.put("provided_at", System.currentTimeMillis());
    System.out.println("tokan");
    String token = InternalHelper.generateToken(MessageConstants.MSG_USER_ANONYMOUS);
    System.out.println("tokan1");
    Redis.client().hmset(token, access, null);
    access.put("access_token", token);
    System.out.println(access);
    return access;
  }

  @Override
  public JsonObject createBasicAuthAccessToken(String clientId, String clientKey, String basicAuthCredentials) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public JsonObject deleteAccessToken(String token) {
    return null;
  }

  @Override
  public JsonObject authorize() {
    return null;
  }

  public AuthClientRepo getAuthClientRepo() {
    return authClientRepo;
  }

  public void setAuthClientRepo(AuthClientRepo authClientRepo) {
    this.authClientRepo = authClientRepo;
  }

}
