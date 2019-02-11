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

import org.springframework.util.Assert;

class ContextService {

  private static final ThreadLocal<MessageContext> CONTEXT = new ThreadLocal<>();

  MessageContext get() {
    return CONTEXT.get();
  }

  void set(MessageContext messageContext) {
    Assert.isNull(CONTEXT.get(),
        "The messageContext was not null before setting it! Clear should have been used!");
    CONTEXT.set(messageContext);
  }

  void clear() {
    CONTEXT.set(null);
  }
}
