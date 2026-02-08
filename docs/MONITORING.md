# VETTR Android - Monitoring and Observability

This document defines the monitoring strategy, SLAs, alert thresholds, and escalation procedures for the VETTR Android app.

## Overview

The VETTR app uses a comprehensive observability strategy to ensure optimal performance and reliability for our users. We track key performance indicators (KPIs) and maintain strict service level agreements (SLAs) to deliver a high-quality experience.

## Monitoring Stack

### Current Implementation (Development/Beta)
- **Crash Tracking**: MockObservabilityService with Logcat logging
- **Performance Metrics**: ObservabilityService interface with in-memory tracking
- **Analytics**: MockAnalyticsService with Logcat logging

### Production Implementation (Planned)
- **Crash Tracking**: Firebase Crashlytics
- **Performance Monitoring**: Firebase Performance Monitoring
- **Analytics**: Firebase Analytics
- **Real User Monitoring (RUM)**: Firebase Performance Monitoring traces
- **Error Reporting**: Firebase Crashlytics with custom error reporting

## Service Level Agreements (SLAs)

### P0 - Critical
| Metric | Target | Alert Threshold | Impact |
|--------|--------|-----------------|--------|
| Crash-free rate | >99% | <99% | Users cannot use the app |
| App availability | >99.9% | <99.9% | Users cannot access the app |
| Auth success rate | >99% | <99% | Users cannot sign in |

### P1 - High Priority
| Metric | Target | Alert Threshold | Impact |
|--------|--------|-----------------|--------|
| App startup time (cold) | <2s | >2000ms | Poor first impression |
| App startup time (warm) | <1s | >1000ms | Noticeable lag |
| API auth latency | <500ms | >500ms | Slow sign-in experience |

### P2 - Medium Priority
| Metric | Target | Alert Threshold | Impact |
|--------|--------|-----------------|--------|
| Screen load time | <1s | >1000ms | Sluggish navigation |
| API response time | <500ms | >500ms | Slow data loading |
| Memory usage | <80% | >80% of max | Risk of OOM crashes |
| ANR rate | <0.1% | >0.1% | App appears frozen |

### P3 - Low Priority
| Metric | Target | Alert Threshold | Impact |
|--------|--------|-----------------|--------|
| Image load time | <2s | >2000ms | Delayed content display |
| Search latency | <300ms | >300ms | Slightly slow search |
| Navigation latency | <100ms | >100ms | Minor UX degradation |

## Key Performance Indicators (KPIs)

### App Health
- **Crash-free users**: Percentage of users who don't experience crashes
- **ANR rate**: Application Not Responding events per session
- **Fatal error rate**: Unrecoverable errors per session

### Performance
- **Cold start time**: Time from app launch to first frame (ContentProvider.onCreate → Application.onCreate)
- **Warm start time**: Time from app resume to first frame
- **Screen load time**: Time from navigation to data displayed (ViewModel init → first data emission)
- **API response time**: Time from request to response for each endpoint
- **Memory footprint**: Average and peak memory usage

### Engagement
- **Session duration**: Average time users spend in the app
- **Screen views**: Most/least visited screens
- **Feature adoption**: Usage of key features (watchlist, alerts, discovery)
- **Retention rate**: D1, D7, D30 retention

## Tracked Metrics

### Automatic Tracking

#### App Startup
```kotlin
// Tracked in VettrApp.onCreate()
observabilityService.trackAppStartup(durationMs)
```
- Measures time from process start (ContentProvider.onCreate) to Application.onCreate complete
- Alert: >2000ms (P1)

#### Screen Load Times
```kotlin
// Tracked in each ViewModel
observabilityService.trackScreenLoadTime(screenName, durationMs)
```
Screens tracked:
- Pulse
- Discovery
- Stocks
- Stock Detail
- Pedigree
- Alerts
- Alert Rule Creator
- Profile

Alert threshold: >1000ms (P2)

#### Memory Usage
```kotlin
// Tracked by MemoryMonitor
observabilityService.trackMemoryUsage(usedMemoryMb, maxMemoryMb)
```
- Tracks current memory usage vs. max available
- Alert: >80% usage (P2)

### Manual Tracking

#### API Performance
```kotlin
val traceId = observabilityService.startTrace("api_stocks_list")
try {
    val response = api.getStocks()
    observabilityService.trackApiCall(
        endpoint = "/stocks",
        durationMs = duration,
        success = true,
        statusCode = 200
    )
} finally {
    observabilityService.stopTrace(traceId)
}
```

#### Custom Operations
```kotlin
observabilityService.trackCustomMetric(
    metricName = "database_query_time",
    value = durationMs.toDouble(),
    unit = "ms"
)
```

## Alert Thresholds and Escalation

### P0 - Critical (Immediate Response)
**Response Time**: <15 minutes
**Escalation**: Page on-call engineer immediately

**Triggers**:
- Crash-free rate drops below 99%
- App availability drops below 99.9%
- Auth success rate drops below 99%

**Actions**:
1. Immediate page to on-call engineer
2. Create incident in incident management system
3. Notify engineering lead and product manager
4. Begin root cause analysis
5. Prepare rollback if necessary

### P1 - High (Urgent Response)
**Response Time**: <1 hour
**Escalation**: Slack alert to engineering channel

**Triggers**:
- Cold startup time exceeds 2000ms (95th percentile)
- API auth latency exceeds 500ms (95th percentile)

**Actions**:
1. Alert engineering channel in Slack
2. Create high-priority ticket
3. Investigate within 1 hour
4. Identify root cause and remediation plan

### P2 - Medium (Standard Response)
**Response Time**: <4 hours
**Escalation**: Email to engineering team

