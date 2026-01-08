package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.JDBCType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/** Unit tests for {@link ColumnMetadata}. */
@DisplayName("ColumnMetadata")
class ColumnMetadataTest {

  /** Tests for the ColumnMetadata class. */
  ColumnMetadataTest() {}

  /** Tests for factory methods. */
  @Nested
  @DisplayName("factory methods")
  class FactoryMethods {

    /** Tests for factory methods. */
    FactoryMethods() {}

    /** Verifies that of() creates metadata with type and nullable. */
    @Test
    @Tag("normal")
    @DisplayName("of() creates metadata with type and nullable")
    void shouldCreateMetadata_whenTypeAndNullableProvided() {
      // When
      final var metadata = ColumnMetadata.of(JDBCType.VARCHAR, true);

      // Then
      assertAll(
          "metadata should have correct default values",
          () -> assertEquals(JDBCType.VARCHAR, metadata.jdbcType(), "jdbcType should be VARCHAR"),
          () -> assertTrue(metadata.nullable(), "nullable should be true"),
          () -> assertFalse(metadata.primaryKey(), "primaryKey should be false"),
          () -> assertEquals(0, metadata.ordinalPosition(), "ordinalPosition should be 0"),
          () -> assertEquals(0, metadata.precision(), "precision should be 0"),
          () -> assertEquals(0, metadata.scale(), "scale should be 0"),
          () -> assertNull(metadata.defaultValue(), "defaultValue should be null"));
    }

    /** Verifies that primaryKey() creates primary key metadata. */
    @Test
    @Tag("normal")
    @DisplayName("primaryKey() creates primary key metadata")
    void shouldCreatePrimaryKeyMetadata_whenPrimaryKeyFactoryMethodCalled() {
      // When
      final var metadata = ColumnMetadata.primaryKey(JDBCType.BIGINT);

      // Then
      assertAll(
          "metadata should have primary key properties",
          () -> assertEquals(JDBCType.BIGINT, metadata.jdbcType(), "jdbcType should be BIGINT"),
          () -> assertFalse(metadata.nullable(), "nullable should be false for primary key"),
          () -> assertTrue(metadata.primaryKey(), "primaryKey should be true"));
    }
  }

  /** Tests for isNumeric method. */
  @Nested
  @DisplayName("isNumeric")
  class IsNumericTests {

    /** Tests for isNumeric method. */
    IsNumericTests() {}

    /**
     * Verifies that isNumeric returns true for numeric types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @Tag("normal")
    @EnumSource(
        value = JDBCType.class,
        names = {
          "TINYINT",
          "SMALLINT",
          "INTEGER",
          "BIGINT",
          "FLOAT",
          "REAL",
          "DOUBLE",
          "DECIMAL",
          "NUMERIC"
        })
    @DisplayName("returns true for numeric types")
    void shouldReturnTrue_whenNumericType(final JDBCType type) {
      // Given
      final var metadata = ColumnMetadata.of(type, true);

      // When
      final var isNumeric = metadata.isNumeric();

      // Then
      assertTrue(isNumeric, "isNumeric should return true for " + type);
    }

    /**
     * Verifies that isNumeric returns false for non-numeric types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @Tag("normal")
    @EnumSource(
        value = JDBCType.class,
        names = {"VARCHAR", "CHAR", "DATE", "TIMESTAMP", "BLOB", "BOOLEAN"})
    @DisplayName("returns false for non-numeric types")
    void shouldReturnFalse_whenNonNumericType(final JDBCType type) {
      // Given
      final var metadata = ColumnMetadata.of(type, true);

      // When
      final var isNumeric = metadata.isNumeric();

      // Then
      assertFalse(isNumeric, "isNumeric should return false for " + type);
    }

    /** Verifies that isNumeric returns false for null type. */
    @Test
    @Tag("edge-case")
    @DisplayName("returns false for null type")
    void shouldReturnFalse_whenNullType() {
      // Given
      final var metadata = new ColumnMetadata(null, true, false, 0, 0, 0, null);

      // When
      final var isNumeric = metadata.isNumeric();

      // Then
      assertFalse(isNumeric, "isNumeric should return false for null type");
    }
  }

