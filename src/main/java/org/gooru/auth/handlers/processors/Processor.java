package org.gooru.auth.handlers.processors;

import org.gooru.auth.handlers.processors.service.MessageResponse;

public interface Processor {
  public MessageResponse process();
}
