package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJCountryRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.Country;

public interface CountryRepo {

  static CountryRepo instance() {
    return new AJCountryRepo();
  }

  Country getCountry(String id);

  Country getCountryByName(String name);

  Country createCountry(Country country);

}
