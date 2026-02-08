# Google Play Store Submission Assets

This document contains all required assets and information for submitting VETTR to the Google Play Store.

---

## App Listing Information

### App Name
**VETTR**

### Short Description (80 characters max)
Intelligence platform for venture and micro-cap investors

### Full Description (4000 characters max)

**VETTR - Venture Capital Intelligence Platform**

VETTR is the comprehensive intelligence platform designed specifically for venture and micro-cap investors. Built for serious investors who need deep insights into TSX-V, CSE, and emerging market opportunities, VETTR transforms complex data into actionable intelligence.

**Key Features:**

**Pulse Dashboard**
- Real-time market activity from TSX-V and CSE exchanges
- Top movers, volume leaders, and sector performance at a glance
- Smart watchlist with instant updates on your tracked stocks
- VETR Score: Our proprietary rating system evaluating companies across 5 critical dimensions

**Discovery Engine**
- Advanced filtering by sector, market cap, exchange, and VETR Score
- Red flag detection: identify regulatory filings, insider selling, and other warning signs
- Trending stocks with momentum indicators
- Sector-based exploration to find hidden opportunities

**Stock Intelligence**
- Comprehensive company profiles with key metrics and historical performance
- Real-time price charts with technical indicators
- Complete filing history: SEDAR+ integration for regulatory documents
- Executive pedigree analysis: track management backgrounds and track records
- Red flag monitoring: stay informed about potential risks

**Pedigree Analysis**
- Deep dive into executive backgrounds and career histories
- Track management movements across companies
- Identify patterns in successful leadership teams
- Historical performance of executives at previous companies

**Smart Alerts**
- Custom alert rules based on price, volume, filings, or VETR Score changes
- Push notifications for time-sensitive opportunities
- Red flag warnings: automatic alerts for regulatory issues
- Watchlist notifications: never miss important updates

**Professional Features**
- Dark theme optimized for extended research sessions
- Offline access to cached data and watchlists
- Biometric authentication for secure access
- Google Sign-In integration
- Background sync keeps your data current

**Why VETTR?**

The venture and micro-cap market is complex, fast-moving, and often opaque. VETTR cuts through the noise by aggregating data from multiple sources, applying proprietary scoring algorithms, and presenting it in an intuitive, mobile-first interface.

Whether you're a seasoned investor tracking dozens of positions or just beginning to explore micro-cap opportunities, VETTR provides the intelligence you need to make informed decisions.

**Data Sources:**
- Real-time market data from TSX-V and CSE
- SEDAR+ regulatory filings
- Executive background verification
- Historical price and volume data
- Sector and industry classifications

**Security & Privacy:**
- Bank-level encryption for all data transmission
- Biometric authentication support
- No sharing of personal investment data
- Google Sign-In for secure authentication
- Local data encryption

**Perfect for:**
- Venture capital investors
- Micro-cap stock traders
- Research analysts
- Financial advisors
- Individual investors exploring emerging markets

Download VETTR today and gain the intelligence advantage in venture and micro-cap investing.

**Note:** VETTR provides research and analysis tools. Investment decisions should be made based on your own research and risk tolerance. Past performance does not guarantee future results.

### Category
**Finance**

### Contact Email
support@vettr.app

### Website
https://vettr.app

### Content Rating
**Everyone** - App contains financial data and analysis tools. No age restrictions required.

### Target Audience
- Investors (18+)
- Financial professionals
- Research analysts

---

## Screenshot Specifications

### Phone Screenshots (Required)
- **Dimensions:** 16:9 aspect ratio minimum
- **Recommended:** 1080 x 1920 pixels (portrait) or 1920 x 1080 pixels (landscape)
- **Format:** PNG or JPEG (24-bit RGB, no alpha)
- **Quantity:** Minimum 2, maximum 8
- **Required Screenshots:**
  1. **Pulse Dashboard** - Show watchlist with real-time data, top movers, VETR scores
  2. **Stock Detail** - Display comprehensive stock profile with charts and metrics
  3. **Discovery Screen** - Show filtering options and search results
  4. **Alerts Dashboard** - Display active alerts and notification setup
  5. **Executive Pedigree** - Show executive background and history
  6. **Filing History** - Display regulatory filings list
  7. **Dark Theme** - Showcase the professional dark UI

