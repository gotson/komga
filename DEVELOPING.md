# Development guidelines

Thanks a lot for contributing to Komga!

## Requirements

You will need:

- Java JDK version 21+
- Nodejs version 18+ (check the `.nvmrc` file)

## Setting up the project

- run `npm install` in the `komga-webui` folder of the project. This will install the necessary tooling for the webui.

## Commit messages

Komga's commit messages follow the [Conventional Commits](https://www.conventionalcommits.org/) standard. This enables automatic versioning, releases, and release notes generation.

## Project organization

Komga is composed of 3 projects:
- `komga`: a Spring Boot backend server that hosts the APIs, but also serves the static assets of the frontend.
- `komga-webui`: a VueJS frontend, built at compile time and served by the backend at runtime.
- `komga-tray`: a thin desktop wrapper that displays a tray-icon

## Backend development

### Spring profiles

Komga uses Spring Profiles extensively:
- `dev`: add more logging, disable periodic scanning, in-memory database, and enable CORS from `localhost:8081` (the frontend dev server)
- `localdb`: a dev profile that stores the database in `./localdb`.
- `noclaim`: will create initial users at startup if none exist and output users and passwords in the standard output
  - if `dev` is active, will create `admin@example.org` with password `admin`, and `user@example.org` with password `user`
  - if `dev` is not active, will create `admin@example.org` with a random password that will be shown in the logs

### Gradle tasks

The backend project uses `gradle` to run all the necessary tasks. If your IDE does not have `gradle` integration, you can run the tasks from the root directory using `./gradlew <taskName>`.

Here is a list of useful tasks:
- `bootRun`: run the application locally, useful for testing your changes.
- `prepareThymeLeaf`: build the frontend, and copy the bundle to `/resources/public`. You need to run this manually if
  you want to test the latest frontend build hosted by Spring.
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

You can run a live development server with `npm run serve` from `/komga-webui`. The dev server will override the URL to connect to `localhost:25600`, so you can also run `gradle bootRun` to have a backend running, serving the API requests. The frontend will be loaded from `localhost:8081`.

Make sure you start the backend with the `dev` profile, else the frontend requests will be denied because of CORS.

## Docker

To build the Docker image, you need to:

- have the webui built and copied to `/resources/public`. To do so, run `./gradlew prepareThymeLeaf`
- prepare the docker image via JReleaser. To do so, run `./gradlew jreleaserPackage`
- the `Dockerfile` will be available in `komga/build/jreleaser/package/docker/`
