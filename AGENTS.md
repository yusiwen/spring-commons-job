# AGENTS.md

## Build Commands

```bash
# Full compile
./mvnw compile

# Run tests
./mvnw test

# Run a single module
./mvnw compile -pl spring-commons-job-core -am

# Run tests with output
./mvnw test -pl spring-commons-job-test -am 2>&1 | tail -20
```

## Prerequisites

- JDK 8 installed at `~/.sdkman/candidates/java/8` (Azul)
- `~/.m2/toolchains.xml` configured with JDK 8:
  ```xml
  <toolchain>
    <type>jdk</type>
    <provides><version>1.8</version><vendor>Azul Systems, Inc.</vendor></provides>
    <configuration><jdkHome>~/.sdkman/candidates/java/8</jdkHome></configuration>
  </toolchain>
  ```

## Project Structure

```
spring-common-job (parent)
├── spring-commons-job-core/     — core: entities, mappers, scheduler, REST API
├── spring-commons-job-starter/  — Spring Boot auto-configuration wrapper
└── spring-commons-job-test/     — integration tests
```

## Architecture

- **DB schema init**: dual-channel (Flyway or DataSourceInitializer fallback)
- **Scheduling**: Spring `ThreadPoolTaskScheduler` + `CronTrigger`
- **ORM**: MyBatis annotation-based mapper, UUID PK, portable SQL
- **Distributed future**: `TaskExecutor` interface, `execute_mode`/`callback_url` fields reserved
