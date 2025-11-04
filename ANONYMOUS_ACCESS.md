# Anonymous Access and Age Restrictions

Komga now supports optional authentication with configurable content restrictions. Users can browse and read content without logging in, while adult/age-restricted content requires authentication.

## Overview

The anonymous access system provides:
- **Optional authentication** - Browse and read without login
- **Age-based content filtering** - Restrict content by age rating
- **Adult content protection** - FSK18/Mature content requires login
- **Per-library configuration** - Control access settings per library
- **Automatic content hiding** - Adult content invisible to anonymous users
- **Flexible age ratings** - Support for multiple rating systems (ESRB, PEGI, FSK)

## Features

### 1. Anonymous User Access

Users can access Komga without authentication for non-restricted content.

**Capabilities:**
- ✅ Browse libraries (if allowed)
- ✅ View series and books
- ✅ Read non-adult content
- ✅ Use OPDS feeds
- ✅ Access public API endpoints
- ❌ Modify or delete content
- ❌ Access adult/age-restricted content
- ❌ Download files
- ❌ Change settings

### 2. Age Rating System

Multiple rating systems supported:

**Universal (No Auth Required):**
- `ALL_AGES` / `EVERYONE` - All ages (0+)
- `EVERYONE_10` - E10+ rating (10+)
- `TEEN` - Teen rating (13+)
- `PEGI_12` - PEGI 12 (12+)
- `PEGI_16` - PEGI 16 (16+)

**Adult (Auth Required):**
- `MATURE_17` - Mature 17+ (17+)
- `MATURE` - Mature (18+)
- `ADULTS_ONLY` - Adults Only (18+)
- `PEGI_18` - PEGI 18 (18+)
- `FSK_18` - FSK 18 (18+)
- `X_RATED` - X-Rated (18+)
- `EXPLICIT` - Explicit content (21+)

### 3. Library Configuration

Each library can be configured independently:

```json
{
  "allowAnonymousAccess": true,
  "hideAdultContentForAnonymous": true,
  "ageRestrictionForAnonymous": 16
}
```

**Settings:**
- `allowAnonymousAccess` - Enable/disable anonymous access to library
- `hideAdultContentForAnonymous` - Hide adult content from anonymous users
- `ageRestrictionForAnonymous` - Maximum age rating for anonymous users (default: 16)

## Configuration

### Enable Anonymous Access for a Library

```bash
curl -X PATCH \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <admin-token>" \
  -d '{
    "allowAnonymousAccess": true,
    "hideAdultContentForAnonymous": true,
    "ageRestrictionForAnonymous": 16
  }' \
  http://localhost:8080/api/v1/libraries/{libraryId}
```

### Common Configurations

#### Configuration 1: Public Library (Safe Content)

```json
{
  "allowAnonymousAccess": true,
  "hideAdultContentForAnonymous": true,
  "ageRestrictionForAnonymous": 13
}
```

**Result:**
- Anyone can browse without login
- Only content rated 13+ or lower visible
- Adult content completely hidden
- Login required to access 18+ content

#### Configuration 2: Teen Library

```json
{
  "allowAnonymousAccess": true,
  "hideAdultContentForAnonymous": true,
  "ageRestrictionForAnonymous": 16
}
```

**Result:**
- Public access allowed
- Content up to PEGI 16 visible
- 18+ content requires authentication

#### Configuration 3: Private Library

```json
{
  "allowAnonymousAccess": false,
  "hideAdultContentForAnonymous": true,
  "ageRestrictionForAnonymous": 0
}
```

**Result:**
- Login required for all access
- No anonymous browsing
- All content protected

#### Configuration 4: Fully Open Library

```json
{
  "allowAnonymousAccess": true,
  "hideAdultContentForAnonymous": false,
  "ageRestrictionForAnonymous": 99
}
```

**Result:**
- Public access allowed
- All content visible (not recommended for public servers)

## API Usage

### Check Anonymous Access Status

```bash
curl http://localhost:8080/api/v1/anonymous/status
```

Response:
```json
{
  "isAnonymous": true,
  "canAccessAdultContent": false,
  "accessibleLibraryCount": 2,
  "accessibleLibraryIds": ["lib1", "lib2"]
}
```

### Get Library Access Settings

```bash
curl http://localhost:8080/api/v1/anonymous/settings
```

Response:
```json
[
  {
    "libraryId": "lib1",
    "libraryName": "Public Manga",
    "allowAnonymousAccess": true,
    "hideAdultContentForAnonymous": true,
    "ageRestrictionForAnonymous": 16
  }
]
```

### List Age Ratings

```bash
curl http://localhost:8080/api/v1/anonymous/age-ratings
```

