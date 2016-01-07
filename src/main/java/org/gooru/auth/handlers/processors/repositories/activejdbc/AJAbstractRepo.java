package org.gooru.auth.handlers.processors.repositories.activejdbc;

import javax.sql.DataSource;

import org.gooru.auth.handlers.infra.DataSourceRegistry;

public abstract class AJAbstractRepo {

  private static final String PRECENTAGE = "%";

  protected String beginsWithPattern(String name) {
    StringBuilder pattern = new StringBuilder();
    pattern.append(name);
    pattern.append(PRECENTAGE);
    return pattern.toString();
  }

  protected DataSource dataSource() {
    return DataSourceRegistry.getInstance().getDefaultDataSource();
  }

}
