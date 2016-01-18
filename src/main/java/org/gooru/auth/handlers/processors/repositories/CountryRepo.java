package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJCountryRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityCountry;

public interface CountryRepo {

  static CountryRepo instance() {
    return new AJCountryRepo();
  }

  AJEntityCountry getCountry(Long id);

  AJEntityCountry getCountryByName(String name);

  AJEntityCountry createCountry(AJEntityCountry country);

  AJEntityCountry createCountry(String name, String creatorId);
}
