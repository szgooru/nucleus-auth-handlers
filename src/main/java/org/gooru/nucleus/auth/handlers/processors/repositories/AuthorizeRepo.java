package org.gooru.nucleus.auth.handlers.processors.repositories;

import org.gooru.nucleus.auth.handlers.processors.command.executor.MessageResponse;

public interface AuthorizeRepo {
  MessageResponse authorize();
}
