package io.github.seijikohara.dbtester.api.annotation;

import io.github.seijikohara.dbtester.api.operation.Operation;
import io.github.seijikohara.dbtester.api.operation.TableOrderingStrategy;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the datasets that must be applied before a test method runs.
 *
 * <p>{@code @DataSet} may be placed on an individual test method or at the test class level. A
 * method-level declaration augments (and, when necessary, overrides) any class-level definition.
 * The annotation is inherited by subclasses to avoid restating common fixtures.
 *
 * <p>Each associated {@link DataSetSource} is executed using the configured {@link #operation()}
 * (default {@link Operation#CLEAN_INSERT}) so that tests start from a deterministic database state.
 * When the {@link #dataSets()} array is empty, the extension locates datasets via the convention
 * settings and applies the test method name as the scenario filter.
 *
 * @see DataSetSource
 * @see ExpectedDataSet
 */
@Inherited
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DataSet {

  /**
   * Lists the datasets that must be executed before the test.
   *
   * <p>Datasets are executed in declaration order. Leaving the array empty instructs the extension
   * to rely on convention-based discovery.
   *
   * @return ordered collection of datasets; empty when convention-based discovery should be used
   */
  DataSetSource[] dataSets() default {};

  /**
   * Provides the database operation that is applied to every dataset in {@link #dataSets()}.
   *
   * @return the preparation operation, defaulting to {@link Operation#CLEAN_INSERT}
   */
  Operation operation() default Operation.CLEAN_INSERT;

  /**
   * Specifies the strategy for determining the table processing order.
   *
   * <p>This affects the order in which tables are inserted, updated, or deleted to ensure foreign
   * key constraints are satisfied.
   *
   * @return the table ordering strategy, defaulting to {@link TableOrderingStrategy#AUTO}
   * @see TableOrderingStrategy
   */
  TableOrderingStrategy tableOrdering() default TableOrderingStrategy.AUTO;
}
