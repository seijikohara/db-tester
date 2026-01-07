package io.github.seijikohara.dbtester.spring.support;

import org.slf4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Utility class for resolving primary bean status in Spring contexts.
 *
 * <p>This class provides methods to determine if a bean is marked with {@code @Primary} annotation
 * in a Spring ApplicationContext. It handles the complexity of working with different context types
 * and bean factory implementations.
 *
 * <p>This class is stateless and thread-safe.
 */
public final class PrimaryBeanResolver {

  /** Prevents instantiation of this utility class. */
  private PrimaryBeanResolver() {
    // Utility class - prevent instantiation
  }

  /**
   * Checks if a bean is marked as primary.
   *
   * <p>This method works with {@link ConfigurableApplicationContext} instances to access the
   * underlying bean definitions. If the context is not configurable or the bean definition cannot
   * be found, it returns false and logs a debug message.
   *
   * @param context the Spring application context
   * @param beanName the bean name to check
   * @param logger the logger for debug messages
   * @return true if the bean is marked as primary, false otherwise
   */
  public static boolean isPrimaryBean(
      final ApplicationContext context, final String beanName, final Logger logger) {

    if (!(context instanceof ConfigurableApplicationContext configurableContext)) {
      logger.debug(
          "ApplicationContext is not ConfigurableApplicationContext, "
              + "cannot determine if bean '{}' is primary",
          beanName);
      return false;
    }

    final var beanFactory = configurableContext.getBeanFactory();

    if (!beanFactory.containsBeanDefinition(beanName)) {
      logger.debug("Bean definition not found for '{}', cannot determine primary status", beanName);
      return false;
    }

    return beanFactory.getBeanDefinition(beanName).isPrimary();
  }
}
