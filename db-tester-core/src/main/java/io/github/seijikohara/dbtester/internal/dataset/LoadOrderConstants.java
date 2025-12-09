package io.github.seijikohara.dbtester.internal.dataset;

/**
 * Constants for load order file handling.
 *
 * <p>This class provides the standard file name for table load order configuration. The load order
 * file specifies the sequence in which tables should be loaded during database operations.
 *
 * <p>This class cannot be instantiated.
 */
public final class LoadOrderConstants {

  /** The name of the load order file. */
  public static final String LOAD_ORDER_FILE = "load-order.txt";

  /** Prevents instantiation. */
  private LoadOrderConstants() {
    throw new UnsupportedOperationException("Utility class cannot be instantiated");
  }
}
