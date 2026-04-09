# Frontend Build Process and Thymeleaf Integration

## Overview

Komga's frontend is built using Vue.js and integrated with the Spring Boot backend through Thymeleaf template processing. This document explains the build pipeline, Thymeleaf tag injection, and how static assets are served.

## Build Pipeline

### 1. Frontend Build (`komga-webui/`)
- **Technology Stack**: Vue.js 2.6 with TypeScript, Vuetify, Vuex, Vue Router
- **Build Tool**: Vue CLI with Webpack
- **Entry Point**: `komga-webui/src/main.ts`
- **Build Output**: `komga-webui/dist/` directory

### 2. Gradle Tasks (`komga/build.gradle.kts`)
The build process is orchestrated by Gradle tasks:

```kotlin
// 1. npmInstall - Install Node.js dependencies
register<Exec>("npmInstall") {
    workingDir(webui) // $rootDir/komga-webui
    commandLine("npm", "install")
}

// 2. npmBuild - Build frontend with Vue CLI
register<Exec>("npmBuild") {
    dependsOn("npmInstall")
    workingDir(webui)
    commandLine("npm", "run", "build")
}

// 3. copyWebDist - Copy built files to resources
register<Sync>("copyWebDist") {
    dependsOn("npmBuild")
    from("$webui/dist/")
    into("$projectDir/src/main/resources/public/")
}

// 4. prepareThymeLeaf - Inject Thymeleaf tags
register<Copy>("prepareThymeLeaf") {
    dependsOn("copyWebDist")
    from("$webui/dist/index.html")
    into("$projectDir/src/main/resources/public/")
    filter { line ->
        line.replace("((?:src|content|href)=\")([\\w]*/.*?)(\")".toRegex()) {
            it.groups[0]?.value + " th:" + it.groups[1]?.value + "@{" + 
            it.groups[2]?.value?.prefixIfNot("/") + "}" + it.groups[3]?.value
        }
    }
}
```

## Thymeleaf Tag Injection

### Purpose
Thymeleaf tags are injected to make static resource URLs context-aware. When Komga is deployed under a subpath (e.g., `http://example.com/komga/`), the tags ensure assets load correctly without hardcoded paths.

### Injected Tags in `index.html`

```html
<!-- Original (from Vue build) -->
<link rel="icon" href="/favicon.ico">
<script>window.resourceBaseUrl = '/'</script>

<!-- After prepareThymeLeaf task -->
<link rel="icon" href="/favicon.ico" th:href="@{/favicon.ico}">
<script th:inline="javascript">
    window.resourceBaseUrl = /*[(${"'" + baseUrl + "'"})]*/ '/'
</script>
```

### Tag Breakdown

1. **URL Rewriting Tags**:
   ```html
   th:href="@{/favicon.ico}"
   th:content="@{/mstile-144x144.png}"
   th:href="@{/manifest.json}"
   ```
   - `@{...}` syntax: Automatically prepends context path
   - Works for `href`, `src`, `content` attributes
   - Applies to: favicons, manifest, tile images

2. **JavaScript Inlining**:
   ```html
   <script th:inline="javascript">
       window.resourceBaseUrl = /*[(${"'" + baseUrl + "'"})]*/ '/'
   </script>
   ```
   - `th:inline="javascript"`: Enables Thymeleaf processing in JS
   - `/*[(${...})]*/`: Injects server-side `baseUrl` variable
   - `window.resourceBaseUrl`: Used by frontend for API calls

### Regex Pattern
The `prepareThymeLeaf` task uses this regex to find and replace URLs:
```regex
((?:src|content|href)=\")([\\w]*/.*?)(\")
```
- Matches: `src="...`, `content="...`, `href="...`
- Groups: Attribute name and URL value
- Replacement: Adds `th:` prefix and `@{...}` wrapper

## Frontend Distribution (`frontend-dist/`)

### Directory Structure
```
frontend-dist/
├── index.html              # Main HTML with Thymeleaf tags
├── js/
│   ├── app.cd1e7444.js     # Main application bundle
│   ├── chunk-vendors.215f0888.js  # Vendor bundle
│   └── [chunk].js          # Code-split chunks
├── css/
│   ├── app.c113e1ad.css    # Application styles
│   └── chunk-vendors.420551a9.css # Vendor styles
├── fonts/                  # Web fonts
├── img/                   # Static images
└── manifest.json          # PWA manifest
```

### Building for Distribution
The `build-local-docker.sh` script can export frontend files:
```bash
./build-local-docker.sh --export-dist ./frontend-dist
```
- Uses Docker multi-stage build to extract dist files
- Creates standalone frontend distribution
- Useful for testing or manual deployment

## Integration with Backend

### 1. Static Resource Serving
- **Location**: `komga/src/main/resources/public/`
- **Spring Boot**: Automatically serves files from `classpath:/public/`
- **Access**: `http://localhost:25600/` serves `index.html`

