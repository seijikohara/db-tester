package io.github.seijikohara.dbtester.internal.dataset;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

/** Unit tests for {@link ScenarioDataSetFactory}. */
@DisplayName("ScenarioDataSetFactory")
class ScenarioDataSetFactoryTest {

  /** Tests for the ScenarioDataSetFactory class. */
  ScenarioDataSetFactoryTest() {}

  /** Default scenario marker for testing. */
  private static final ScenarioMarker DEFAULT_MARKER = new ScenarioMarker("[Scenario]");

  /** Tests for the create(Path, Collection, ScenarioMarker, DataSource) method. */
  @Nested
  @DisplayName("create(Path, Collection<ScenarioName>, ScenarioMarker, DataSource) method")
  class CreateWithCollectionMethod {

    /** Tests for the create method with collection of scenario names. */
    CreateWithCollectionMethod() {}

    /**
     * Verifies that create returns dataset when valid directory provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return dataset when valid directory provided")
    void shouldReturnDataSet_whenValidDirectoryProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(tempDir, "USERS.csv", "[Scenario],ID,NAME", "test1,1,Alice", "test1,2,Bob");
      final var scenarioNames = List.of(new ScenarioName("test1"));

      // When
      final var result =
          ScenarioDataSetFactory.create(tempDir, scenarioNames, DEFAULT_MARKER, null);

      // Then
      assertAll(
          "should return valid dataset",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertNotNull(result.getTables(), "tables should not be null"));
    }

    /**
     * Verifies that create throws exception when path is not a directory.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when path is not a directory")
    void shouldThrowException_whenPathIsNotDirectory(final @TempDir Path tempDir)
        throws IOException {
      // Given
      final var filePath = tempDir.resolve("file.txt");
      Files.writeString(filePath, "content");
      final var scenarioNames = List.of(new ScenarioName("test1"));

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> ScenarioDataSetFactory.create(filePath, scenarioNames, DEFAULT_MARKER, null));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("not a directory"),
          "exception message should mention not a directory");
    }

    /**
     * Verifies that create throws exception when directory does not exist.
     *
     * @param tempDir temporary directory for test files
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when directory does not exist")
    void shouldThrowException_whenDirectoryDoesNotExist(final @TempDir Path tempDir) {
      // Given
      final var nonExistentDir = tempDir.resolve("nonexistent");
      final var scenarioNames = List.of(new ScenarioName("test1"));

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () ->
                  ScenarioDataSetFactory.create(
                      nonExistentDir, scenarioNames, DEFAULT_MARKER, null));

      assertNotNull(exception.getMessage(), "exception message should not be null");
      assertTrue(
          exception.getMessage().contains("not a directory"),
          "exception message should mention not a directory");
    }

    /**
     * Verifies that create handles empty scenario names.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should return dataset with all rows when empty scenario names provided")
    void shouldReturnDataSetWithAllRows_whenEmptyScenarioNamesProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(
          tempDir,
          "USERS.csv",
          "[Scenario],ID,NAME",
          "test1,1,Alice",
          "test2,2,Bob",
          "test1,3,Eve");
      final List<ScenarioName> emptyScenarioNames = List.of();

      // When
      final var result =
          ScenarioDataSetFactory.create(tempDir, emptyScenarioNames, DEFAULT_MARKER, null);

      // Then
      assertNotNull(result, "should return dataset even with empty scenario names");
    }
  }

  /** Tests for the create(Path, ScenarioName, ScenarioMarker, DataSource) method. */
  @Nested
  @DisplayName("create(Path, ScenarioName, ScenarioMarker, DataSource) method")
  class CreateWithSingleScenarioMethod {

    /** Tests for the create method with single scenario name. */
    CreateWithSingleScenarioMethod() {}

    /**
     * Verifies that create returns dataset when single scenario name provided.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return dataset when single scenario name provided")
    void shouldReturnDataSet_whenSingleScenarioNameProvided(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createCsvFile(
          tempDir,
          "USERS.csv",
          "[Scenario],ID,NAME",
          "test1,1,Alice",
          "test2,2,Bob",
          "test1,3,Eve");
      final var scenarioName = new ScenarioName("test1");

      // When
      final var result = ScenarioDataSetFactory.create(tempDir, scenarioName, DEFAULT_MARKER, null);

      // Then
      assertNotNull(result, "should return dataset for single scenario name");
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
