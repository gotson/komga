# Development guidelines

Thanks a lot for contributing to Komga!

## Requirements

You will need:

- Java JDK version 8 or 11
- Nodejs version 10+, with `npm` version 6+

## Commit messages

Komga's commit messages follow the [Conventional Commits](https://www.conventionalcommits.org/) standard. This enables automatic versioning, releases, and release notes generation.

Commit messages are enforced using commit hooks ran on the developer's PC. To install the necessary tooling, you need to run `npm install` in the root folder of the project. This will install the necessary commit hooks.

## Project organization

Komga is composed of 2 projects:
- `komga`: a Spring Boot backend server that hosts the APIs, but also serves the static assets of the frontend.
- `komga-webui`: a VueJS frontend, built at compile time and served by the backend at runtime.

## Backend development

### Spring profiles

Komga uses Spring Profiles extensively:
- `dev`: add more logging, disable periodic scanning, in-memory database
- `localdb`: a dev profile that stores the database in `./testdb`.
- `noclaim`: will create initial users at startup if none exist and output users and passwords in the standard output
  - if `dev` is active, will create `admin@example.org` with password `admin`, and `user@example.org` with password `user`
  - if `dev` is not active, will create `admin@example.org` with a random password

### Gradle tasks

The backend project uses `gradle` to run all the necessary tasks. If your IDE does not have `gradle` integration, you can run the tasks from the root directory using `./gradlew <taskName>`.

Here is a list of useful tasks:
- `bootRun`: run the application locally, useful for testing your changes.
- `copyWebDist`: build the frontend, and copy the bundle to `/resources/public`. You need to run this manually if you want to test the latest frontend build hosted by Spring.
- `test`: run automated tests. Always run this before committing.
- `jooq-codegen-primary`: generates the jOOQ DSL.

`bootRun` needs to be run with a profile or list of profiles, usually:
- `dev,noclaim`: when testing with a blank database
- `dev,localdb,noclaim`: when testing with an existing database

There are few ways you can run the task with a profile:
- `./gradlew bootRun --args='--spring.profiles.active=dev'`
- On Linux: `SPRING_PROFILES_ACTIVE=dev ./gradlew bootRun`
- On Windows:
```
SET SPRING_PROFILES_ACTIVE=dev
./gradlew bootRun
```
- If you use IntelliJ, some Run Configurations are saved in the repository and available from the Gradle panel

## Frontend development

You can run a live development server with `npm run serve` from `/komga-webui`. The dev server will override the URL to connect to `localhost:8080`, so you can also run `gradle bootRun` to have a backend running, serving the API requests. The frontend will be loaded from `localhost:8081`.

