# Publishing to Maven Central

This document describes how to publish DB Tester modules to Maven Central using the gradle-maven-publish-plugin.

## Published Modules

The following modules are published to Maven Central:

| Module | Artifact ID | Description |
|--------|-------------|-------------|
| `db-tester-core` | `db-tester-core` | Public API and pure JDBC implementation |
| `db-tester-junit` | `db-tester-junit` | JUnit extension |
| `db-tester-spock` | `db-tester-spock` | Spock extension |
| `db-tester-kotest` | `db-tester-kotest` | Kotest AnnotationSpec extension |
| `db-tester-junit-spring-boot-starter` | `db-tester-junit-spring-boot-starter` | Spring Boot auto-configuration for JUnit |
| `db-tester-spock-spring-boot-starter` | `db-tester-spock-spring-boot-starter` | Spring Boot auto-configuration for Spock |
| `db-tester-kotest-spring-boot-starter` | `db-tester-kotest-spring-boot-starter` | Spring Boot auto-configuration for Kotest |
| `db-tester-bom` | `db-tester-bom` | Bill of Materials |

All modules share the same version and are released together.

## Version Management

This project uses [axion-release-plugin](https://github.com/allegro/axion-release-plugin) for version management:

- **Version source**: Git tags (e.g., `v1.2.0` → version `1.2.0`)
- **Snapshot versions**: Automatically derived from latest tag + commits
- **No gradle.properties version**: Version is always derived from Git

```bash
# Check current version
./gradlew currentVersion

# Force a specific version for build/publish
./gradlew build -Prelease.forceVersion=1.2.0
```

## Prerequisites

### 1. Central Portal Account & Namespace

Create an account:
- Visit [central.sonatype.com](https://central.sonatype.com/)
- Sign up with your email address

Verify the namespace (`io.github.seijikohara`):

1. In Central Portal, navigate to "Namespaces"
2. Click "Add Namespace"
3. Enter `io.github.seijikohara`
4. For GitHub verification:
   - Create a temporary public repository: `https://github.com/seijikohara/OSSRH-<random-id>` (use the ID provided by Central Portal)
   - Click "Verify" in Central Portal
   - Delete the temporary repository after verification

For custom domains, add a TXT DNS record instead.

### 2. Generate API Token

1. Log in to [central.sonatype.com/account](https://central.sonatype.com/account)
2. Click **"Generate User Token"**
3. Save the username and password (token) securely

### 3. GPG Key Setup

Generate and configure GPG key for artifact signing:

```bash
# Generate new GPG key (RSA 4096-bit recommended)
gpg --full-generate-key

# Select:
# - (1) RSA and RSA
# - 4096 bits
# - 0 = key does not expire (or set expiration as needed)
# - Enter your name and email (must match GitHub account)

# List keys to get Key ID
gpg --list-secret-keys --keyid-format=short

# Example output:
# sec   rsa4096/ABCD1234 2024-01-01 [SC]

# Publish public key to key servers
gpg --keyserver keyserver.ubuntu.com --send-keys ABCD1234
gpg --keyserver keys.openpgp.org --send-keys ABCD1234

# Test signing
echo "test" | gpg --clearsign
```

### 4. Configure Credentials

Create or update `~/.gradle/gradle.properties`:

```properties
# Maven Central Portal credentials
mavenCentralUsername=your-username-from-step-2
mavenCentralPassword=your-password-token-from-step-2

# GPG signing configuration (choose one method)

# Option 1: Use GPG agent (recommended for local development)
signing.gnupg.keyName=ABCD1234
signing.gnupg.passphrase=your-gpg-passphrase

# Option 2: In-memory key (recommended for CI)
# signing.keyId=ABCD1234
# signing.password=your-gpg-passphrase
# signing.secretKeyRingFile=/path/to/secring.gpg
```

Alternatively, set as environment variables (for CI):

```bash
export ORG_GRADLE_PROJECT_mavenCentralUsername="your-username"
export ORG_GRADLE_PROJECT_mavenCentralPassword="your-token"
export ORG_GRADLE_PROJECT_signingInMemoryKeyId="ABCD1234"
export ORG_GRADLE_PROJECT_signingInMemoryKeyPassword="your-passphrase"
export ORG_GRADLE_PROJECT_signingInMemoryKey="$(gpg --armor --export-secret-keys ABCD1234)"
```

## Publishing Process

There are two ways to publish releases:

1. **GitHub Actions (recommended)**: Automated workflow with validation and approval
2. **Local release**: Manual release from your development machine

---

## Option 1: GitHub Actions Release (Recommended)

The recommended way to release is using the GitHub Actions workflow, which provides:
- Version validation (MAJOR.MINOR.PATCH format)
- Automatic check that version is newer than existing tags
- Dry-run mode for testing
- Manual approval gate before actual deployment

### Prerequisites (One-time Setup)

1. **GitHub Environment**: Create `maven-central` environment in repository settings
   - Settings → Environments → New environment
   - Name: `maven-central`
   - Add required reviewers (yourself)

2. **GitHub Secrets**: Add the following secrets in repository settings
   - Settings → Secrets and variables → Actions → New repository secret

   | Secret | Description |
   |--------|-------------|
   | `GPG_PRIVATE_KEY` | GPG private key (ASCII armor format: `gpg --armor --export-secret-keys KEY_ID`) |
   | `GPG_PASSPHRASE` | GPG key passphrase |
   | `GPG_KEY_ID` | GPG key ID (short format, e.g., `ABCD1234`) |
   | `MAVEN_CENTRAL_USERNAME` | Maven Central username from step 2 |
   | `MAVEN_CENTRAL_TOKEN` | Maven Central token from step 2 |

### Release Steps

**Step 1: Run dry-run (optional but recommended)**

1. Go to Actions → Release → Run workflow
2. Enter version (e.g., `1.2.0`)
3. Check "Dry-run mode"
4. Click "Run workflow"

This validates the version, builds, and tests without creating tags or publishing.

**Step 2: Run actual release**

1. Go to Actions → Release → Run workflow
2. Enter version (e.g., `1.2.0`)
3. Leave "Dry-run mode" unchecked
4. Click "Run workflow"
5. After `build-and-dry-run` job completes, approve the deployment in the `maven-central` environment
6. Wait for the `release` job to complete

The workflow performs the following operations:
- Version validation
- Build and test all modules
- Deploy to Maven Central
- Create Git tag (e.g., `v1.2.0`) via axion-release-plugin
- Create GitHub Release with auto-generated notes

### Verify Release

Verify the release at the following locations:
- GitHub Release: https://github.com/seijikohara/db-tester/releases
- Maven Central: https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit

---

## Option 2: Local Release

For local releases from your development machine, follow the steps below.

### Step 1: Ensure Clean State

```bash
# Ensure all changes are committed
git status

# Check current version (derived from Git tags)
./gradlew currentVersion

# Run all tests
./gradlew clean build test
```

### Step 2: Determine Release Version

Determine the next version based on semantic versioning:

```bash
# List existing tags
git tag -l 'v*' --sort=-v:refname

# Determine next version based on changes:
# - PATCH (1.0.0 -> 1.0.1): Bug fixes
# - MINOR (1.0.0 -> 1.1.0): New features (backward compatible)
# - MAJOR (1.0.0 -> 2.0.0): Breaking changes
```

### Step 3: Build and Publish

Build and publish all modules with the release version (replace `1.2.0` with your target version):

```bash
# Publish to Maven Central (all modules)
./gradlew publishAndReleaseToMavenCentral -Prelease.forceVersion=1.2.0 --no-configuration-cache
```

This command performs the following operations:
- Builds all modules
- Signs all artifacts with GPG
- Uploads to Maven Central
- Automatically releases (no manual staging repository approval required)

This can take several minutes for deployment validation and publishing.

### Step 4: Create Git Tag

Use axion-release-plugin to create and push the tag:

```bash
./gradlew createRelease -Prelease.version=1.2.0
```

Or manually:

```bash
git tag -a v1.2.0 -m "Release v1.2.0"
git push origin v1.2.0
```

### Step 5: Create GitHub Release

```bash
gh release create v1.2.0 --title "Release 1.2.0" --generate-notes
```

### Step 6: Verify Publication

Verify the GitHub Release (available immediately):
- Visit `https://github.com/seijikohara/db-tester/releases`
- Verify changelog and release notes

Verify the Maven Central publication (available in 10-30 minutes):
- Core: `https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-core`
- JUnit: `https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit`
- Spock: `https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-spock`
- BOM: `https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-bom`
- JUnit Spring Boot Starter: `https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-junit-spring-boot-starter`
- Spock Spring Boot Starter: `https://central.sonatype.com/artifact/io.github.seijikohara/db-tester-spock-spring-boot-starter`

To test the download, add the following to your build file:

```kotlin
dependencies {
    // Using BOM (recommended)
    testImplementation(platform("io.github.seijikohara:db-tester-bom:1.2.0"))
    testImplementation("io.github.seijikohara:db-tester-junit")
    // Optional: Spock integration
    testImplementation("io.github.seijikohara:db-tester-spock")
    // Optional: Spring Boot integration for JUnit
    testImplementation("io.github.seijikohara:db-tester-junit-spring-boot-starter")
    // Optional: Spring Boot integration for Spock
    testImplementation("io.github.seijikohara:db-tester-spock-spring-boot-starter")
}
```

## Available Gradle Tasks

### Version Management (axion-release-plugin)

```bash
# Show current version from Git tags
./gradlew currentVersion

# Force version for build
./gradlew build -Prelease.forceVersion=1.2.0

# Create and push tag
./gradlew createRelease -Prelease.version=1.2.0
```

### Publishing

```bash
# Publish to Maven Central (all modules)
./gradlew publishAndReleaseToMavenCentral -Prelease.forceVersion=1.2.0 --no-configuration-cache

# Publish to local Maven repository (for testing)
./gradlew publishToMavenLocal -Prelease.forceVersion=1.2.0

# Publish to Maven Central without automatic release (manual staging)
./gradlew publishAllPublicationsToMavenCentralRepository -Prelease.forceVersion=1.2.0 --no-configuration-cache
```

## Troubleshooting

### GPG Signing Errors

**Error: "Inappropriate ioctl for device"**

This error occurs when GPG cannot prompt for passphrase in non-TTY environments.

Configure in-memory signing in `~/.gradle/gradle.properties`:

```properties
signing.keyId=ABCD1234
signing.password=your-gpg-passphrase
signing.secretKeyRingFile=/path/to/secring.gpg
```

For other GPG issues:

```bash
# Ensure gpg-agent is running
gpg-agent --daemon

# Test signing (may fail in non-TTY environment)
echo "test" | gpg --clearsign

# List available keys
gpg --list-secret-keys --keyid-format=short

# If key not found, check key location
gpgconf --list-dirs
```

### Credentials Not Found

```bash
# Verify environment variables
echo $ORG_GRADLE_PROJECT_mavenCentralUsername
echo $ORG_GRADLE_PROJECT_signingInMemoryKeyId

# Or check config file
cat ~/.gradle/gradle.properties
```

### Maven Central Validation Errors

The gradle-maven-publish-plugin automatically validates:
- GPG signatures (all `.asc` files)
- Sources JAR present
- Javadoc JAR present
- POM completeness (name, description, URL, license, developers, SCM)

If validation fails, check:
```bash
# Check build output for errors
./gradlew publishToMavenLocal -Prelease.forceVersion=1.2.0 --info
```

### Configuration Cache Issues

Always use `--no-configuration-cache` with publishing tasks:

```bash
# Correct
./gradlew publishAndReleaseToMavenCentral -Prelease.forceVersion=1.2.0 --no-configuration-cache

# May have issues
./gradlew publishAndReleaseToMavenCentral -Prelease.forceVersion=1.2.0
```

### Version Issues

```bash
# Check current version
./gradlew currentVersion

# Fetch all tags if out of sync
git fetch --tags

# List tags
git tag -l 'v*' --sort=-v:refname
```

## Best Practices

1. **Test locally first**: Always use `publishToMavenLocal` before releasing
2. **Use dry run**: Test with GitHub Actions dry-run mode for first-time releases
3. **Verify namespace**: Ensure namespace is verified in Central Portal before releasing
4. **Backup GPG keys**: Store GPG keys securely (export and backup)
5. **Document credentials**: Keep credential locations documented for team members
6. **Monitor releases**: Watch GitHub Releases and Maven Central for publication status
7. **Follow semantic versioning**: Use semantic versioning (major.minor.patch)

## References

- [axion-release-plugin Documentation](https://axion-release-plugin.readthedocs.io/)
- [gradle-maven-publish-plugin Documentation](https://vanniktech.github.io/gradle-maven-publish-plugin/)
- [Central Portal Documentation](https://central.sonatype.org/)
- [Maven Central Requirements](https://central.sonatype.org/publish/requirements/)
