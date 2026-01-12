package io.github.seijikohara.dbtester.api.annotation;

import io.github.seijikohara.dbtester.api.config.RowOrdering;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the datasets that define the expected database state after a test executes.
 *
 * <p>{@code @ExpectedDataSet} supports the same placement semantics as {@link DataSet}: it can be
 * declared on individual methods or on the enclosing test class, and method-level declarations take
 * precedence. The annotation is inherited by subclasses unless overridden.
 *
 * <p>Each dataset is verified against the live database using the extension's assertion engine.
 * Validation is read-only; no rows are modified as part of the comparison. If the {@link
 * #sources()} array is empty the loader resolves datasets via the standard directory conventions.
 *
 * @see DataSetSource
 * @see DataSet
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpectedDataSet {

  /**
   * Lists the dataset sources that should be considered the canonical post-test state.
   *
   * <p>Sources are validated in declaration order. An empty array signals that the framework should
   * deduce their location from the convention settings.
   *
   * @return ordered collection of dataset sources for verification
   */
  DataSetSource[] sources() default {};

  /**
   * Specifies the strategy for determining the table processing order during verification.
   *
   * <p>This affects the order in which tables are compared to ensure consistent validation
   * behavior.
   *
   * @return the table ordering strategy, defaulting to {@link TableOrderingStrategy#AUTO}
   * @see TableOrderingStrategy
   */
  TableOrderingStrategy tableOrdering() default TableOrderingStrategy.AUTO;

  /**
   * Specifies how rows should be compared during verification.
   *
   * <p>When set to {@link RowOrdering#UNORDERED}, rows are compared without considering their
   * position in the result set. This is useful when the database does not guarantee row ordering.
   *
   * <p>If not specified, the value from {@link
   * io.github.seijikohara.dbtester.api.config.ConventionSettings#rowOrdering()} is used.
   *
   * @return the row ordering strategy, defaulting to {@link RowOrdering#ORDERED}
   * @see RowOrdering
   */
  RowOrdering rowOrdering() default RowOrdering.ORDERED;

  /**
   * Specifies the number of retry attempts for verification.
   *
   * <p>When verification fails, the framework retries the comparison up to this many additional
   * times, waiting {@link #retryDelayMillis()} between attempts. This is useful for eventual
   * consistency scenarios.
   *
   * <p>A value of {@code -1} means the global setting from {@link
   * io.github.seijikohara.dbtester.api.config.ConventionSettings#retryCount()} is used.
   *
   * @return the number of retry attempts, or -1 to use the global setting
   */
  int retryCount() default -1;

  /**
   * Specifies the delay between retry attempts in milliseconds.
   *
   * <p>This delay allows transient inconsistencies to resolve before the next verification attempt.
   *
   * <p>A value of {@code -1} means the global setting from {@link
   * io.github.seijikohara.dbtester.api.config.ConventionSettings#retryDelay()} is used.
   *
   * @return the delay in milliseconds, or -1 to use the global setting
   */
  long retryDelayMillis() default -1;
}
