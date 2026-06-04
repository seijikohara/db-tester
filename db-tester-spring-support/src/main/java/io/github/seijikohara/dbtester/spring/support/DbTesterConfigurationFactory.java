package io.github.seijikohara.dbtester.spring.support;

import io.github.seijikohara.dbtester.api.config.ColumnStrategyMapping;
import io.github.seijikohara.dbtester.api.config.Configuration;
import io.github.seijikohara.dbtester.api.config.ConventionSettings;
import io.github.seijikohara.dbtester.api.config.ExecutionSettings;
import io.github.seijikohara.dbtester.api.config.OperationDefaults;
import io.github.seijikohara.dbtester.api.config.VerificationSettings;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builds a {@link Configuration} from {@link DbTesterProperties}.
 *
 * <p>This factory maps Spring Boot configuration properties to the framework {@link Configuration}.
 * It is shared by the JUnit, Spock, and Kotest Spring Boot starters to avoid duplicating the
 * mapping logic.
 *
 * @see Configuration
 * @see DbTesterProperties
 */
public final class DbTesterConfigurationFactory {

  /** Prevents instantiation of this utility class. */
  private DbTesterConfigurationFactory() {
    // Utility class - prevent instantiation
  }

  /**
   * Builds a Configuration from the specified properties.
   *
   * @param properties the DB Tester properties
   * @return the configuration built from properties
   */
  public static Configuration toConfiguration(final DbTesterProperties properties) {
    final DbTesterProperties.ConventionProperties conventionProps = properties.getConvention();
    final DbTesterProperties.VerificationProperties verificationProps =
        properties.getVerification();
    final DbTesterProperties.ExecutionProperties executionProps = properties.getExecution();
    final DbTesterProperties.OperationProperties operationProps = properties.getOperation();

    final ConventionSettings conventions =
        ConventionSettings.builder()
            .baseDirectory(conventionProps.getBaseDirectory())
            .expectationSuffix(conventionProps.getExpectationSuffix())
            .scenarioMarker(conventionProps.getScenarioMarker())
            .dataFormat(conventionProps.getDataFormat())
            .tableMergeStrategy(conventionProps.getTableMergeStrategy())
            .loadOrderFileName(conventionProps.getLoadOrderFileName())
            .build();

    final Map<String, ColumnStrategyMapping> columnStrategies =
        verificationProps.getColumnStrategies().stream()
            .filter(
                prop ->
                    prop.getColumnName() != null
                        && !prop.getColumnName().isBlank()
                        && prop.getStrategy() != null)
            .map(
                prop -> {
                  final var mapping =
                      ColumnStrategyConverter.toColumnStrategyMapping(
                          prop.getColumnName(), prop.getStrategy(), prop.getPattern());
                  return ColumnStrategyConverter.toMapEntry(prop.getColumnName(), mapping);
                })
            .collect(
                Collectors.toMap(
                    Map.Entry::getKey, Map.Entry::getValue, (first, second) -> second));

    final VerificationSettings verification =
        VerificationSettings.builder()
            .globalExcludeColumns(verificationProps.getGlobalExcludeColumns())
            .globalColumnStrategies(columnStrategies)
            .rowOrdering(verificationProps.getRowOrdering())
            .retryCount(verificationProps.getRetryCount())
            .retryDelay(verificationProps.getRetryDelay())
            .build();

    final ExecutionSettings execution =
        ExecutionSettings.builder()
            .queryTimeout(executionProps.getQueryTimeout())
            .transactionMode(executionProps.getTransactionMode())
            .build();

    final OperationDefaults operations =
        OperationDefaults.builder()
            .preparation(operationProps.getPreparation())
            .expectation(operationProps.getExpectation())
            .build();

    return Configuration.builder()
        .conventions(conventions)
        .verification(verification)
        .execution(execution)
        .operations(operations)
        .build();
  }
}
