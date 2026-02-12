/**
 * Test context abstraction for framework-agnostic test execution.
 *
 * <p><strong>API Category: Extension API</strong> — This package is intended for framework
 * integrators building custom testing extensions. Test authors typically do not interact with this
 * package directly.
 *
 * <p>This package provides the {@link io.github.seijikohara.dbtester.api.context.TestContext}
 * record which captures the essential context of a running test, independent of any specific
 * testing framework.
 */
@NullMarked
package io.github.seijikohara.dbtester.api.context;

import org.jspecify.annotations.NullMarked;
