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

Both **ESLint** and b are used in combination.

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

- `./src/components`: Pure UI components, driven by model/props. Those are reusable components.
- `./src/fragments`: Fragments interact with other layers of the application, like API or Pinia stores. They are split into separate files for easier organization, but are not necessarily reused.
- `./src/pages`: Pages make use of components/fragments as well as API / Pinia stores. Each Component in that folder is converted to a navigable route using [unplugin-vue-router](https://github.com/posva/unplugin-vue-router). Pages contain a special `<route>` to define the layout to use as well as other router meta attributes.
- `./src/layouts`: Wrapper component around Pages.

Components and Fragments are automatically imported using [unplugin-vue-components](https://github.com/unplugin/unplugin-vue-components).

## Icons

[UnoCSS Icons preset](https://unocss.dev/presets/icons) is used for icons, with the MDI set from Iconify.