### 7-inch Tablet Screenshots (Optional but Recommended)
- **Dimensions:** 1024 x 1920 pixels (portrait) or 1920 x 1024 pixels (landscape)
- **Format:** PNG or JPEG (24-bit RGB, no alpha)
- **Quantity:** Minimum 2, maximum 8
- **Notes:** Show responsive layout with better use of space

### 10-inch Tablet Screenshots (Optional but Recommended)
- **Dimensions:** 1536 x 2048 pixels (portrait) or 2048 x 1536 pixels (landscape)
- **Format:** PNG or JPEG (24-bit RGB, no alpha)
- **Quantity:** Minimum 2, maximum 8
- **Notes:** Show tablet-optimized layouts with multi-pane views

### Screenshot Guidelines
- Use actual app screenshots (no mockups or composites)
- Show realistic data (use the 25 Canadian stocks seed data)
- No user personal information visible
- Screenshots should represent current app version
- Highlight key features and value propositions
- Maintain consistent status bar and navigation elements
- Use high-quality images (no pixelation or compression artifacts)

---

## Feature Graphic

### Specifications
- **Dimensions:** 1024 x 500 pixels
- **Format:** PNG or JPEG (24-bit RGB, no alpha)
- **File Size:** Maximum 1 MB
- **Usage:** Displayed at the top of the Play Store listing

