package io.github.seijikohara.dbtester.api.export;

/**
 * Defines how LOB (Large Object) columns are handled during export.
 *
 * <p>BLOB and CLOB columns require special handling because they can contain large amounts of data
 * that may not be suitable for text-based export formats.
 *
 * @see ExportConfiguration
 */
public enum LobHandling {

  /**
   * Export LOB values as Base64-encoded strings.
   *
   * <p>The exported value includes the {@code [BASE64]} prefix for compatibility with the import
   * format. This allows round-trip export and import of binary data.
   */
  BASE64,

  /**
   * Omit LOB columns from export.
   *
   * <p>Columns containing BLOB or CLOB values are excluded from the exported data. Use this option
   * when binary data is not needed or to reduce export file size.
   */
  OMIT
}
