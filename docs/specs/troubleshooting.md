---
title: "Troubleshooting - DB Tester"
description: "Practical troubleshooting guide with symptom-diagnosis-solution workflows."
---

# Troubleshooting

This guide provides practical solutions for common issues when using DB Tester.
For detailed exception specifications, see [Error Handling](error-handling).

## Quick Diagnosis

Use this checklist to identify your issue category:

| Symptom | Category | Jump to |
|---------|----------|---------|
| "Dataset directory not found" | Data Loading | [DataSetLoadException](#datasetloadexception) |
| "File is empty" or parse errors | Data Loading | [DataSetLoadException](#datasetloadexception) |
| "Table name conflict detected in AUTO format mode" | Data Loading | [DataSetLoadException](#datasetloadexception) |
| "Assertion failed: N differences" | Validation | [ValidationException](#validationexception) |
| "No default data source registered" | Configuration | [DataSource Issues](#datasource-issues) |
| Test runs slowly | Performance | [Performance Optimization](#performance-optimization) |
| Unexpected test failures | Common Mistakes | [Common Mistakes](#common-mistakes) |

---

## DataSetLoadException

### Directory Not Found on Classpath

**Symptom**:
```
Dataset directory not found on classpath: 'com/example/UserRepositoryTest'
Expected location: src/test/resources/com/example/UserRepositoryTest
```

**Diagnosis**:
1. Check if the directory exists at `src/test/resources/{package}/{TestClassName}/`
2. Verify the package path uses forward slashes
3. Confirm the test class name matches exactly (case-sensitive)

**Solution**:
```bash
# Create the directory structure
mkdir -p src/test/resources/com/example/UserRepositoryTest
```

::: tip Convention
The directory path follows `{package}/{TestClassName}/` by default.
To customize, configure `baseDirectory` in [Configuration](configuration).
:::

### No Supported Files Found

**Symptom**:
```
Dataset directory exists but contains no supported data files: '/path/to/datasets'
Supported file extensions: [.csv, .tsv, .json, .yaml]
Hint: Add at least one data file (for example, TABLE_NAME.csv)...
Found files: [README.txt, notes.md]
```

The `Found files` line lists all files in the directory to help diagnose the issue. This line is omitted when the directory is empty.

**Diagnosis**:
1. Check the `Found files` list for files with incorrect extensions
2. Verify file extensions match the configured `dataFormat`
3. Confirm files are not hidden (no `.` prefix) and are in the correct directory level

**Solution**:

| dataFormat Setting | Expected Extension |
|--------------------|-------------------|
| `DataFormat.AUTO` (default) | `.csv`, `.tsv`, `.json`, `.yaml` |
| `DataFormat.CSV` | `.csv` |
| `DataFormat.TSV` | `.tsv` |
| `DataFormat.JSON` | `.json` |
| `DataFormat.YAML` | `.yaml` |

See [Data Formats](data-formats) for file format details.

### Empty File Error

**Symptom**:
```
File is empty: /path/to/USERS.csv
```

**Solution**:
Add at least a header row and one data row:

```csv
ID,NAME,EMAIL
1,Alice,alice@example.com
```

### Parse Failure

**Symptom**:
```
Failed to parse file: /path/to/USERS.csv
```

**Diagnosis**:
1. Check for unescaped special characters (commas, quotes)
2. Verify consistent column count across rows
3. Check file encoding (UTF-8 recommended)

**Solution**:
- Escape commas in values: `"value, with comma"`
- Escape quotes: `"value ""with quotes"""`
- Use TSV format if data contains many commas

### Table Name Conflict in AUTO Format Mode

**Symptom**:
```
Table name conflict detected in AUTO format mode.
The following table names are defined in multiple files with different formats:

  Table 'USERS':
    - USERS.csv
    - USERS.yaml

Each table name must be unique across all file formats in a directory.
To resolve, remove duplicate files or specify a concrete format:
  DataFormat.CSV, DataFormat.TSV, DataFormat.JSON, or DataFormat.YAML
```

**Diagnosis**:
When using `DataFormat.AUTO` (the default), the framework loads all supported file formats from the dataset directory. If the same table name appears in multiple files with different extensions, the framework cannot determine which file to use.

**Solution**:

| Approach | Action |
|----------|--------|
| Remove duplicates | Keep only one file per table name (e.g., remove `USERS.yaml` if `USERS.csv` exists) |
| Specify concrete format | Set `DataFormat.CSV`, `DataFormat.TSV`, `DataFormat.JSON`, or `DataFormat.YAML` in `ConventionSettings` |

```java
// Option 1: Remove the duplicate file from the dataset directory

// Option 2: Specify a concrete format
var conventions = ConventionSettings.builder()
    .dataFormat(DataFormat.CSV)
    .build();
```

See [Data Formats - Automatic Format Detection](data-formats#automatic-format-detection) for details.

### Load Order File Error

**Symptom**:
```
Failed to read load order file: /path/to/load-order.txt
```

**Diagnosis**:
When using `TableOrderingStrategy.LOAD_ORDER_FILE`, the `load-order.txt` file is required.

**Solution**:
Create `load-order.txt` in your dataset directory:

```
PARENT_TABLE
CHILD_TABLE
GRANDCHILD_TABLE
```

See [Data Formats - Load Order](data-formats#load-order) for details.

---

## ValidationException

### Understanding YAML Output

When validation fails, DB Tester outputs structured YAML:

```yaml
Assertion failed: 2 differences in USERS
summary:
  status: FAILED
  total_differences: 2
tables:
  USERS:
    differences:
      - path: row_count
        expected: 3
        actual: 2
      - path: "row[0].EMAIL"
        expected: john@example.com
        actual: jane@example.com
```

### Row Count Mismatch

**Symptom**:
```yaml
- path: row_count
  expected: 3
  actual: 2
```

**Diagnosis**:
1. Check `[Scenario]` column filtering
2. Verify all expected rows are in the CSV
3. Check if test logic deleted rows unexpectedly

**Solution**:

| Cause | Action |
|-------|--------|
| Missing `[Scenario]` value | Add test method name to `[Scenario]` column |
| Wrong scenario name | Match exactly with test method name |
| Extra rows filtered | Remove `[Scenario]` column to load all rows |

See [Data Formats - Scenario Filtering](data-formats#scenario-filtering).

### Cell Value Mismatch

**Symptom**:
```yaml
- path: "row[0].EMAIL"
  expected: john@example.com
  actual: jane@example.com
```

**Diagnosis**:
1. Compare expected CSV with actual database state
2. Check if test logic updated the value
3. Verify row ordering matches

**Solution**:

| Cause | Action |
|-------|--------|
| Row order differs | Use `rowOrdering = RowOrdering.UNORDERED` |
| Timestamp precision | Check comparison strategy for date columns |
| Floating point | Values within epsilon (1e-6) match automatically |

### Using excludeColumns and columnStrategies

**Precedence Rule**: `excludeColumns` takes priority over `columnStrategies`.

```java
@ExpectedDataSet(sources = @DataSetSource(
    excludeColumns = {"CREATED_AT"},  // Excluded first
    columnStrategies = {
        @ColumnStrategy(name = "UPDATED_AT", strategy = Strategy.IGNORE)
    }
))
```

In this example, `CREATED_AT` is excluded entirely.
`UPDATED_AT` uses the IGNORE strategy for comparison.

See [Public API](public-api) for annotation details.

---

## DataSource Issues

### Default DataSource Not Registered

**Symptom**:
```
No default data source registered
```

**Diagnosis**:
1. Check `@BeforeAll` method signature includes `ExtensionContext`
2. Verify `registerDefault()` is called
3. Confirm no exception occurred during registration

**Solution**:

::: code-group

```java [JUnit]
@BeforeAll
static void setUp(ExtensionContext context) throws SQLException {
    var dataSource = new JdbcDataSource();
    dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
    DatabaseTestExtension.getRegistry(context).registerDefault(dataSource);
}
```

```groovy [Spock]
def setupSpec() {
    def dataSource = new JdbcDataSource()
    dataSource.setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    registry.registerDefault(dataSource)
}
```

```kotlin [Kotest]
init {
    extensions(DatabaseTestExtension(registryProvider = { registry }))
}

override suspend fun beforeSpec(spec: Spec) {
    val dataSource = JdbcDataSource().apply {
        setURL("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    }
    registry.registerDefault(dataSource)
}
```

:::

### Named DataSource Not Found

**Symptom**:
```
No data source registered for name: secondary_db
```

**Solution**:
Register the named DataSource:

```java
registry.register("secondary_db", secondaryDataSource);
```

Then reference it in annotations:

```java
@DataSet(sources = @DataSetSource(dataSourceName = "secondary_db"))
```

---

## Performance Optimization

### Large Dataset Optimization

**Symptom**: Tests with many rows run slowly.

**Solutions**:

| Optimization | Impact | How |
|--------------|--------|-----|
| Use `RowOrdering.ORDERED` | Fastest comparison (O(n)) | Set in `@ExpectedDataSet` |
| Use `TRUNCATE_INSERT` | Faster than `CLEAN_INSERT` | Set in `@DataSet` |
| Create `load-order.txt` | Skip metadata discovery | Add file to dataset directory |
| Reduce dataset size | Fewer rows to process | Use `[Scenario]` filtering |

::: warning RowOrdering Performance
`RowOrdering.UNORDERED` performs O(n*m) comparison in worst case.
Use `ORDERED` when row order is predictable.
:::

See [Database Operations](database-operations) for operation details.

### Connection Pool Configuration

**Symptom**: Connection timeout or pool exhaustion.

**Note**: Connection pooling is external to DB Tester.
Configure your connection pool (HikariCP, c3p0, etc.) appropriately.

**Recommendations**:
- Set appropriate `maximumPoolSize` for parallel test execution
- Configure `connectionTimeout` for slow database connections
- Use `DB_CLOSE_DELAY=-1` for H2 in-memory databases

### Memory Management

**Symptom**: OutOfMemoryError with large datasets.

**Solutions**:
1. Split large CSVs into smaller files per scenario
2. Use `[Scenario]` column to load only relevant rows
3. Increase JVM heap size for tests: `-Xmx512m`

---

## Common Mistakes

### Classpath Placement Error

**Mistake**: Placing dataset files outside `src/test/resources`.

**Correct Structure**:
```
src/test/resources/
└── com/example/UserRepositoryTest/
    ├── USERS.csv
    └── expected/
        └── USERS.csv
```

### Scenario Column Name Mismatch

**Mistake**: Using different scenario marker than configured.

**Default**: `[Scenario]` column

**Custom Configuration**:
```java
Configuration.builder()
    .conventions(ConventionSettings.builder()
        .scenarioMarker("[TestCase]")  // Custom marker
        .build())
    .build();
```

### Extension Mismatch

**Mistake**: Using unsupported file extensions with a concrete `DataFormat`.

With `DataFormat.AUTO` (the default), the framework accepts all supported extensions (`.csv`, `.tsv`, `.json`, `.yaml`). When a concrete format is configured, only files with the matching extension are loaded.

**Solution**:
Use `DataFormat.AUTO` (default) to load all supported formats, or configure the matching format:
```java
ConventionSettings.builder()
    .dataFormat(DataFormat.TSV)
    .build();
```

Or rename files to match the configured format extension.

### Expectation Suffix Mismatch

**Mistake**: Expected files not in `expected/` subdirectory.

**Default**: `expected/` suffix for expectation datasets.

**Custom Configuration**:
```java
ConventionSettings.builder()
    .expectationSuffix("verify/")  // Custom suffix
    .build();
```

See [Configuration](configuration) for all settings.

### Table Name Case Sensitivity

**Mistake**: CSV filename case does not match table name.

**Example**:
- Table created as `USERS` (H2 uppercase)
- CSV named `users.csv` (lowercase)

**Solution**: Match the exact case of your database table name.
H2 converts unquoted identifiers to uppercase.

### Foreign Key Order

**Mistake**: Inserting child records before parent records.

**Solution**: Create `load-order.txt` in your dataset directory:

```
PARENT
CHILD
GRANDCHILD
```

Then configure the table ordering strategy:

```java
@DataSet(tableOrdering = TableOrderingStrategy.LOAD_ORDER_FILE)
```

---

## Debugging Workflow

### Step 1: Enable DEBUG Logging

```properties
# application.properties or logback.xml
logging.level.io.github.seijikohara.dbtester=DEBUG
```

### Step 2: Check Dataset Loading

DEBUG output shows:
- Which files are being loaded
- Table order determination
- Row filtering by scenario

### Step 3: Verify Database State

Query the database directly after `@DataSet` preparation:

```java
@Test
@DataSet
void debugTest() throws SQLException {
    try (var conn = dataSource.getConnection();
         var stmt = conn.createStatement();
         var rs = stmt.executeQuery("SELECT * FROM USERS")) {
        while (rs.next()) {
            System.out.println(rs.getString("NAME"));
        }
    }
}
```

### Step 4: Compare Expected vs Actual

If validation fails, the YAML output shows exact differences.
Use this to identify whether the issue is in:
- Expected data (CSV)
- Test logic
- Database state

---

## Related Documentation

- [Error Handling](error-handling) - Exception specifications
- [Configuration](configuration) - Framework settings
- [Data Formats](data-formats) - Data format structure
- [Database Operations](database-operations) - Operation types
- [Public API](public-api) - Annotation reference
