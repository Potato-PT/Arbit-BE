# Arbit Table Specifications

현재 JPA 엔티티를 기준으로 작성한 `arbit_local` 스키마 명세이다.

## 공통 규칙

- DBMS: MySQL 8
- UUID PK/FK: `BINARY(16)`
- 숫자형 PK: `BIGINT AUTO_INCREMENT`
- 모든 엔티티 테이블은 `created_at DATETIME(6) NOT NULL`, `updated_at DATETIME(6) NULL`을 포함한다.
- 별도 표기가 없는 FK는 대상 테이블의 PK를 참조한다.

## 테이블 목록

| 테이블 | 설명 |
|---|---|
| `users` | 사용자 |
| `categories` | 이벤트 카테고리 |
| `events` | 이벤트 |
| `bookmarks` | 사용자 북마크 |
| `reviews` | 사용자 리뷰 |
| `user_categories` | 사용자 선호 카테고리 |
| `preference_keywords` | 선호 키워드 |
| `classification_keywords` | 분류 키워드 |
| `age_restriction_keywords` | 연령 제한 키워드 |
| `event_keywords` | 이벤트 선호 키워드 |
| `event_classifications` | 이벤트 분류 키워드 |
| `event_age_restrictions` | 이벤트 연령 제한 |
| `keywords` | 추천 모델 통합 키워드 |
| `user_keyword_weights` | 사용자별 키워드 가중치 |
| `event_keyword_weights` | 이벤트별 키워드 가중치 |
| `user_preference_events` | 사용자가 선택한 선호 이벤트 |
| `event_detail_view_logs` | 이벤트 상세 조회 기록 |
| `event_action_logs` | 이벤트 행동 기록 |
| `recommendations` | 사용자별 현재 추천 결과 |
| `recommendation_runs` | 추천 실행 이력 |
| `recommendation_items` | 추천 실행별 결과 항목 |

## users

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BINARY(16)` | NO | PK |
| `username` | `VARCHAR(50)` | NO | UNIQUE |
| `password` | `VARCHAR(255)` | NO | |
| `nickname` | `VARCHAR(100)` | YES | |
| `profile_image_url` | `VARCHAR(1000)` | YES | |
| `age` | `INT` | YES | |
| `gender` | `ENUM('MALE','FEMALE','NONSELECT')` | YES | |
| `residential_area` | `VARCHAR(255)` | YES | |
| `residential_latitude` | `DOUBLE` | YES | |
| `residential_longitude` | `DOUBLE` | YES | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

## categories

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `name` | `VARCHAR(50)` | NO | UNIQUE |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

## events

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BINARY(16)` | NO | PK |
| `category_id` | `BIGINT` | NO | FK -> `categories.id` |
| `title` | `VARCHAR(200)` | NO | |
| `description` | `TEXT` | YES | |
| `poster_image_url` | `VARCHAR(1000)` | YES | |
| `venue` | `VARCHAR(100)` | NO | |
| `venue_address` | `VARCHAR(255)` | YES | |
| `district` | `VARCHAR(50)` | NO | |
| `latitude` | `DOUBLE` | YES | |
| `longitude` | `DOUBLE` | YES | |
| `start_date` | `DATE` | NO | |
| `end_date` | `DATE` | NO | |
| `free` | `BIT(1)` | NO | |
| `status` | `VARCHAR(20)` | NO | `UPCOMING`, `ONGOING`, `CLOSED` |
| `average_rating` | `DECIMAL(3,2)` | NO | |
| `price` | `VARCHAR(255)` | YES | |
| `time` | `VARCHAR(255)` | YES | |
| `booking_url` | `VARCHAR(1000)` | YES | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

## 사용자-이벤트 활동 테이블

### bookmarks

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`user_id`, `event_id`)

### reviews

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `rating` | `INT` | NO | 애플리케이션 검증 범위 1~5 |
| `content` | `VARCHAR(200)` | NO | |
| `verification_image_url` | `VARCHAR(500)` | YES | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`user_id`, `event_id`)

### event_detail_view_logs

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

### event_action_logs

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `action_type` | `ENUM('DETAIL_VIEW','HOMEPAGE_CLICK')` | NO | |
| `source` | `ENUM('HOME','SEARCH','RECOMMENDATION','EVENT_DETAIL')` | YES | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- INDEX: (`user_id`, `created_at`)
- INDEX: (`event_id`, `action_type`, `created_at`)
- INDEX: (`user_id`, `event_id`, `action_type`)

## 선호 및 분류 테이블

### user_categories

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `category_id` | `BIGINT` | NO | FK -> `categories.id` |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`user_id`, `category_id`)

### preference_keywords / classification_keywords / age_restriction_keywords

세 테이블은 동일한 컬럼 구조를 사용한다.

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `value` | `VARCHAR(50)` | NO | UNIQUE |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

### event_keywords

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `preference_keyword_id` | `BIGINT` | NO | FK -> `preference_keywords.id` |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`event_id`, `preference_keyword_id`)

### event_classifications

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `classification_keyword_id` | `BIGINT` | NO | FK -> `classification_keywords.id` |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`event_id`, `classification_keyword_id`)

### event_age_restrictions

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id`, UNIQUE |
| `age_restriction_keyword_id` | `BIGINT` | NO | FK -> `age_restriction_keywords.id` |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

### user_preference_events

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `event_id` | `BINARY(16)` | NO | 논리적 이벤트 ID, FK 없음 |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`user_id`, `event_id`)

## 추천 모델 키워드 테이블

### keywords

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `type` | `VARCHAR(30)` | NO | `CATEGORY`, `MOOD`, `TOPIC`, `AUDIENCE`, `AGE`, `FEATURE` |
| `value` | `VARCHAR(80)` | NO | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`type`, `value`)

### user_keyword_weights

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `keyword_id` | `BIGINT` | NO | FK -> `keywords.id` |
| `weight` | `DECIMAL(5,4)` | NO | |
| `source` | `VARCHAR(30)` | NO | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`user_id`, `keyword_id`)

### event_keyword_weights

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `keyword_id` | `BIGINT` | NO | FK -> `keywords.id` |
| `weight` | `DECIMAL(5,4)` | NO | |
| `source` | `VARCHAR(30)` | NO | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`event_id`, `keyword_id`)

## 추천 결과 테이블

### recommendations

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `match_score` | `DECIMAL(5,2)` | NO | |
| `reason` | `VARCHAR(500)` | NO | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`user_id`, `event_id`)

### recommendation_runs

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `user_id` | `BINARY(16)` | NO | FK -> `users.id` |
| `algorithm` | `VARCHAR(50)` | NO | |
| `model_version` | `VARCHAR(50)` | NO | |
| `input_event_ids_json` | `TEXT` | YES | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

### recommendation_items

| 컬럼 | 타입 | NULL | 키/제약조건 |
|---|---|---|---|
| `id` | `BIGINT` | NO | PK, AUTO_INCREMENT |
| `run_id` | `BIGINT` | NO | FK -> `recommendation_runs.id` |
| `event_id` | `BINARY(16)` | NO | FK -> `events.id` |
| `rank_no` | `INT` | NO | |
| `score` | `DECIMAL(8,4)` | NO | |
| `reason` | `VARCHAR(300)` | NO | |
| `feature_scores_json` | `TEXT` | YES | |
| `created_at` | `DATETIME(6)` | NO | |
| `updated_at` | `DATETIME(6)` | YES | |

- UNIQUE: (`run_id`, `event_id`)
