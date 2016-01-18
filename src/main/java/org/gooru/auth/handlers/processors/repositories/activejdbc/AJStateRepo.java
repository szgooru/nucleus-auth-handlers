package org.gooru.auth.handlers.processors.repositories.activejdbc;

import java.util.UUID;

import org.gooru.auth.handlers.processors.repositories.StateRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityState;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;

public class AJStateRepo extends AJAbstractRepo implements StateRepo {

  private static final String GET_STATE_BY_NAME = "name = ?";

  private static final String GET_STATE_BY_ID = "id = ?";

  @Override
  public AJEntityState createState(AJEntityState state) {
    Base.open(dataSource());
    state.saveIt();
    Base.commitTransaction();
    Base.close();
    return state;
  }

  @Override
  public AJEntityState createState(String name, String creatorId) {
    AJEntityState state = new AJEntityState();
    state.setName(name);
    state.setCode(UUID.randomUUID().toString());
    state.setCreatorId(creatorId);
    return createState(state);
  }

  @Override
  public AJEntityState getStateById(Long id) {
    return query(GET_STATE_BY_ID, id);
  }

  @Override
  public AJEntityState getStateByName(String name) {
    return query(GET_STATE_BY_NAME, name);
  }

  private AJEntityState query(String whereClause, Object... params) {
    Base.open(dataSource());
    LazyList<AJEntityState> results = AJEntityState.where(whereClause, params);
    AJEntityState state = results.size() > 0 ? results.get(0) : null;
    Base.close();
    return state;
  }
}
