/**
 * Spring support utilities for DB Tester framework.
 *
 * <p>This package provides common utilities for integrating DB Tester with Spring-based
 * applications. The classes in this package are designed to be used by Spring Boot starter modules
 * to eliminate code duplication.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.spring.support.DataSourceRegistrarSupport} - Common
 *       logic for DataSource registration
 *   <li>{@link io.github.seijikohara.dbtester.spring.support.PrimaryBeanResolver} - Utility for
 *       resolving primary bean status
 * </ul>
 *
 * @see io.github.seijikohara.dbtester.api.config.DataSourceRegistry
 */
@NullMarked
package io.github.seijikohara.dbtester.spring.support;

import org.jspecify.annotations.NullMarked;
