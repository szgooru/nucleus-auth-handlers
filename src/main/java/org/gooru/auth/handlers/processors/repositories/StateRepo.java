package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJStateRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.State;

public interface StateRepo {

  static StateRepo instance() {
    return new AJStateRepo();
  }

  State createState(State state);

  State createState(String name);
  
  State getStateById(Long id);

  State getStateByName(String name);
}
