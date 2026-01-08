package io.github.seijikohara.dbtester.api.domain;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.JDBCType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
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
    @Tag("normal")
    @DisplayName("of(String) creates column with default settings")
    void shouldCreateColumnWithDefaults_whenStringProvided() {
      // When
      final var column = Column.of("user_id");

      // Then
      assertAll(
          "column should have default settings",
          () -> assertEquals("user_id", column.getNameValue(), "name value should be 'user_id'"),
          () ->
              assertEquals(
                  new ColumnName("user_id"), column.getName(), "name should equal ColumnName"),
          () -> assertFalse(column.hasMetadata(), "should not have metadata"),
          () -> assertTrue(column.getMetadata().isEmpty(), "metadata should be empty"),
          () ->
              assertEquals(
                  ComparisonStrategy.STRICT,
                  column.getComparisonStrategy(),
                  "comparison strategy should be STRICT"),
          () -> assertFalse(column.isIgnored(), "should not be ignored"));
    }

    /** Verifies that of(ColumnName) creates a column from existing ColumnName. */
    @Test
    @Tag("normal")
    @DisplayName("of(ColumnName) creates column from existing ColumnName")
    void shouldCreateColumn_whenColumnNameProvided() {
      // Given
      final var name = new ColumnName("email");

      // When
      final var column = Column.of(name);

      // Then
      assertAll(
          "column should use provided ColumnName",
          () -> assertSame(name, column.getName(), "name should be the same instance"),
          () -> assertEquals("email", column.getNameValue(), "name value should be 'email'"));
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
    @Tag("normal")
    @DisplayName("builds column with all properties")
    void shouldBuildColumnWithAllProperties_whenAllPropertiesSet() {
      // When
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

      // Then
      assertAll(
          "column should have all specified properties",
          () -> assertEquals("price", column.getNameValue(), "name value should be 'price'"),
          () -> assertTrue(column.hasMetadata(), "should have metadata"),
          () -> assertTrue(column.getMetadata().isPresent(), "metadata should be present"));

      final var metadata = column.getMetadata().orElseThrow();
      assertAll(
          "metadata should have correct values",
          () -> assertEquals(JDBCType.DECIMAL, metadata.jdbcType(), "jdbcType should be DECIMAL"),
          () -> assertEquals(10, metadata.precision(), "precision should be 10"),
          () -> assertEquals(2, metadata.scale(), "scale should be 2"),
          () -> assertFalse(metadata.nullable(), "nullable should be false"),
          () -> assertFalse(metadata.primaryKey(), "primaryKey should be false"),
          () -> assertEquals(3, metadata.ordinalPosition(), "ordinalPosition should be 3"),
          () -> assertEquals("0.00", metadata.defaultValue(), "defaultValue should be '0.00'"));

      assertEquals(
          ComparisonStrategy.NUMERIC,
          column.getComparisonStrategy(),
          "comparison strategy should be NUMERIC");
    }

    /** Verifies that builder builds a column with regex pattern. */
    @Test
    @Tag("normal")
    @DisplayName("builds column with regex pattern")
    void shouldBuildColumnWithRegexPattern_whenRegexPatternSet() {
      // When
      final var column = Column.builder("uuid").regexPattern("[a-f0-9-]{36}").build();

      // Then
      assertAll(
          "column should have regex comparison strategy",
          () ->
              assertEquals(
                  ComparisonStrategy.Type.REGEX,
                  column.getComparisonStrategy().getType(),
                  "comparison strategy type should be REGEX"),
          () ->
              assertTrue(
                  column.getComparisonStrategy().getPattern().isPresent(),
                  "pattern should be present"));
    }

    /** Verifies that builder builds a column with IGNORE strategy. */
    @Test
    @Tag("normal")
    @DisplayName("builds column with IGNORE strategy")
    void shouldBuildColumnWithIgnoreStrategy_whenIgnoreStrategySet() {
      // When
      final var column =
          Column.builder("created_at").comparisonStrategy(ComparisonStrategy.IGNORE).build();

      // Then
      assertTrue(column.isIgnored(), "column should be ignored");
    }

    /** Verifies that builder with minimal settings creates no metadata. */
    @Test
    @Tag("edge-case")
    @DisplayName("builds column with minimal settings creates no metadata")
    void shouldNotCreateMetadata_whenMinimalSettingsProvided() {
      // When
      final var column = Column.builder("name").nullable(true).build();

      // Then
      assertFalse(column.hasMetadata(), "column should not have metadata with minimal settings");
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
    @Tag("normal")
    @DisplayName("creates new column with updated strategy")
    void shouldCreateNewColumnWithUpdatedStrategy_whenWithComparisonStrategyCalled() {
      // Given
      final var original = Column.of("id");

      // When
      final var updated = original.withComparisonStrategy(ComparisonStrategy.IGNORE);

      // Then
      assertAll(
          "withComparisonStrategy should create new column with updated strategy",
          () -> assertNotSame(original, updated, "should be a different instance"),
          () ->
              assertEquals(
                  ComparisonStrategy.STRICT,
                  original.getComparisonStrategy(),
                  "original should keep STRICT strategy"),
          () ->
              assertEquals(
                  ComparisonStrategy.IGNORE,
                  updated.getComparisonStrategy(),
                  "updated should have IGNORE strategy"),
          () -> assertEquals(original.getName(), updated.getName(), "names should be equal"));
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
    @Tag("normal")
    @DisplayName("creates new column with updated metadata")
    void shouldCreateNewColumnWithUpdatedMetadata_whenWithMetadataCalled() {
      // Given
      final var original = Column.of("id");
      final var metadata = ColumnMetadata.primaryKey(JDBCType.BIGINT);

      // When
      final var updated = original.withMetadata(metadata);

      // Then
      assertAll(
          "withMetadata should create new column with updated metadata",
          () -> assertNotSame(original, updated, "should be a different instance"),
          () -> assertFalse(original.hasMetadata(), "original should not have metadata"),
          () -> assertTrue(updated.hasMetadata(), "updated should have metadata"),
          () ->
              assertSame(
                  metadata, updated.getMetadata().orElseThrow(), "metadata should be the same"));
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
    @Tag("normal")
    @DisplayName("equals based on column name only")
    void shouldBeEqual_whenSameColumnName() {
      // Given
      final var col1 = Column.of("id");
      final var col2 = Column.builder("id").jdbcType(JDBCType.INTEGER).primaryKey(true).build();

      // When & Then
      assertAll(
          "columns with same name should be equal",
          () -> assertEquals(col1, col2, "columns should be equal"),
          () -> assertEquals(col1.hashCode(), col2.hashCode(), "hash codes should be equal"));
    }

    /** Verifies that different names are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("different names are not equal")
    void shouldNotBeEqual_whenDifferentNames() {
      // Given
      final var col1 = Column.of("id");
      final var col2 = Column.of("user_id");

      // When & Then
      assertNotEquals(col1, col2, "columns with different names should not be equal");
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
    @Tag("normal")
    @DisplayName("compares by column name")
    void shouldCompareByName_whenCompareToMethodCalled() {
      // Given
      final var a = Column.of("a");
      final var b = Column.of("b");

      // When & Then
      assertAll(
          "compareTo should compare by column name",
          () -> assertTrue(a.compareTo(b) < 0, "'a' should be less than 'b'"),
          () -> assertTrue(b.compareTo(a) > 0, "'b' should be greater than 'a'"),
          () -> assertEquals(0, a.compareTo(Column.of("a")), "'a' should equal 'a'"));
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
    @Tag("normal")
    @DisplayName("returns readable representation")
    void shouldReturnReadableRepresentation_whenToStringCalled() {
      // Given
      final var column = Column.of("user_name");

      // When
      final var result = column.toString();

      // Then
      assertEquals("Column[user_name]", result, "toString should return readable representation");
    }
  }
}