Response:
```json
[
  {
    "code": "EVERYONE",
    "minimumAge": 0,
    "requiresAuth": false,
    "isAdultContent": false,
    "isExplicitContent": false
  },
  {
    "code": "TEEN",
    "minimumAge": 13,
    "requiresAuth": false,
    "isAdultContent": false,
    "isExplicitContent": false
  },
  {
    "code": "MATURE",
    "minimumAge": 18,
    "requiresAuth": true,
    "isAdultContent": true,
    "isExplicitContent": false
  }
]
```

### Check Authentication Requirement

```bash
curl http://localhost:8080/api/v1/anonymous/requires-auth
```

Response:
```json
{
  "isAuthenticated": false,
  "requiresAuthForAdultContent": true,
  "hasFullAccess": false
}
```

## Public API Endpoints

These endpoints are accessible to anonymous users (GET only):

- `GET /api/v1/libraries` - List accessible libraries
- `GET /api/v1/libraries/{id}` - Get library details
- `GET /api/v1/series` - List accessible series (filtered)
- `GET /api/v1/series/{id}` - Get series details
- `GET /api/v1/books` - List accessible books (filtered)
- `GET /api/v1/books/{id}` - Get book details
- `GET /api/v1/books/{id}/pages` - List book pages
- `GET /api/v1/books/{id}/pages/{number}` - Get book page (non-adult)
- `GET /api/v1/collections` - List collections
- `GET /api/v1/readlists` - List read lists
- `GET /api/v1/languages` - Language settings
- `GET /api/v1/reader/modes` - Reader modes
- `GET /api/v1/anonymous/*` - Anonymous access info
- `GET /opds/v1.2/catalog` - OPDS feed
- `GET /opds/v2/catalog` - OPDS v2 feed

**Note:** All write operations (POST, PUT, PATCH, DELETE) require authentication.

## Content Filtering

### Automatic Filtering

Content is automatically filtered for anonymous users:

1. **Library Level:**
   - Only libraries with `allowAnonymousAccess=true` visible
   - Other libraries completely hidden

2. **Series Level:**
   - Series age rating checked against `ageRestrictionForAnonymous`
   - Adult content (18+) automatically hidden if `hideAdultContentForAnonymous=true`

3. **Book Level:**
   - Inherits series age rating
   - Same filtering rules apply

### Filter Behavior

**For Anonymous Users:**
```
if ageRating <= ageRestrictionForAnonymous:
    show content
else:
    hide content (as if it doesn't exist)
```

**For Authenticated Users:**
```
always show all accessible content
```

## Setting Age Ratings

Age ratings can be set through metadata:

### In ComicInfo.xml

```xml
<?xml version="1.0"?>
<ComicInfo>
  <Title>My Comic</Title>
  <AgeRating>Teen</AgeRating>
  <!-- or -->
  <AgeRating>18+</AgeRating>
  <!-- or -->
  <AgeRating>Mature 17+</AgeRating>
</ComicInfo>
```

### Via API

```bash
curl -X PATCH \
  -H "Authorization: Bearer <token>" \
  -d '{"ageRating": 18}' \
  http://localhost:8080/api/v1/series/{id}/metadata
```

### Via Plugin

```kotlin
SeriesMetadataPatch(
  title = "My Manga",
  ageRating = 18,  // Numeric age rating
  // ... other metadata
)
```

## Security Considerations

### Best Practices

1. **Public Servers:**
   - Always enable `hideAdultContentForAnonymous`
   - Set `ageRestrictionForAnonymous` to 16 or lower
   - Review content ratings regularly

2. **Private Servers:**
   - Consider disabling anonymous access entirely
   - Use `allowAnonymousAccess=false` for personal collections

3. **Mixed Content:**
   - Separate libraries for different age groups
   - Enable anonymous access only for appropriate libraries

4. **Legal Compliance:**
   - Ensure age ratings comply with local laws
   - Set appropriate restrictions for your jurisdiction
   - Consider using FSK_18 in Germany, PEGI_18 in EU, etc.

### Content Rating Guidelines

**Recommended Ratings:**

- **All Ages (0+):** Children's content, no violence, no adult themes
- **Teen (13+):** Mild violence, some mature themes, no nudity
- **Mature 17+ (17+):** Strong violence, adult themes, suggestive content
- **Mature/Adults Only (18+):** Explicit violence, sexual content, adult themes
- **Explicit (21+):** Pornographic content, extreme violence

### Privacy

Anonymous users are tracked by:
- Session ID (temporary)
- IP address (for rate limiting)
- No personal data stored
- Session cleared on browser close

## Use Cases

### Use Case 1: Public Manga Library

**Scenario:** Public library with manga for teens

**Configuration:**
```json
{
  "allowAnonymousAccess": true,
  "hideAdultContentForAnonymous": true,
  "ageRestrictionForAnonymous": 16
}
```

**Behavior:**
- Anyone can browse without creating account
- Content rated 16+ and below accessible
- 18+ manga requires login
- Anonymous users see curated selection

### Use Case 2: Private Adult Collection

**Scenario:** Personal collection with adult manga

