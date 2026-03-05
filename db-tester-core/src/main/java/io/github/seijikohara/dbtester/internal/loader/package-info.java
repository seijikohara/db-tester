/**
 * Provides dataset loader implementations with convention-based file resolution.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider} —
 *       implements the {@link io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider} SPI
 *   <li>{@link io.github.seijikohara.dbtester.internal.loader.TestClassNameBasedDataSetLoader} —
 *       resolves dataset files based on test class and method names
 * </ul>
 *
 * <p><strong>Internal package.</strong> Not part of the public API. Classes may change without
 * notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.loader;

import org.jspecify.annotations.NullMarked;
