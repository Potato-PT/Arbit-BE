# AGENTS.md

Instructions for agents working on this project.

## 1. Priority Rules

Follow these rules first.

- Never modify request JSON or response JSON formats that are already defined in Swagger/OpenAPI.
- When asked to develop or change a feature, write or update the Swagger/OpenAPI definition first.
- After writing Swagger/OpenAPI, verify that request and response fields match the intended API contract before changing implementation code.
- Keep controllers thin. Put business logic in services.
- All controller responses must use `ApiResponse<T>`.
- All exception handling must go through `GlobalExceptionHandler`.
- Do not return raw exception messages directly.
- When fixing an error by checking logs after execution, run and inspect logs up to 3 times. If the issue still fails on the 3rd attempt, stop retrying and explain why the last attempt failed.

## 2. Project Summary

- Project name: `arbit`
- Stack: Java 17, Spring Boot 3.3.4, Gradle, MySQL, Spring Data JPA, Spring Security, JWT, Lombok, Docker Compose, Google Cloud Storage
- Domain: personalized exhibition, performance, and event recommendation service

Main features:

- JWT-based authentication
- Personalized recommendation system
- Preference keyword management
- Event search and filtering
- Bookmark system
- Review and rating system
- Profile-based cloud storage abstraction
- Recommendation score generation

Planned features:

- OCR-based review verification
- Social login
- NLP-based keyword extraction
- Personalized notifications
- Embedding/vector-based recommendation
- Redis caching
- Elasticsearch-based search

## 3. Package and Code Structure

Keep all packages under:

```text
com.arbit.app
```

Recommended structure:

```text
src/main/java/com/arbit/app/
|- auth
|- user
|- category
|- keyword
|- event
|- recommendation
|- bookmark
|- review
|- storage
|- notification
`- common
   |- response
   |- exception
   |- config
   `- util
```

Code conventions:

- Prefer constructor injection.
- Prefer immutable DTOs.
- Repositories should contain only persistence logic.
- Use descriptive method names.
- Avoid hardcoded strings.
- Extract constants when values are repeated.

## 4. Domain Model

Core entities:

- `USER`
- `EVENT`
- `CATEGORY`
- `REVIEW`
- `BOOKMARK`
- `RECOMMENDATION`
- `CLASSIFICATION_KEYWORD`
- `EVENT_CLASSIFICATION`
- `USER_CATEGORY`
- `USER_PREFERENCE_KEYWORD`
- `PREFERENCE_KEYWORD`
- `EVENT_KEYWORD`

Respect the ERD when creating entities and relationships.

## 5. Database Rules

- Use JPA + Hibernate.
- Database is MySQL.
- Keep `spring.jpa.hibernate.ddl-auto=update`.
- Use UUID for `USER` and `EVENT`.
- Use `@Enumerated(EnumType.STRING)` for every enum field.
- Include `created_at` and `updated_at` when applicable.
- Avoid N+1 queries with fetch join or `EntityGraph` when needed.
- Enforce bookmark uniqueness: one user cannot bookmark the same event twice.
- Enforce review uniqueness: one user can write only one review per event.

## 6. Authentication and Security Rules

Authentication is based on:

- username
- password
- JWT access token

Requirements:

- Encode passwords with BCrypt.
- Keep JWT authentication stateless.
- Use Spring Security filter chain.
- Return unauthorized responses in the unified error format.

Future extension targets:

- Google Login
- Apple Login

## 7. API Rules

- Every controller response must use `ApiResponse<T>`.
- Keep request and response JSON formats identical to the Swagger/OpenAPI contract.
- Centralize error handling in `GlobalExceptionHandler`.
- Handle validation errors in `GlobalExceptionHandler`.

Example:

```java
return ApiResponse.success(data);
```

## 8. Recommendation Rules

Recommendation logic is a core domain feature.

Recommendation scoring may include:

- user selected categories
- preference keywords
- event keywords
- review-derived tags
- similar users
- mood or classification similarity
- distance or location relevance

Recommendation results should contain:

- `match_score`
- recommendation reason text

Example reason:

```text
"미디어아트 키워드와 일치"
```

Implementation rule:

- Keep recommendation generation logic inside `recommendation/service`.
- Do not place recommendation logic directly in controllers.

## 9. Review Rules

Review requirements:

- rating range: `1` to `5`
- max review text length: `200`

Planned review extensions:

- OCR verification image upload
- positive/negative keyword extraction

When a review is created:

- update average rating
- extract preference keywords if enabled
- reflect the change in recommendation data immediately

## 10. Event Rules

Event status values:

```text
upcoming
ongoing
closed
```

Default and supported sorting:

- closing soon
- distance
- latest

Supported filtering:

- category
- district
- free or paid
- ongoing or upcoming

## 11. Storage Rules

- Keep storage implementations profile-specific.
- Use `@Profile("gcp")` for `GcsStorageService`.
- Do not mix provider-specific logic into business services.
- Controllers and services must depend on `StorageService` interface only.

## 12. Scheduler Rules

- Keep scheduled notification logic in `NotificationScheduler`.

Possible notification targets:

- bookmarked events
- upcoming exhibitions
- early bird deadlines
- closing soon reminders

## 13. Validation Rules

Use Bean Validation annotations such as:

- `@NotNull`
- `@NotBlank`
- `@Size`
- `@Min`
- `@Max`

Example:

```java
@Size(max = 200)
private String content;
```

## 14. Configuration and Environment Rules

Maintain these files:

```text
build.gradle
settings.gradle
gradlew
gradlew.bat
docker-compose.yml
.gitignore
.github/workflows/ci.yml
src/main/resources/application.yml
src/main/resources/application-gcp.yml
```

Environment rules:

- Never commit secrets.
- Use `.env` for local secrets.
- Keep `.env` in `.gitignore`.
- Never modify `.gitignore` unless the user explicitly asks for a `.gitignore` change.
- Keep the default server configuration for deployment on the VM instance with Docker Compose.
- When running against `arbit_local`, apply local database and server settings only through temporary process environment variables; do not persist local settings in deployment configuration files.
- After stopping a locally executed server, leave the project configured for VM instance Docker deployment.
- When running Arbit locally, use the local MySQL database.
- When the user types `restart`, start both the `Arbit` and `Arbit-AI` servers at the same time using the local MySQL database.
- For `restart`, use a dedicated visible run terminal and execute the run commands there so server logs remain visible.
- Use `SPRING_PROFILES_ACTIVE=gcp` when selecting the GCP profile.

## 15. Local Development Commands

Start MySQL:

```powershell
docker compose up -d
```

Set JDK 17:

```powershell
$env:JAVA_HOME='C:\Program Files\Java\jdk-17'
```

Compile:

```powershell
.\gradlew.bat compileJava --no-daemon
```

Build:

```powershell
.\gradlew.bat clean build --no-daemon
```

CI:

```sh
sh ./gradlew build --no-daemon
sh ./gradlew test --no-daemon
```

## 16. Testing Rules

Recommended test layers:

- repository tests
- service tests
- controller integration tests

Priority test targets:

- JWT authentication
- recommendation generation
- bookmark duplication prevention
- review validation
- filter and search logic
