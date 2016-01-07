package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.state.StateService;
import org.gooru.auth.handlers.processors.service.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class StateCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(StateCommandExecutor.class);

  private StateService stateService;

  public StateCommandExecutor() {
    setStateService(StateService.instance());
  }

  @Override
  public JsonObject exec(String command, JsonObject userContext, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.CREATE_USER:
      
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

  public StateService getStateService() {
    return stateService;
  }

  public void setStateService(StateService stateService) {
    this.stateService = stateService;
  }

  
}
