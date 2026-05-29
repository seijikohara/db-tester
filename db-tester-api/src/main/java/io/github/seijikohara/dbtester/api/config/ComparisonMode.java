package io.github.seijikohara.dbtester.api.config;

/**
 * Selects how the comparison engine reacts to parse failures inside flexible strategies.
 *
 * <p>The flexible {@link io.github.seijikohara.dbtester.api.domain.ComparisonStrategy} variants
 * ({@code NUMERIC}, {@code TIMESTAMP_FLEXIBLE}, {@code DATE_FLEXIBLE}, {@code JSON_EQUIVALENT})
 * historically fell back to {@link java.util.Objects#equals(Object, Object)} or to comparing the
 * raw string when the supplied value could not be parsed into the expected representation. Silent
 * fallback hides preparation errors and may let mismatched data pass verification.
 *
 * <p>Choose {@link #STRICT} to make parse failures throw {@link
 * io.github.seijikohara.dbtester.api.exception.ValidationException} so the test fails fast with a
 * descriptive message. Choose {@link #LENIENT} to retain the historical fallback behaviour while
 * still emitting a warning via SLF4J. {@code STRICT} is the default for new deployments.
 *
 * @see OperationDefaults#comparisonMode()
 */
public enum ComparisonMode {

  /** Throws an exception whenever a flexible strategy fails to parse one of its inputs. */
  STRICT,

  /** Logs a warning and falls back to the historical comparison when parsing fails. */
  LENIENT
}