### 2. Thymeleaf Controller
```java
// Komga uses Thymeleaf for serving index.html
// The controller maps root path to index.html
```

### 3. Base URL Handling
- **Server-side**: `baseUrl` is determined from request context
- **Client-side**: `window.resourceBaseUrl` used for API calls
- **API Proxy**: Frontend routes API requests through correct path

## Development vs Production

### Development Mode
- **Vue CLI Dev Server**: `npm run serve` on port 8081
- **Hot Reload**: Instant updates without full rebuild
- **API Proxy**: Dev server proxies API calls to backend (port 25600)

### Production Build
- **Optimized Bundles**: Minified JS, CSS with hash filenames
- **Code Splitting**: Dynamic imports for smaller initial load
- **Tree Shaking**: Unused code removed

## Testing

### Unit Tests
```bash
cd komga-webui
npm run test:unit
```
- **Framework**: Jest with Vue Test Utils
- **Location**: `komga-webui/tests/unit/`
- **Coverage**: Component functions and utilities

### Linting
```bash
npm run lint
```
- **Tools**: ESLint with Vue/TypeScript rules
- **Configuration**: `.eslintrc.js`
- **Pre-commit**: Runs automatically in CI

## Common Issues and Solutions

### 1. Missing Thymeleaf Tags
**Symptom**: Assets fail to load when deployed under subpath
**Solution**: Ensure `prepareThymeLeaf` task runs before `bootJar`
```bash
./gradlew :komga:prepareThymeLeaf :komga:bootJar
```

### 2. Cached Assets with Hash Names
**Symptom**: Browser caches old JS/CSS files
**Solution**: Webpack hash filenames (e.g., `app.cd1e7444.js`) bust cache automatically

### 3. Base URL Incorrect
**Symptom**: API calls go to wrong path
**Debug**: Check `window.resourceBaseUrl` in browser console
**Fix**: Verify Thymeleaf `baseUrl` injection

### 4. Docker Build Issues
**Symptom**: Frontend not updated in Docker image
**Solution**: Use optimized Docker build with separate frontend stage
```bash
./build-local-docker.sh --export-dist
```

## Best Practices

1. **Always run full build chain**:
   ```bash
   ./gradlew :komga:prepareThymeLeaf :komga:bootJar
   ```

2. **Test with subpath deployment**:
   ```bash
   # Deploy to /komga context
   server.servlet.context-path=/komga
   ```

3. **Monitor bundle sizes**:
   - Check `frontend-dist/js/` directory size
   - Use Webpack Bundle Analyzer if needed

4. **Keep Thymeleaf tags minimal**:
   - Only inject where context-awareness needed
   - Avoid performance overhead

## Local Development Setup

### Quick Start with Pre-built Frontend

When you have a pre-built frontend in `frontend-dist/` (e.g., from Docker export or previous build), follow these steps to integrate with the Spring Boot backend:

1. **Copy static assets to public directory**:
   ```bash
   mkdir -p komga/src/main/resources/public
   cp -r frontend-dist/* komga/src/main/resources/public/
   ```

2. **Move index.html to templates directory** (required for Thymeleaf processing):
   ```bash
   mkdir -p komga/src/main/resources/templates
   mv komga/src/main/resources/public/index.html komga/src/main/resources/templates/
   ```

3. **Start backend excluding frontend build tasks**:
   ```bash
   ./gradlew :komga:bootRun -x npmInstall -x npmBuild -x copyWebDist -x prepareThymeLeaf
   ```

4. **Verify frontend is served**:
   - Open http://localhost:25600/
   - Check browser console for errors
   - Static assets should load (JS, CSS, fonts)

### Using PostgreSQL (Optional)
If you need PostgreSQL for development, start the database first:
```bash
docker-compose -f docker-compose.local.yml up -d postgres
```
Then set environment variables or activate `postgresql` profile.

### Troubleshooting

**Thymeleaf template error "Error resolving template [index]"**:
- Ensure `index.html` is in `komga/src/main/resources/templates/`
- Remove duplicate `index.html` from `public/` directory

**Static assets not loading (404)**:
- Verify JS/CSS files exist in `komga/src/main/resources/public/`
- Check that Thymeleaf tags are properly injected (view page source)
- Ensure `baseUrl` is correctly set (should be `/` for local development)

**Database errors during startup**:
- Use SQLite default by not setting any database environment variables
- Or ensure PostgreSQL is running and schema is applied

## Related Files

- `komga/build.gradle.kts`: Gradle build configuration
- `komga-webui/vue.config.js`: Vue CLI configuration
- `komga-webui/package.json`: Frontend dependencies
- `build-local-docker.sh`: Docker build script
- `frontend-dist/index.html`: Final HTML with Thymeleaf tags