# Comprehensive Project Review Design

## Objective

Full review of the DB Tester project covering committed code on main and uncommitted working tree changes. Goal: identify inconsistencies, API design issues, documentation gaps, test coverage gaps, and best practices violations for overall quality improvement.

## Approach: Layer-based Deep Review (6 Areas + Cross-cutting)

### Area 1: Public API Design Review

- Annotation design consistency: `@DataSet`, `@ExpectedDataSet`, `@DataSetSource`, `@ExportDataSet`, `@ColumnStrategy`
- Configuration API usability and immutability
- Operation enum completeness and naming
- Exception hierarchy appropriateness and message clarity
- SPI interface extensibility and stability
- Domain types (TableName, ColumnName, etc.) design quality
- Programmatic API (DatabasePreparation, DatabaseAssertion) usability

### Area 2: Internal Implementation Review

- JDBC operations correctness and error handling (especially new UpsertExecutor)
- ExpectationVerifier comparison logic
- Data format parsers robustness (CSV/TSV/JSON/YAML)
- Lifecycle management (preparation/expectation/export)
- RefreshExecutor to UpsertExecutor migration impact

### Area 3: Documentation/Specification Consistency

- Specs (docs/specs/) vs actual implementation alignment
- README/Getting Started accuracy
- Javadoc/KDoc/Groovydoc quality against documentation.md standards
- Spring configuration metadata completeness
- Example module coverage

### Area 4: Test Quality

- Test coverage gaps across modules
- Edge case and error case coverage
- Test independence and reproducibility
- Test naming and organization consistency

### Area 5: Build/CI/Dependencies

- Gradle configuration health
- Dependency appropriateness and currency
- JPMS configuration correctness
- CI workflow reliability

### Area 6: Framework Consistency

- JUnit/Spock/Kotest API uniformity
- Spring Boot Starter configuration uniformity
- Example module coverage consistency

### Cross-cutting: Best Practices (Codex-assisted)

- Comparison with DBUnit, Database Rider patterns
- Java test library design conventions
- JUnit 6 Extension API best practices
- Spring Boot 4 auto-configuration patterns

## Execution Strategy

- Run all 6 areas in parallel using specialized agents
- Use Codex for official documentation reference in cross-cutting analysis
- Consolidate findings with severity and impact prioritization
- Present structured report with concrete improvement proposals
