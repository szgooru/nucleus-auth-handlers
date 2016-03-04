package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.AJCountryRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;

public interface CountryRepo {

  static CountryRepo instance() {
    return new AJCountryRepo();
  }

  AJEntityCountry getCountry(String id);

  AJEntityCountry getCountryByName(String name);

  AJEntityCountry createCountry(AJEntityCountry country);

  AJEntityCountry createCountry(String name, String creatorId);
}
