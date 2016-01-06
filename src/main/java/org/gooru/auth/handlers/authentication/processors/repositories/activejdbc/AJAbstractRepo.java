package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc;

import javax.sql.DataSource;

import org.gooru.auth.handlers.authentication.infra.DataSourceRegistry;
import org.javalite.activejdbc.Model;

public abstract class AJAbstractRepo {

  abstract protected <T extends Model> T query(String whereClause, Object... params);
  
  protected DataSource dataSource() {
    return DataSourceRegistry.getInstance().getDefaultDataSource();
  }

}
