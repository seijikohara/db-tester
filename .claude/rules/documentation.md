---
paths: "**/*.md"
---

# Documentation Style Rules

**All rules in this document are mandatory.**

---

## Table of Contents

1. [Quick Reference](#quick-reference)
2. [Core Principles](#core-principles)
   - [Writing Style](#writing-style)
   - [Language Requirements](#language-requirements)
   - [Prohibited Elements](#prohibited-elements)
3. [Javadoc](#javadoc)
   - [DocLint Configuration](#doclint-configuration)
   - [Class Documentation](#class-documentation)
   - [Method Documentation](#method-documentation)
   - [Field Documentation](#field-documentation)
   - [Deprecation Tags](#deprecation-tags)
   - [Package Documentation](#package-documentation)
4. [Markdown Documentation](#markdown-documentation)
   - [File Structure](#file-structure)
   - [README Files](#readme-files)
   - [Code Examples](#code-examples)
   - [Tables and Lists](#tables-and-lists)
   - [Accessibility and Structure](#accessibility-and-structure)
5. [Code Comments](#code-comments)
   - [Inline Comments](#inline-comments)
   - [Block Comments](#block-comments)
6. [Commit Messages](#commit-messages)
   - [Conventional Commits](#conventional-commits)
   - [Message Structure](#message-structure)
7. [Version References](#version-references)
8. [Links and References](#links-and-references)

---

## Quick Reference

| Category | Rule |
|----------|------|
| Language | English only; formal and technical |
| Tone | Objective; no subjective adjectives |
| Style | Direct statements; imperative mood for instructions |
| Javadoc | Required for all public and private elements; DocLint enforced |
| Markdown | Consistent structure; no trailing whitespace |
| Comments | Explain the reasoning, not the operation; avoid redundant comments |
| Commits | Conventional Commits format; present tense |

---

## Core Principles

### Writing Style

All documentation must be **objective, technical, and formal**. Subjective expressions and casual language are prohibited.

**Target audience**: Software developers with Java/Groovy experience.

**Requirements**:
- Use concise, direct statements
- Use precise technical terminology
- Maintain professional tone throughout
- Express one concept per sentence
- Prefer active voice
- Limit sentences to 25 words maximum
- Limit paragraphs to 3-5 sentences

**Voice Guidelines**:

| Context | Voice | Example |
|---------|-------|---------|
| Instructions | Imperative | "Configure the data source" |
| Descriptions | Active | "The method returns a list" |
| Explanations | Active | "This class manages connections" |
| State/Results | Passive (acceptable) | "The connection is closed automatically" |

### Language Requirements

**English only**: All documentation, comments, and commit messages must be in English.

**Technical precision**:
- Use exact technical terms
- Define acronyms on first use
- Maintain consistent terminology throughout

| Term | Usage |
|------|-------|
| DataSource | Java interface (capitalized) |
| data source | Generic database connection concept |
| CSV | Comma-Separated Values (define once) |

### Prohibited Elements

The following elements are prohibited in technical documentation:

**Subjective adjectives**:
- "modern", "powerful", "elegant", "beautiful"
- "simple", "easy", "straightforward" (when describing complexity)
- "great", "best", "amazing", "perfect"

**Casual contractions**:
- "don't", "won't", "can't", "shouldn't"
- "let's", "you'll", "we'll", "it's"

**Conversational phrases**:
- "you should", "we recommend", "you might want to"
- "as you can see", "note that you"
- "simply", "just", "basically"

**Redundant modifiers**:
- "very", "really", "quite", "extremely"
- "actually", "obviously", "clearly"

**Exclamation marks**: Prohibited in technical documentation.

**Verbose phrases**:
- "in order to" → "to"
- "due to the fact that" → "because"
- "at the present time" → "now"
- "in the event that" → "if"

**Vague enumerations**:
- "etc.", "and so on", "and more" (specify items or use "such as")

**Overly formal words**:
- "utilize" → "use"
- "leverage" → "use"
- "facilitate" → "enable" or "help"

**Polite phrases** (unnecessary in technical documentation):
- "please", "kindly"

**Correction Examples**:

| Prohibited | Correct |
|------------|---------|
| "A modern, powerful framework" | "An annotation-driven framework" |
| "You should configure the DataSource" | "Configure the DataSource" |
| "Simply call the method" | "Call the method" |
| "This makes it easy to test" | "This enables testing" |
| "Don't use null values" | "Null values are prohibited" |
| "It's important to note" | (omit; state the fact directly) |

---

## Javadoc

### DocLint Configuration

Javadoc is **strictly enforced** by DocLint with the following configuration:

```kotlin
tasks.withType<Javadoc> {
    (options as StandardJavadocDocletOptions).apply {
        addStringOption("Xdoclint:all", "-quiet")
        addBooleanOption("Werror", true)
    }
}
```

**DocLint checks**:
- Missing Javadoc for public or protected elements
- Missing `@param` tags for parameters
- Missing `@return` tags for non-void methods
- Missing `@throws` tags for declared exceptions
- HTML syntax errors in Javadoc comments

### Class Documentation

**Required elements**:
1. Summary sentence (first sentence ending with period)
2. Purpose and responsibility
3. `@see` references to related classes

**Structure**:

```java
/**
 * Manages DataSource instances for database testing.
 *
 * <p>This class provides registration and retrieval of {@link DataSource} instances
 * used during test preparation and expectation phases. Each instance is identified
 * by a unique name or registered as the default.
 *
 * @see DataSource
 * @see DatabaseTestExtension
 */
public final class DataSourceRegistry {
```

**Summary sentence rules**:
- Starts with a verb in third person singular ("Manages", "Provides", "Represents")
- Describes what the class does, not how
- Ends with a period
- Must be a complete sentence

### Method Documentation

**Required elements**:
1. Summary sentence
2. Additional explanation (if behavior is complex)
3. `@param` for each parameter
4. `@return` for non-void methods (omit for void)
5. `@throws` for each declared exception

**Structure**:

```java
/**
 * Registers a DataSource with the specified name.
 *
 * <p>If a DataSource with the same name already exists, it is replaced.
 * The name must be unique within this registry.
 *
 * @param name the unique identifier for the DataSource
 * @param dataSource the DataSource instance to register
 * @throws IllegalArgumentException if name is blank
 */
public void register(final String name, final DataSource dataSource) {
```

**Void methods**: Omit `@return` tag entirely.

```java
/**
 * Clears all registered DataSources.
 *
 * <p>After calling this method, the registry contains no DataSources.
 */
public void clear() {
```

**Private methods**: Require complete Javadoc (DocLint enforced).

```java
/**
 * Validates the DataSource name.
 *
 * @param name the name to validate
 * @throws IllegalArgumentException if name is blank
 */
private void validateName(final String name) {
```

### Field Documentation

**Instance fields**: Single-line Javadoc comment.

```java
/** The registered DataSources indexed by name. */
private final ConcurrentHashMap<String, DataSource> dataSources;

/** The default DataSource, or {@code null} if not set. */
private @Nullable DataSource defaultDataSource;
```

**Constants**: Document the purpose and any constraints.

```java
/** Maximum number of retry attempts for database connections. */
private static final int MAX_RETRY_COUNT = 3;

/** Default timeout in milliseconds for database operations. */
private static final long DEFAULT_TIMEOUT_MS = 30_000L;
```

### Tag Format Standards

**Null documentation policy**: This project uses `@NullMarked` packages with NullAway for compile-time null safety. Non-null is the default and does not require documentation. Only document nullable cases.

**@param format**: `@param paramName the [noun]`

| Pattern | Example |
|---------|---------|
| Non-null parameter | `@param context the test context` |
| Nullable parameter | `@param handler the failure handler, or {@code null} for default` |
| Collection | `@param columns the column names (may be empty)` |

**@return format by type**:

| Return Type | Format | Example |
|-------------|--------|---------|
| Simple value | `the [noun]` | `@return the configuration` |
| Collection | `immutable list of [noun]` | `@return immutable list of tables` |
| Optional | `an Optional containing [noun], or empty if [condition]` | `@return an Optional containing the table, or empty if not found` |
| boolean | `{@code true} if [condition], {@code false} otherwise` | `@return {@code true} if registered, {@code false} otherwise` |
| Nullable | `the [noun], or {@code null} if [condition]` | `@return the entry, or {@code null} if not found` |

**HTML tags in Javadoc**:

| Tag | Status |
|-----|--------|
| `<p>` | Allowed for paragraphs |
| `<ul>`, `<ol>`, `<li>` | Allowed for lists |
| `<h2>`, `<h3>`, etc. | Prohibited - use `<p>` with description instead |
| `<pre>{@code ...}</pre>` | Prohibited for usage examples in class documentation |

### Deprecation Tags

**`@deprecated` tag**: Document deprecated elements with replacement guidance.

```java
/**
 * Loads data from the specified file.
 *
 * @param file the file to load
 * @deprecated Use {@link #load(Path)} instead. This method will be removed in version 2.0.
 */
@Deprecated(since = "1.5.0", forRemoval = true)
public void load(File file) {
```

**Deprecation requirements**:
- Include `@deprecated` Javadoc tag with migration instructions
- Include `@Deprecated` annotation with `since` attribute
- Set `forRemoval = true` if planned for removal

### Package Documentation

Every package requires a `package-info.java` file.

**Structure**:

```java
/**
 * Database assertion and validation utilities.
 *
 * <p>This package provides classes for comparing expected datasets with actual
 * database state. The primary entry point is {@link DatabaseAssertion}.
 *
 * <p>Key classes:
 *
 * <ul>
 *   <li>{@link DatabaseAssertion} - Main assertion interface
 *   <li>{@link AssertionFailureHandler} - Custom failure handling
 * </ul>
 *
 * @see io.github.seijikohara.dbtester.api.annotation
 */
@NullMarked
package io.github.seijikohara.dbtester.api.assertion;

import org.jspecify.annotations.NullMarked;
```

---

## Markdown Documentation

### File Structure

**Standard sections for README files**:

1. Title (H1)
2. Brief description (1-2 sentences)
3. Features or Overview
4. Requirements/Prerequisites
5. Installation
6. Usage
7. Configuration (if applicable)
8. API Reference or Key Classes
9. Related Modules
10. Documentation links

### README Files

**Title format**: `# Project Name - Module Name`

```markdown
# DB Tester - JUnit Module

This module provides JUnit integration for the DB Tester framework.
```

**Section headers**: Use H2 (`##`) for main sections, H3 (`###`) for subsections.

```markdown
## Installation

### Gradle

```kotlin
dependencies {
    testImplementation("io.github.seijikohara:db-tester-junit:VERSION")
}
```

### Maven
```

**Feature lists**: Use bold for feature names, followed by description.

```markdown
## Features

- **Annotation-Driven Testing** - Configure test data using `@Preparation` and `@Expectation`
- **Convention-Based Loading** - Automatic data file resolution based on test class structure
- **Multiple DataSource Support** - Register and use multiple database connections
```

### Code Examples

**Fenced code blocks**: Always specify language.

```markdown
```java
@ExtendWith(DatabaseTestExtension.class)
class UserRepositoryTest {
    // ...
}
```
```

**Inline code**: Use backticks for class names, method names, and file paths.

```markdown
Use `@Preparation` to load test data before each test method.
The configuration file is located at `src/test/resources/application.properties`.
```

**Code block guidelines**:
- Include only relevant code
- Use `// ...` for omitted sections
- Add comments for non-obvious behavior

### Tables and Lists

**Tables**: Use for structured comparisons or reference data.

```markdown
| Annotation | Purpose |
|------------|---------|
| `@Preparation` | Load test data before test execution |
| `@Expectation` | Verify database state after test execution |
```

**Lists**: Use for sequential steps or related items.

```markdown
1. Add the dependency to your build file
2. Register the JUnit extension
3. Create test data files
4. Annotate test methods
```

### Accessibility and Structure

**Heading hierarchy**: Do not skip heading levels. Use H1 → H2 → H3 in sequence.

```markdown
<!-- Correct -->
# Title
## Section
### Subsection

<!-- Incorrect - skips H2 -->
# Title
### Subsection
```

**Line length**: Limit lines to 120 characters for readability in code editors.

**Blank lines**:
- One blank line between paragraphs
- One blank line before and after code blocks
- One blank line before headings (except at document start)

**Link text**: Use descriptive text that indicates the destination.

```markdown
<!-- Correct -->
See the [installation guide](docs/INSTALL.md) for setup instructions.

<!-- Incorrect -->
Click [here](docs/INSTALL.md) for setup instructions.
```

---

## Code Comments

### Inline Comments

**When to use**:
- Explain complex algorithms
- Document non-obvious design decisions
- Reference external requirements or specifications

**When to avoid**:
- Restating what the code does
- Obvious operations
- Temporary notes (use TODO instead)

**Format**:

```java
// Use parallel stream for large datasets (>10000 rows) based on profiling results
return rows.parallelStream()
    .filter(this::matchesScenario)
    .toList();
```

### Block Comments

**Comment markers**: Use standardized prefixes for actionable comments.

| Marker | Purpose | Example |
|--------|---------|---------|
| `TODO` | Planned enhancement or missing feature | `// TODO: Add XML format support` |
| `FIXME` | Known bug or broken code requiring fix | `// FIXME: Race condition on concurrent access` |
| `HACK` | Temporary workaround (document why) | `// HACK: Workaround for library bug #456` |
| `NOTE` | Important information for maintainers | `// NOTE: Order matters due to dependency` |

**TODO comments**: Include issue reference when available.

```java
// TODO(#123): Implement batch processing for large datasets
// TODO: Add support for XML format (requires schema validation)
```

**FIXME comments**: Describe the problem and impact.

```java
// FIXME: Memory leak when processing large files - causes OOM after ~1000 iterations
```

**Section markers**: Prohibited. Use proper class structure instead.

```java
// INCORRECT: Section markers
// ================== GETTERS ==================

// CORRECT: Organize by access modifier in class structure
```

---

## Commit Messages

### Conventional Commits

Use [Conventional Commits](https://www.conventionalcommits.org/) format.

**Structure**:

```
<type>(<scope>): <description>

[optional body]

[optional footer]
```

**Types**:

| Type | Purpose |
|------|---------|
| `feat` | New feature |
| `fix` | Bug fix |
| `docs` | Documentation changes |
| `style` | Code style (formatting, no logic change) |
| `refactor` | Code refactoring (no feature or fix) |
| `test` | Test additions or modifications |
| `chore` | Build, CI, or tooling changes |

**Scope**: Module or component name (optional but recommended).

### Message Structure

**Subject line**:
- Present tense imperative ("add", "fix", "update")
- Lowercase first letter
- No period at end
- Maximum 72 characters

```
feat(junit): add support for nested test class data loading
fix(core): resolve CSV parsing error with quoted commas
docs(api): update Javadoc for Operation enum
```

**Body** (optional):
- Explain what and why, not how
- Wrap at 72 characters
- Separate from subject with blank line

```
feat(spring): add automatic DataSource registration

Add SpringBootDatabaseTestExtension that automatically registers
Spring-managed DataSource beans with the testing framework.

This eliminates the need for manual DataSource registration in
@BeforeAll methods when using Spring Boot.
```

**Footer** (optional):
- Breaking changes: `BREAKING CHANGE: <description>`
- Issue references: `Closes #123`

---

## Version References

**Major versions only**: Use major version numbers without minor or patch versions.

| Incorrect         | Correct       |
|-------------------|---------------|
| Java 21.0.1       | Java 21       |
| Spring Boot 4.0.1 | Spring Boot 4 |
| JUnit 6.0.1       | JUnit 6       |
| Groovy 5.0.1      | Groovy 5      |

**Exception**: Pre-release or milestone versions when specifically relevant.

```markdown
## Requirements

- Java 21 or later
- Spring Boot 4 or later
- JUnit 6 or later
```

**Placeholder for current version**:

```markdown
testImplementation("io.github.seijikohara:db-tester-junit:VERSION")
```

Replace `VERSION` with actual version number in release documentation.

---

## Links and References

### Internal Links

**Relative paths**: Use relative paths for links within the repository.

```markdown
See the [core module documentation](../db-tester-core/README.md) for details.
Related: [JUnit examples](../examples/db-tester-example-junit/)
```

**Source code links**: Link to specific files with relative paths.

```markdown
[`DatabaseTestExtension`](src/main/java/io/github/seijikohara/dbtester/junit/jupiter/extension/DatabaseTestExtension.java)
```

### External Links

**Format**: Use descriptive link text, not raw URLs.

```markdown
<!-- Correct -->
See [Maven Central](https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit) for available versions.

<!-- Incorrect -->
See https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit for available versions.
```

### Javadoc References

**`{@link}` usage**: Reference classes and methods within Javadoc.

```java
/**
 * Registers with {@link DataSourceRegistry} for test execution.
 *
 * @see DatabaseTestExtension#getRegistry(ExtensionContext)
 */
```

**`{@code}` usage**: Format inline code in Javadoc.

```java
/**
 * Returns {@code true} if the DataSource is registered.
 * Use {@code registry.contains("name")} to check registration.
 */
```

