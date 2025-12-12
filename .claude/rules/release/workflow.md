# Release Workflow

This document describes the release workflow for DB Tester modules.

For detailed publishing setup (credentials, GPG keys, Maven Central configuration), see [publishing.md](publishing.md).

## Overview

The project supports two release methods:

1. **GitHub Actions (Recommended)**: Automated workflow with validation and approval gate
2. **Local Release**: Manual release from your development machine

### Released Modules

All modules share the same version and are released together. See [publishing.md](publishing.md#published-modules) for the complete list.

### Version Management

This project uses [axion-release-plugin](https://github.com/allegro/axion-release-plugin) for version management. Versions are derived from Git tags:

- **Tagged commit** (e.g., `v1.2.0`): Release version `1.2.0`
- **After tagged commit**: Snapshot version `1.2.1-SNAPSHOT`
- **No tags**: Default version `0.1.0-SNAPSHOT`

```bash
# Check current version
./gradlew currentVersion
```

---

## Option 1: GitHub Actions Release (Recommended)

The GitHub Actions workflow provides:
- Version validation (MAJOR.MINOR.PATCH format only)
- Automatic check that version is newer than existing tags
- Dry-run mode for testing before actual release
- Manual approval gate via GitHub Environments

### Prerequisites

See [publishing.md](publishing.md#option-1-github-actions-release-recommended) for one-time setup:
- GitHub Environment (`maven-central`) with required reviewers
- GitHub Secrets (`GPG_PRIVATE_KEY`, `GPG_PASSPHRASE`, `GPG_KEY_ID`, `MAVEN_CENTRAL_USERNAME`, `MAVEN_CENTRAL_TOKEN`)

### Release Process

**Step 1: Run Dry-Run (Optional but Recommended)**

1. Go to **Actions** → **Release** → **Run workflow**
2. Enter version (e.g., `1.2.0`)
3. Check **"Dry-run mode"**
4. Click **"Run workflow"**

This validates the version, builds, and tests without creating tags or publishing.

**Step 2: Run Actual Release**

1. Go to **Actions** → **Release** → **Run workflow**
2. Enter version (e.g., `1.2.0`)
3. Leave **"Dry-run mode"** unchecked
4. Click **"Run workflow"**
5. Wait for `validate` and `build-and-dry-run` jobs to complete
6. **Approve** the deployment in the `maven-central` environment
7. Wait for `release` job to complete

The workflow automatically:
- Validates the version
- Builds and tests all modules
- Deploys to Maven Central
- Creates Git tag (e.g., `v1.2.0`) via axion-release-plugin
- Creates GitHub Release with auto-generated notes

### Verify Release

- **GitHub Release**: https://github.com/seijikohara/db-tester/releases
- **Maven Central** (available in 10-30 minutes): See [publishing.md](publishing.md#step-6-verify-publication) for verification URLs

---

## Option 2: Local Release

For local releases from your development machine.

### Prerequisites

See [publishing.md](publishing.md) for detailed setup instructions:
- Central Portal account and namespace verification
- API token generation
- GPG key setup
- Credential configuration in `~/.gradle/gradle.properties`

### Local Release Process

**Step 1: Check Current Version**

```bash
./gradlew currentVersion
git tag -l 'v*' --sort=-v:refname
```

**Step 2: Create Local Tag**

Create a local tag for the release version:

```bash
git tag v1.2.0
```

**Step 3: Build and Test**

```bash
./gradlew clean build
./gradlew currentVersion  # Verify: should show 1.2.0, not SNAPSHOT
```

**Step 4: Publish to Maven Central**

```bash
./gradlew publishAndReleaseToMavenCentral --no-configuration-cache
```

**Step 5: Push Git Tag**

After successful publish, push the tag to remote:

```bash
git push origin v1.2.0
```

**Important**: Push the tag only after successful publish. If publish fails, delete the local tag with `git tag -d v1.2.0` and retry.

**Step 6: Create GitHub Release**

```bash
gh release create v1.2.0 --title "Release 1.2.0" --generate-notes
```

**Step 7: Verify Release**

See [publishing.md](publishing.md#step-6-verify-publication) for verification URLs and test instructions.

---

## Changelog Generation

GitHub Release automatically generates changelogs based on merged pull requests and commit history.

### Commit Message Format

Use Conventional Commits format for better changelog organization:

```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

**Commit Types:**
- `feat` - New feature (appears in changelog)
- `fix` - Bug fix (appears in changelog)
- `docs` - Documentation changes
- `refactor` - Code refactoring
- `perf` - Performance improvements
- `test` - Adding or updating tests

**Examples:**

```bash
git commit -m "feat: add support for PostgreSQL 17"
git commit -m "fix: resolve null pointer exception in DatabaseAssertion"
git commit -m "feat!: redesign API for better usability

BREAKING CHANGE: DatabaseTestExtension.getRegistry() now returns Optional<DataSourceRegistry>"
```

## Version Bumping Strategy

Version is determined manually following [Semantic Versioning](https://semver.org/):

- **PATCH** (1.0.0 → 1.0.1): Bug fixes, documentation updates
- **MINOR** (1.0.0 → 1.1.0): New features (backward compatible)
- **MAJOR** (1.0.0 → 2.0.0): Breaking changes

## Troubleshooting

For troubleshooting GPG signing errors, credential issues, and Maven Central deployment failures, see [publishing.md](publishing.md#troubleshooting).

## Best Practices

1. **Use Conventional Commits**: Format commit messages for automatic changelog generation
2. **Test locally first**: Use `publishToMavenLocal` before releasing
3. **Use dry-run mode**: Test with GitHub Actions dry-run mode for first-time releases
4. **Keep main branch clean**: Avoid force pushes after tagging
5. **Document breaking changes**: Include details in commit messages and release notes
6. **Verify releases**: Watch GitHub Releases and Maven Central for publication status

## References

- [axion-release-plugin Documentation](https://axion-release-plugin.readthedocs.io/)
- [gradle-maven-publish-plugin Documentation](https://vanniktech.github.io/gradle-maven-publish-plugin/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
