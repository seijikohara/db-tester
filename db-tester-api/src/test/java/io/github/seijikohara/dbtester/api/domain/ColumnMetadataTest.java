package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.JDBCType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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
    @DisplayName("of() creates metadata with type and nullable")
    void ofCreatesMetadataWithTypeAndNullable() {
      final var metadata = ColumnMetadata.of(JDBCType.VARCHAR, true);

      assertEquals(JDBCType.VARCHAR, metadata.jdbcType());
      assertTrue(metadata.nullable());
      assertFalse(metadata.primaryKey());
      assertEquals(0, metadata.ordinalPosition());
      assertEquals(0, metadata.precision());
      assertEquals(0, metadata.scale());
      assertNull(metadata.defaultValue());
    }

    /** Verifies that primaryKey() creates primary key metadata. */
    @Test
    @DisplayName("primaryKey() creates primary key metadata")
    void primaryKeyCreatesPrimaryKeyMetadata() {
      final var metadata = ColumnMetadata.primaryKey(JDBCType.BIGINT);

      assertEquals(JDBCType.BIGINT, metadata.jdbcType());
      assertFalse(metadata.nullable());
      assertTrue(metadata.primaryKey());
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
    void returnsTrueForNumericTypes(final JDBCType type) {
      final var metadata = ColumnMetadata.of(type, true);
      assertTrue(metadata.isNumeric());
    }

    /**
     * Verifies that isNumeric returns false for non-numeric types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @EnumSource(
        value = JDBCType.class,
        names = {"VARCHAR", "CHAR", "DATE", "TIMESTAMP", "BLOB", "BOOLEAN"})
    @DisplayName("returns false for non-numeric types")
    void returnsFalseForNonNumericTypes(final JDBCType type) {
      final var metadata = ColumnMetadata.of(type, true);
      assertFalse(metadata.isNumeric());
    }

    /** Verifies that isNumeric returns false for null type. */
    @Test
    @DisplayName("returns false for null type")
    void returnsFalseForNullType() {
      final var metadata = new ColumnMetadata(null, true, false, 0, 0, 0, null);
      assertFalse(metadata.isNumeric());
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
    void returnsTrueForTextualTypes(final JDBCType type) {
      final var metadata = ColumnMetadata.of(type, true);
      assertTrue(metadata.isTextual());
    }

    /**
     * Verifies that isTextual returns false for non-textual types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @EnumSource(
        value = JDBCType.class,
        names = {"INTEGER", "DATE", "BLOB", "BOOLEAN"})
    @DisplayName("returns false for non-textual types")
    void returnsFalseForNonTextualTypes(final JDBCType type) {
      final var metadata = ColumnMetadata.of(type, true);
      assertFalse(metadata.isTextual());
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
    @EnumSource(
        value = JDBCType.class,
        names = {"DATE", "TIME", "TIMESTAMP", "TIME_WITH_TIMEZONE", "TIMESTAMP_WITH_TIMEZONE"})
    @DisplayName("returns true for temporal types")
    void returnsTrueForTemporalTypes(final JDBCType type) {
      final var metadata = ColumnMetadata.of(type, true);
      assertTrue(metadata.isTemporal());
    }

    /**
     * Verifies that isTemporal returns false for non-temporal types.
     *
     * @param type the JDBC type to test
     */
    @ParameterizedTest
    @EnumSource(
        value = JDBCType.class,
        names = {"VARCHAR", "INTEGER", "BOOLEAN"})
    @DisplayName("returns false for non-temporal types")
    void returnsFalseForNonTemporalTypes(final JDBCType type) {
      final var metadata = ColumnMetadata.of(type, true);
      assertFalse(metadata.isTemporal());
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
    @EnumSource(
        value = JDBCType.class,
        names = {"BINARY", "VARBINARY", "LONGVARBINARY", "BLOB"})
    @DisplayName("returns true for binary types")
    void returnsTrueForBinaryTypes(final JDBCType type) {
      final var metadata = ColumnMetadata.of(type, true);
      assertTrue(metadata.isBinary());
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
    @EnumSource(
        value = JDBCType.class,
        names = {"BOOLEAN", "BIT"})
    @DisplayName("returns true for boolean types")
    void returnsTrueForBooleanTypes(final JDBCType type) {
      final var metadata = ColumnMetadata.of(type, true);
      assertTrue(metadata.isBoolean());
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
    @DisplayName("returns true for integer primary key without default")
    void returnsTrueForIntegerPkWithoutDefault() {
      final var metadata = new ColumnMetadata(JDBCType.INTEGER, false, true, 1, 0, 0, null);
      assertTrue(metadata.isLikelyAutoIncrement());
    }

    /** Verifies that isLikelyAutoIncrement returns true for bigint primary key without default. */
    @Test
    @DisplayName("returns true for bigint primary key without default")
    void returnsTrueForBigintPkWithoutDefault() {
      final var metadata = new ColumnMetadata(JDBCType.BIGINT, false, true, 1, 0, 0, null);
      assertTrue(metadata.isLikelyAutoIncrement());
    }

    /** Verifies that isLikelyAutoIncrement returns false for non-primary key. */
    @Test
    @DisplayName("returns false for non-primary key")
    void returnsFalseForNonPk() {
      final var metadata = new ColumnMetadata(JDBCType.INTEGER, false, false, 1, 0, 0, null);
      assertFalse(metadata.isLikelyAutoIncrement());
    }

    /** Verifies that isLikelyAutoIncrement returns false for primary key with default value. */
    @Test
    @DisplayName("returns false for primary key with default value")
    void returnsFalseForPkWithDefault() {
      final var metadata = new ColumnMetadata(JDBCType.INTEGER, false, true, 1, 0, 0, "0");
      assertFalse(metadata.isLikelyAutoIncrement());
    }

    /** Verifies that isLikelyAutoIncrement returns false for non-integer primary key. */
    @Test
    @DisplayName("returns false for non-integer primary key")
    void returnsFalseForNonIntegerPk() {
      final var metadata = new ColumnMetadata(JDBCType.VARCHAR, false, true, 1, 0, 0, null);
      assertFalse(metadata.isLikelyAutoIncrement());
    }
  }
}
