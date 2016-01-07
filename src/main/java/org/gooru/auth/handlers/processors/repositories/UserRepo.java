package org.gooru.auth.handlers.processors.repositories;

import org.gooru.auth.handlers.processors.repositories.activejdbc.AJUserRepo;
import org.gooru.auth.handlers.processors.repositories.activejdbc.entities.User;

public interface UserRepo {

  static UserRepo instance() {
    return new AJUserRepo();
  }

  User getUser(String userId);

  User create(User user);

  User update(User user);
}
