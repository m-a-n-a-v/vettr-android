# Firebase App Distribution - Beta Testing Guide

## Overview

Firebase App Distribution is Google's beta testing platform for distributing pre-release builds to testers before submitting to the Google Play Store. This document outlines the complete setup and beta testing strategy for VETTR Android.

## Table of Contents

1. [Firebase Project Setup](#firebase-project-setup)
2. [Beta Tester Groups](#beta-tester-groups)
3. [Build Configuration](#build-configuration)
4. [Distribution Process](#distribution-process)
5. [Beta Release Notes Template](#beta-release-notes-template)
6. [Feedback Collection](#feedback-collection)
7. [Beta Testing Timeline](#beta-testing-timeline)
8. [Testing Checklist](#testing-checklist)

---

## Firebase Project Setup

### Prerequisites

- Firebase Console access: https://console.firebase.google.com/
- Google account with owner/editor permissions for VETTR project
- Firebase CLI installed: `npm install -g firebase-tools`

### Step 1: Create/Link Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing project: **VETTR**
3. Click "Add app" → Select Android (robot icon)
4. Enter package name: `com.vettr.android`
5. Download `google-services.json`
6. Place `google-services.json` in `app/` directory

### Step 2: Add Firebase App Distribution Plugin

Update `app/build.gradle.kts`:

```kotlin
plugins {
    // ... existing plugins
    id("com.google.gms.google-services")
    id("com.google.firebase.appdistribution")
}
```

Update `build.gradle.kts` (project-level):

```kotlin
buildscript {
    dependencies {
        classpath("com.google.gms:google-services:4.4.0")
        classpath("com.google.firebase:firebase-appdistribution-gradle:4.0.1")
    }
}
```

### Step 3: Configure Firebase CLI

```bash
# Login to Firebase
firebase login

# Initialize Firebase App Distribution
firebase apps:sdkconfig ANDROID
```

---

## Beta Tester Groups

### Group Structure

Firebase App Distribution supports multiple tester groups for targeted distribution.

#### 1. **Internal Team (5-7 testers)**

**Purpose**: Daily builds, rapid iteration, core features testing

**Members**:
- Product Manager
- Lead Developer
- QA Engineer
- UX Designer
- Backend Engineer

**Access Level**: All builds (debug + beta)

**Distribution**: Automatic on every build

---

#### 2. **External Beta Testers (10-20 testers)**

**Purpose**: Real-world usage, UX feedback, edge case discovery

**Target Profile**:
- Active investors (venture capital, micro-cap focus)
- Mix of technical and non-technical users
- TSX-V and CSE market familiarity
- Android device diversity (different manufacturers, OS versions)

**Recruitment**:
- Existing VETTR web/iOS users
- LinkedIn investor groups
- Reddit r/CanadianInvestor community
- Twitter finance community

**Access Level**: Beta builds only (after internal QA)

**Distribution**: Manual releases with release notes

---

#### 3. **Device Matrix (5-10 testers)**

**Purpose**: Device/OS compatibility testing

**Target Devices**:
- Samsung Galaxy S23 (Android 14)
- Google Pixel 8 (Android 14)
- OnePlus 11 (Android 13)
- Motorola Edge 40 (Android 13)
- Samsung Galaxy A54 (Android 13)
- Older devices: Samsung Galaxy S20 (Android 12), Pixel 5 (Android 13)

**Access Level**: Specific compatibility builds

**Distribution**: On-demand for device-specific testing

---

### Creating Tester Groups in Firebase Console

1. Go to Firebase Console → **App Distribution** → **Testers & Groups**
2. Click **Add Group**
3. Create groups:
   - `internal-team`
   - `external-beta`
   - `device-matrix`
4. Add testers by email address
5. Testers will receive invitation emails with app download links

---

## Build Configuration

### Beta Build Type

The app already has a `debug` build type configured. For beta distribution, we'll use the **debug** build with beta-specific configuration.

### Current Build Configuration

From `app/build.gradle.kts`:

```kotlin
buildTypes {
    getByName("debug") {
        isDebuggable = true
        applicationIdSuffix = ".debug"
        versionNameSuffix = "-debug"
    }
    getByName("release") {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
}
```

### Build for Beta Distribution

Use the **debug** build type for beta testing:

```bash
./gradlew assembleDebug
```

Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

### Version Naming for Beta

Beta versions should follow semantic versioning with `-beta` suffix:

- **Format**: `MAJOR.MINOR.PATCH-beta.BUILD`
- **Examples**:
  - `1.0.0-beta.1` (first beta build)
  - `1.0.0-beta.2` (second beta build after fixes)
  - `1.0.0-beta.3` (third beta build)

Update version in `app/build.gradle.kts`:

```kotlin
defaultConfig {
    versionCode = 1
    versionName = "1.0.0-beta.1"  // Update this for each beta release
}
```

---

## Distribution Process

### Manual Distribution (Recommended for Beta)

#### Step 1: Build the APK

```bash
# Clean build
./gradlew clean

# Build debug APK
./gradlew assembleDebug

# Verify APK exists
ls -lh app/build/outputs/apk/debug/app-debug.apk
```

#### Step 2: Upload to Firebase Console

1. Go to Firebase Console → **App Distribution** → **Releases**
2. Click **Distribute app**
3. Select APK: `app/build/outputs/apk/debug/app-debug.apk`
4. Select tester groups (e.g., `internal-team`, `external-beta`)
5. Add release notes (see template below)
6. Click **Distribute**

#### Step 3: Notify Testers

Firebase automatically sends email notifications to all testers in selected groups with:
- Download link
- Release notes
- Installation instructions

---

### Automated Distribution with Gradle (Optional)

Update `app/build.gradle.kts`:

```kotlin
firebaseAppDistribution {
    releaseNotes = "Beta release notes here"
    releaseNotesFile = "release-notes.txt"
    groups = "internal-team, external-beta"
    serviceCredentialsFile = "firebase-service-account.json"
}
```

Build and distribute:

```bash
./gradlew assembleDebug appDistributionUploadDebug
```

**Note**: Automated distribution requires Firebase service account credentials. For security, manual distribution is recommended for beta phase.

---

## Beta Release Notes Template

### Format

Beta release notes should be concise, user-focused, and highlight what testers should focus on.

### Template

```markdown
# VETTR Android Beta v1.0.0-beta.X

**Release Date**: YYYY-MM-DD

## What's New

- [Feature/Fix]: Brief description
- [Feature/Fix]: Brief description
- [Feature/Fix]: Brief description

## Focus Areas for Testing

Please pay special attention to:
1. [Specific feature or workflow]
2. [Specific feature or workflow]
3. [Edge cases or scenarios]

## Known Issues

- [Issue description] - Fix planned for next beta
- [Issue description] - Under investigation

## Feedback Priorities

We'd love your feedback on:
- 🎨 **User Experience**: Is the navigation intuitive?
- ⚡ **Performance**: Are screens loading quickly?
- 🐛 **Bugs**: Any crashes or unexpected behavior?
- 📱 **Device Compatibility**: Works well on your device?

## How to Provide Feedback

- **In-app**: Profile → Settings → Send Feedback
- **Email**: beta-feedback@vettr.com
- **Slack**: #vettr-android-beta (for internal team)

## Installation Notes

- This is a **debug build** - you can install it alongside the release version
- App icon will have a debug badge
- Performance may be slightly slower than release builds

Thank you for helping make VETTR better! 🚀
```

---

### Example Beta Release Notes

#### Beta 1 (Initial Release)

```markdown
# VETTR Android Beta v1.0.0-beta.1

**Release Date**: 2026-02-10

## What's New

This is the first beta release of VETTR Android! 🎉

- ✅ Google Sign-In authentication with biometric unlock
- 📊 Pulse feed with 25+ Canadian venture stocks (TSX-V, CSE)
- 🔍 Discovery tools: stock screener, trending stocks, sector analysis
- 📄 Pedigree: executive profiles, SEC filings, insider transactions
- 🚨 Custom alerts with real-time push notifications
- 🏆 VETR Score with detailed scoring breakdown
- 🌙 Dark theme optimized for extended reading

## Focus Areas for Testing

Please pay special attention to:
1. **Authentication flow**: Google Sign-In → Biometric setup → App access
2. **Pulse feed**: Stock list loading, filtering, favoriting
3. **Stock detail screens**: Navigation between tabs, data loading
4. **Alerts**: Creating custom alerts, receiving notifications
5. **Overall UX**: Navigation flow, screen transitions, visual polish

## Known Issues

- Offline mode not yet implemented - requires internet connection
- Some stock logos may not display correctly
- Alert notification sound cannot be customized yet

## Feedback Priorities

We'd love your feedback on:
- 🎨 **User Experience**: Is the app intuitive for new users?
- ⚡ **Performance**: Are screens loading in <2 seconds?
- 🐛 **Bugs**: Any crashes, freezes, or data errors?
- 📱 **Device Compatibility**: Works well on your specific device/OS?
- 💡 **Features**: What's missing? What would you improve?

## How to Provide Feedback

- **In-app**: Profile → Settings → Send Feedback
- **Email**: beta-feedback@vettr.com

## Installation Notes

- Debug build - can install alongside any future release version
- App icon includes "DEBUG" badge
- First launch may take 10-15 seconds to load seed data

**Minimum Requirements**: Android 8.0 (API 26) or higher

Thank you for being an early tester! Your feedback will directly shape the final release. 🚀
```

---

#### Beta 2 (Bug Fixes)

```markdown
# VETTR Android Beta v1.0.0-beta.2

**Release Date**: 2026-02-17

## What's Fixed

- 🐛 Fixed crash when opening stock detail on older Android devices (API 26-28)
- 🐛 Fixed alert notifications not appearing on some Samsung devices
- 🐛 Fixed stock logos not loading correctly for CSE-listed stocks
- ⚡ Improved Pulse feed loading time (50% faster on slow connections)
- 🎨 Fixed dark theme color inconsistencies in alert settings

## What's New

- ➕ Added pull-to-refresh on all major screens
- 🔔 Alert notification sound now customizable (Settings)
- 📊 Added market cap and volume to stock list cards

## Focus Areas for Testing

Please verify:
1. Stock detail screens work smoothly on your device
2. Alert notifications appear correctly and on time
3. Stock logos display for all TSX-V and CSE stocks
4. Pull-to-refresh works on Pulse, Discovery, Alerts screens

## Known Issues

- Offline mode still under development
- Some users report slow first launch on low-end devices

## How to Provide Feedback

- **In-app**: Profile → Settings → Send Feedback
- **Email**: beta-feedback@vettr.com

Thanks for your continued testing! 🙏
```

---

## Feedback Collection

### In-App Feedback (Recommended)

Users can submit feedback directly from the app via **Profile → Settings → Send Feedback**.

Implementation already exists in `ProfileScreen.kt`:
- Feedback button opens email intent with pre-filled subject
- Includes device info, OS version, app version

### Firebase Analytics Events

Track beta tester behavior with custom events:

```kotlin
// Already implemented in ViewModels
analytics.logEvent("screen_view", Bundle().apply {
    putString("screen_name", "pulse")
})

analytics.logEvent("stock_favorite", Bundle().apply {
    putString("ticker", stock.ticker)
})
```

### Feedback Channels

1. **In-App Feedback Form**
   - Fastest for users
   - Auto-includes device/app info
   - Sent to: `beta-feedback@vettr.com`

2. **Email** (manual)
   - Send to: `beta-feedback@vettr.com`
   - Include: Device model, Android version, app version, screenshots

3. **Slack** (internal team only)
   - Channel: `#vettr-android-beta`
   - Real-time discussion
   - Bug triage and prioritization

4. **Firebase Crashlytics**
   - Automatic crash reporting
   - No user action required
   - Review crashes in Firebase Console

### Feedback Tracking Spreadsheet

Create a Google Sheet to track all beta feedback:

| Date | Tester | Category | Priority | Description | Status | Fixed In |
|------|--------|----------|----------|-------------|--------|----------|
| 2026-02-10 | john@example.com | Bug | High | App crashes on stock detail | In Progress | beta.2 |
| 2026-02-10 | jane@example.com | UX | Medium | Alert settings confusing | Backlog | - |
| 2026-02-11 | internal-team | Performance | High | Pulse feed slow on 3G | In Progress | beta.2 |

**Categories**: Bug, UX, Performance, Feature Request, Question

**Priority**: Critical, High, Medium, Low

**Status**: New, In Progress, Fixed, Backlog, Wont Fix

---

## Beta Testing Timeline

### Minimum 2-Week Beta Period

Firebase App Distribution beta testing should run for **at least 2 weeks** before Play Store submission.

### Recommended Timeline (4 Weeks)

#### Week 1: Internal Testing

- **Day 1**: Distribute **beta.1** to `internal-team` group only
- **Days 2-3**: Internal team tests core flows, reports critical bugs
- **Days 4-5**: Fix critical bugs, build **beta.2**
- **Day 6**: Distribute **beta.2** to `internal-team`, verify fixes
- **Day 7**: Internal QA sign-off

#### Week 2: External Beta Launch

- **Day 8**: Distribute **beta.2** (or **beta.3**) to `external-beta` group
- **Days 9-14**: External testers use app, submit feedback
- **Daily**: Monitor Firebase Crashlytics for crashes
- **Daily**: Review feedback in tracking spreadsheet

#### Week 3: Iteration & Fixes

- **Day 15**: Triage all feedback, prioritize fixes
- **Days 16-18**: Implement high-priority bug fixes and UX improvements
- **Day 19**: Build **beta.4**, distribute to all groups
- **Days 20-21**: Verify fixes, collect final feedback

#### Week 4: Final Testing & Launch Prep

- **Days 22-25**: No new features, bug fixes only
- **Days 26-27**: Build **release candidate** (RC1)
- **Day 28**: Final smoke testing, QA sign-off
- **Day 29**: Prepare Play Store listing, screenshots, release notes
- **Day 30**: Submit to Google Play Store! 🚀

---

### Key Milestones

- ✅ **Internal QA Pass**: All core flows working, no critical bugs
- ✅ **External Beta Launch**: At least 10 external testers actively using app
- ✅ **Crash-Free Rate >98%**: Based on Firebase Crashlytics data
- ✅ **Positive Feedback**: Majority of testers rate UX as "Good" or "Excellent"
- ✅ **Performance SLAs Met**: App startup <2s, screen loads <1s
- ✅ **Final QA Sign-Off**: No known critical or high-priority bugs

---

## Testing Checklist

### Before Each Beta Release

- [ ] Run all unit tests: `./gradlew testDebugUnitTest`
- [ ] Run detekt linting: `./gradlew detekt`
- [ ] Build debug APK: `./gradlew assembleDebug`
- [ ] Install APK on physical device, smoke test core flows
- [ ] Update version name in `build.gradle.kts` (e.g., `1.0.0-beta.2`)
- [ ] Write release notes following template
- [ ] Upload to Firebase App Distribution
- [ ] Send notification to tester groups
- [ ] Post in Slack #vettr-android-beta channel

### Core Flows to Test (Smoke Test)

1. **Authentication**
   - [ ] Google Sign-In works
   - [ ] Biometric unlock works
   - [ ] Logout and re-login works

2. **Pulse Feed**
   - [ ] Stock list loads within 2 seconds
   - [ ] Filters work (All, Favorites, Trending)
   - [ ] Pull-to-refresh works
   - [ ] Favorite/unfavorite stocks works

3. **Stock Detail**
   - [ ] Tap stock → stock detail loads
   - [ ] Switch between tabs (Overview, Pedigree, Filings, Alerts)
   - [ ] VETR Score displays correctly
   - [ ] Charts render (price, performance)

4. **Discovery**
   - [ ] Screener loads with 25+ stocks
   - [ ] Filters apply correctly (sector, market cap, VETR score)
   - [ ] Trending stocks section loads

5. **Pedigree**
   - [ ] Executive profiles display
   - [ ] Insider transactions load
   - [ ] SEC filings display with links

6. **Alerts**
   - [ ] Create new alert (price, volume, insider)
   - [ ] Alert saves and appears in list
   - [ ] Edit and delete alerts work
   - [ ] Push notification received (if triggerable)

7. **Profile**
   - [ ] User info displays (name, email, photo)
   - [ ] Settings screen loads
   - [ ] App version displayed
   - [ ] Logout works

### Device Compatibility Testing

Test on at least 3 different devices:

- [ ] **High-end device** (e.g., Samsung Galaxy S23, Pixel 8)
- [ ] **Mid-range device** (e.g., Samsung A54, OnePlus Nord)
- [ ] **Low-end device** (e.g., Motorola Moto G)

Test on at least 2 different Android versions:

- [ ] **Android 14** (latest)
- [ ] **Android 12** (2 versions old)

### Performance Verification

- [ ] App startup time <2 seconds (cold start)
- [ ] Screen transitions smooth (60 fps)
- [ ] No memory leaks (test with Android Profiler)
- [ ] API calls complete <500ms (on WiFi)
- [ ] Offline mode shows appropriate error (when implemented)

---

## Firebase Console - Monitoring Beta

### Track Beta Metrics

1. Go to Firebase Console → **App Distribution** → **Releases**
2. View metrics for each beta release:
   - **Downloads**: How many testers downloaded the build
   - **Adoption**: % of testers who updated to latest build
   - **Feedback**: Number of feedback submissions
   - **Crashes**: Crash-free rate for this build

### Monitor Crashes

1. Go to Firebase Console → **Crashlytics**
2. View crash reports:
   - Crash-free rate (target: >98%)
   - Top crashes by occurrence
   - Affected devices and OS versions
3. Prioritize crashes affecting >5% of users

### Analyze User Behavior

1. Go to Firebase Console → **Analytics** → **Events**
2. Review custom events:
   - `screen_view`: Most viewed screens
   - `stock_favorite`: Most favorited stocks
   - `alert_created`: Most common alert types
3. Identify drop-off points in user flows

---

## Beta Testing Success Criteria

Before graduating to Play Store release, verify:

- ✅ **At least 10 external beta testers** actively using app for 2+ weeks
- ✅ **Crash-free rate >98%** across all devices/OS versions
- ✅ **All critical bugs fixed** (no P0/P1 bugs remaining)
- ✅ **Positive UX feedback** from majority of testers
- ✅ **Performance SLAs met**: startup <2s, screen loads <1s, API <500ms
- ✅ **Core flows tested** on at least 3 different devices
- ✅ **QA sign-off** from internal team and lead tester

---

## Additional Resources

- [Firebase App Distribution Docs](https://firebase.google.com/docs/app-distribution)
- [Firebase Crashlytics Setup](https://firebase.google.com/docs/crashlytics/get-started?platform=android)
- [Firebase Analytics Events](https://firebase.google.com/docs/analytics/events)
- [Google Play Pre-Launch Report](https://support.google.com/googleplay/android-developer/answer/7002270)

---

## Next Steps After Beta

Once beta testing is complete:

1. Build **release APK**: `./gradlew assembleRelease`
2. Sign APK with production keystore
3. Upload to Google Play Console (Internal Testing track first)
4. Submit for review with Play Store assets (see `docs/PLAY_STORE_ASSETS.md`)
5. Promote to Production after Play Store review approval

---

**Document Version**: 1.0
**Last Updated**: 2026-02-08
**Maintained By**: VETTR Android Team
