# CI/CD Process for VETTR Android

This document describes the Continuous Integration and Continuous Deployment pipeline for the VETTR Android application.

## Overview

The VETTR Android app uses GitHub Actions for automated builds, tests, and deployments. The CI/CD pipeline ensures code quality, catches regressions early, and automates the release process.

## Workflow Configuration

### Location
`.github/workflows/build.yml`

### Triggers

The pipeline is triggered on:
- **Push to main branch**: Runs full build, test, and code quality checks
- **Pull requests to main**: Runs full build, test, and code quality checks (required for PR approval)
- **Tag push (v*)**: Triggers release build in addition to standard checks

## Pipeline Jobs

### 1. Build Job

Runs on every push and pull request to the main branch.

**Steps:**
1. **Checkout code**: Uses `actions/checkout@v4` to fetch the repository
2. **Set up JDK 17**: Uses `actions/setup-java@v4` with Temurin distribution
3. **Cache Gradle dependencies**: Automatically caches Gradle dependencies for faster builds
4. **Grant execute permission**: Makes `gradlew` executable
5. **Build with Gradle**: Runs `./gradlew assembleDebug`
6. **Run unit tests**: Runs `./gradlew testDebugUnitTest`
7. **Run detekt**: Runs `./gradlew detekt` for static code analysis (continues on error)

**GitHub Status Checks:**
- Pull requests require the build job to pass before merging
- Build status is visible on the PR page
- Failed builds block PR merges

### 2. Release Job

Runs only when a version tag (starting with `v`) is pushed.

**Conditions:**
- Only runs on tag push (e.g., `v1.0.0`, `v1.2.3-beta`)
- Depends on successful completion of the build job

**Steps:**
1. **Checkout code**: Uses `actions/checkout@v4`
2. **Set up JDK 17**: Uses `actions/setup-java@v4` with Temurin distribution
3. **Cache Gradle dependencies**: Automatically caches Gradle dependencies
4. **Grant execute permission**: Makes `gradlew` executable
5. **Build release APK**: Runs `./gradlew assembleRelease`
6. **Upload release artifacts**: Uploads the release APK as a GitHub Actions artifact

## Gradle Dependency Caching

The workflow uses GitHub Actions' built-in Gradle caching via the `setup-java` action:
- Caches Gradle wrapper, dependencies, and build cache
- Significantly reduces build times on subsequent runs
- Cache key based on Gradle wrapper and dependency files

## Code Quality Checks

### Detekt

Static code analysis tool for Kotlin:
- Runs as part of the build job
- Currently configured with `continue-on-error: true` (non-blocking)
- Once fully configured, will become a blocking check

### Unit Tests

- All unit tests run on every build
- Failed tests block PR merges
- Test results available in GitHub Actions logs

## Release Process

### Creating a Release

1. Ensure all changes are committed and pushed to main
2. Tag the release:
   ```bash
   git tag v1.0.0
   git push origin v1.0.0
   ```
3. GitHub Actions automatically:
   - Runs the build job (tests, detekt)
   - Builds the release APK
   - Uploads the APK as an artifact

### Downloading Release Artifacts

1. Navigate to the Actions tab in GitHub
2. Find the workflow run for the tag
3. Download the `release-apk` artifact
4. Extract and use the APK for distribution

## Local Development

### Running CI checks locally

Before pushing, run the same checks locally:

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew testDebugUnitTest

# Run detekt
./gradlew detekt
```

### Build Configuration

- **Debug builds**: Use `./gradlew assembleDebug`
- **Release builds**: Use `./gradlew assembleRelease`
- **Clean builds**: Use `./gradlew clean` before building

## Troubleshooting

### Build Failures

1. Check the GitHub Actions logs for error details
2. Reproduce the issue locally with the same Gradle command
3. Ensure all dependencies are properly declared
4. Verify JDK 17 is being used

### Cache Issues

If builds fail due to cache corruption:
1. Manually clear the cache in GitHub Actions settings
2. Re-run the workflow

### Permission Issues

If gradlew permission errors occur:
- The workflow automatically grants execute permission
- Ensure gradlew is committed with proper permissions locally

## Future Enhancements

- Add code coverage reporting
- Integrate Firebase App Distribution for automatic beta distribution
- Add Play Store deployment automation
- Add performance benchmarking
- Configure strict detekt rules (blocking)

## Maintenance

- Review and update actions versions quarterly
- Monitor build times and optimize as needed
- Keep Gradle wrapper updated
- Review and adjust detekt rules as codebase evolves

## Support

For CI/CD issues:
- Check GitHub Actions logs first
- Review this documentation
- Contact the development team
- File an issue in the repository
