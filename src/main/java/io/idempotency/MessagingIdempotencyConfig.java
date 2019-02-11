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

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
public class MessagingIdempotencyConfig {

  @Value("${spring.redis.host:localhost}")
  private String host;
  @Value("${spring.redis.port:6379}")
  private int port;

  @ConditionalOnMissingBean(RedisConnectionFactory.class)
  @Bean
  public RedisConnectionFactory redisConnectionFactory() {
    return new LettuceConnectionFactory(host, port);
  }

  @Bean
  public RedisTemplate<String, Message> redisTemplate(RedisConnectionFactory factory) {
    Jackson2JsonRedisSerializer<Message> serializer =
        new Jackson2JsonRedisSerializer<>(Message.class);
    RedisTemplate<String, Message> redisTemplate = new RedisTemplate<>();
    redisTemplate.setConnectionFactory(factory);
    redisTemplate.setDefaultSerializer(serializer);
    redisTemplate.setKeySerializer(new StringRedisSerializer());
    redisTemplate.setHashKeySerializer(serializer);
    redisTemplate.setValueSerializer(serializer);
    redisTemplate.setEnableTransactionSupport(true);
    return redisTemplate;
  }

  @Bean
  public ContextService idempotentMessageContextMessageService() {
    return new ContextService();
  }

  @Bean
  public MessageInterceptor idempotentMessageInterceptor(ContextService contextService) {
    return new MessageInterceptor(contextService);
  }

  @Bean
  public IdempotencyBarrier idempotentMessageAspect(ContextService contextService,
                                                    MessageService messageService) {
    return new IdempotencyBarrier(contextService, messageService);
  }

  @Bean
  public MessageService idempotentMessageService(RedisTemplate<String, Message> redisTemplate) {
    return new MessageServiceImpl(redisTemplate);
  }

  @Bean
  public RabbitMqBeanPostProcessor rabbitMqBeanPostProcessor(MessageInterceptor interceptor){
    return new RabbitMqBeanPostProcessor(interceptor);
  }
}
