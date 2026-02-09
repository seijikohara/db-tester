package io.github.seijikohara.dbtester.api.export;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import io.github.seijikohara.dbtester.api.config.DataFormat;
import java.nio.file.Path;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/** Unit tests for {@link DataSetExporter}. */
@DisplayName("DataSetExporter")
class DataSetExporterTest {

  /** Tests for the DataSetExporter class. */
  DataSetExporterTest() {}

  /** Tests for the export() method. */
  @Nested
  @DisplayName("export() method")
  class ExportMethod {

    /** Tests for the export method. */
    ExportMethod() {}

    /**
     * Verifies that export throws IllegalArgumentException when AUTO format is used.
     *
     * @param tempDir temporary directory for test files
     */
    @Test
    @Tag("error")
    @DisplayName("should throw IllegalArgumentException when AUTO format is used")
    void shouldThrowIllegalArgumentException_whenAutoFormatIsUsed(final @TempDir Path tempDir) {
      // Given
      final var dataSource = mock(DataSource.class);
      final var tableNames = List.of("USERS");

      // When & Then
      final var exception =
          assertThrows(
              IllegalArgumentException.class,
              () -> DataSetExporter.export(dataSource, tableNames, tempDir, DataFormat.AUTO),
              "should throw IllegalArgumentException for AUTO format");

      final var message = exception.getMessage();
      assertNotNull(message, "message should not be null");
      assertTrue(
          message.contains("AUTO format cannot be used for export"),
          "should mention AUTO format is not supported for export");
    }
  }
}
