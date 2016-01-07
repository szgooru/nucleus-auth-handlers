package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.List;

import org.gooru.auth.handlers.processors.repositories.StateRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.State;
import org.javalite.activejdbc.Base;

public class AJStateRepo extends AJAbstractRepo implements StateRepo {

  private static final String LIST_STATES = "name like  ?";

  private static final String LIST_COUNTRY_STATES = "country_id = ? and name like  ?";

  @Override
  public State createState(State state) {
    Base.open(dataSource());
    state.saveIt();
    Base.commitTransaction();
    Base.close();
    return state;
  }

  @Override
  public List<State> getStates(String name, long offset, long limit) {
    return queryList(LIST_STATES, offset, limit, beginsWithPattern(name));
  }

  @Override
  public List<State> getStates(String countryId, String name, long offset, long limit) {
    return queryList(LIST_COUNTRY_STATES, offset, limit, countryId, beginsWithPattern(name));
  }

  private List<State> queryList(String whereClause, long offset, long limit, Object... params) {
    Base.open(dataSource());
    List<State> results = State.where(whereClause, params).offset(offset).limit(limit);
    Base.close();
    return results;
  }

}
