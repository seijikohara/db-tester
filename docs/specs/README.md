# DB Tester Technical Specifications

This directory contains technical specifications for the DB Tester framework.

---

## Specification Documents

| Document | Description |
|----------|-------------|
| [01-OVERVIEW.md](01-OVERVIEW) | Framework purpose and key concepts |
| [02-ARCHITECTURE.md](02-ARCHITECTURE) | Module structure and dependencies |
| [03-PUBLIC-API.md](03-PUBLIC-API) | Annotations and configuration classes |
| [04-CONFIGURATION.md](04-CONFIGURATION) | Configuration options and conventions |
| [05-DATA-FORMATS.md](05-DATA-FORMATS) | CSV/TSV file structure and parsing |
| [06-DATABASE-OPERATIONS.md](06-DATABASE-OPERATIONS) | Supported operations and execution flow |
| [07-TEST-FRAMEWORKS.md](07-TEST-FRAMEWORKS) | JUnit and Spock integration |
| [08-SPI.md](08-SPI) | Service Provider Interface extension points |
| [09-ERROR-HANDLING.md](09-ERROR-HANDLING) | Error messages and exception types |

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

## Quick Reference

### Annotations

| Annotation | Purpose |
|------------|---------|
| `@Preparation` | Execute datasets before test |
| `@Expectation` | Verify database state after test |
| `@DataSet` | Configure individual dataset parameters |

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
| Preparation operation | CLEAN_INSERT |

### Directory Convention

```
src/test/resources/
└── {package}/{TestClassName}/
    ├── TABLE.csv           # Preparation data
    └── expected/
        └── TABLE.csv       # Expectation data
```
