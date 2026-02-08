# VETTR Android - Build Performance Baselines

This document tracks build performance metrics for the VETTR Android app.

## Gradle Build Optimizations

The following Gradle optimizations are configured:

### gradle.properties
- `org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8` - 4GB heap for Gradle daemon
- `org.gradle.daemon=true` - Keep Gradle daemon running between builds
- `org.gradle.caching=true` - Enable local build cache
- `org.gradle.parallel=true` - Execute tasks in parallel when possible
- `android.nonTransitiveRClass=true` - Optimize R class generation

### settings.gradle.kts
- Local build cache enabled with 7-day cleanup
- Cache directory: `.gradle/build-cache`

## Build Performance Baselines

Measured on: 2026-02-08
Environment: macOS (Darwin 25.2.0)
Gradle Version: 8.9
AGP Version: 8.7.3
Kotlin Version: 2.0.21

### Clean Build
```
./gradlew clean assembleDebug
Time: ~17 seconds
Tasks: 42 executed
```

### Incremental Build (no changes)
```
./gradlew assembleDebug
Time: ~0.8 seconds
Tasks: 41 up-to-date
```

### Incremental Build (single file change)
```
./gradlew assembleDebug (after changing one Kotlin file)
Time: ~3-4 seconds
Tasks: 2 executed, 39 up-to-date
```

## Build Performance Tips

1. **Use Gradle daemon**: Keep daemon running for faster subsequent builds
2. **Enable build cache**: Reuse outputs from previous builds
3. **Use parallel execution**: Leverage multiple CPU cores
4. **Sufficient heap memory**: 4GB prevents out-of-memory errors during large builds
5. **Incremental compilation**: Only recompile changed files and their dependents
6. **Use up-to-date checks**: Gradle skips tasks when inputs haven't changed

## Monitoring Build Performance

To see detailed build timing information:
```bash
./gradlew assembleDebug --profile
```

The report will be available in: `build/reports/profile/`

To see which tasks take the longest:
```bash
./gradlew assembleDebug --scan
```

This generates a build scan with detailed insights.
