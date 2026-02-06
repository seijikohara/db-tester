package io.github.seijikohara.dbtester.api.config;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DataFormat}. */
@DisplayName("DataFormat")
class DataFormatTest {

  /** Tests for the DataFormat enum. */
  DataFormatTest() {}

  /** Tests for the CSV format. */
  @Nested
  @DisplayName("CSV format")
  class CsvFormat {

    /** Tests for the CSV format. */
    CsvFormat() {}

    /** Verifies that CSV format has correct extension. */
    @Test
    @Tag("normal")
    @DisplayName("should have .csv extension")
    void shouldHaveCsvExtension() {
      // When
      final var extension = DataFormat.CSV.getExtension();

      // Then
      assertEquals(".csv", extension, "CSV extension should be .csv");
    }

    /** Verifies that CSV format name is CSV. */
    @Test
    @Tag("normal")
    @DisplayName("should have name CSV")
    void shouldHaveNameCsv() {
      // When
      final var name = DataFormat.CSV.name();

      // Then
      assertEquals("CSV", name, "name should be CSV");
    }
  }

  /** Tests for the TSV format. */
  @Nested
  @DisplayName("TSV format")
  class TsvFormat {

    /** Tests for the TSV format. */
    TsvFormat() {}

    /** Verifies that TSV format has correct extension. */
    @Test
    @Tag("normal")
    @DisplayName("should have .tsv extension")
    void shouldHaveTsvExtension() {
      // When
      final var extension = DataFormat.TSV.getExtension();

      // Then
      assertEquals(".tsv", extension, "TSV extension should be .tsv");
    }

    /** Verifies that TSV format name is TSV. */
    @Test
    @Tag("normal")
    @DisplayName("should have name TSV")
    void shouldHaveNameTsv() {
      // When
      final var name = DataFormat.TSV.name();

      // Then
      assertEquals("TSV", name, "name should be TSV");
    }
  }

  /** Tests for the JSON format. */
  @Nested
  @DisplayName("JSON format")
  class JsonFormat {

    /** Tests for the JSON format. */
    JsonFormat() {}

    /** Verifies that JSON format has correct extension. */
    @Test
    @Tag("normal")
    @DisplayName("should have .json extension")
    void shouldHaveJsonExtension() {
      // When
      final var extension = DataFormat.JSON.getExtension();

      // Then
      assertEquals(".json", extension, "JSON extension should be .json");
    }

    /** Verifies that JSON format name is JSON. */
    @Test
    @Tag("normal")
    @DisplayName("should have name JSON")
    void shouldHaveNameJson() {
      // When
      final var name = DataFormat.JSON.name();

      // Then
      assertEquals("JSON", name, "name should be JSON");
    }
  }

  /** Tests for the YAML format. */
  @Nested
  @DisplayName("YAML format")
  class YamlFormat {

    /** Tests for the YAML format. */
    YamlFormat() {}

    /** Verifies that YAML format has correct extension. */
    @Test
    @Tag("normal")
    @DisplayName("should have .yaml extension")
    void shouldHaveYamlExtension() {
      // When
      final var extension = DataFormat.YAML.getExtension();

      // Then
      assertEquals(".yaml", extension, "YAML extension should be .yaml");
    }

    /** Verifies that YAML format name is YAML. */
    @Test
    @Tag("normal")
    @DisplayName("should have name YAML")
    void shouldHaveNameYaml() {
      // When
      final var name = DataFormat.YAML.name();

      // Then
      assertEquals("YAML", name, "name should be YAML");
    }
  }

  /** Tests for enum values. */
  @Nested
  @DisplayName("values() method")
  class ValuesMethod {

    /** Tests for the values method. */
    ValuesMethod() {}

    /** Verifies that values returns all formats. */
    @Test
    @Tag("normal")
    @DisplayName("should return all formats")
    void shouldReturnAllFormats() {
      // When
      final var values = DataFormat.values();

      // Then
      assertAll(
          "should have all formats",
          () -> assertNotNull(values, "values should not be null"),
          () -> assertEquals(4, values.length, "should have four formats"),
          () -> assertEquals(DataFormat.CSV, values[0], "first should be CSV"),
          () -> assertEquals(DataFormat.TSV, values[1], "second should be TSV"),
          () -> assertEquals(DataFormat.JSON, values[2], "third should be JSON"),
          () -> assertEquals(DataFormat.YAML, values[3], "fourth should be YAML"));
    }
  }

  /** Tests for valueOf method. */
  @Nested
  @DisplayName("valueOf() method")
  class ValueOfMethod {

    /** Tests for the valueOf method. */
    ValueOfMethod() {}

    /** Verifies that valueOf returns correct format for CSV. */
    @Test
    @Tag("normal")
    @DisplayName("should return CSV for valueOf(\"CSV\")")
    void shouldReturnCsvForValueOfCsv() {
      // When
      final var format = DataFormat.valueOf("CSV");

      // Then
      assertEquals(DataFormat.CSV, format, "should return CSV");
    }

    /** Verifies that valueOf returns correct format for TSV. */
    @Test
    @Tag("normal")
    @DisplayName("should return TSV for valueOf(\"TSV\")")
    void shouldReturnTsvForValueOfTsv() {
      // When
      final var format = DataFormat.valueOf("TSV");

      // Then
      assertEquals(DataFormat.TSV, format, "should return TSV");
    }

    /** Verifies that valueOf returns correct format for JSON. */
    @Test
    @Tag("normal")
    @DisplayName("should return JSON for valueOf(\"JSON\")")
    void shouldReturnJsonForValueOfJson() {
      // When
      final var format = DataFormat.valueOf("JSON");

      // Then
      assertEquals(DataFormat.JSON, format, "should return JSON");
    }

    /** Verifies that valueOf returns correct format for YAML. */
    @Test
    @Tag("normal")
    @DisplayName("should return YAML for valueOf(\"YAML\")")
    void shouldReturnYamlForValueOfYaml() {
      // When
      final var format = DataFormat.valueOf("YAML");

      // Then
      assertEquals(DataFormat.YAML, format, "should return YAML");
    }
  }

  /** Tests for enum equality. */
  @Nested
  @DisplayName("equality")
  class Equality {

    /** Tests for equality. */
    Equality() {}

    /** Verifies that CSV and TSV are not equal. */
    @Test
    @Tag("normal")
    @DisplayName("should not be equal between CSV and TSV")
    void shouldNotBeEqualBetweenCsvAndTsv() {
      // When & Then
      assertNotEquals(DataFormat.CSV, DataFormat.TSV, "CSV and TSV should not be equal");
    }
  }
}
