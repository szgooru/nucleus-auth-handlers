package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.school.SchoolDistrictService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class SchoolDistrictCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(SchoolDistrictCommandExecutor.class);

  private SchoolDistrictService schoolDistrictService;

  public SchoolDistrictCommandExecutor() {
    setSchoolDistrictService(SchoolDistrictService.instance());
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

  public SchoolDistrictService getSchoolDistrictService() {
    return schoolDistrictService;
  }

  public void setSchoolDistrictService(SchoolDistrictService schoolDistrictService) {
    this.schoolDistrictService = schoolDistrictService;
  }

  
}
