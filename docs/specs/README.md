# DB Tester Technical Specifications

This directory contains technical specifications for the DB Tester framework.

---

## Specification Documents

| Document | Description |
|----------|-------------|
| [01-overview.md](01-overview) | Framework purpose and key concepts |
| [02-architecture.md](02-architecture) | Module structure and dependencies |
| [03-public-api.md](03-public-api) | Annotations and configuration classes |
| [04-configuration.md](04-configuration) | Configuration options and conventions |
| [05-data-formats.md](05-data-formats) | CSV and TSV file structure and parsing |
| [06-database-operations.md](06-database-operations) | Supported operations and execution flow |
| [07-test-frameworks.md](07-test-frameworks) | JUnit, Spock, and Kotest integration |
| [08-spi.md](08-spi) | Service Provider Interface extension points |
| [09-error-handling.md](09-error-handling) | Error messages and exception types |

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
| `REFRESH` | Upsert (insert or update) |
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
