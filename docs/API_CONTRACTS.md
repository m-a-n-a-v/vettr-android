# VETTR API Contracts

This document specifies the REST API endpoints expected by the VETTR Android application. Backend developers should implement these contracts to ensure seamless integration with the mobile client.

## Table of Contents

- [Base URL](#base-url)
- [Authentication](#authentication)
- [Error Codes](#error-codes)
- [Rate Limiting](#rate-limiting)
- [Pagination](#pagination)
- [Auth Endpoints](#auth-endpoints)
- [Stock Endpoints](#stock-endpoints)
- [Filing Endpoints](#filing-endpoints)
- [Executive Endpoints](#executive-endpoints)
- [Alert Rule Endpoints](#alert-rule-endpoints)
- [Red Flag Endpoints](#red-flag-endpoints)
- [VETR Score Endpoints](#vetr-score-endpoints)

---

## Base URL

```
Production: https://api.vettr.com/v1
Staging: https://staging-api.vettr.com/v1
```

All endpoints are prefixed with the base URL.

---

## Authentication

VETTR uses Bearer token authentication. Include the access token in the Authorization header:

```
Authorization: Bearer <access_token>
```

Tokens expire after 24 hours. Use the refresh token to obtain a new access token without requiring re-authentication.

---

## Error Codes

All error responses follow this format:

```json
{
  "error": {
    "code": "ERROR_CODE",
    "message": "Human-readable error message",
    "details": {}
  }
}
```

### Standard HTTP Status Codes

| Code | Description |
|------|-------------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request - Invalid parameters |
| 401 | Unauthorized - Invalid or expired token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource doesn't exist |
| 422 | Unprocessable Entity - Validation error |
| 429 | Too Many Requests - Rate limit exceeded |
| 500 | Internal Server Error |
| 503 | Service Unavailable - Maintenance mode |

### Error Code Reference

| Error Code | HTTP Status | Description |
|------------|-------------|-------------|
| `INVALID_TOKEN` | 401 | Access token is invalid or expired |
| `INVALID_REFRESH_TOKEN` | 401 | Refresh token is invalid or expired |
| `INVALID_ID_TOKEN` | 400 | Google ID token validation failed |
| `USER_NOT_FOUND` | 404 | User account doesn't exist |
| `STOCK_NOT_FOUND` | 404 | Stock ticker not found |
| `RATE_LIMIT_EXCEEDED` | 429 | Too many requests |
| `VALIDATION_ERROR` | 422 | Request validation failed |
| `INTERNAL_ERROR` | 500 | Unexpected server error |

---

## Rate Limiting

Rate limits are applied per user (identified by access token) or per IP address (for unauthenticated requests).

| Endpoint Category | Rate Limit |
|-------------------|------------|
| Authentication | 10 requests/minute |
| Stock Data | 100 requests/minute |
| Filings | 100 requests/minute |
| Alert Rules | 50 requests/minute |
| Red Flags | 100 requests/minute |
| VETR Score | 100 requests/minute |

Rate limit information is included in response headers:

```
X-RateLimit-Limit: 100
X-RateLimit-Remaining: 95
X-RateLimit-Reset: 1672531200
```

When rate limit is exceeded, the response includes:

```json
{
  "error": {
    "code": "RATE_LIMIT_EXCEEDED",
    "message": "Too many requests. Try again in 45 seconds.",
    "details": {
      "retry_after": 45
    }
  }
}
```

---

## Pagination

List endpoints support cursor-based pagination for efficient data retrieval.

### Request Parameters

- `limit` (integer, optional): Number of items per page (default: 50, max: 100)
- `cursor` (string, optional): Pagination cursor from previous response

### Response Format

```json
{
  "data": [...],
  "pagination": {
    "next_cursor": "eyJpZCI6MTIzNDU2fQ==",
    "has_more": true,
    "total_count": 250
  }
}
```

### Example Request

```
GET /stocks?limit=20&cursor=eyJpZCI6MTIzNDU2fQ==
```

---

## Auth Endpoints

### POST /auth/signup

Create a new user account using Google Sign-In.

**Request:**

```json
{
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "provider": "google",
  "display_name": "John Doe",
  "avatar_url": "https://lh3.googleusercontent.com/..."
}
```

**Response (201):**

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "rt_1234567890abcdef",
  "token_type": "Bearer",
  "expires_in": 86400,
  "user": {
    "id": "usr_1234567890",
    "email": "john@example.com",
    "display_name": "John Doe",
    "avatar_url": "https://lh3.googleusercontent.com/...",
    "tier": "free",
    "created_at": 1672531200000
  }
}
```

---

### POST /auth/login

Authenticate an existing user using Google Sign-In.

**Request:**

```json
{
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
  "provider": "google"
}
```

**Response (200):**

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "rt_1234567890abcdef",
  "token_type": "Bearer",
  "expires_in": 86400,
  "user": {
    "id": "usr_1234567890",
    "email": "john@example.com",
    "display_name": "John Doe",
    "avatar_url": "https://lh3.googleusercontent.com/...",
    "tier": "premium",
    "created_at": 1672531200000
  }
}
```

---

### POST /auth/refresh

Refresh an expired access token using a refresh token.

**Request:**

```json
{
  "refresh_token": "rt_1234567890abcdef"
}
```

**Response (200):**

```json
{
  "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refresh_token": "rt_0987654321fedcba",
  "token_type": "Bearer",
  "expires_in": 86400
}
```

**Error Response (401):**

```json
{
  "error": {
    "code": "INVALID_REFRESH_TOKEN",
    "message": "Refresh token is invalid or expired. Please log in again."
  }
}
```

---

### POST /auth/logout

Invalidate the current access and refresh tokens.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Request Body:**

```json
{
  "refresh_token": "rt_1234567890abcdef"
}
```

**Response (200):**

```json
{
  "success": true,
  "message": "Successfully logged out"
}
```

---

### POST /auth/google

Authenticate using Google Sign-In ID token. This endpoint handles both signup and login.

**Request:**

```json
{
  "id_token": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response (200):**

Same as `/auth/login` response.

---

## Stock Endpoints

### GET /stocks

Fetch all available stocks with market data and VETR scores.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Query Parameters:**

- `limit` (integer, optional): Number of stocks per page (default: 50, max: 100)
- `cursor` (string, optional): Pagination cursor
- `exchange` (string, optional): Filter by exchange (e.g., "TSX-V", "CSE")
- `sector` (string, optional): Filter by sector
- `min_vetr_score` (integer, optional): Minimum VETR score (0-100)
- `sort` (string, optional): Sort order - `vetr_score_desc`, `vetr_score_asc`, `price_change_desc`, `price_change_asc`, `name_asc` (default: `vetr_score_desc`)

**Response (200):**

```json
{
  "data": [
    {
      "id": "stk_1234567890",
      "ticker": "NXO",
      "name": "Nexus Gold Corp",
      "exchange": "TSX-V",
      "sector": "Mining",
      "market_cap": 45000000.0,
      "price": 0.52,
      "price_change": 2.35,
      "vetr_score": 82,
      "is_favorite": false
    },
    {
      "id": "stk_0987654321",
      "ticker": "FDM",
      "name": "Fathom Nickel Inc",
      "exchange": "CSE",
      "sector": "Mining",
      "market_cap": 28000000.0,
      "price": 0.38,
      "price_change": -1.25,
      "vetr_score": 78,
      "is_favorite": true
    }
  ],
  "pagination": {
    "next_cursor": "eyJpZCI6InN0a18wOTg3NjU0MzIxIn0=",
    "has_more": true,
    "total_count": 125
  }
}
```

---

### GET /stocks/{ticker}

Fetch detailed information for a specific stock by ticker symbol.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `ticker` (string): Stock ticker symbol (e.g., "NXO", "FDM")

**Response (200):**

```json
{
  "id": "stk_1234567890",
  "ticker": "NXO",
  "name": "Nexus Gold Corp",
  "exchange": "TSX-V",
  "sector": "Mining",
  "market_cap": 45000000.0,
  "price": 0.52,
  "price_change": 2.35,
  "vetr_score": 82,
  "is_favorite": false
}
```

**Error Response (404):**

```json
{
  "error": {
    "code": "STOCK_NOT_FOUND",
    "message": "Stock with ticker 'ABC' not found"
  }
}
```

---

### POST /stocks/search

Search stocks by name, ticker, or sector with fuzzy matching.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Request Body:**

```json
{
  "query": "gold",
  "filters": {
    "exchange": ["TSX-V", "CSE"],
    "sector": ["Mining"],
    "min_vetr_score": 70,
    "max_vetr_score": 100
  },
  "limit": 20
}
```

**Response (200):**

```json
{
  "data": [
    {
      "id": "stk_1234567890",
      "ticker": "NXO",
      "name": "Nexus Gold Corp",
      "exchange": "TSX-V",
      "sector": "Mining",
      "market_cap": 45000000.0,
      "price": 0.52,
      "price_change": 2.35,
      "vetr_score": 82,
      "is_favorite": false
    }
  ],
  "pagination": {
    "next_cursor": null,
    "has_more": false,
    "total_count": 1
  }
}
```

---

### POST /stocks/{id}/favorite

Toggle favorite status for a stock.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `id` (string): Stock ID (e.g., "stk_1234567890")

**Request Body:**

```json
{
  "is_favorite": true
}
```

**Response (200):**

```json
{
  "id": "stk_1234567890",
  "ticker": "NXO",
  "is_favorite": true
}
```

---

## Filing Endpoints

### GET /filings/{ticker}

Fetch all filings for a specific stock ticker.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `ticker` (string): Stock ticker symbol (e.g., "NXO")

**Query Parameters:**

- `limit` (integer, optional): Number of filings per page (default: 50, max: 100)
- `cursor` (string, optional): Pagination cursor
- `type` (string, optional): Filter by filing type (e.g., "Press Release", "Financial Statement")
- `start_date` (long, optional): Filter by start date (Unix timestamp in milliseconds)
- `end_date` (long, optional): Filter by end date (Unix timestamp in milliseconds)

**Response (200):**

```json
{
  "data": [
    {
      "id": "fil_1234567890",
      "stock_id": "stk_1234567890",
      "type": "Press Release",
      "title": "Nexus Gold Announces High-Grade Gold Discovery",
      "date": 1672531200000,
      "summary": "Nexus Gold Corp. is pleased to announce the discovery of high-grade gold mineralization at its flagship project...",
      "is_read": false,
      "is_material": true
    },
    {
      "id": "fil_0987654321",
      "stock_id": "stk_1234567890",
      "type": "Financial Statement",
      "title": "Q4 2025 Financial Results",
      "date": 1672444800000,
      "summary": "Nexus Gold Corp. reports Q4 2025 financial results with revenue of $2.5M...",
      "is_read": true,
      "is_material": true
    }
  ],
  "pagination": {
    "next_cursor": "eyJpZCI6ImZpbF8wOTg3NjU0MzIxIn0=",
    "has_more": true,
    "total_count": 45
  }
}
```

---

### GET /filings/search

Search filings across all stocks with keyword matching.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Query Parameters:**

- `q` (string, required): Search query keyword
- `limit` (integer, optional): Number of filings per page (default: 50, max: 100)
- `cursor` (string, optional): Pagination cursor
- `type` (string, optional): Filter by filing type
- `ticker` (string, optional): Filter by stock ticker
- `start_date` (long, optional): Filter by start date (Unix timestamp in milliseconds)
- `end_date` (long, optional): Filter by end date (Unix timestamp in milliseconds)

**Example Request:**

```
GET /filings/search?q=acquisition&type=Press Release&limit=20
```

**Response (200):**

Same structure as GET /filings/{ticker} response.

---

## Executive Endpoints

### GET /executives/{ticker}

Fetch all executives and board members for a specific stock ticker.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `ticker` (string): Stock ticker symbol (e.g., "NXO")

**Response (200):**

```json
{
  "data": [
    {
      "id": "exe_1234567890",
      "stock_id": "stk_1234567890",
      "name": "Alex Johnson",
      "title": "CEO & Director",
      "years_at_company": 5.2,
      "previous_companies": [
        "Barrick Gold Corporation (VP Exploration)",
        "Newmont Mining (Senior Geologist)"
      ],
      "education": "PhD in Geology, MIT",
      "specialization": "Mineral Exploration",
      "social_linkedin": "https://linkedin.com/in/alexjohnson",
      "social_twitter": null
    },
    {
      "id": "exe_0987654321",
      "stock_id": "stk_1234567890",
      "name": "Sarah Chen",
      "title": "CFO",
      "years_at_company": 3.5,
      "previous_companies": [
        "KPMG (Senior Auditor)",
        "Deloitte (Manager)"
      ],
      "education": "MBA, CPA",
      "specialization": "Financial Management",
      "social_linkedin": "https://linkedin.com/in/sarahchen",
      "social_twitter": "https://twitter.com/sarahchen"
    }
  ]
}
```

---

### GET /executives/{id}

Fetch detailed information for a specific executive by ID.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `id` (string): Executive ID (e.g., "exe_1234567890")

**Response (200):**

```json
{
  "id": "exe_1234567890",
  "stock_id": "stk_1234567890",
  "stock_ticker": "NXO",
  "stock_name": "Nexus Gold Corp",
  "name": "Alex Johnson",
  "title": "CEO & Director",
  "years_at_company": 5.2,
  "previous_companies": [
    "Barrick Gold Corporation (VP Exploration)",
    "Newmont Mining (Senior Geologist)"
  ],
  "education": "PhD in Geology, MIT",
  "specialization": "Mineral Exploration",
  "social_linkedin": "https://linkedin.com/in/alexjohnson",
  "social_twitter": null,
  "bio": "Alex Johnson has over 20 years of experience in mineral exploration...",
  "notable_achievements": [
    "Led discovery of 2M oz gold deposit in Nevada",
    "Published 15+ papers in peer-reviewed journals"
  ]
}
```

---

## Alert Rule Endpoints

### GET /alerts/rules

Fetch all alert rules for the authenticated user.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Query Parameters:**

- `limit` (integer, optional): Number of rules per page (default: 50, max: 100)
- `cursor` (string, optional): Pagination cursor
- `is_active` (boolean, optional): Filter by active status

**Response (200):**

```json
{
  "data": [
    {
      "id": "alr_1234567890",
      "user_id": "usr_1234567890",
      "stock_ticker": "NXO",
      "rule_type": "price_target",
      "trigger_condition": "price >= 0.60",
      "is_active": true,
      "created_at": 1672531200000,
      "last_triggered_at": null,
      "frequency": "immediate"
    },
    {
      "id": "alr_0987654321",
      "user_id": "usr_1234567890",
      "stock_ticker": "FDM",
      "rule_type": "filing_notification",
      "trigger_condition": "new_filing",
      "is_active": true,
      "created_at": 1672444800000,
      "last_triggered_at": 1672617600000,
      "frequency": "daily_digest"
    }
  ],
  "pagination": {
    "next_cursor": null,
    "has_more": false,
    "total_count": 2
  }
}
```

---

### POST /alerts/rules

Create a new alert rule.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Request Body:**

```json
{
  "stock_ticker": "NXO",
  "rule_type": "price_target",
  "trigger_condition": "price >= 0.60",
  "frequency": "immediate"
}
```

**Rule Types:**

- `price_target`: Alert when stock price reaches target
- `price_change`: Alert on price change percentage
- `filing_notification`: Alert on new filings
- `vetr_score_change`: Alert when VETR score changes
- `red_flag`: Alert on new red flags

**Frequency Options:**

- `immediate`: Instant notification
- `daily_digest`: Once per day summary
- `weekly_digest`: Once per week summary

**Response (201):**

```json
{
  "id": "alr_1234567890",
  "user_id": "usr_1234567890",
  "stock_ticker": "NXO",
  "rule_type": "price_target",
  "trigger_condition": "price >= 0.60",
  "is_active": true,
  "created_at": 1672531200000,
  "last_triggered_at": null,
  "frequency": "immediate"
}
```

---

### PUT /alerts/rules/{id}

Update an existing alert rule.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `id` (string): Alert rule ID (e.g., "alr_1234567890")

**Request Body:**

```json
{
  "trigger_condition": "price >= 0.65",
  "is_active": false
}
```

**Response (200):**

```json
{
  "id": "alr_1234567890",
  "user_id": "usr_1234567890",
  "stock_ticker": "NXO",
  "rule_type": "price_target",
  "trigger_condition": "price >= 0.65",
  "is_active": false,
  "created_at": 1672531200000,
  "last_triggered_at": null,
  "frequency": "immediate"
}
```

---

### DELETE /alerts/rules/{id}

Delete an alert rule.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `id` (string): Alert rule ID (e.g., "alr_1234567890")

**Response (200):**

```json
{
  "success": true,
  "message": "Alert rule deleted successfully"
}
```

---

## Red Flag Endpoints

### GET /red-flags/{ticker}

Fetch all red flags for a specific stock ticker.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `ticker` (string): Stock ticker symbol (e.g., "NXO")

**Query Parameters:**

- `limit` (integer, optional): Number of flags per page (default: 50, max: 100)
- `cursor` (string, optional): Pagination cursor
- `severity` (string, optional): Filter by severity (low, medium, high, critical)
- `is_acknowledged` (boolean, optional): Filter by acknowledgement status

**Response (200):**

```json
{
  "data": [
    {
      "id": "rfl_1234567890",
      "stock_ticker": "NXO",
      "flag_type": "executive_turnover",
      "severity": "medium",
      "score": -8.5,
      "description": "CFO departed after only 8 months - potential governance concern",
      "detected_at": 1672531200000,
      "is_acknowledged": false
    },
    {
      "id": "rfl_0987654321",
      "stock_ticker": "NXO",
      "flag_type": "filing_delay",
      "severity": "high",
      "score": -15.0,
      "description": "Quarterly financial filing is 14 days overdue",
      "detected_at": 1672444800000,
      "is_acknowledged": true
    }
  ],
  "pagination": {
    "next_cursor": null,
    "has_more": false,
    "total_count": 2
  }
}
```

**Flag Types:**

- `executive_turnover`: High executive turnover rate
- `filing_delay`: Late or missing regulatory filings
- `financial_restatement`: Financial statement restatements
- `insider_selling`: Significant insider stock sales
- `audit_concern`: Auditor concerns or changes
- `regulatory_action`: Regulatory warnings or sanctions

**Severity Levels:**

- `low`: Score impact 0 to -5 points
- `medium`: Score impact -5 to -10 points
- `high`: Score impact -10 to -20 points
- `critical`: Score impact > -20 points

---

### POST /red-flags/{id}/acknowledge

Acknowledge a red flag (mark as reviewed by user).

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `id` (string): Red flag ID (e.g., "rfl_1234567890")

**Response (200):**

```json
{
  "id": "rfl_1234567890",
  "stock_ticker": "NXO",
  "flag_type": "executive_turnover",
  "severity": "medium",
  "score": -8.5,
  "description": "CFO departed after only 8 months - potential governance concern",
  "detected_at": 1672531200000,
  "is_acknowledged": true,
  "acknowledged_at": 1672617600000
}
```

---

## VETR Score Endpoints

### GET /vetr-score/{ticker}

Fetch current VETR score and breakdown for a specific stock ticker.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `ticker` (string): Stock ticker symbol (e.g., "NXO")

**Response (200):**

```json
{
  "stock_ticker": "NXO",
  "stock_name": "Nexus Gold Corp",
  "overall_score": 82,
  "scores": {
    "pedigree_score": 85,
    "filing_velocity_score": 78,
    "red_flag_score": -8,
    "growth_score": 90,
    "governance_score": 88
  },
  "calculated_at": 1672531200000,
  "score_change_24h": 2,
  "score_change_7d": 5,
  "score_change_30d": -3
}
```

**Score Components:**

- `pedigree_score` (0-100): Executive experience and qualifications
- `filing_velocity_score` (0-100): Timeliness and frequency of regulatory filings
- `red_flag_score` (-100 to 0): Negative score from red flags
- `growth_score` (0-100): Revenue and market cap growth trends
- `governance_score` (0-100): Corporate governance quality

**Overall Score Calculation:**

```
overall_score = (pedigree_score + filing_velocity_score + growth_score + governance_score) / 4 + red_flag_score
```

Final score is clamped to 0-100 range.

---

### GET /vetr-score/{ticker}/history

Fetch historical VETR score data for trend analysis.

**Request Headers:**

```
Authorization: Bearer <access_token>
```

**Path Parameters:**

- `ticker` (string): Stock ticker symbol (e.g., "NXO")

**Query Parameters:**

- `start_date` (long, optional): Start date (Unix timestamp in milliseconds)
- `end_date` (long, optional): End date (Unix timestamp in milliseconds)
- `interval` (string, optional): Data interval - `daily`, `weekly`, `monthly` (default: `daily`)

**Response (200):**

```json
{
  "stock_ticker": "NXO",
  "stock_name": "Nexus Gold Corp",
  "interval": "daily",
  "history": [
    {
      "id": "vsh_1234567890",
      "stock_ticker": "NXO",
      "overall_score": 82,
      "pedigree_score": 85,
      "filing_velocity_score": 78,
      "red_flag_score": -8,
      "growth_score": 90,
      "governance_score": 88,
      "calculated_at": 1672531200000
    },
    {
      "id": "vsh_0987654321",
      "stock_ticker": "NXO",
      "overall_score": 80,
      "pedigree_score": 85,
      "filing_velocity_score": 75,
      "red_flag_score": -10,
      "growth_score": 88,
      "governance_score": 87,
      "calculated_at": 1672444800000
    }
  ]
}
```

---

## Implementation Notes

### Date/Time Format

All timestamps are Unix timestamps in milliseconds (e.g., `1672531200000` for January 1, 2023 00:00:00 UTC).

### JSON Serialization

- Use snake_case for JSON field names (e.g., `access_token`, `stock_ticker`)
- Omit null fields from responses to reduce payload size
- Boolean fields should be `true` or `false` (lowercase)

### Token Expiration

- Access tokens expire after 24 hours (`expires_in: 86400` seconds)
- Refresh tokens expire after 90 days
- Client should refresh tokens when `expires_in` time has elapsed
- Client should handle 401 responses by attempting token refresh before re-login

### Idempotency

POST endpoints for creating resources should support idempotency keys to prevent duplicate creation:

```
Idempotency-Key: <unique-request-id>
```

If the same idempotency key is received within 24 hours, return the original response (200) instead of creating a duplicate (201).

### Content Negotiation

All requests and responses use JSON format:

```
Content-Type: application/json
Accept: application/json
```

### CORS

API should support CORS for web client integration:

```
Access-Control-Allow-Origin: https://app.vettr.com
Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS
Access-Control-Allow-Headers: Authorization, Content-Type, Idempotency-Key
```

---

## Testing Recommendations

### Mock Data

For development and testing, backend should provide:

- 25 Canadian venture capital stocks (TSX-V and CSE)
- Mix of sectors: Mining, Technology, Energy, Healthcare, Finance
- Realistic price ranges ($0.10 - $5.00)
- VETR scores ranging from 45-95

### Health Check Endpoint

```
GET /health
```

Response:

```json
{
  "status": "healthy",
  "version": "1.0.0",
  "timestamp": 1672531200000
}
```

### API Documentation

Backend should provide OpenAPI/Swagger documentation at:

```
GET /docs
```

---

## Support

For API integration questions or issues, contact:

- Email: api-support@vettr.com
- Developer Portal: https://developers.vettr.com
- Status Page: https://status.vettr.com
