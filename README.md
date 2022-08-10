# json-validation-service

An implementation of the following interface:

```
POST    /schema/SCHEMAID        - Upload a JSON Schema with unique `SCHEMAID`
GET     /schema/SCHEMAID        - Download a JSON Schema with unique `SCHEMAID`

POST    /validate/SCHEMAID      - Validate a JSON document against the JSON Schema identified by `SCHEMAID`
```

The implementation uses http4s as the main HTTP layer and stores the data in a Postgres database using Skunk.
An in-memory storage implementation is available for testing purposes. It can be enabled by setting the `DB_IN_MEMORY` environment variable.

## Running

1. Install sbt, Docker, Docker Compose
2. Build the application's Docker image:

```bash
sbt publishLocal
```

3. Run the application and its dependencies:

```bash
docker-compose up -d
```

By default, the application runs on `localhost:4000`.

## Examples of usage

Assuming usage of httpie:

```bash
http post :4000/schema/config-schema < ./e2e/src/e2e/resources/examples/config-schema.json
http :4000/schema/config-schema
http post :4000/validate/config-schema < ./e2e/src/e2e/resources/examples/config.json
```
