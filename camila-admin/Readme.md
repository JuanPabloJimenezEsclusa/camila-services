# admin

This project provides a user interface (UI) for managing services.

## Prerequisites

* JDK ~= [25.x](https://openjdk.org/projects/jdk/25/)
* Docker ~= [29.x](https://docs.docker.com/engine/release-notes/29/)
* Maven ~= [3.9.x](https://maven.apache.org/download.cgi)
* Gradle ~= [9.5.x](https://gradle.org/releases)
* Spring ~= [7.x](https://spring.io/projects/spring-framework#learn)
* Spring-boot ~= [4.0.x](https://spring.io/projects/spring-boot#learn)
* Spring-cloud ~= [2025.1.x](https://spring.io/projects/spring-cloud#learn)
* Native Image compilation
  * GraalVM ~= [25.x](https://www.graalvm.org/release-notes/JDK_25/)
  * GCC >= (linux, x86_64, 11.4.0)
    * `zlib1g-dev`

## Links

* **UI (dev):** <http://localhost:8100/> (Link to the admin UI in development environment)
* **Actuator:**
    * <http://localhost:8100/actuator/health> (Link to the health endpoint of the service)

## Operations (build, deploy)

For instructions on building and deploying this project, refer to the [Operate - Readme](.operate/Readme.md) file.
