package org.gooru.auth.handlers.authentication.processors.repositories.activejdbc;

import javax.sql.DataSource;

import org.gooru.auth.handlers.authentication.infra.DataSourceRegistry;

public abstract class AJAbstractRepo {

  protected DataSource dataSource() {
    return DataSourceRegistry.getInstance().getDefaultDataSource();
  }

}
