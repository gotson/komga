## OpenAPI

[OpenAPI-fetch](https://openapi-ts.dev/openapi-fetch/) is used to generate a fully typed client from the Komga OpenAPI documentation.

The `openapi-generate` tasks will generate bindings, and should be run when the OpenAPI definition has changed.

## Pinia Colada

[Pinia Colada](https://pinia-colada.esm.dev/) is used for all the API queries and mutations.

## Tests

We use Vitest projects to separate different kind of tests:
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
