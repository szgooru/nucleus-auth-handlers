package org.gooru.auth.handlers.processors.command.executor;

import io.vertx.core.MultiMap;
import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.constants.CommandConstants;
import org.gooru.auth.handlers.constants.ParameterConstants;
import org.gooru.auth.handlers.processors.exceptions.InvalidRequestException;
import org.gooru.auth.handlers.processors.service.country.CountryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CountryCommandExecutor implements CommandExecutor {

  private static final Logger LOG = LoggerFactory.getLogger(CountryCommandExecutor.class);

  private CountryService countryService;

  public CountryCommandExecutor() {
    setCountryService(CountryService.instance());
  }

  @Override
  public JsonObject exec(String command, JsonObject userContext, MultiMap headers, JsonObject params, JsonObject body) {
    JsonObject result = null;
    switch (command) {
    case CommandConstants.LIST_COUNTRY:
      result = getCountryService().getCountries(params.getString(ParameterConstants.PARAM_QUERY));
      break;
    default:
      LOG.error("Invalid command type passed in, not able to handle");
      throw new InvalidRequestException();
    }
    return result;
  }

  public CountryService getCountryService() {
    return countryService;
  }

  public void setCountryService(CountryService countryService) {
    this.countryService = countryService;
  }

}
