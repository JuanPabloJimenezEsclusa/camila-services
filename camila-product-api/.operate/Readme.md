# camila-product-api-operate

This section provides information on how to build, deploy, and run the project locally.

❗ The `chaos-monkey` profile can be activated to locally test [Principles Of Chaos](https://principlesofchaos.org/).

## Scripts

The following table summarizes the available scripts for building, running, and deploying the project:

| Command                                                   | Description                                                          |
|-----------------------------------------------------------|----------------------------------------------------------------------|
| [build-aot](./build-aot.sh)                               | Builds the project with AOT (Ahead-of-Time) compilation              |
| [build-image](./build-image.sh)                           | Builds the project and generates a Docker image                      |
| [build-image-native](./build-image-native.sh)             | Builds the native project and generates a Docker image               |
| [run-spring-boot](./run-spring-boot.sh)                   | Runs the project using Spring Boot and docker-compose maven plugins  |
| [run-aot](./run-aot.sh)                                   | Runs the project with AOT (Ahead-of-Time) compilation                |
| [run-image](./run-image.sh)                               | Runs the project in a Docker container                               |
| [run-ddbb-mongo-local](./run-ddbb-mongo-local.sh)         | Sets up a local MongoDB test database in a container                 |
| [init-ddbb-mongo-local](./init-ddbb-mongo-local.sh)       | Loads test data into the MongoDB database                            |
| [cleanup-ddbb-mongo-local](./cleanup-ddbb-mongo-local.sh) | Removes the MongoDB user and database                                |
| [run-ddbb-couchbase-local](./run-ddbb-couchbase-local.sh) | Sets up a local Couchbase test database in a container               |
