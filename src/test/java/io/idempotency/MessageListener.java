package io.idempotency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@IdempotentConsumer
public class MessageListener {

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageListener.class);

  public void handleMessage(Object message) {
    LOGGER.info("Received : {}", message);
    if ("exception".equals(message)) {
      throw new RuntimeException();
    }
  }
}
