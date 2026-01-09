package io.github.seijikohara.dbtester.api.annotation;

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
}
