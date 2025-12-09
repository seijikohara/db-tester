/**
 * Dataset loader implementations and convention-based file resolution.
 *
 * <p>This package provides internal implementations for loading datasets from resources, including:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.internal.loader.DefaultDataSetLoaderProvider} -
 *       implements the {@link io.github.seijikohara.dbtester.api.spi.DataSetLoaderProvider} SPI
 *   <li>{@link io.github.seijikohara.dbtester.internal.loader.TestClassNameBasedDataSetLoader} -
 *       convention-based loader that resolves dataset files based on test class and method names
 * </ul>
 *
 * <p><strong>Note:</strong> This is an internal package and is not part of the public API. Classes
 * in this package may change without notice.
 */
@NullMarked
package io.github.seijikohara.dbtester.internal.loader;

import org.jspecify.annotations.NullMarked;
