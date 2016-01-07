package org.gooru.auth.handlers.processors.repositories;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJStateRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.State;

public interface StateRepo {

  static StateRepo instance() {
    return new AJStateRepo();
  }
  
  State createState(State state);
  
  List<State> getStates(String name,  long offset, long limit);
  
  List<State> getStates(String countryId, String name,  long offset, long limit);
  
}
