package org.gooru.auth.handlers.processors.service.authorize;

import org.gooru.auth.handlers.processors.data.transform.model.AuthorizeDTO;
import org.gooru.auth.handlers.processors.service.MessageResponse;

public interface AuthorizeService {
  static AuthorizeService instance() {
    return new AuthorizeServiceImpl();
  }

  MessageResponse authorize(AuthorizeDTO authorizeDTO,  String requestDomain);
}
