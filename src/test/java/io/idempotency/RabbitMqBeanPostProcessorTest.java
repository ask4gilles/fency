package io.idempotency;

import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.test.util.ReflectionTestUtils;

import org.aopalliance.aop.Advice;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;

class RabbitMqBeanPostProcessorTest {

  @InjectMocks
  private RabbitMqBeanPostProcessor rabbitMqBeanPostProcessor;
  @Mock
  private MessageInterceptor messageInterceptor;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  void testPostProcessAfterInitialization() {
    // given
    Object object = new Object();

    // when
    Object result = rabbitMqBeanPostProcessor.postProcessAfterInitialization(object, "object");

    // then
    assertThat(result).isNotNull();
    assertThat(result).isSameAs(object);
  }

  @Test
  void testSimpleRabbitListenerContainerFactoryInstrumentation() {
    // given
    SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();

    // when
    Object result = rabbitMqBeanPostProcessor.postProcessBeforeInitialization(factory, "factory");

    // then
    assertThat(result).isNotNull();
    assertThat(result).isInstanceOf(SimpleRabbitListenerContainerFactory.class);
    Advice[] adviceChain = factory.getAdviceChain();
    assertThat(adviceChain).isNotNull();
    assertThat(adviceChain).isNotEmpty();
    assertThat(adviceChain).hasSize(1);
    assertThat(adviceChain[0]).isEqualTo(messageInterceptor);
  }

  @Test
  void testSimpleMessageListenerContainerInstrumentation() {
    // given
    SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();

    // when
    Object result = rabbitMqBeanPostProcessor.postProcessBeforeInitialization(container, "container");

    // then
    assertThat(result).isNotNull();
    assertThat(result).isInstanceOf(SimpleMessageListenerContainer.class);
    Advice[] adviceChain = (Advice[]) ReflectionTestUtils.getField(result, "adviceChain");
    assertThat(adviceChain).isNotNull();
    assertThat(adviceChain).isNotEmpty();
    assertThat(adviceChain).hasSize(1);
    assertThat(adviceChain[0]).isEqualTo(messageInterceptor);
  }
}