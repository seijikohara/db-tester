package io.github.seijikohara.dbtester.internal.loader;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
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

/** Unit tests for {@link DataSetFactory}. */
@DisplayName("DataSetFactory")
class DataSetFactoryTest {

  /** Tests for the DataSetFactory class. */
  DataSetFactoryTest() {}

  /** Tests for the constructor. */
  @Nested
  @DisplayName("constructor")
  class ConstructorMethod {

    /** Tests for the constructor. */
    ConstructorMethod() {}

    /** Verifies that constructor creates instance when called. */
    @Test
    @Tag("normal")
    @DisplayName("should create instance when called")
    void shouldCreateInstance_whenCalled() {
      // When
      final var factory = new DataSetFactory();

      // Then
      assertNotNull(factory, "factory should not be null");
    }
  }

  /** Tests for the createDataSet() method. */
  @Nested
  @DisplayName("createDataSet() method")
  class CreateDataSetMethod {

    /** Tests for the createDataSet method. */
    CreateDataSetMethod() {}

    /**
     * Verifies that createDataSet returns data set when valid directory provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return data set when valid directory provided")
    void shouldReturnDataSet_whenValidDirectoryProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var factory = new DataSetFactory();
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = factory.createDataSet(tempDir, List.of(), scenarioMarker, null);

      // Then
      assertAll(
          "data set should be created",
          () -> assertNotNull(dataSet, "data set should not be null"),
          () -> assertEquals(1, dataSet.getTables().size(), "should have one table"),
          () ->
              assertEquals(
                  "TABLE1",
                  dataSet.getTables().get(0).getName().value(),
                  "table name should be TABLE1"));
    }

    /**
     * Verifies that createDataSet returns data set with scenario filtering when scenario names
     * provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return data set with scenario filtering when scenario names provided")
    void shouldReturnDataSetWithScenarioFiltering_whenScenarioNamesProvided(
        final @TempDir Path tempDir) throws IOException {
      // Given
      final var factory = new DataSetFactory();
      createCsvFile(
          tempDir, "TABLE1.csv", "SCENARIO,COL1", "scenario1,A", "scenario2,B", "scenario1,C");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");
      final var scenarioNames = List.of(new ScenarioName("scenario1"));

      // When
      final var dataSet = factory.createDataSet(tempDir, scenarioNames, scenarioMarker, null);

      // Then
      final var table = dataSet.getTables().get(0);
      final var rows = table.getRows();
      assertAll(
          "data set should be filtered by scenario",
          () -> assertEquals(1, dataSet.getTables().size(), "should have one table"),
          () -> assertEquals(2, rows.size(), "should have two rows matching scenario1"));
    }

    /**
     * Verifies that createDataSet detects CSV extension when CSV files exist.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should detect CSV extension when CSV files exist")
    void shouldDetectCsvExtension_whenCsvFilesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var factory = new DataSetFactory();
      createCsvFile(tempDir, "TABLE1.csv", "COL1", "A");
      createCsvFile(tempDir, "TABLE2.csv", "COL2", "B");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When
      final var dataSet = factory.createDataSet(tempDir, List.of(), scenarioMarker, null);

      // Then
      assertAll(
          "CSV extension should be detected",
          () -> assertNotNull(dataSet, "data set should not be null"),
          () -> assertEquals(2, dataSet.getTables().size(), "should have two tables"));
    }

    /**
     * Verifies that createDataSet throws exception when no data files found.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when no data files found")
    void shouldThrowException_whenNoDataFilesFound(final @TempDir Path tempDir) throws IOException {
      // Given
      final var factory = new DataSetFactory();
      Files.writeString(tempDir.resolve("unsupported.txt"), "data");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> factory.createDataSet(tempDir, List.of(), scenarioMarker, null));

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("No data files"),
          "exception should mention no data files");
    }

    /**
     * Verifies that createDataSet throws exception when unsupported extension only.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when unsupported extension only")
    void shouldThrowException_whenUnsupportedExtensionOnly(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var factory = new DataSetFactory();
      Files.writeString(tempDir.resolve("TABLE1.unsupported"), "data");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> factory.createDataSet(tempDir, List.of(), scenarioMarker, null));

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("No data files"),
          "exception should mention no data files");
    }

    /**
     * Verifies that createDataSet throws exception when directory read fails.
     *
     * @param tempDir temporary directory for test files
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when directory read fails")
    void shouldThrowException_whenDirectoryReadFails(final @TempDir Path tempDir) {
      // Given
      final var factory = new DataSetFactory();
      final var nonExistentDir = tempDir.resolve("nonexistent");
      final var scenarioMarker = new ScenarioMarker("SCENARIO");

      // When & Then
      assertThrows(
          DataSetLoadException.class,
          () -> factory.createDataSet(nonExistentDir, List.of(), scenarioMarker, null));
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
