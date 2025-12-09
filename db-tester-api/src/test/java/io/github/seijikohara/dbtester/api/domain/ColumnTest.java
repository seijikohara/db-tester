package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.JDBCType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Column}. */
@DisplayName("Column")
class ColumnTest {

  /** Tests for the Column class. */
  ColumnTest() {}

  /** Tests for factory methods. */
  @Nested
  @DisplayName("factory methods")
  class FactoryMethods {

    /** Tests for factory methods. */
    FactoryMethods() {}

    /** Verifies that of(String) creates a column with default settings. */
    @Test
    @DisplayName("of(String) creates column with default settings")
    void ofStringCreatesColumnWithDefaults() {
      final var column = Column.of("user_id");

      assertEquals("user_id", column.getNameValue());
      assertEquals(new ColumnName("user_id"), column.getName());
      assertFalse(column.hasMetadata());
      assertNull(column.getMetadata());
      assertEquals(ComparisonStrategy.STRICT, column.getComparisonStrategy());
      assertFalse(column.isIgnored());
    }

    /** Verifies that of(ColumnName) creates a column from existing ColumnName. */
    @Test
    @DisplayName("of(ColumnName) creates column from existing ColumnName")
    void ofColumnNameCreatesColumn() {
      final var name = new ColumnName("email");
      final var column = Column.of(name);

      assertSame(name, column.getName());
      assertEquals("email", column.getNameValue());
    }
  }

  /** Tests for the builder. */
  @Nested
  @DisplayName("builder")
  class BuilderTests {

    /** Tests for the builder. */
    BuilderTests() {}

    /** Verifies that builder builds a column with all properties. */
    @Test
    @DisplayName("builds column with all properties")
    void buildsColumnWithAllProperties() {
      final var column =
          Column.builder("price")
              .jdbcType(JDBCType.DECIMAL)
              .precision(10)
              .scale(2)
              .nullable(false)
              .primaryKey(false)
              .ordinalPosition(3)
              .defaultValue("0.00")
              .comparisonStrategy(ComparisonStrategy.NUMERIC)
              .build();

      assertEquals("price", column.getNameValue());
      assertTrue(column.hasMetadata());

      final var metadata = column.getMetadata();
      assertNotNull(metadata);
      assertEquals(JDBCType.DECIMAL, metadata.jdbcType());
      assertEquals(10, metadata.precision());
      assertEquals(2, metadata.scale());
      assertFalse(metadata.nullable());
      assertFalse(metadata.primaryKey());
      assertEquals(3, metadata.ordinalPosition());
      assertEquals("0.00", metadata.defaultValue());

      assertEquals(ComparisonStrategy.NUMERIC, column.getComparisonStrategy());
    }

    /** Verifies that builder builds a column with regex pattern. */
    @Test
    @DisplayName("builds column with regex pattern")
    void buildsColumnWithRegexPattern() {
      final var column = Column.builder("uuid").regexPattern("[a-f0-9-]{36}").build();

      assertEquals(ComparisonStrategy.Type.REGEX, column.getComparisonStrategy().getType());
      assertNotNull(column.getComparisonStrategy().getPattern());
    }

    /** Verifies that builder builds a column with IGNORE strategy. */
    @Test
    @DisplayName("builds column with IGNORE strategy")
    void buildsColumnWithIgnoreStrategy() {
      final var column =
          Column.builder("created_at").comparisonStrategy(ComparisonStrategy.IGNORE).build();

      assertTrue(column.isIgnored());
    }

    /** Verifies that builder with minimal settings creates no metadata. */
    @Test
    @DisplayName("builds column with minimal settings creates no metadata")
    void buildsColumnWithMinimalSettingsCreatesNoMetadata() {
      final var column = Column.builder("name").nullable(true).build();

      assertFalse(column.hasMetadata());
    }
  }

  /** Tests for withComparisonStrategy method. */
  @Nested
  @DisplayName("withComparisonStrategy")
  class WithComparisonStrategy {

    /** Tests for withComparisonStrategy method. */
    WithComparisonStrategy() {}

    /** Verifies that withComparisonStrategy creates a new column with updated strategy. */
    @Test
    @DisplayName("creates new column with updated strategy")
    void createsNewColumnWithUpdatedStrategy() {
      final var original = Column.of("id");
      final var updated = original.withComparisonStrategy(ComparisonStrategy.IGNORE);

      assertNotSame(original, updated);
      assertEquals(ComparisonStrategy.STRICT, original.getComparisonStrategy());
      assertEquals(ComparisonStrategy.IGNORE, updated.getComparisonStrategy());
      assertEquals(original.getName(), updated.getName());
    }
  }

  /** Tests for withMetadata method. */
  @Nested
  @DisplayName("withMetadata")
  class WithMetadata {

    /** Tests for withMetadata method. */
    WithMetadata() {}

    /** Verifies that withMetadata creates a new column with updated metadata. */
    @Test
    @DisplayName("creates new column with updated metadata")
    void createsNewColumnWithUpdatedMetadata() {
      final var original = Column.of("id");
      final var metadata = ColumnMetadata.primaryKey(JDBCType.BIGINT);
      final var updated = original.withMetadata(metadata);

      assertNotSame(original, updated);
      assertFalse(original.hasMetadata());
      assertTrue(updated.hasMetadata());
      assertSame(metadata, updated.getMetadata());
    }
  }

  /** Tests for equality and hashCode. */
  @Nested
  @DisplayName("equality and hashCode")
  class EqualityTests {

    /** Tests for equality and hashCode. */
    EqualityTests() {}

    /** Verifies that equals is based on column name only. */
    @Test
    @DisplayName("equals based on column name only")
    void equalsBasedOnNameOnly() {
      final var col1 = Column.of("id");
      final var col2 = Column.builder("id").jdbcType(JDBCType.INTEGER).primaryKey(true).build();

      assertEquals(col1, col2);
      assertEquals(col1.hashCode(), col2.hashCode());
    }

    /** Verifies that different names are not equal. */
    @Test
    @DisplayName("different names are not equal")
    void differentNamesNotEqual() {
      final var col1 = Column.of("id");
      final var col2 = Column.of("user_id");

      assertNotEquals(col1, col2);
    }
  }

  /** Tests for compareTo method. */
  @Nested
  @DisplayName("compareTo")
  class CompareToTests {

    /** Tests for compareTo method. */
    CompareToTests() {}

    /** Verifies that compareTo compares by column name. */
    @Test
    @DisplayName("compares by column name")
    void comparesByName() {
      final var a = Column.of("a");
      final var b = Column.of("b");

      assertTrue(a.compareTo(b) < 0);
      assertTrue(b.compareTo(a) > 0);
      assertEquals(0, a.compareTo(Column.of("a")));
    }
  }

  /** Tests for toString method. */
  @Nested
  @DisplayName("toString")
  class ToStringTests {

    /** Tests for toString method. */
    ToStringTests() {}

    /** Verifies that toString returns a readable representation. */
    @Test
    @DisplayName("returns readable representation")
    void returnsReadableRepresentation() {
      final var column = Column.of("user_name");

      assertEquals("Column[user_name]", column.toString());
    }
  }
}
