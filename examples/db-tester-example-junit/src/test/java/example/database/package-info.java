/**
 * Contains database-specific integration tests validating cross-vendor compatibility.
 *
 * <p>Each subpackage targets a specific database:
 *
 * <ul>
 *   <li>{@link example.database.derby} — Apache Derby embedded database
 *   <li>{@link example.database.hsqldb} — HSQLDB (HyperSQL Database)
 *   <li>{@link example.database.mssql} — Microsoft SQL Server via Testcontainers
 *   <li>{@link example.database.mysql} — MySQL via Testcontainers
 *   <li>{@link example.database.oracle} — Oracle Database via Testcontainers
 *   <li>{@link example.database.pgsql} — PostgreSQL via Testcontainers
 * </ul>
 */
@NullMarked
package example.database;

import org.jspecify.annotations.NullMarked;
