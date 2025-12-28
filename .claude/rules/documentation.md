# Technical Writing Standards

This document defines the technical writing standards for the DB Tester project.

## Scope

These standards apply to:

- Javadoc, KDoc, and Groovydoc comments
- Markdown documentation (README, specifications)
- Code comments
- Commit messages

## Language and Style

### Language

Use English for all documentation.

### Voice

Use active voice. Passive voice is acceptable only when the actor is unknown or irrelevant.

| Context | Voice | Example |
|---------|-------|---------|
| Procedures | Imperative | "Register the DataSource." |
| Descriptions | Active | "The method returns a list." |
| Results | Passive (acceptable) | "The connection is closed automatically." |

### Sentence Structure

- Write one idea per sentence.
- Limit sentences to 25 words.
- Limit paragraphs to 5 sentences.

### Word Choice

Use precise technical terms. Avoid vague or subjective language.

**Prohibited**:

| Category | Examples |
|----------|----------|
| Subjective adjectives | modern, powerful, elegant, simple, easy |
| Filler words | very, really, quite, actually, basically |
| Informal contractions | don't, won't, can't, it's, let's |
| Vague quantifiers | some, many, few, several |
| Unnecessary politeness | please, kindly |

**Replace**:

| Avoid | Use |
|-------|-----|
| utilize, leverage | use |
| in order to | to |
| due to the fact that | because |
| at the present time | now |
| a number of | several, specific number |
| etc., and so on | list items or use "such as" |

### Terminology

Define acronyms on first use. Maintain consistent terminology.

| Term | Context |
|------|---------|
| `DataSource` | Java interface (capitalized) |
| data source | Generic concept |

### Punctuation

- Do not use exclamation marks.
- Use serial commas (Oxford comma).
- Use straight quotes, not curly quotes.

## API Documentation

### Javadoc

DocLint enforces Javadoc with `-Xdoclint:all` and `-Werror`.

#### Class Documentation

```java
/**
 * Manages DataSource instances for database testing.
 *
 * <p>This class provides registration and retrieval of DataSource instances
 * used during test preparation and expectation phases.
 *
 * @see DataSource
 * @see DatabaseTestExtension
 */
public final class DataSourceRegistry {
```

**Structure**:

1. Summary sentence (verb in third person singular)
2. Description paragraphs (wrapped in `<p>` tags)
3. `@see` references

**Summary sentence verbs**:

| Verb | Usage |
|------|-------|
| Manages | Classes that control lifecycle |
| Provides | Classes that supply functionality |
| Represents | Value objects and data classes |
| Executes | Classes that perform operations |
| Validates | Classes that check correctness |

#### Method Documentation

```java
/**
 * Registers a DataSource with the specified name.
 *
 * <p>If a DataSource with the same name exists, it is replaced.
 *
 * @param name the unique identifier for the DataSource
 * @param dataSource the DataSource instance to register
 * @throws IllegalArgumentException if name is blank
 */
public void register(String name, DataSource dataSource) {
```

**Structure**:

1. Summary sentence
2. Description paragraphs (if needed)
3. `@param` for each parameter
4. `@return` for non-void methods
5. `@throws` for declared exceptions

**Tag formats**:

| Tag | Format |
|-----|--------|
| `@param` | `@param name the [noun phrase]` |
| `@return` | `@return the [noun phrase]` |
| `@throws` | `@throws ExceptionType if [condition]` |

**Return value patterns**:

| Type | Format |
|------|--------|
| Object | `@return the configuration` |
| Collection | `@return list of table names` |
| Optional | `@return an Optional containing the value, or empty if not found` |
| boolean | `@return true if registered, false otherwise` |
| Nullable | `@return the value, or null if not found` |

#### Field Documentation

```java
/** The registered DataSources indexed by name. */
private final Map<String, DataSource> dataSources;

/** Maximum retry attempts for database connections. */
private static final int MAX_RETRIES = 3;
```

#### Package Documentation

Every package requires `package-info.java`.

```java
/**
 * Database assertion utilities.
 *
 * <p>This package provides classes for comparing expected datasets
 * with actual database state.
 *
 * @see io.github.seijikohara.dbtester.api.annotation
 */
@NullMarked
package io.github.seijikohara.dbtester.api.assertion;

import org.jspecify.annotations.NullMarked;
```

#### Deprecation

```java
/**
 * Loads data from the file.
 *
 * @param file the file to load
 * @deprecated Use {@link #load(Path)} instead. Removed in 2.0.
 */
@Deprecated(since = "1.5", forRemoval = true)
public void load(File file) {
```

#### HTML in Javadoc

| Tag | Usage |
|-----|-------|
| `<p>` | Paragraph separator |
| `<ul>`, `<li>` | Unordered lists |
| `<ol>`, `<li>` | Ordered lists |
| `{@code text}` | Inline code |
| `{@link Class}` | Class reference |
| `<pre>{@code ...}</pre>` | Code blocks |

