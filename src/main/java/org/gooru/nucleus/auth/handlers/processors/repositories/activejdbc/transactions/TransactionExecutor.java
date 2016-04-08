package org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.transactions;

import java.sql.SQLException;

import org.gooru.nucleus.auth.handlers.infra.DataSourceRegistry;
import org.gooru.nucleus.auth.handlers.processors.command.executor.DBExecutor;
import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;
import org.gooru.nucleus.auth.handlers.utils.ServerValidatorUtility;
import org.javalite.activejdbc.Base;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransactionExecutor {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransactionExecutor.class);

  public static MessageResponse executeTransaction(DBExecutor handler)  {
    // First validations without any DB
    handler.checkSanity();
    // Now we need to run with transaction, if we are going to continue
    return executeWithTransaction(handler);
  }

  private static MessageResponse executeWithTransaction(DBExecutor handler)  {
    try {
      Base.open(DataSourceRegistry.getInstance().getDefaultDataSource());
      // If we need a read only transaction, then it is time to set up now
      if (handler.handlerReadOnly()) {
        Base.connection().setReadOnly(true);
      }
      Base.openTransaction();
      handler.validateRequest();
      MessageResponse executionResult = handler.executeRequest();
      Base.commitTransaction();
      return executionResult;
    } catch (Throwable e) {
      Base.rollbackTransaction();
      LOGGER.error("Caught exeption, need to rollback and abort", e);
      // Most probably we do not know what to do with this, so send internal
      // error
      ServerValidatorUtility.reject(e);
    } finally {
      if (handler.handlerReadOnly()) {
        // restore the settings
        try {
          Base.connection().setReadOnly(false);
        } catch (SQLException e) {
          LOGGER.error("Exception while marking connetion to be read/write", e);
        }
      }
      Base.close();
    }
    return null;
  }

  private TransactionExecutor() {
    throw new AssertionError();
  }
}
