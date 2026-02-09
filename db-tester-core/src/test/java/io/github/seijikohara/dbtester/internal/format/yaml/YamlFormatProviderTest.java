package io.github.seijikohara.dbtester.internal.format.yaml;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.seijikohara.dbtester.api.domain.CellValue;
import io.github.seijikohara.dbtester.api.domain.ColumnName;
import io.github.seijikohara.dbtester.api.exception.DataSetLoadException;
import io.github.seijikohara.dbtester.internal.domain.FileExtension;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link YamlFormatProvider}. */
@DisplayName("YamlFormatProvider")
class YamlFormatProviderTest {

  /** Tests for the YamlFormatProvider class. */
  YamlFormatProviderTest() {}

  /** The provider instance under test. */
  private YamlFormatProvider provider;

  /** Sets up test fixtures before each test. */
  @BeforeEach
  void setUp() {
    provider = new YamlFormatProvider();
  }

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
      final var instance = new YamlFormatProvider();

      // Then
      assertNotNull(instance, "instance should not be null");
    }
  }

  /** Tests for the supportedFileExtension() method. */
  @Nested
  @DisplayName("supportedFileExtension() method")
  class SupportedFileExtensionMethod {

    /** Tests for the supportedFileExtension method. */
    SupportedFileExtensionMethod() {}

    /** Verifies that supportedFileExtension returns yaml extension. */
    @Test
    @Tag("normal")
    @DisplayName("should return yaml extension when called")
    void shouldReturnYamlExtension_whenCalled() {
      // When
      final var result = provider.supportedFileExtension();

      // Then
      assertEquals(new FileExtension("yaml"), result, "should return yaml extension");
    }
  }

  /** Tests for the parse() method. */
  @Nested
  @DisplayName("parse(Path) method")
  class ParseMethod {

    /** Tests for the parse method. */
    ParseMethod() {}

    /**
     * Verifies that parse returns dataset when valid YAML file exists.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should return dataset when valid YAML file exists")
    void shouldReturnDataSet_whenValidYamlFileExists(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "users.yaml",
          """
          - ID: 1
            NAME: John
            EMAIL: john@example.com
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      assertAll(
          "dataset should contain parsed table",
          () -> assertNotNull(result, "result should not be null"),
          () -> assertEquals(1, result.getTables().size(), "should have one table"));

      final var table = result.getTables().getFirst();
      assertAll(
          "table should have correct structure",
          () -> assertEquals("users", table.getName().value(), "should have correct table name"),
          () -> assertEquals(3, table.getColumns().size(), "should have 3 columns"),
          () -> assertEquals(1, table.getRowCount(), "should have 1 row"));
    }

    /**
     * Verifies that parse handles multiple YAML files.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple YAML files when multiple files exist")
    void shouldHandleMultipleFiles_whenMultipleFilesExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "users.yaml",
          """
          - ID: 1
            NAME: John
          """);
      createYamlFile(
          tempDir,
          "orders.yaml",
          """
          - ID: 1
            USER_ID: 1
            AMOUNT: 100
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      assertEquals(2, result.getTables().size(), "should have two tables");
    }

    /**
     * Verifies that parse handles NULL values correctly.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle NULL values when null exists in YAML")
    void shouldHandleNullValues_whenNullExistsInYaml(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "users.yaml",
          """
          - ID: 1
            NAME: null
            EMAIL: john@example.com
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var nameValue = row.getValue(new ColumnName("NAME"));

      assertEquals(CellValue.NULL, nameValue, "null cell should be NULL");
    }

    /**
     * Verifies that parse handles tilde null notation.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should handle tilde null notation when ~ is used")
    void shouldHandleTildeNull_whenTildeIsUsed(final @TempDir Path tempDir) throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "users.yaml",
          """
          - ID: 1
            NAME: ~
            EMAIL: john@example.com
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var nameValue = row.getValue(new ColumnName("NAME"));

      assertEquals(CellValue.NULL, nameValue, "tilde should be treated as NULL");
    }

    /**
     * Verifies that parse handles multiple rows.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should handle multiple rows when list contains multiple items")
    void shouldHandleMultipleRows_whenListContainsMultipleItems(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "users.yaml",
          """
          - ID: 1
            NAME: Alice
          - ID: 2
            NAME: Bob
          - ID: 3
            NAME: Charlie
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      assertEquals(3, table.getRowCount(), "should have 3 rows");
    }

    /**
     * Verifies that parse handles YAML comments.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should ignore YAML comments")
    void shouldIgnoreYamlComments_whenCommentsExist(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "users.yaml",
          """
          # This is a comment
          - ID: 1  # inline comment
            NAME: John
            # Another comment
            EMAIL: john@example.com
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      assertEquals(1, table.getRowCount(), "should have 1 row");
      assertEquals(3, table.getColumns().size(), "should have 3 columns");
    }

    /**
     * Verifies that parse converts numeric values to strings.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should convert numeric values to strings")
    void shouldConvertNumericValues_toStrings(final @TempDir Path tempDir) throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "products.yaml",
          """
          - ID: 1
            PRICE: 99.99
            QUANTITY: 10
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var priceValue = row.getValue(new ColumnName("PRICE"));
      final var quantityValue = row.getValue(new ColumnName("QUANTITY"));

      assertAll(
          "numeric values should be converted to strings",
          () -> assertNotNull(priceValue.value(), "price value should not be null"),
          () ->
              assertTrue(
                  priceValue.value() != null && priceValue.value().toString().contains("99.99"),
                  "price should be converted to string"),
          () -> assertNotNull(quantityValue.value(), "quantity value should not be null"),
          () ->
              assertEquals(
                  "10",
                  quantityValue.value() != null ? quantityValue.value().toString() : "",
                  "quantity should be string"));
    }

    /**
     * Verifies that parse throws exception when file is empty array.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when YAML list is empty")
    void shouldThrowException_whenYamlListIsEmpty(final @TempDir Path tempDir) throws IOException {
      // Given
      createYamlFile(tempDir, "users.yaml", "[]");

      // When & Then
      final var exception =
          assertThrows(
              DataSetLoadException.class,
              () -> provider.parse(tempDir),
              "should throw DataSetLoadException");

      final var message = exception.getMessage();
      assertTrue(
          message != null && message.contains("no rows"),
          "exception message should mention no rows");
    }

    /**
     * Verifies that parse throws exception when YAML is invalid.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("error")
    @DisplayName("should throw exception when YAML is invalid")
    void shouldThrowException_whenYamlIsInvalid(final @TempDir Path tempDir) throws IOException {
      // Given
      createYamlFile(tempDir, "users.yaml", "invalid: yaml: structure: [");

      // When & Then
      assertThrows(
          DataSetLoadException.class,
          () -> provider.parse(tempDir),
          "should throw DataSetLoadException for invalid YAML");
    }

    /**
     * Verifies that parse throws exception when path is not a directory.
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
      final var file = tempDir.resolve("notadirectory.yaml");
      Files.writeString(file, "[]");

      // When & Then
      assertThrows(
          DataSetLoadException.class,
          () -> provider.parse(file),
          "should throw DataSetLoadException when path is a file");
    }
  }

  /** Tests for template expression processing. */
  @Nested
  @DisplayName("template expression processing")
  class TemplateExpressionProcessing {

    /** Tests for template expression processing. */
    TemplateExpressionProcessing() {}

    /**
     * Verifies that parse resolves UUID template expressions.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should resolve UUID template expression when ${uuid} is used")
    void shouldResolveUuidExpression_whenUuidTemplateUsed(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "users.yaml",
          """
          - ID: "${uuid}"
            NAME: John
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var idString = (String) row.getValue(new ColumnName("ID")).value();

      assertAll(
          "UUID expression should be resolved",
          () -> assertNotNull(idString, "ID value should not be null"),
          () ->
              assertFalse(
                  idString != null && idString.contains("${uuid}"),
                  "should not contain template expression"),
          () ->
              assertTrue(
                  idString != null
                      && idString.matches(
                          "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"),
                  "should be a valid UUID format"));
    }

    /**
     * Verifies that parse resolves sequence template expressions.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("normal")
    @DisplayName("should resolve sequence template expression when ${sequence} is used")
    void shouldResolveSequenceExpression_whenSequenceTemplateUsed(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "users.yaml",
          """
          - ID: "${sequence}"
            NAME: Alice
          - ID: "${sequence}"
            NAME: Bob
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var rows = table.getRows();

      assertAll(
          "sequence expressions should be resolved incrementally",
          () ->
              assertEquals(
                  "1",
                  rows.getFirst().getValue(new ColumnName("ID")).value(),
                  "first row should have sequence value 1"),
          () ->
              assertEquals(
                  "2",
                  rows.get(1).getValue(new ColumnName("ID")).value(),
                  "second row should have sequence value 2"));
    }

    /**
     * Verifies that parse passes through numeric values without modification.
     *
     * @param tempDir temporary directory for test files
     * @throws IOException if file operations fail
     */
    @Test
    @Tag("edge-case")
    @DisplayName("should pass through numeric values when no template expression present")
    void shouldPassThroughNumericValues_whenNoTemplateExpression(final @TempDir Path tempDir)
        throws IOException {
      // Given
      createYamlFile(
          tempDir,
          "products.yaml",
          """
          - ID: 42
            PRICE: 99.99
            NAME: "${uuid}"
          """);

      // When
      final var result = provider.parse(tempDir);

      // Then
      final var table = result.getTables().getFirst();
      final var row = table.getRows().getFirst();
      final var nameString = (String) row.getValue(new ColumnName("NAME")).value();

      assertAll(
          "numeric values should be passed through unchanged",
          () ->
              assertEquals(
                  "42",
                  row.getValue(new ColumnName("ID")).value(),
                  "integer should be converted to string"),
          () ->
              assertEquals(
                  "99.99",
                  row.getValue(new ColumnName("PRICE")).value(),
                  "decimal should be converted to string"),
          () ->
              assertFalse(
                  nameString != null && nameString.contains("${uuid}"),
                  "UUID template should be resolved"));
    }
  }

  /**
   * Creates a YAML file with the specified content.
   *
   * @param dir the directory to create the file in
   * @param fileName the file name
   * @param content the YAML content
   * @throws IOException if file creation fails
   */
  private static void createYamlFile(final Path dir, final String fileName, final String content)
      throws IOException {
    Files.writeString(dir.resolve(fileName), content);
  }
}
