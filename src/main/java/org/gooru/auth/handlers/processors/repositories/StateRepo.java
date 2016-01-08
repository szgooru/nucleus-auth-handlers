package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJStateRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.State;

public interface StateRepo {

  static StateRepo instance() {
    return new AJStateRepo();
  }

  State createState(State state);

  State getStateById(String countryId, String id);

  State getStateByName(String countryId, String name);
}
