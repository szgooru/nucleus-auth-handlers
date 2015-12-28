package org.gooru.auth.handlers.infra.jdbc;

import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.jdbc.JDBCClient;

import javax.sql.DataSource;

public final class JDBC {

  public static JDBCClient client;

  public void create(Vertx vertx, DataSource dataSource) {
    if (client == null) {
     // client = JDBCClient.createShared(vertx, dataSource);
    }
  }
  
  public static JDBCClient client() {
    return client;
  }

}