**Configuration:**
```json
{
  "allowAnonymousAccess": false,
  "hideAdultContentForAnonymous": true,
  "ageRestrictionForAnonymous": 0
}
```

**Behavior:**
- Login required for all access
- All content protected
- No public browsing

### Use Case 3: Family-Friendly Comics

**Scenario:** Family comics library

**Configuration:**
```json
{
  "allowAnonymousAccess": true,
  "hideAdultContentForAnonymous": true,
  "ageRestrictionForAnonymous": 13
}
```

**Behavior:**
- Public access for family content
- Only E, E10+, and Teen content visible
- Mature content requires login

### Use Case 4: Research/Academic Library

**Scenario:** Academic manga research collection

**Configuration:**
```json
{
  "allowAnonymousAccess": true,
  "hideAdultContentForAnonymous": false,
  "ageRestrictionForAnonymous": 99
}
```

**Behavior:**
- Public access for research
- All content visible (with warnings)
- Suitable for academic environments with age verification

## Client Implementation

### Detecting Anonymous Mode

```javascript
const checkAuth = async () => {
  const response = await fetch('/api/v1/anonymous/status');
  const status = await response.json();

  if (status.isAnonymous) {
    showLoginPrompt();
    hideAdultContent();
  }
};
```

### Handling Age-Restricted Content

```javascript
const loadContent = async (contentId) => {
  try {
    const response = await fetch(`/api/v1/series/${contentId}`);

    if (response.status === 403) {
      // Content requires authentication
      showLoginDialog('This content requires authentication to access.');
    } else if (response.status === 404) {
      // Content doesn't exist or is hidden
      showError('Content not found or unavailable.');
    } else {
      const content = await response.json();
      displayContent(content);
    }
  } catch (error) {
    handleError(error);
  }
};
```

### Showing Login Prompt

```javascript
const showAgeRestrictedPrompt = () => {
  return `
    <div class="age-restriction-notice">
      <h3>Age-Restricted Content</h3>
      <p>This content is restricted to users 18 years and older.</p>
      <button onclick="showLogin()">Login to Continue</button>
    </div>
  `;
};
```

## Troubleshooting

### Issue: All content requires login

**Cause:** Library has `allowAnonymousAccess=false`

**Solution:**
```bash
curl -X PATCH \
  -H "Authorization: Bearer <admin-token>" \
  -d '{"allowAnonymousAccess": true}' \
  http://localhost:8080/api/v1/libraries/{id}
```

### Issue: No content visible for anonymous users

**Cause:** All content has age rating above `ageRestrictionForAnonymous`

**Solution:**
1. Check content age ratings
2. Increase `ageRestrictionForAnonymous`
3. Or set `hideAdultContentForAnonymous=false` (not recommended)

### Issue: Adult content visible to anonymous users

**Cause:** `hideAdultContentForAnonymous=false`

**Solution:**
```bash
curl -X PATCH \
  -H "Authorization: Bearer <admin-token>" \
  -d '{"hideAdultContentForAnonymous": true}' \
  http://localhost:8080/api/v1/libraries/{id}
```

### Issue: Anonymous users can modify content

**Cause:** This should never happen - indicates security issue

**Solution:**
- Check security configuration
- Verify OptionalAuthenticationFilter is active
- Review Spring Security settings

## Migration Guide

### Enabling Anonymous Access for Existing Library

1. **Review Content:**
   ```bash
   # Check age ratings in library
   curl http://localhost:8080/api/v1/series?libraryId={id}
   ```

2. **Set Age Ratings:**
   - Update series metadata with appropriate age ratings
   - Use ComicInfo.xml or API

3. **Enable Access:**
   ```bash
   curl -X PATCH \
     -H "Authorization: Bearer <token>" \
     -d '{
       "allowAnonymousAccess": true,
       "hideAdultContentForAnonymous": true,
       "ageRestrictionForAnonymous": 16
     }' \
     http://localhost:8080/api/v1/libraries/{id}
   ```

4. **Test:**
   - Browse without login
   - Verify adult content hidden
   - Test authentication for restricted content

## Performance Considerations

- **Filtering:** Content filtering is efficient with database-level queries
- **Caching:** Anonymous access results can be cached more aggressively
- **Load:** Anonymous users have minimal server load (read-only)
- **Sessions:** Anonymous sessions use minimal memory

## Future Enhancements

Planned features:
- [ ] Age verification with ID upload
- [ ] Time-based access restrictions
- [ ] Parental controls
- [ ] Content warnings/tags
- [ ] Custom age rating systems
- [ ] Region-specific restrictions
- [ ] Anonymous read progress (session-based)

## Contributing

To improve anonymous access:
1. Report issues with age rating detection
2. Suggest additional rating systems
3. Contribute localized age ratings
4. Improve content filtering logic

## License

Anonymous access features are part of Komga and follow the same license.
