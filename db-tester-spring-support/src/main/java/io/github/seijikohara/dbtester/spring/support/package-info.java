/**
 * Provides Spring support utilities for the DB Tester framework.
 *
 * <p>Spring Boot starter modules use these utilities to share DataSource registration logic.
 *
 * <ul>
 *   <li>{@link io.github.seijikohara.dbtester.spring.support.DataSourceRegistrarSupport} — common
 *       logic for DataSource registration
 *   <li>{@link io.github.seijikohara.dbtester.spring.support.PrimaryBeanResolver} — utility for
 *       resolving primary bean status
 * </ul>
 *
 * @see io.github.seijikohara.dbtester.api.config.DataSourceRegistry
 */
@NullMarked
package io.github.seijikohara.dbtester.spring.support;

import org.jspecify.annotations.NullMarked;
