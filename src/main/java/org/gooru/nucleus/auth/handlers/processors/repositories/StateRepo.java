package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.AJStateRepo;
import org.gooru.nucleus.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;

public interface StateRepo {

  static StateRepo instance() {
    return new AJStateRepo();
  }

  AJEntityState createState(AJEntityState state);

  AJEntityState createState(String name, String countryId, String creatorId);
  
  AJEntityState getStateById(String id);

  AJEntityState getStateByName(String name);
}
