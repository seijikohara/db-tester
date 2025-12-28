package io.github.seijikohara.dbtester.api.operation;

/**
 * Standard database operations supported by the framework.
 *
 * <p>These operations define how datasets are applied to the database during test preparation. Each
 * operation corresponds to a specific database manipulation strategy, from inserts to upserts and
 * truncations.
 *
 * @see io.github.seijikohara.dbtester.api.dataset.DataSet
 */
public enum Operation {

  /**
   * No-op operation.
   *
   * <p>Leaves the database unchanged. Useful when dataset is only for validation purposes.
   */
  NONE,

  /**
   * Updates existing rows without inserting new ones.
   *
   * <p>Existing rows identified by primary key are updated. Rows not found in the database are
   * ignored.
   */
  UPDATE,

  /**
   * Inserts only new rows.
   *
   * <p>Fails when duplicate rows exist. Use this when database must be empty or when inserting into
   * tables without existing data.
   */
  INSERT,

  /**
   * Upserts rows by updating matches and inserting new entries.
   *
   * <p>Updates existing rows by primary key and inserts rows that do not exist.
   */
  REFRESH,

  /**
   * Deletes only the dataset rows identified by primary key.
   *
   * <p>Removes specific rows from the database based on primary key matching. Other rows remain
   * unchanged.
   */
  DELETE,

  /**
   * Removes all rows from the referenced tables without resetting sequences.
   *
   * <p>Deletes all rows from tables referenced in the dataset. Identity columns and sequences are
   * not reset.
   */
  DELETE_ALL,

  /**
   * Truncates the tables, resetting identity columns where supported.
   *
   * <p>Uses database TRUNCATE command to remove all rows and reset identity columns. Not all
   * databases support sequence reset via truncate.
   */
  TRUNCATE_TABLE,

  /**
   * Deletes all rows before inserting the dataset.
   *
   * <p>Equivalent to DELETE_ALL followed by INSERT. Most common operation for test preparation.
   */
  CLEAN_INSERT,

  /**
   * Truncates the tables before inserting the dataset.
   *
   * <p>Equivalent to TRUNCATE_TABLE followed by INSERT. Use when identity column reset is required.
   */
  TRUNCATE_INSERT
}
