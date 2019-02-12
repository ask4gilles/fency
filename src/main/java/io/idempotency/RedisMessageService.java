/*
 * Copyright 2016-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.idempotency;

import java.util.Optional;
import java.util.StringJoiner;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@Transactional
@RequiredArgsConstructor
class RedisMessageService implements MessageService {

  private final RedisTemplate<String, Message> redisTemplate;

  @SuppressWarnings("unchecked")
  @Override
  public void save(Message message) {

    redisTemplate.execute(new SessionCallback<Object>() {
      @Override
      public Object execute(RedisOperations operations) throws DataAccessException {
        operations.multi(); // start of a transaction
        String key = getKey(message.getId(), message.getConsumerQueueName());
        operations.opsForValue().set(key, message);

        operations.exec();
        return null;
      }
    });
  }

  @Override
  public Optional<Message> find(MessageContext context) {
    String key = getKey(context.getMessageId(), context.getConsumerQueueName());
    return Optional.ofNullable(redisTemplate.opsForValue().get(key));
  }

  private String getKey(String messageId, String consumerQueueName) {
    StringJoiner stringJoiner = new StringJoiner(":");
    stringJoiner.add(messageId);
    stringJoiner.add(consumerQueueName);
    return stringJoiner.toString();
  }
}
