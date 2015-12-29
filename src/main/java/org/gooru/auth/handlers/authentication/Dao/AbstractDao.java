package org.gooru.auth.handlers.authentication.Dao;

import org.gooru.auth.handlers.infra.JDBC;

import io.vertx.ext.sql.SQLConnection;
import rx.Observable;

public abstract class AbstractDao {

  private JDBC jdbc;

  private Observable<SQLConnection> query;

  public AbstractDao() {
    jdbc = new JDBC();
    setQuery(jdbc.getConnection());
  }

  public Observable<SQLConnection> getQuery() {
    return query;
  }

  public void setQuery(Observable<SQLConnection> query) {
    this.query = query;
  }

}
