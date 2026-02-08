# VETTR Android - Phase 2 Notes

This document outlines known issues, limitations, and recommendations for Phase 2 development.

## Phase 1 Completion Status

All 155 user stories have been completed and verified:
- ✅ Build: `./gradlew clean assembleDebug` succeeds
- ✅ Tests: `./gradlew testDebugUnitTest` passes all unit tests
- ✅ Release: `./gradlew assembleRelease` succeeds with ProGuard
- ✅ Lint: `./gradlew detekt` passes with zero errors

## Known Limitations

### 1. Mock Data
- The app currently uses mock/seed data for stocks, filings, executives, and alerts
- Real API integration is stubbed out but not fully implemented
- Network calls return mock data from local repositories

### 2. Authentication
- Google Sign-In is implemented but not connected to a real backend
- No actual user account creation or session management
- Auth tokens are not persisted or refreshed with a real server
- Biometric authentication works locally but doesn't validate with backend

### 3. Sync Functionality
- WorkManager is configured but sync operations use mock data
- No actual data synchronization with remote server
- Sync status updates are simulated

### 4. Network Security
- Certificate pinning is configured but not enabled (no actual pins)
- Network security config is ready but needs actual certificate pins when backend is available

### 5. Real-time Features
- Stock price updates are simulated, not real-time
- Alert notifications are generated locally, not from server push
- No WebSocket or SSE implementation for live data

### 6. Pedigree Tab
- Executive tracking and red flags are based on mock data
- No actual SEC filing parsing or data extraction
- Ownership change calculations are simulated

### 7. VETR Score
- Algorithm is implemented but not validated against real data
- No A/B testing or score calibration performed
- Scores are calculated from mock metrics

## Recommendations for Phase 2

### High Priority

1. **Backend Integration**
   - Implement real REST API endpoints
   - Connect authentication flow to actual user management system
   - Replace mock repositories with real API data sources
   - Implement proper error handling for network failures

2. **Real-time Data**
   - Implement WebSocket connection for live stock updates
   - Add server-sent events for alert notifications
   - Implement proper data reconciliation on reconnect

3. **Data Persistence**
   - Ensure Room database properly caches API responses
   - Implement offline-first architecture
   - Add conflict resolution for sync operations

4. **Testing**
   - Add integration tests for API endpoints
   - Add UI tests for critical user flows
   - Implement screenshot testing for visual regression
   - Add performance testing for large data sets

### Medium Priority

5. **Certificate Pinning**
   - Add actual certificate pins for api.vettr.com
   - Test pin rotation strategy
   - Implement backup pins

6. **Analytics & Monitoring**
   - Connect ObservabilityService to real analytics platform
   - Implement crash reporting (e.g., Crashlytics)
   - Add performance monitoring
   - Set up A/B testing framework

7. **Push Notifications**
   - Implement FCM for push notifications
   - Add notification preferences and management
   - Handle notification clicks and deep links

8. **App Distribution**
   - Set up Google Play Console
   - Configure app signing
   - Create release tracks (internal, alpha, beta, production)
   - Set up fastlane for automated deployment

### Low Priority

9. **Accessibility**
   - Add content descriptions for all interactive elements
   - Test with TalkBack screen reader
   - Ensure proper focus order
   - Add semantic labels for Compose components

10. **Localization**
    - Complete French translations
    - Add support for additional languages
    - Test RTL layout support

11. **Advanced Features**
    - Add portfolio management
    - Implement watchlist import/export
    - Add comparison views for multiple stocks
    - Implement custom alert conditions

## Technical Debt

1. **Deprecation Warnings**
   - Some Compose APIs show deprecation warnings (e.g., Icons.Filled.TrendingUp)
   - OkHttp ResponseBody.create() uses deprecated signature
   - Android TRIM_MEMORY constants are deprecated

2. **Detekt Configuration**
   - Some deprecated properties in detekt.yml need updating
   - Consider migrating to newer rule names

3. **Gradle**
   - Using features deprecated in Gradle 9.0
   - Consider upgrading when Gradle 9.0 is released

4. **Build Warnings**
   - Some native libraries cannot be stripped (libandroidx.graphics.path.so, libdatastore_shared_counter.so)
   - SDK 36 warning suppressed in gradle.properties

## Security Considerations

1. **API Keys**
   - Need strategy for storing API keys securely
   - Consider using Google Secrets Manager or similar
   - Implement key rotation policy

2. **Data Privacy**
   - Implement data retention policies
   - Add user data export functionality (GDPR)
   - Add user data deletion (right to be forgotten)
   - Update privacy policy

3. **ProGuard/R8**
   - Current rules are basic, may need adjustment with real API
   - Test obfuscation doesn't break reflection-based code
   - Verify all necessary classes are kept

## Performance Notes

Current build performance:
- Clean build: ~17 seconds
- Incremental build (no changes): ~0.8 seconds
- Incremental build (single file): ~3-4 seconds

These are good baseline numbers. Monitor build times as the project grows.

## Next Steps

1. Set up backend API (or mock API server for development)
2. Replace mock repositories with real API implementations
3. Implement proper authentication flow with backend
4. Add comprehensive error handling
5. Set up CI/CD pipeline
6. Begin beta testing with internal users
7. Collect feedback and iterate

---

*Last Updated: 2026-02-08*
*Phase 1 Status: Complete*
