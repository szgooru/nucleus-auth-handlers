package org.gooru.auth.handlers.processors.service.country;

import io.vertx.core.json.JsonObject;

import org.gooru.auth.handlers.processors.repositories.CountryRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.Country;

public class CountryServiceImpl implements CountryService {

  private static final String COUNTRIES = "countries";
  private CountryRepo countryRepo;

  public CountryServiceImpl() {
    setCountryRepo(CountryRepo.instance());
  }

  @Override
  public Country createCountry(String name, String code) {
    Country country = new Country();
    country.setName(name);
    country.setCode(code);
    return getCountryRepo().createCountry(country);
  }

  @Override
  public JsonObject getCountries(String query) {
    return new JsonObject().put(COUNTRIES, getCountryRepo().getCountries(query, 0, 20));
  }

  public CountryRepo getCountryRepo() {
    return countryRepo;
  }

  public void setCountryRepo(CountryRepo countryRepo) {
    this.countryRepo = countryRepo;
  }

}
