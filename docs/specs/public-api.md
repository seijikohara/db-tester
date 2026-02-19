---
title: "Public API Reference - DB Tester"
description: "Comprehensive API reference for DB Tester annotations, configuration, and interfaces."
---

# DB Tester Specification - Public API

The `db-tester-api` module exports packages organized into three layers by intended audience:

| Layer | Packages | Audience | Stability |
|-------|----------|----------|-----------|
| **User API** | `annotation`, `config`, `operation`, `exception`, `preparation` | All users | Stable |
| **Advanced API** | `assertion`, `export`, `domain`, `dataset` | Users with programmatic needs | Stable |
| **Extension SPI** | `spi`, `loader`, `context`, `scenario` | Framework integrators | Evolving SPI |

## API Reference Pages

| Page | Description |
|------|-------------|
| [Annotations](annotations) | `@DataSet`, `@ExpectedDataSet`, `@DataSetSource`, `@ColumnStrategy`, `Strategy`, `RowOrdering` |
| [Dataset Interfaces](dataset-interfaces) | `TableSet`, `Table`, `Row`, and domain value objects (`CellValue`, `TableName`, `ColumnName`, `ComparisonStrategy`) |
| [Programmatic API](assertion-api) | `DatabaseAssertion`, `DatabaseQueryAssertion`, `DataSetExporter`, `DatabasePreparation` |
| [Exceptions](exceptions) | Exception hierarchy, default values reference, and column comparison precedence |

## Related Specifications

- [Getting Started](getting-started) - Quick start guide
- [Overview](overview) - Framework introduction
- [Configuration](configuration) - Configuration classes
- [Database Operations](database-operations) - Operation enum details
- [SPI](spi) - Service Provider Interface extension points
- [Error Handling](error-handling) - Error messages and exception types
