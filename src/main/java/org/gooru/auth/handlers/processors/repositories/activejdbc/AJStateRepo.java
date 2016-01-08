package org.gooru.auth.handlers.processors.repositories.activejdbc;

import org.gooru.auth.handlers.processors.repositories.StateRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.State;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJStateRepo extends AJAbstractRepo implements StateRepo {

  private static final String GET_STATE_BY_NAME = "name = ?";

  private static final String GET_STATE_BY_ID = "country_id = ? and id = ?";

  @Override
  public State createState(State state) {
    Base.open(dataSource());
    state.saveIt();
    Base.commitTransaction();
    Base.close();
    return state;
  }

  @Override
  public State getStateById(String countryId, String id) {
    return query(GET_STATE_BY_ID, id);
  }

  @Override
  public State getStateByName(String countryId, String name) {
    return query(GET_STATE_BY_NAME, countryId, name);
  }

  private State query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<State> results = State.where(whereClause, params);
    State state = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return state;
  }
}
