package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJUserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.AJEntityUser;

public interface UserRepo {

  static UserRepo instance() {
    return new AJUserRepo();
  }

  AJEntityUser getUser(String userId);

  AJEntityUser create(AJEntityUser user);

  AJEntityUser update(AJEntityUser user);
}