### Design Requirements
- **Background:** Navy (#0D1B2A) matching app theme
- **Logo:** VETTR wordmark in white or green accent (#00C853)
- **Tagline:** "Intelligence Platform for Venture Investors"
- **Visual Elements:**
  - Subtle chart or graph elements suggesting financial data
  - Clean, professional design
  - Readable on both light and dark Play Store themes
- **No Text Clipping:** Ensure all text is fully visible
- **No Screenshots:** Use original graphics, not app screenshots
- **Brand Consistency:** Match app's dark, professional aesthetic

### Example Layout
```
┌─────────────────────────────────────────────────┐
│                                                 │
│         [VETTR Logo]                           │
│                                                 │
│    Intelligence Platform for Venture Investors │
│                                                 │
│         [Subtle chart/graph element]           │
│                                                 │
└─────────────────────────────────────────────────┘
```

---

## App Icon

### Specifications
- **Dimensions:** 512 x 512 pixels
- **Format:** PNG (32-bit, with alpha channel)
- **File Size:** Maximum 1 MB
- **Design:** Follows adaptive icon guidelines
  - Safe zone: 66dp diameter circle
  - Foreground layer: Icon graphic
  - Background layer: Solid color or gradient

### Current Icon
The VETTR app icon is already configured in:
- `app/src/main/res/mipmap-*/ic_launcher.png`
- `app/src/main/res/mipmap-*/ic_launcher_round.png`
- `app/src/main/ic_launcher-playstore.png` (512x512 for Play Store)

---

## Privacy Policy

### Privacy Policy URL
**Required:** Must be hosted and accessible before Play Store submission

### Recommended URL
`https://vettr.app/privacy-policy`

### Privacy Policy Content

See `PRIVACY_POLICY.md` in this directory for the complete privacy policy content.

**Key Points to Cover:**
1. **Information Collection**
   - Google Sign-In authentication data
   - Device identifiers for sync
   - Usage analytics (Firebase Analytics)
   - Crash reports (Firebase Crashlytics)

2. **Data Usage**
   - Personalization of watchlists and alerts
   - App improvement and debugging
   - No selling of user data
   - No third-party advertising

3. **Data Storage**
   - Local device storage (Room database)
   - Encrypted cloud backup (if implemented)
   - Secure transmission (HTTPS/TLS)

4. **Third-Party Services**
   - Google Sign-In (authentication)
   - Firebase (analytics, crashlytics)
   - Backend API (market data)

5. **User Rights**
   - Data access and export
   - Account deletion
   - Opt-out of analytics
   - GDPR compliance (EU users)
   - CCPA compliance (California users)

6. **Security Measures**
   - Biometric authentication
   - Encrypted data transmission
   - Secure token storage

7. **Contact Information**
   - Email: privacy@vettr.app
   - Data protection officer contact

---

## Terms of Service

### Terms of Service URL
**Required:** Must be hosted and accessible before Play Store submission

### Recommended URL
`https://vettr.app/terms-of-service`

### Terms of Service Content

See `TERMS_OF_SERVICE.md` in this directory for the complete terms of service.

**Key Sections to Include:**

1. **Service Description**
   - VETTR provides financial research tools
   - Not financial advice or recommendations
   - Data provided "as-is" with no guarantees of accuracy

2. **Investment Disclaimers**
   - All investments carry risk
   - Past performance doesn't guarantee future results
   - Users are responsible for their own investment decisions
   - VETTR is not a registered investment advisor

3. **Acceptable Use**
   - Personal, non-commercial use only
   - No automated scraping or data extraction
   - No reverse engineering
   - Compliance with securities laws

4. **Liability Limitations**
   - No liability for investment losses
   - No liability for data inaccuracies
   - No liability for service interruptions
   - Limited to amount paid for service (free tier = no liability)

5. **Data Accuracy Disclaimer**
   - Market data may be delayed
   - Filing data sourced from public records
   - VETR scores are proprietary and subjective
   - Users should verify information independently

6. **Intellectual Property**
   - VETTR brand and trademarks
   - Proprietary algorithms and scoring systems
   - User data ownership

7. **Account Termination**
   - Right to suspend or terminate accounts
   - Conditions for termination
   - Effect on user data

8. **Dispute Resolution**
   - Governing law and jurisdiction
   - Arbitration clauses
   - Class action waiver

9. **Changes to Terms**
   - Right to modify terms with notice
   - Continued use implies acceptance

---

## App Rating Questionnaire

### Content Rating: Everyone

**Violence**
- No violence or threatening content: **YES**

**Sexual Content**
- No sexual or suggestive content: **YES**

**Language**
- No profanity or crude humor: **YES**

**Controlled Substances**
- No references to drugs, alcohol, or tobacco: **YES**

**Gambling**
- No simulated gambling: **YES**
- **Note:** Stock trading is investment, not gambling in Play Store context

**User Interaction**
- Users can interact or exchange information: **NO**
- No user-generated content
- No social features or chat

**Personal Information**
- App shares user's physical location with other users: **NO**
- App shares user's personal information with other users: **NO**
- App collects personal information (email via Google Sign-In): **YES**

**Advertising**
- App contains ads: **NO**

**Financial Features**
- App facilitates financial transactions: **NO**
- App displays financial information: **YES**
- **Note:** Display only, no actual trading or transactions

### COPPA Compliance (Children's Online Privacy Protection Act)

**Target Audience**
- App is NOT directed at children under 13: **YES**
- Age Gate Required: **NO** (Finance category, implicitly 18+)

**Compliance Statement**
- VETTR does not knowingly collect information from children under 13
- If we discover such data, it will be deleted immediately
- Parents/guardians can contact privacy@vettr.app

---

## Release Notes for Version 1.0.0

### Version 1.0.0 - Initial Release

**What's New**

VETTR brings professional-grade intelligence to venture and micro-cap investors. Our initial release includes:

**Market Intelligence**
- Real-time data from TSX-V and CSE exchanges
- Proprietary VETR Score rating system
- Comprehensive stock profiles with charts and metrics
- Complete regulatory filing history via SEDAR+ integration

**Discovery & Research**
- Advanced filtering by sector, market cap, and more
- Red flag detection for potential risks
- Executive pedigree analysis
- Trending stocks and sector performance

**Personalization**
- Custom watchlists with real-time updates
- Smart alert system with push notifications
- Biometric authentication for secure access
- Dark theme optimized for extended research sessions

**Key Features**
- Pulse Dashboard: Your command center for market activity
- Discovery Engine: Find opportunities with advanced filters
- Stock Intelligence: Deep dive into company fundamentals
- Pedigree Analysis: Evaluate executive track records
- Smart Alerts: Stay informed with custom notifications

**Security & Performance**
- Bank-level encryption
- Biometric authentication
- Offline access to cached data
- Background sync for up-to-date information

Thank you for choosing VETTR. We're committed to providing the best intelligence platform for venture and micro-cap investors.

Have feedback? Contact us at support@vettr.app

---

## Localization

### Primary Language
**English (United States)**

### Future Localization (Phase 2)
- English (Canada)
- French (Canada)

---

## Store Listing Tags

### Keywords for ASO (App Store Optimization)
- venture capital
- stock research
- TSX-V
- CSE
- investment intelligence
- micro-cap stocks
- Canadian stocks
- SEDAR
- regulatory filings
- stock analysis
- financial research
- investor tools
- market intelligence
- penny stocks
- emerging markets
- venture investors
- stock screening
- investment research
- financial data
- stock alerts

### Promotional Text (170 characters)
Professional intelligence for venture investors. Track TSX-V & CSE stocks, analyze filings, monitor executives, and get alerts on opportunities.

---

## Distribution

### Countries
- **Initial Launch:** Canada
- **Phase 2:** United States (pending regulatory review)

### Pricing
- **Free tier:** Full access to all features
- **Future:** Premium tier with advanced analytics (Phase 2)

---

## Technical Requirements

### Minimum SDK
- **minSdk:** 26 (Android 8.0 Oreo)

### Target SDK
- **targetSdk:** 34 (Android 14)

### Supported Devices
- Phone (compact)
- Tablet (7-inch and 10-inch)
- Foldable devices

### Screen Sizes
- Small (phone)
- Normal (phone)
- Large (7-inch tablet)
- XLarge (10-inch tablet)

### Required Permissions
- INTERNET (market data)
- ACCESS_NETWORK_STATE (connectivity monitoring)
- USE_BIOMETRIC (biometric authentication)
- POST_NOTIFICATIONS (alert notifications, Android 13+)

---

## Pre-Launch Checklist

- [ ] All screenshots generated and reviewed
- [ ] Feature graphic created and approved
- [ ] Privacy policy hosted and accessible
- [ ] Terms of service hosted and accessible
- [ ] App icon finalized (512x512 PNG)
- [ ] Release notes written and reviewed
- [ ] Content rating questionnaire completed
- [ ] Test on multiple devices (phone, 7-inch tablet, 10-inch tablet)
- [ ] Verify all Play Store listing text
- [ ] Promo video (optional, Phase 2)
- [ ] Beta testing completed via Firebase App Distribution
- [ ] Final QA sign-off
- [ ] Legal review of privacy policy and terms
- [ ] Verify analytics and crash reporting configured
- [ ] Release build signed with production keystore

---

## Support Information

### Developer Contact
- **Email:** support@vettr.app
- **Website:** https://vettr.app
- **Privacy Questions:** privacy@vettr.app

### Support Channels
- In-app feedback form (Phase 2)
- Email support
- Website FAQ (to be created)

---

## Notes

- All assets must be finalized before Play Store submission
- Privacy policy and terms of service must be hosted on vettr.app domain
- Screenshots should use realistic seed data (25 Canadian stocks)
- Feature graphic should be professionally designed (consider hiring designer)
- Beta testing period minimum 2 weeks before public launch
- Monitor Play Store console for policy compliance warnings
- Keep release notes concise and user-focused
- Update this document as requirements change

**Document Version:** 1.0.0
**Last Updated:** 2026-02-08
**Owner:** Product Team