  /** Tests for isTextual method. */
  @Nested
  @DisplayName("isTextual")
  class IsTextualTests {

    /** Tests for isTextual method. */
    IsTextualTests() {}

    /**
     * Verifies that isTextual returns true for textual types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @Tag("normal")
    @EnumSource(
        value = JDBCType.class,
        names = {
          "CHAR",
          "VARCHAR",
          "LONGVARCHAR",
          "NCHAR",
          "NVARCHAR",
          "LONGNVARCHAR",
          "CLOB",
          "NCLOB"
        })
    @DisplayName("returns true for textual types")
    void shouldReturnTrue_whenTextualType(final JDBCType type) {
      // Given
      final var metadata = ColumnMetadata.of(type, true);

      // When
      final var isTextual = metadata.isTextual();

      // Then
      assertTrue(isTextual, "isTextual should return true for " + type);
    }

    /**
     * Verifies that isTextual returns false for non-textual types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @Tag("normal")
    @EnumSource(
        value = JDBCType.class,
        names = {"INTEGER", "DATE", "BLOB", "BOOLEAN"})
    @DisplayName("returns false for non-textual types")
    void shouldReturnFalse_whenNonTextualType(final JDBCType type) {
      // Given
      final var metadata = ColumnMetadata.of(type, true);

      // When
      final var isTextual = metadata.isTextual();

      // Then
      assertFalse(isTextual, "isTextual should return false for " + type);
    }
  }

  /** Tests for isTemporal method. */
  @Nested
  @DisplayName("isTemporal")
  class IsTemporalTests {

    /** Tests for isTemporal method. */
    IsTemporalTests() {}

    /**
     * Verifies that isTemporal returns true for temporal types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @Tag("normal")
    @EnumSource(
        value = JDBCType.class,
        names = {"DATE", "TIME", "TIMESTAMP", "TIME_WITH_TIMEZONE", "TIMESTAMP_WITH_TIMEZONE"})
    @DisplayName("returns true for temporal types")
    void shouldReturnTrue_whenTemporalType(final JDBCType type) {
      // Given
      final var metadata = ColumnMetadata.of(type, true);

      // When
      final var isTemporal = metadata.isTemporal();

      // Then
      assertTrue(isTemporal, "isTemporal should return true for " + type);
    }

    /**
     * Verifies that isTemporal returns false for non-temporal types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @Tag("normal")
    @EnumSource(
        value = JDBCType.class,
        names = {"VARCHAR", "INTEGER", "BOOLEAN"})
    @DisplayName("returns false for non-temporal types")
    void shouldReturnFalse_whenNonTemporalType(final JDBCType type) {
      // Given
      final var metadata = ColumnMetadata.of(type, true);

      // When
      final var isTemporal = metadata.isTemporal();

      // Then
      assertFalse(isTemporal, "isTemporal should return false for " + type);
    }
  }

  /** Tests for isBinary method. */
  @Nested
  @DisplayName("isBinary")
  class IsBinaryTests {

    /** Tests for isBinary method. */
    IsBinaryTests() {}

    /**
     * Verifies that isBinary returns true for binary types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @Tag("normal")
    @EnumSource(
        value = JDBCType.class,
        names = {"BINARY", "VARBINARY", "LONGVARBINARY", "BLOB"})
    @DisplayName("returns true for binary types")
    void shouldReturnTrue_whenBinaryType(final JDBCType type) {
      // Given
      final var metadata = ColumnMetadata.of(type, true);

      // When
      final var isBinary = metadata.isBinary();

      // Then
      assertTrue(isBinary, "isBinary should return true for " + type);
    }
  }

  /** Tests for isBoolean method. */
  @Nested
  @DisplayName("isBoolean")
  class IsBooleanTests {

