package org.gooru.auth.handlers.infra;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;

import org.gooru.auth.handlers.bootstrap.startup.Initializer;

import rx.Observable;

public final class JDBC implements Initializer {

  public static JDBCClient client;

  @Override
  public void initializeComponent(Vertx vertx, JsonObject config) {
    if (client == null) {
      client = JDBCClient.create(vertx, DataSourceRegistry.getInstance().getDefaultDataSource());
    }
  }

  public Observable<SQLConnection> getConnection() {
    io.vertx.rx.java.ObservableFuture<SQLConnection> handler = io.vertx.rx.java.RxHelper.observableFuture();
    client().getConnection(handler.toHandler());
    return handler;
  }

  public static JDBCClient client() {
    return client;
  }

}
