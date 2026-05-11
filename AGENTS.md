# AGENTS.md

Instructions for agents working on this project.

---

# Project Overview

This is a Spring Boot backend project named `arbit`.

The service provides personalized exhibition/performance/event recommendations
based on user preferences, keywords, bookmarks, reviews, and classification tags.

Main features include:

- JWT-based authentication
- Personalized recommendation system
- Preference keyword management
- Exhibition/performance/event search & filtering
- Bookmark system
- Review & rating system
- OCR-based review verification (planned)
- Profile-based cloud storage abstraction
- Recommendation score generation

---

# Tech Stack

- Java 17
- Spring Boot 3.3.4
- Gradle (Groovy DSL)
- MySQL
- Spring Data JPA
- Spring Security + JWT
- Lombok
- Docker Compose
- AWS S3 / Google Cloud Storage

---

# Package Structure

Keep package names under:

```text
com.arbit.app
```

Recommended domain-oriented structure:

```text
src/main/java/com/arbit/app/

├── auth
├── user
├── category
├── keyword
├── event
├── recommendation
├── bookmark
├── review
├── storage
├── notification
├── common
│   ├── response
│   ├── exception
│   ├── config
│   └── util
```

---

# Main Domain Model

The system is built around these core entities:

- USER
- EVENT
- CATEGORY
- REVIEW
- BOOKMARK
- RECOMMENDATION
- CLASSIFICATION_KEYWORD
- EVENT_CLASSIFICATION
- USER_CATEGORY
- USER_PREFERENCE_KEYWORD
- PREFERENCE_KEYWORD
- EVENT_KEYWORD

ERD reference must be respected when creating entities and relationships.

---

# Database Rules

- Use JPA + Hibernate
- Database: MySQL
- Keep:

```yaml
spring.jpa.hibernate.ddl-auto=update
```

- UUID should be used for:
  - USER
  - EVENT

- Use `@Enumerated(EnumType.STRING)` for all ENUM fields.

- Always include:
  - created_at
  - updated_at (when applicable)

- Avoid N+1 queries:
  - use fetch join
  - use EntityGraph when needed

- Bookmark uniqueness:
  - one user cannot bookmark the same event twice

- Review uniqueness:
  - one user can write only one review per event

---

# Authentication Rules

Authentication is based on:

- username
- password
- JWT access token

Requirements:

- Password must be encrypted using BCrypt
- JWT must be stateless
- Use Spring Security filter chain
- Unauthorized responses should return unified error format

Future extension:
- Google Login
- Apple Login

---

# API Response Rules

All controller responses must use:

```java
ApiResponse<T>
```

Example:

```java
return ApiResponse.success(data);
```

Error handling must be centralized in:

```text
GlobalExceptionHandler
```

Do not return raw exception messages directly.

---

# Recommendation System Rules

Recommendation logic is a core domain feature.

Recommendation score may include:

- User selected categories
- Preference keywords
- Event keywords
- Review-derived tags
- Similar users
- Mood/classification similarity
- Distance/location relevance

Recommendation result should contain:

- match_score
- recommendation reason text

Example:

```text
"미디어아트 키워드와 일치"
```

Keep recommendation generation logic inside:

```text
recommendation/service
```

Do not place recommendation logic directly inside controllers.

---

# Review System Rules

Review features:

- Rating: 1~5
- Max review text length: 200
- OCR verification image upload (planned)
- Positive/negative keyword extraction (planned)

When review is created:

- Update average rating
- Extract preference keywords if enabled
- Reflect immediately in recommendation data

---

# Event Rules

Event status values:

```text
upcoming
ongoing
closed
```

Event sorting:

- closing soon (default)
- distance
- latest

Filtering:

- category
- district
- free/paid
- ongoing/upcoming

---

# Storage Rules

Storage implementation must remain profile-specific.

Use:

```java
@Profile("aws")
```

for:

```text
S3StorageService
```

Use:

```java
@Profile("gcp")
```

for:

```text
GcsStorageService
```

Never mix provider-specific logic inside business services.

Controllers/services should depend on:

```text
StorageService interface
```

only.

---

# Scheduler Rules

Scheduled notifications must remain in:

```text
NotificationScheduler
```

Possible notification targets:

- bookmarked events
- upcoming exhibitions
- early bird deadlines
- closing soon reminders

---

# Validation Rules

Use Bean Validation:

- @NotNull
- @NotBlank
- @Size
- @Min
- @Max

Examples:

```java
@Size(max = 200)
private String content;
```

All validation errors should be handled by GlobalExceptionHandler.

---

# Configuration Files

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
src/main/resources/application-aws.yml
src/main/resources/application-gcp.yml
```

---

# Local Development

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

---

# Environment Rules

Secrets must NOT be committed.

Use:

```text
.env
```

for local secrets.

`.env` must remain gitignored.

Profile selection:

```text
SPRING_PROFILES_ACTIVE=aws
```

or:

```text
SPRING_PROFILES_ACTIVE=gcp
```

---

# Coding Conventions

- Prefer constructor injection
- Prefer immutable DTOs
- Keep controllers thin
- Business logic belongs in services
- Repository should contain only DB logic
- Use descriptive method names
- Avoid hardcoded strings
- Extract constants when repeated

---

# Testing Rules

Recommended test layers:

- Repository tests
- Service tests
- Controller integration tests

Important test targets:

- JWT authentication
- Recommendation generation
- Bookmark duplication prevention
- Review validation
- Filter/search logic

---

# Future Expansion Notes

Planned future features:

- Social login
- NLP-based keyword extraction
- OCR ticket verification
- Personalized notification system
- Embedding/vector-based recommendation
- Redis caching
- Elasticsearch-based search
