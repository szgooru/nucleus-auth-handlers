package org.gooru.auth.handlers.authentication.impl;

import io.vertx.core.json.JsonObject;
import io.vertx.rx.java.RxHelper;

import org.gooru.auth.handlers.authentication.model.AuthClient;

import rx.Observable;

public class AuthenticationServiceImpl implements AuthenticationService {

  @Override
  public Observable<JsonObject> createAccessToken(AuthClient authClient) {
    Observable<JsonObject> observable = RxHelper.observableFuture();

    return observable;
  }

  @Override
  public JsonObject deleteAccessToken(String token) {
    return null;
  }

  @Override
  public JsonObject authorize() {
    return null;
  }
}
