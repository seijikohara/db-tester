package io.github.seijikohara.dbtester.spring.support;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/** Unit tests for {@link PrimaryBeanResolver}. */
@DisplayName("PrimaryBeanResolver")
class PrimaryBeanResolverTest {

  /** Tests for PrimaryBeanResolver. */
  PrimaryBeanResolverTest() {}

  /** Mock logger for tests. */
  private Logger logger;

  /** Sets up test fixtures. */
  @BeforeEach
  void setUp() {
    logger = mock(Logger.class);
  }

  /** Tests for the isPrimaryBean method. */
  @Nested
  @DisplayName("isPrimaryBean method")
  class IsPrimaryBeanMethod {

    /** Tests for isPrimaryBean. */
    IsPrimaryBeanMethod() {}

    /** Verifies true when bean is primary. */
    @Test
    @Tag("normal")
    @DisplayName("should return true when bean is primary")
    void shouldReturnTrueWhenBeanIsPrimary() {
      // Given
      final var context = mock(ConfigurableApplicationContext.class);
      final var beanFactory = mock(ConfigurableListableBeanFactory.class);
      final var beanDefinition = mock(BeanDefinition.class);

      when(context.getBeanFactory()).thenReturn(beanFactory);
      when(beanFactory.containsBeanDefinition("myBean")).thenReturn(true);
      when(beanFactory.getBeanDefinition("myBean")).thenReturn(beanDefinition);
      when(beanDefinition.isPrimary()).thenReturn(true);

      // When
      final var result = PrimaryBeanResolver.isPrimaryBean(context, "myBean", logger);

      // Then
      assertTrue(result, "should return true when bean is marked as @Primary");
    }

    /** Verifies false when bean is not primary. */
    @Test
    @Tag("normal")
    @DisplayName("should return false when bean is not primary")
    void shouldReturnFalseWhenBeanIsNotPrimary() {
      // Given
      final var context = mock(ConfigurableApplicationContext.class);
      final var beanFactory = mock(ConfigurableListableBeanFactory.class);
      final var beanDefinition = mock(BeanDefinition.class);

      when(context.getBeanFactory()).thenReturn(beanFactory);
      when(beanFactory.containsBeanDefinition("myBean")).thenReturn(true);
      when(beanFactory.getBeanDefinition("myBean")).thenReturn(beanDefinition);
      when(beanDefinition.isPrimary()).thenReturn(false);

      // When
      final var result = PrimaryBeanResolver.isPrimaryBean(context, "myBean", logger);

      // Then
      assertFalse(result, "should return false when bean is not marked as @Primary");
    }

    /** Verifies false when context is not configurable. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false when context is not ConfigurableApplicationContext")
    void shouldReturnFalseWhenContextIsNotConfigurable() {
      // Given
      final var context = mock(ApplicationContext.class);

      // When
      final var result = PrimaryBeanResolver.isPrimaryBean(context, "myBean", logger);

      // Then
      assertFalse(result, "should return false when context is not ConfigurableApplicationContext");
    }

    /** Verifies false when bean definition not found. */
    @Test
    @Tag("edge-case")
    @DisplayName("should return false when bean definition not found")
    void shouldReturnFalseWhenBeanDefinitionNotFound() {
      // Given
      final var context = mock(ConfigurableApplicationContext.class);
      final var beanFactory = mock(ConfigurableListableBeanFactory.class);

      when(context.getBeanFactory()).thenReturn(beanFactory);
      when(beanFactory.containsBeanDefinition("myBean")).thenReturn(false);

      // When
      final var result = PrimaryBeanResolver.isPrimaryBean(context, "myBean", logger);

      // Then
      assertFalse(result, "should return false when bean definition is not found");
    }
  }
}
