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

## Configuration

The application can be configured with environment variables. At the time of writing, here's a full list:

```
HTTP_HOST - the hostname to bind the server to. Default: 0.0.0.0
HTTP_PORT or PORT - the TCP port to bind the server to. Default: 4000

DB_IN_MEMORY - whether to use an in-memory persistence method. Any value is allowed. Default: <undefined>
DB_HOST - the hostname of the database server. Default: localhost
DB_PORT - the port of the database server. Default: 5432
DB_USER - the username to be used by the database client. Default: postgres
DB_PASSWORD - the username to be used by the database client. Default: example
DB_MAX_CONNECTIONS - the maximum amount of connections for the database client. Default: 10
```

Additionally, e2e tests can be configured:

```
E2E_BASE_URL - the base URL to use when executing calls against a server. Default: http://localhost:4000
```

An example e2e test run with a modified URL would look like this:

```
E2E_BASE_URL=https://example.com sbt e2e/E2EConfig/test
```

## Deployment

For demonstration purposes, the application is being deployed to https://json-validation.herokuapp.com on every commit to main.
For example, visit [/schema/config-schema](https://json-validation.herokuapp.com/schema/config-schema) in your brower.
