## OpenAPI

[OpenAPI-fetch](https://openapi-ts.dev/openapi-fetch/) is used to generate a fully typed client from the Komga OpenAPI documentation.

The `openapi-generate` tasks will generate bindings, and should be run when the OpenAPI definition has changed.

## Pinia Colada

[Pinia Colada](https://pinia-colada.esm.dev/) is used for all the API queries and mutations.

## Tests

Vitest projects are used to specify different kind of tests:

- `unit`: unit tests
- `storybook`: component tests, defined in Storybook stories. Those can be run from Storybook directly or through Vitest.

[Mock Service Worker](https://mswjs.io/) is used to mock the Komga API.

## Code formatting

Both **ESLint** and **Prettier** are used in combination.

Tasks:

- ESLint: `lint` and `lint:fix`
- Prettier: `prettier` and `prettier:fix`

## Type checking

Vite does not perform type-checking and leaves the job to the IDE. The `type-check` task can be used to perform type checking.

## Storybook

[Storybook](https://storybook.js.org/) is used for component development and testing. Run `storybook:dev` to start the Storybook server.

## i18n

[FormatJS](https://formatjs.github.io/) is used for i18n.

Message IDs are generated automatically using the ESLint plugin, and hard-coded message IDs should be avoided.

Tasks:

- `i18n:extract`: extracts the messages from the source into the `i18n` folder. This folder is what Weblate uses.
- `i18n:compile`: compiles the translated files in `i18n` into `./src/i18n`. This folder is what the application uses at runtime.

The Vite plugin [dir2json](https://github.com/buddywang/vite-plugin-dir2json) is used to load the available translation files, see `./src/utils/locale-helper.ts` for more details.

## Components

Vue template files are segregated in different categories depending on usage:

- `./src/components`: UI components.
- `./src/pages`: Pages make use of components as well as API / Pinia stores. Each Component in that folder is converted to a navigable route using [unplugin-vue-router](https://github.com/posva/unplugin-vue-router). Pages contain a special `<route>` to define the layout to use as well as other router meta attributes.
- `./src/layouts`: Wrapper component around Pages.

Components are automatically imported using [unplugin-vue-components](https://github.com/unplugin/unplugin-vue-components).

## Icons

[UnoCSS Icons preset](https://unocss.dev/presets/icons) is used for icons, with the MDI set from Iconify.

## Base URL

The generated bundle is server by Apache Tomcat when running Spring. By default the site is hosted at `/`, but if `server.servlet-context-path` is set, the base URL can be dynamic.

The base URL needs to be set correctly so the web app works:

- in the API client
- in the Vue Router, to properly handle the web history
- in the generated bundle, to load other files (js/css/images)

1. Vite is [configured](./vite.config.mts) with the experimental `renderBuiltUrl`, which will use a dynamic function (`window.buildUrl`) defined in `index.html` to generate the asset path at runtime. This is only supported withing JS files though.
2. To handle the dynamic path in `index.html`, a Gradle task `nextuiCopyIndex` modifies `index.html` to duplicate `href`, `src` and `content` attributes as Thymeleaf variants.

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
5. `window.ressourceBaseUrl` is subsequently used in Typescript code to set the base URL for:
    - the API client and the images served [by API](./src/api/base.ts)
    - the [Vue Router](./src/router/index.ts)