### KDoc

Follow Javadoc conventions with Kotlin syntax.

```kotlin
/**
 * Manages DataSource instances for database testing.
 *
 * This class provides registration and retrieval of DataSource instances
 * used during test preparation and expectation phases.
 *
 * @see DataSource
 * @see DatabaseTestExtension
 */
class DataSourceRegistry {
```

**Differences from Javadoc**:

- Omit `<p>` tags; use blank lines for paragraphs.
- Use `[ClassName]` for links instead of `{@link ClassName}`.
- Use backticks for inline code instead of `{@code}`.

```kotlin
/**
 * Registers a DataSource with the specified name.
 *
 * If a DataSource with the same name exists, it is replaced.
 *
 * @param name the unique identifier for the DataSource
 * @param dataSource the DataSource instance to register
 * @throws IllegalArgumentException if name is blank
 * @see [DataSourceRegistry]
 */
fun register(name: String, dataSource: DataSource) {
```

### Groovydoc

Follow Javadoc conventions. Groovydoc uses the same syntax as Javadoc.

```groovy
/**
 * Manages DataSource instances for database testing.
 *
 * <p>This class provides registration and retrieval of DataSource instances
 * used during test preparation and expectation phases.
 *
 * @see DataSource
 * @see DatabaseTestExtension
 */
class DataSourceRegistry {
```

## Markdown

### Document Structure

README files follow this structure:

1. Title (H1)
2. Overview (1-2 sentences)
3. Features or Architecture
4. Requirements
5. Installation
6. Usage
7. Configuration
8. API Reference
9. Related Modules
10. Documentation links

### Headings

Use heading levels in sequence. Do not skip levels.

```markdown
# Title
## Section
### Subsection
```

### Code Blocks

Specify the language for syntax highlighting.

````markdown
```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {
    // ...
}
```
````

Use backticks for inline code: `@Preparation`, `DataSource`.

### Tables

Use tables for structured data.

```markdown
| Annotation | Purpose |
|------------|---------|
| `@Preparation` | Load test data before execution |
| `@Expectation` | Verify database state after execution |
```

### Lists

Use numbered lists for sequential steps. Use bullet lists for unordered items.

```markdown
1. Add the dependency.
2. Register the extension.
3. Create test data files.

- Annotation-driven configuration
- Convention-based loading
- Multiple DataSource support
```

### Links

Use descriptive link text.

```markdown
See the [installation guide](docs/INSTALL.md) for setup.
```

Do not use "click here" or raw URLs as link text.

### Line Length

Limit lines to 120 characters.

### Spacing

- One blank line between paragraphs.
- One blank line before and after code blocks.
- One blank line before headings.

## Code Comments

### When to Comment

Comment to explain:

- Complex algorithms
- Non-obvious design decisions
- External requirements or constraints
- Workarounds for known issues

Do not comment:

- Obvious code behavior
- What the code does (the code shows this)

### Comment Markers

| Marker | Purpose |
|--------|---------|
| `TODO` | Planned work |
| `FIXME` | Known bug requiring fix |
| `HACK` | Temporary workaround |
| `NOTE` | Important context |

Include issue references when available.

```java
// TODO(#123): Implement batch processing
// FIXME: Race condition on concurrent access
// HACK: Workaround for library bug; remove after upgrade
// NOTE: Order matters due to foreign key constraints
```

## Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/) format.

### Format

```
<type>(<scope>): <description>

[body]

[footer]
```

### Types

| Type | Purpose |
|------|---------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation |
| `style` | Formatting |
| `refactor` | Code restructuring |
| `test` | Test changes |
| `chore` | Build and tooling |

### Subject Line

- Use imperative mood: "add", "fix", "update"
- Use lowercase
- No period at end
- Maximum 72 characters

```
feat(kotest): add Kotest framework support
fix(core): resolve CSV parsing with quoted commas
docs(api): update Operation enum documentation
```

### Body

Explain what and why, not how. Wrap at 72 characters.

```
feat(spring): add automatic DataSource registration

Add SpringBootDatabaseTestExtension that registers Spring-managed
DataSource beans with the testing framework.

This eliminates manual DataSource registration in @BeforeAll methods.
```

### Footer

```
BREAKING CHANGE: rename Configuration.create() to Configuration.of()

Closes #123
```

## Version Numbers

Use major versions in documentation.

| Avoid | Use |
|-------|-----|
| Java 21.0.1 | Java 21 |
| Spring Boot 4.0.1 | Spring Boot 4 |
| Kotest 6.0.7 | Kotest 6 |

Use `VERSION` as placeholder in dependency examples.

```kotlin
testImplementation("io.github.seijikohara:db-tester-junit:VERSION")
```

## Null Safety Documentation

This project uses `@NullMarked` packages with NullAway. Non-null is the default.

Document only nullable cases:

```java
/**
 * @param handler the failure handler, or null for default behavior
 * @return the result, or null if not found
 */
```
