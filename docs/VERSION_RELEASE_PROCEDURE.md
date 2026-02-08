# VETTR Android - Version Release Procedure

This document outlines the versioning strategy and release procedure for the VETTR Android app to Google Play Store.

## Version Numbering

### Semantic Versioning (versionName)

We follow [Semantic Versioning](https://semver.org/) (MAJOR.MINOR.PATCH):

- **MAJOR** (X.0.0): Breaking changes, major feature overhauls, or significant architecture changes
  - Example: 1.0.0 → 2.0.0 (complete redesign)

- **MINOR** (1.X.0): New features, backwards-compatible functionality additions
  - Example: 1.0.0 → 1.1.0 (added Pedigree feature)

- **PATCH** (1.0.X): Bug fixes, minor improvements, performance optimizations
  - Example: 1.0.0 → 1.0.1 (fixed crash on launch)

### Version Code (versionCode)

- Integer that must **always increase** with each release
- Used by Google Play to determine which version is newer
- Never reuse or decrease this number
- Typically increments by 1 for each build

### Current Version State

Versions are tracked in two places:

1. **app/build.gradle.kts** (line 18-19):
   ```kotlin
   versionCode = 1
   versionName = "1.0.0"
   ```

2. **version.properties** (root directory):
   ```properties
   versionCode=1
   versionName=1.0.0
   ```

## Release Procedure

### 1. Pre-Release Checklist

Before creating a release:

- [ ] All user stories for the release are complete and passing
- [ ] All tests pass: `./gradlew testDebugUnitTest`
- [ ] Build succeeds: `./gradlew assembleDebug`
- [ ] Lint checks pass: `./gradlew detekt`
- [ ] ProGuard release build succeeds: `./gradlew assembleRelease`
- [ ] App tested on multiple devices (min API 26, target API 35)
- [ ] All crash reports resolved
- [ ] Release notes drafted

### 2. Update Version Numbers

#### Manual Update (Local Development)

1. Update `app/build.gradle.kts`:
   ```kotlin
   versionCode = 2  // Increment by 1
   versionName = "1.0.1"  // Follow semantic versioning
   ```

2. Update `version.properties`:
   ```properties
   versionCode=2
   versionName=1.0.1
   ```

3. Commit the changes:
   ```bash
   git add app/build.gradle.kts version.properties
   git commit -m "chore: Bump version to 1.0.1 (2)"
   ```

#### CI/CD Automated Update (Future)

When CI/CD is set up:

1. CI reads `version.properties`
2. CI increments `versionCode` by 1
3. CI updates `version.properties` and commits
4. CI passes version to Gradle: `./gradlew -PversionCode=X -PversionName=Y assembleRelease`

### 3. Build Release APK/AAB

```bash
# Set JAVA_HOME to Android Studio JBR
export JAVA_HOME="/Applications/Android Studio.app/Contents/jbr/Contents/Home"

# Build release bundle (AAB - recommended for Google Play)
./gradlew bundleRelease

# Output: app/build/outputs/bundle/release/app-release.aab

# OR build release APK
./gradlew assembleRelease

# Output: app/build/outputs/apk/release/app-release.apk
```

### 4. Sign the Release

Google Play requires signed releases. Ensure you have:

- **Keystore file**: `vettr-release.keystore` (NEVER commit to git)
- **Keystore password**: Stored securely (e.g., 1Password, CI secrets)
- **Key alias**: `vettr-key`
- **Key password**: Stored securely

Signing is configured in `app/build.gradle.kts` (not shown in this setup - add in production):

```kotlin
android {
    signingConfigs {
        release {
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: "path/to/keystore")
            storePassword = System.getenv("KEYSTORE_PASSWORD")
            keyAlias = System.getenv("KEY_ALIAS")
            keyPassword = System.getenv("KEY_PASSWORD")
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            // ...
        }
    }
}
```

### 5. Test Release Build

Before uploading to Play Store:

1. Install release build on physical device:
   ```bash
   adb install app/build/outputs/apk/release/app-release.apk
   ```

2. Test critical flows:
   - [ ] Google Sign-In
   - [ ] Stock data loading
   - [ ] Navigation (all 5 tabs)
   - [ ] Alerts
   - [ ] Settings persistence
   - [ ] Offline mode

3. Verify ProGuard hasn't broken anything:
   - Check crash logs if any
   - Verify Room entities are kept
   - Verify Retrofit models deserialize correctly

### 6. Create Git Tag

```bash
# Create annotated tag
git tag -a v1.0.1 -m "Release 1.0.1 - Bug fixes and performance improvements"

# Push tag to remote
git push origin v1.0.1
```

### 7. Upload to Google Play Console

1. Go to [Google Play Console](https://play.google.com/console)
2. Select VETTR app
3. Navigate to **Production** → **Create new release**
4. Upload `app-release.aab`
5. Add release notes (see Release Notes section)
6. Set rollout percentage (start with 20% for new features, 100% for hotfixes)
7. Review and roll out

### 8. Post-Release

- [ ] Monitor crash reports in Play Console
- [ ] Monitor user reviews
- [ ] Track analytics for adoption rate
- [ ] Monitor backend logs for API errors
- [ ] Update changelog in repository

## Release Notes Format

### Template

```
Version 1.0.1

What's New:
• Added Pedigree feature to view executive backgrounds
• Improved Red Flag detection accuracy
• New VETR Score breakdown screen

Improvements:
• Faster stock data sync
• Reduced memory usage by 15%
• Improved offline mode reliability

Bug Fixes:
• Fixed crash when opening alerts
• Fixed incorrect date formatting in filings
• Fixed biometric login on Android 14
```

### Best Practices

- Keep it user-friendly (avoid technical jargon)
- Highlight top 3-5 features
- Be concise (Play Store shows ~500 chars before "Read more")
- Use emojis sparingly (bullet points are fine)
- Always include bug fixes

## Rollback Procedure

If critical issue is discovered post-release:

1. **Immediate**: Halt rollout in Play Console (Production → Halt rollout)
2. **Fix**: Create hotfix branch, fix issue, increment version (e.g., 1.0.1 → 1.0.2)
3. **Test**: Thoroughly test hotfix on multiple devices
4. **Release**: Follow release procedure with new version
5. **Post-mortem**: Document what went wrong and how to prevent it

## Version History

| Version | Version Code | Release Date | Notes |
|---------|--------------|--------------|-------|
| 1.0.0   | 1            | TBD          | Initial release |

## CI/CD Integration (Future)

When setting up automated releases:

1. **Trigger**: Git tag push (`v*.*.*`)
2. **Read version**: Parse from tag (e.g., `v1.0.1` → `1.0.1`)
3. **Update files**: Update `version.properties`, commit
4. **Build**: `./gradlew bundleRelease`
5. **Sign**: Use secrets for keystore credentials
6. **Upload**: Use [Gradle Play Publisher](https://github.com/Triple-T/gradle-play-publisher)
7. **Notify**: Slack/Discord notification

Example GitHub Actions workflow (not yet implemented):

```yaml
name: Release
on:
  push:
    tags:
      - 'v*.*.*'
jobs:
  release:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 17
        uses: actions/setup-java@v3
        with:
          java-version: '17'
      - name: Build release bundle
        run: ./gradlew bundleRelease
      - name: Upload to Play Store
        uses: r0adkll/upload-google-play@v1
        with:
          serviceAccountJsonPlainText: ${{ secrets.SERVICE_ACCOUNT_JSON }}
          packageName: com.vettr.android
          releaseFiles: app/build/outputs/bundle/release/app-release.aab
          track: production
```

## References

- [Google Play Console](https://play.google.com/console)
- [Android App Bundle Documentation](https://developer.android.com/guide/app-bundle)
- [Semantic Versioning](https://semver.org/)
- [ProGuard/R8 Configuration](https://developer.android.com/studio/build/shrink-code)
