/**
 * Database-specific integration tests demonstrating framework compatibility.
 *
 * <p>This package contains integration tests for various database management systems, validating
 * that the framework works correctly across different database vendors and configurations.
 *
 * <p>Each subpackage represents a specific database vendor:
 *
 * <ul>
 *   <li>{@link example.database.derby} - Apache Derby embedded database tests
 *   <li>{@link example.database.hsqldb} - HSQLDB (HyperSQL Database) tests
 *   <li>{@link example.database.mssql} - Microsoft SQL Server tests using Testcontainers
 *   <li>{@link example.database.mysql} - MySQL database tests using Testcontainers
 *   <li>{@link example.database.oracle} - Oracle Database tests using Testcontainers
 *   <li>{@link example.database.pgsql} - PostgreSQL database tests using Testcontainers
 * </ul>
 *
 * <p>These tests serve as:
 *
 * <ul>
 *   <li>Compatibility validation for different database vendors
 *   <li>Demonstration of framework setup for specific databases
 *   <li>Smoke tests for database-specific features
 *   <li>Reference implementations for production use
 * </ul>
 */
@NullMarked
package example.database;

import org.jspecify.annotations.NullMarked;
