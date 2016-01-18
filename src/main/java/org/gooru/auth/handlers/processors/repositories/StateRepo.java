package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJStateRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;

public interface StateRepo {

  static StateRepo instance() {
    return new AJStateRepo();
  }

  AJEntityState createState(AJEntityState state);

  AJEntityState createState(String name, String creatorId);
  
  AJEntityState getStateById(Long id);

  AJEntityState getStateByName(String name);
}
