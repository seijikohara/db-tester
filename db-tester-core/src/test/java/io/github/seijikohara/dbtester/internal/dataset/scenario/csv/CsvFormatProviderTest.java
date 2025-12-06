package io.github.seijikohara.dbtester.internal.dataset.scenario.csv;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import io.github.seijikohara.dbtester.internal.domain.ScenarioMarker;
import io.github.seijikohara.dbtester.internal.domain.ScenarioName;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link CsvFormatProvider}. */
@DisplayName("CsvFormatProvider")
class CsvFormatProviderTest {

  /** Tests for the CsvFormatProvider class. */
  CsvFormatProviderTest() {}

  /** Tests for the supportedFileExtension() method. */
  @Nested
  @DisplayName("supportedFileExtension() method")
  class SupportedFileExtensionMethod {

    /** Tests for the supportedFileExtension method. */
    SupportedFileExtensionMethod() {}

    /** Verifies that supportedFileExtension returns csv extension when called. */
    @Test
    @Tag("normal")
    @DisplayName("should return csv extension when called")
    void shouldReturnCsvExtension_whenCalled() {
      // Given
      final var provider = new CsvFormatProvider();

      // When
      final var extension = provider.supportedFileExtension();

      // Then
      assertEquals(new FileExtension("csv"), extension, "should return csv extension");
    }
  }

  /** Tests for the createDataSet() method. */
  @Nested
  @DisplayName("createDataSet() method")
  class CreateDataSetMethod {

    /** Tests for the createDataSet method. */
    CreateDataSetMethod() {}

    /**
     * Verifies that createDataSet returns CsvScenarioDataSet instance when called.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return CsvScenarioDataSet instance when called")
    void shouldReturnCsvScenarioDataSetInstance_whenCalled(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      final var provider = new CsvFormatProvider();
      final var scenarioNames = List.<ScenarioName>of();
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = provider.createDataSet(tempDir, scenarioNames, scenarioMarker, null);

      // Then
      assertAll(
          "should return CsvScenarioDataSet instance",
          () -> assertNotNull(dataSet, "data set should not be null"),
          () ->
              assertInstanceOf(
                  CsvScenarioDataSet.class, dataSet, "should be CsvScenarioDataSet instance"));
    }

    /**
     * Verifies that createDataSet passes parameters to CsvScenarioDataSet when called.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should pass parameters to CsvScenarioDataSet when called")
    void shouldPassParametersToCsvScenarioDataSet_whenCalled(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "TABLE1.csv", "SCENARIO,COL1", "scenario1,A", "scenario2,B");
      final var provider = new CsvFormatProvider();
      final var scenarioNames = List.of(new ScenarioName("scenario1"));
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = provider.createDataSet(tempDir, scenarioNames, scenarioMarker, null);

      // Then
      assertAll(
          "should create dataset with correct parameters",
          () -> assertEquals(1, dataSet.getTables().size(), "should have one table"),
          () -> {
            final var table = dataSet.getTables().get(0);
            assertEquals(1, table.getRows().size(), "should have filtered rows by scenario names");
          });
    }
  }

  /**
   * Creates a CSV file with the specified content.
   *
   * @param dir the directory to create the file in
   * @param fileName the file name
   * @param lines the CSV lines
   * @throws IOException if file creation fails
   */
  private static void createCsvFile(final Path dir, final String fileName, final String... lines)
      throws IOException {
    final var content = String.join("\n", lines);
    Files.writeString(dir.resolve(fileName), content);
  }
}
