# DB Tester Technical Specifications

This directory contains technical specifications for the DB Tester framework.

---

## Specification Documents

| Document | Description |
|----------|-------------|
| [overview.md](overview) | Framework purpose and key concepts |
| [architecture.md](architecture) | Module structure and dependencies |
| [public-api.md](public-api) | Annotations and configuration classes |
| [configuration.md](configuration) | Configuration options and conventions |
| [data-formats.md](data-formats) | CSV and TSV file structure and parsing |
| [database-operations.md](database-operations) | Supported operations and execution flow |
| [test-frameworks.md](test-frameworks) | JUnit, Spock, and Kotest integration |
| [spi.md](spi) | Service Provider Interface extension points |
| [error-handling.md](error-handling) | Error messages and exception types |

---

## Reading Order

For comprehensive understanding, read the specifications in this order:

1. **Overview** - Understand the framework purpose and key concepts
2. **Architecture** - Learn the module structure and design patterns
3. **Public API** - Review available annotations and interfaces
4. **Configuration** - Understand configuration options
5. **Data Formats** - Learn dataset file structure
6. **Database Operations** - Understand supported operations
7. **Test Frameworks** - Learn framework-specific integration
8. **SPI** - Explore extension points
9. **Error Handling** - Understand error messages and debugging

---

## Modules

| Module | Description |
|--------|-------------|
| `db-tester-bom` | Bill of Materials for version management |
| `db-tester-api` | Public API (annotations, configuration, SPI interfaces) |
| `db-tester-core` | Core implementation (JDBC operations, format parsing) |
| `db-tester-junit` | JUnit Jupiter extension |
| `db-tester-spock` | Spock extension |
| `db-tester-kotest` | Kotest AnnotationSpec extension |
| `db-tester-junit-spring-boot-starter` | Spring Boot auto-configuration for JUnit |
| `db-tester-spock-spring-boot-starter` | Spring Boot auto-configuration for Spock |
| `db-tester-kotest-spring-boot-starter` | Spring Boot auto-configuration for Kotest |

---

## Quick Reference

### Annotations

| Annotation | Purpose |
|------------|---------|
| `@DataSet` | Execute datasets before test |
| `@ExpectedDataSet` | Verify database state after test |
| `@DataSetSource` | Configure individual dataset parameters |

### Database Operations

| Operation | Description |
|-----------|-------------|
| `CLEAN_INSERT` | Delete all then insert (default) |
| `INSERT` | Insert new rows |
| `UPDATE` | Update existing rows |
| `UPSERT` | Upsert (insert or update) |
| `DELETE` | Delete specific rows |
| `DELETE_ALL` | Delete all rows |
| `TRUNCATE_TABLE` | Truncate tables |
| `TRUNCATE_INSERT` | Truncate then insert |
| `NONE` | No operation |

### Configuration Defaults

| Setting | Default Value |
|---------|---------------|
| Base directory | null (classpath-relative) |
| Expectation suffix | `/expected` |
| Scenario marker | `[Scenario]` |
| Data format | CSV |
| Table merge strategy | UNION_ALL |
| DataSet operation | CLEAN_INSERT |

### Directory Convention

```
src/test/resources/
└── {package}/{TestClassName}/
    ├── TABLE.csv           # DataSet data
    └── expected/
        └── TABLE.csv       # ExpectedDataSet data
```