**Triggers**:
- Screen load time exceeds 1000ms (95th percentile)
- API response time exceeds 500ms (95th percentile)
- Memory usage exceeds 80%
- ANR rate exceeds 0.1%

**Actions**:
1. Email engineering team
2. Create medium-priority ticket
3. Investigate during business hours
4. Address in next sprint if not critical

### P3 - Low (Monitoring Only)
**Response Time**: Best effort
**Escalation**: Dashboard monitoring only

**Triggers**:
- Image load time exceeds 2000ms
- Search latency exceeds 300ms
- Navigation latency exceeds 100ms

**Actions**:
1. Log to monitoring dashboard
2. Create backlog ticket if persistent
3. Address in future sprint planning

## Monitoring Dashboard

### Key Metrics to Display

1. **App Health**
   - Crash-free rate (24h, 7d, 30d)
   - ANR rate
   - Error rate by severity

2. **Performance**
   - Cold start time (p50, p95, p99)
   - Screen load times by screen
   - API response times by endpoint
   - Memory usage (average, p95)

3. **Engagement**
   - Active users (DAU, MAU)
   - Session duration
   - Screen views by screen
   - Feature adoption rates

4. **Technical**
   - API error rates by endpoint
   - Network failure rate
   - Cache hit rate
   - Database query times

## Implementation Guide

### Setting Up Firebase (Production)

1. **Add Firebase to your project**
   ```gradle
   // app/build.gradle.kts
   dependencies {
       implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
       implementation("com.google.firebase:firebase-crashlytics-ktx")
       implementation("com.google.firebase:firebase-analytics-ktx")
       implementation("com.google.firebase:firebase-perf-ktx")
   }
   ```

2. **Create FirebaseObservabilityService**
   ```kotlin
   @Singleton
   class FirebaseObservabilityService @Inject constructor(
       private val crashlytics: FirebaseCrashlytics,
       private val performance: FirebasePerformance,
       private val analytics: FirebaseAnalytics
   ) : ObservabilityService {
       // Implement interface methods using Firebase APIs
   }
   ```

3. **Update ObservabilityModule**
   ```kotlin
   @Binds
   @Singleton
   abstract fun bindObservabilityService(
       firebaseObservabilityService: FirebaseObservabilityService
   ): ObservabilityService
   ```

### Adding Custom Traces

```kotlin
// In your ViewModel or Repository
val traceId = observabilityService.startTrace("load_stocks")
try {
    val stocks = stockRepository.getStocks()
    observabilityService.stopTrace(traceId, mapOf(
        "stock_count" to stocks.size.toString(),
        "cache_hit" to cacheHit.toString()
    ))
} catch (e: Exception) {
    observabilityService.stopTrace(traceId, mapOf("error" to e.message.orEmpty()))
    throw e
}
```

### Tracking SLA Violations

```kotlin
// Automatically tracked by MockObservabilityService
if (durationMs > threshold) {
    observabilityService.trackSlaViolation(
        slaName = "app_startup_time",
        actualValue = durationMs.toDouble(),
        threshold = 2000.0
    )
}
```

## Alerting Configuration

### Slack Integration
```yaml
alerts:
  - name: high_crash_rate
    condition: crash_free_rate < 0.99
    severity: P0
    channel: "#vettr-alerts-critical"

  - name: slow_startup
    condition: cold_start_p95 > 2000
    severity: P1
    channel: "#vettr-alerts-high"
```

### Email Notifications
```yaml
escalation:
  P0:
    - type: pagerduty
      immediate: true
  P1:
    - type: slack
      channel: "#vettr-alerts-high"
    - type: email
      recipients: ["engineering@vettr.com"]
  P2:
    - type: email
      recipients: ["engineering@vettr.com"]
```

## Runbooks

### High Crash Rate (P0)
1. Check Firebase Crashlytics for crash clusters
2. Identify affected versions and devices
3. Analyze crash stack traces
4. If widespread: prepare hotfix or rollback
5. If isolated: create targeted fix for next release

### Slow Startup Time (P1)
1. Check Firebase Performance Monitoring for startup traces
2. Analyze ContentProvider and Application.onCreate timing
3. Review recent changes that affect startup path
4. Identify bottlenecks (database, network, initialization)
5. Optimize or defer non-critical startup tasks

### High Memory Usage (P2)
1. Check MemoryMonitor logs for usage patterns
2. Use Android Profiler to identify memory leaks
3. Review image cache configuration (Coil)
4. Check for retained ViewModel instances
5. Optimize memory-intensive operations

## Testing SLAs

### Load Testing
- Simulate 1000+ concurrent users
- Verify API response times stay <500ms
- Ensure startup time remains <2s

### Stress Testing
- Test app under low memory conditions
- Verify graceful degradation
- Ensure no crashes under resource constraints

### Performance Regression Testing
- Run automated performance tests on each PR
- Block merges that degrade startup time >10%
- Alert on screen load time regressions

## Continuous Improvement

### Weekly Review
- Review dashboard metrics
- Identify trends and anomalies
- Prioritize performance improvements

### Monthly Review
- Analyze SLA adherence
- Update thresholds based on data
- Refine alerting rules
- Review escalation effectiveness

### Quarterly Review
- Benchmark against competitors
- Set new performance goals
- Plan major performance initiatives
- Update monitoring strategy

## Contact and Support

- **Engineering Team**: engineering@vettr.com
- **On-Call Engineer**: Use PagerDuty
- **Incident Response**: Slack #vettr-incidents
- **Performance Questions**: Slack #vettr-performance

---

Last Updated: 2026-02-08
Version: 1.0
