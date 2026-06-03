---
title: "Versioning and Compatibility Policy - DB Tester"
description: "Semantic versioning, API stability tiers, and the deprecation policy for DB Tester."
---

# Versioning and Compatibility Policy

DB Tester follows [Semantic Versioning 2.0.0](https://semver.org/spec/v2.0.0.html). A release version
has the form `MAJOR.MINOR.PATCH`. All published modules share one version and release together.

## Version increments

| Increment | When |
|-----------|------|
| `MAJOR` | A backward-incompatible change to a stable API. |
| `MINOR` | A backward-compatible feature addition. |
| `PATCH` | A backward-compatible bug fix. |

## Pre-1.0 (0.x) caveat

While the version is below `1.0.0`, the API is still stabilizing. Under SemVer, a `0.x` release may
introduce backward-incompatible changes in a `MINOR` increment (for example, `0.8.x` to `0.9.0`).
Breaking changes in the `0.x` series are recorded in the [changelog](https://github.com/seijikohara/db-tester/blob/main/CHANGELOG.md).

## API stability tiers

The `db-tester-api` module groups packages by intended audience and stability. The module descriptor
([`module-info.java`](https://github.com/seijikohara/db-tester/blob/main/db-tester-api/src/main/java/module-info.java))
records the same grouping.

| Tier | Packages | Compatibility |
|------|----------|---------------|
| User API | `annotation`, `config`, `operation`, `exception`, `preparation` | Stable. Backward-compatible within a major version once 1.0 ships. |
| Advanced API | `assertion`, `export`, `domain`, `dataset` | Stable. Programmatic surface for advanced users. |
| Extension SPI | `spi`, `loader`, `context`, `scenario` | Evolving. Supported, but may gain capabilities; integrators should expect additive changes. |

Types under the `internal` packages of any module are implementation details and are not part of the
public API, regardless of Java visibility.

## What counts as a breaking change

For the stable tiers, the following are breaking and require a `MAJOR` increment (post-1.0):

- Removing or renaming a public type, method, field, or annotation attribute.
- Changing a method signature or return type.
- Narrowing accepted input or widening a thrown checked exception.
- Changing documented runtime behavior in a way that breaks a conforming caller.

The following are not breaking:

- Adding a new type, method, overload, or annotation attribute with a default.
- Widening a `requires` directive to `transitive`.
- Adding a new enum constant (callers must handle unknown constants defensively).

## Deprecation policy

Before a stable API element is removed, it is deprecated for at least one `MINOR` release:

- The element is annotated `@Deprecated`, with `forRemoval = true` and a `since` value.
- The Javadoc names the replacement.
- Removal occurs no earlier than the next `MAJOR` release after deprecation.

The Extension SPI tier may change with a shorter notice because it targets integrators who track the
framework closely.
