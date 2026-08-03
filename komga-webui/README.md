## Requirements

- Nodejs (check the `komga-webui/.nvmrc` file)

## Frontend development

You can run a live development server with `npm run serve`. The dev server will override the URL to connect to `localhost:8080`, so you can also run `gradle bootRun` to have a backend running, serving the API requests. The frontend will be loaded from `localhost:8081`.

Make sure you start the backend with the `dev` profile, else the frontend requests will be denied because of CORS.

## Base URL

The generated bundle is server by Apache Tomcat when running Spring. By default the site is hosted at `/`, but if `server.servlet-context-path` is set, the base URL can be dynamic.

The base URL needs to be set correctly so the web app works:

- in the API client
- in the Vue Router, to properly handle the web history
- in the generated bundle, to load other files (js/css/images)

1. Webpack is configured with `__webpack_public_path__` to change the path dynamically.
2. To handle the dynamic path in `index.html`, a Gradle task `webuiCopyIndex` modifies `index.html` to duplicate `href`, `src` and `content` attributes as Thymeleaf variants.

    For example the following:

    ```html
    <script
        type="module"
        crossorigin
        src="/assets/index-xEUJQodq.js"
    ></script>
    <link
        rel="stylesheet"
        crossorigin
        href="/assets/index-CQqFNa2f.css"
    />
    ```

    will be transformed to:

    ```html
    <script
        type="module"
        crossorigin
        src="/assets/index-xEUJQodq.js"
        th:src="@{/assets/index-xEUJQodq.js}"
    ></script>
    <link
        rel="stylesheet"
        crossorigin
        href="/assets/index-CQqFNa2f.css"
        th:href="@{/assets/index-CQqFNa2f.css}"
    />
    ```

    In Thymeleaf, `@{}` will prepend the path with the context path dynamically when serving `index.html`.

3. when the `index.html` is served by the `IndexController`, a `baseUrl` attribute is injected, which contains the servlet context path (by default `/`, but could be `/komga` for example)
4. the `index.html` contains a Thymeleaf script block that will be processed when serving the page, effectively injecting the `baseUrl` value into `window.ressourceBaseUrl`.
5. `window.ressourceBaseUrl` is subsequently used in Typescript code to set the base URL