    /** Tests for isBoolean method. */
    IsBooleanTests() {}

    /**
     * Verifies that isBoolean returns true for boolean types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @Tag("normal")
    @EnumSource(
        value = JDBCType.class,
        names = {"BOOLEAN", "BIT"})
    @DisplayName("returns true for boolean types")
    void shouldReturnTrue_whenBooleanType(final JDBCType type) {
      // Given
      final var metadata = ColumnMetadata.of(type, true);

      // When
      final var isBoolean = metadata.isBoolean();

      // Then
      assertTrue(isBoolean, "isBoolean should return true for " + type);
    }
  }

  /** Tests for isLikelyAutoIncrement method. */
  @Nested
  @DisplayName("isLikelyAutoIncrement")
  class IsLikelyAutoIncrementTests {

    /** Tests for isLikelyAutoIncrement method. */
    IsLikelyAutoIncrementTests() {}

    /** Verifies that isLikelyAutoIncrement returns true for integer primary key without default. */
    @Test
    @Tag("normal")
    @DisplayName("returns true for integer primary key without default")
    void shouldReturnTrue_whenIntegerPkWithoutDefault() {
      // Given
      final var metadata = new ColumnMetadata(JDBCType.INTEGER, false, true, 1, 0, 0, null);

      // When
      final var isLikelyAutoIncrement = metadata.isLikelyAutoIncrement();

      // Then
      assertTrue(
          isLikelyAutoIncrement,
          "isLikelyAutoIncrement should return true for integer PK without default");
    }

    /** Verifies that isLikelyAutoIncrement returns true for bigint primary key without default. */
    @Test
    @Tag("normal")
    @DisplayName("returns true for bigint primary key without default")
    void shouldReturnTrue_whenBigintPkWithoutDefault() {
      // Given
      final var metadata = new ColumnMetadata(JDBCType.BIGINT, false, true, 1, 0, 0, null);

      // When
      final var isLikelyAutoIncrement = metadata.isLikelyAutoIncrement();

      // Then
      assertTrue(
          isLikelyAutoIncrement,
          "isLikelyAutoIncrement should return true for bigint PK without default");
    }

    /** Verifies that isLikelyAutoIncrement returns false for non-primary key. */
    @Test
    @Tag("normal")
    @DisplayName("returns false for non-primary key")
    void shouldReturnFalse_whenNonPrimaryKey() {
      // Given
      final var metadata = new ColumnMetadata(JDBCType.INTEGER, false, false, 1, 0, 0, null);

      // When
      final var isLikelyAutoIncrement = metadata.isLikelyAutoIncrement();

      // Then
      assertFalse(
          isLikelyAutoIncrement, "isLikelyAutoIncrement should return false for non-primary key");
    }

    /** Verifies that isLikelyAutoIncrement returns false for primary key with default value. */
    @Test
    @Tag("normal")
    @DisplayName("returns false for primary key with default value")
    void shouldReturnFalse_whenPkWithDefault() {
      // Given
      final var metadata = new ColumnMetadata(JDBCType.INTEGER, false, true, 1, 0, 0, "0");

      // When
      final var isLikelyAutoIncrement = metadata.isLikelyAutoIncrement();

      // Then
      assertFalse(
          isLikelyAutoIncrement,
          "isLikelyAutoIncrement should return false for PK with default value");
    }

    /** Verifies that isLikelyAutoIncrement returns false for non-integer primary key. */
    @Test
    @Tag("normal")
    @DisplayName("returns false for non-integer primary key")
    void shouldReturnFalse_whenNonIntegerPk() {
      // Given
      final var metadata = new ColumnMetadata(JDBCType.VARCHAR, false, true, 1, 0, 0, null);

      // When
      final var isLikelyAutoIncrement = metadata.isLikelyAutoIncrement();

      // Then
      assertFalse(
          isLikelyAutoIncrement, "isLikelyAutoIncrement should return false for non-integer PK");
    }
  }
}
