# Arbit 테이블 명세서

현재 Spring Boot JPA 엔티티를 기준으로 작성한 데이터베이스 테이블 정의 문서입니다. 타입과 제약조건은 엔티티 어노테이션을 MySQL 관점으로 해석했습니다.

## 문서 개요

대상 패키지: `com.arbit.app` / 기준 파일: `src/main/java/com/arbit/app/**/entity`

| 항목 | 내용 |
| --- | --- |
| Project | arbit |
| Stack | Java 17, Spring Boot 3.3.4, JPA, MySQL |
| Entity Tables | 19개 |
| Updated | 2026-06-01 |

### 공통 규칙

`BaseTimeEntity`를 상속한 모든 엔티티는 `created_at`, `updated_at` 감사 컬럼을 가집니다. `created_at`은 생성 시 필수이며 수정 불가, `updated_at`은 마지막 수정 시각을 저장합니다.

> 주의: 실제 DDL은 Hibernate `ddl-auto=update`, MySQL 버전, naming strategy에 따라 일부 물리 타입과 인덱스명이 달라질 수 있습니다. 이 문서는 현재 엔티티 계약을 기준으로 한 테이블 명세입니다.

### 테이블 목차

- [users](#users): 사용자 계정과 프로필
- [categories](#categories): 이벤트 카테고리 마스터
- [events](#events): 전시, 공연, 행사 정보
- [keywords](#keywords): 통합 키워드 마스터
- [preference_keywords](#preference_keywords): 취향 키워드 마스터
- [classification_keywords](#classification_keywords): 분류 키워드 마스터
- [age_restriction_keywords](#age_restriction_keywords): 연령 제한 키워드
- [user_categories](#user_categories): 사용자 선호 카테고리
- [user_keyword_weights](#user_keyword_weights): 사용자 키워드 가중치
- [event_keywords](#event_keywords): 이벤트 취향 키워드 매핑
- [event_keyword_weights](#event_keyword_weights): 이벤트 키워드 가중치
- [event_classifications](#event_classifications): 이벤트 분류 매핑
- [event_age_restrictions](#event_age_restrictions): 이벤트 연령 제한
- [bookmarks](#bookmarks): 사용자 북마크
- [reviews](#reviews): 사용자 리뷰
- [recommendations](#recommendations): 개인화 추천 결과
- [recommendation_runs](#recommendation_runs): 추천 실행 이력
- [recommendation_items](#recommendation_items): 추천 실행별 항목

## users

회원 로그인 계정, 공개 프로필, 거주 지역 및 위치 기반 추천에 필요한 좌표 정보를 저장합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `User` |
| Primary Key | `id` UUID, `BINARY(16)` |
| Unique | `username` |
| Enums | `gender`: MALE, FEMALE, NONSELECT |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BINARY(16) | PK NOT NULL | 사용자 식별자입니다. 애플리케이션에서 UUID를 생성합니다. |
| `username` | username | VARCHAR(50) | UNIQUE NOT NULL | 로그인 계정명입니다. |
| `password` | password | VARCHAR(255) | NOT NULL | 암호화된 비밀번호 해시입니다. |
| `nickname` | nickname | VARCHAR(100) | NOT NULL | 사용자 표시 이름입니다. |
| `profile_image_url` | profileImageUrl | VARCHAR(1000) | NULL | 프로필 이미지 URL입니다. |
| `age` | age | INT | NOT NULL | 사용자 나이입니다. |
| `gender` | gender | VARCHAR(20) | NULL | 사용자 성별 선택값입니다. |
| `residential_area` | residentialArea | VARCHAR(255) | NOT NULL | 사용자의 거주 지역명입니다. |
| `residential_latitude` | residentialLatitude | DOUBLE | NULL | 거주지 위도입니다. |
| `residential_longitude` | residentialLongitude | DOUBLE | NULL | 거주지 경도입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## categories

전시, 공연, 행사 등을 분류하는 상위 카테고리 마스터 테이블입니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `Category` |
| Primary Key | `id` BIGINT auto increment |
| Unique | `name` |
| Referenced By | `events`, `user_categories` |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 카테고리 식별자입니다. |
| `name` | name | VARCHAR(50) | UNIQUE NOT NULL | 카테고리명입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## events

추천과 검색의 대상인 전시, 공연, 행사 정보를 저장합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `Event` |
| Primary Key | `id` UUID, `BINARY(16)` |
| Foreign Key | `category_id` → `categories.id` |
| Enums | `status`: UPCOMING, ONGOING, CLOSED |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BINARY(16) | PK NOT NULL | 이벤트 식별자입니다. 애플리케이션에서 UUID를 생성합니다. |
| `category_id` | category | BIGINT | FK NOT NULL | 이벤트가 속한 카테고리입니다. |
| `title` | title | VARCHAR(200) | NOT NULL | 이벤트 제목입니다. |
| `description` | description | TEXT | NULL | 이벤트 설명입니다. |
| `poster_image_url` | posterImageUrl | VARCHAR(1000) | NULL | 포스터 또는 대표 이미지 URL입니다. |
| `venue` | venue | VARCHAR(100) | NOT NULL | 행사가 열리는 장소명입니다. |
| `venue_address` | venueAddress | VARCHAR(255) | NULL | 장소 주소입니다. |
| `district` | district | VARCHAR(50) | NOT NULL | 지역구 또는 행정구역명입니다. |
| `latitude` | latitude | DOUBLE | NULL | 이벤트 장소 위도입니다. |
| `longitude` | longitude | DOUBLE | NULL | 이벤트 장소 경도입니다. |
| `start_date` | startDate | DATE | NOT NULL | 이벤트 시작일입니다. |
| `end_date` | endDate | DATE | NOT NULL | 이벤트 종료일입니다. |
| `free` | free | BIT / BOOLEAN | NOT NULL | 무료 이벤트 여부입니다. |
| `status` | status | VARCHAR(20) | NOT NULL | 이벤트 진행 상태입니다. |
| `average_rating` | averageRating | DECIMAL(3,2) | NOT NULL | 리뷰 기반 평균 평점입니다. 생성 시 0으로 초기화됩니다. |
| `price` | price | VARCHAR(255) | NULL | 가격 설명입니다. |
| `booking_url` | bookingUrl | VARCHAR(1000) | NULL | 예매 또는 상세 페이지 URL입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## keywords

추천 계산에 사용하는 통합 키워드 마스터 테이블입니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `Keyword` |
| Primary Key | `id` BIGINT auto increment |
| Unique | `uk_keyword_type_value`: type + value |
| Enums | `type`: CATEGORY, MOOD, TOPIC, AUDIENCE, AGE, FEATURE |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 키워드 식별자입니다. |
| `type` | type | VARCHAR(30) | UK NOT NULL | 키워드 유형입니다. |
| `value` | value | VARCHAR(80) | UK NOT NULL | 키워드 값입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## preference_keywords

사용자 취향과 이벤트 특성을 연결하는 취향 키워드 마스터 테이블입니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `PreferenceKeyword` |
| Primary Key | `id` BIGINT auto increment |
| Unique | `value` |
| Referenced By | `event_keywords` |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 취향 키워드 식별자입니다. |
| `value` | value | VARCHAR(50) | UNIQUE NOT NULL | 키워드 값입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## classification_keywords

이벤트 분류에 사용하는 키워드 마스터 테이블입니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `ClassificationKeyword` |
| Primary Key | `id` BIGINT auto increment |
| Unique | `value` |
| Referenced By | `event_classifications` |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 분류 키워드 식별자입니다. |
| `value` | value | VARCHAR(50) | UNIQUE NOT NULL | 분류 키워드 값입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## age_restriction_keywords

이벤트 연령 제한 정보를 표현하는 키워드 마스터 테이블입니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `AgeRestrictionKeyword` |
| Primary Key | `id` BIGINT auto increment |
| Unique | `value` |
| Referenced By | `event_age_restrictions` |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 연령 제한 키워드 식별자입니다. |
| `value` | value | VARCHAR(50) | UNIQUE NOT NULL | 연령 제한 값입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## user_categories

사용자와 선호 카테고리를 연결합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `UserCategory` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `user_id`, `category_id` |
| Unique | `uk_user_category`: user + category |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 매핑 식별자입니다. |
| `user_id` | user | BINARY(16) | FK UK NOT NULL | `users.id`를 참조합니다. |
| `category_id` | category | BIGINT | FK UK NOT NULL | `categories.id`를 참조합니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## user_keyword_weights

사용자별 통합 키워드 가중치를 저장합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `UserKeywordWeight` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `user_id`, `keyword_id` |
| Unique | `uk_user_keyword_weight`: user + keyword |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 가중치 식별자입니다. |
| `user_id` | user | BINARY(16) | FK UK NOT NULL | `users.id`를 참조합니다. |
| `keyword_id` | keyword | BIGINT | FK UK NOT NULL | `keywords.id`를 참조합니다. |
| `weight` | weight | DECIMAL(5,4) | NOT NULL | 사용자 키워드 가중치입니다. |
| `source` | source | VARCHAR(30) | NOT NULL | 가중치 생성 출처입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## event_keywords

이벤트와 취향 키워드를 연결합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `EventKeyword` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `event_id`, `preference_keyword_id` |
| Unique | `uk_event_keyword`: event + preference keyword |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 매핑 식별자입니다. |
| `event_id` | event | BINARY(16) | FK UK NOT NULL | `events.id`를 참조합니다. |
| `preference_keyword_id` | preferenceKeyword | BIGINT | FK UK NOT NULL | `preference_keywords.id`를 참조합니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## event_keyword_weights

이벤트별 통합 키워드 가중치를 저장합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `EventKeywordWeight` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `event_id`, `keyword_id` |
| Unique | `uk_event_keyword_weight`: event + keyword |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 가중치 식별자입니다. |
| `event_id` | event | BINARY(16) | FK UK NOT NULL | `events.id`를 참조합니다. |
| `keyword_id` | keyword | BIGINT | FK UK NOT NULL | `keywords.id`를 참조합니다. |
| `weight` | weight | DECIMAL(5,4) | NOT NULL | 이벤트 키워드 가중치입니다. |
| `source` | source | VARCHAR(30) | NOT NULL | 가중치 생성 출처입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## event_classifications

이벤트와 분류 키워드를 연결합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `EventClassification` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `event_id`, `classification_keyword_id` |
| Unique | `uk_event_classification`: event + classification keyword |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 매핑 식별자입니다. |
| `event_id` | event | BINARY(16) | FK UK NOT NULL | `events.id`를 참조합니다. |
| `classification_keyword_id` | classificationKeyword | BIGINT | FK UK NOT NULL | `classification_keywords.id`를 참조합니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## event_age_restrictions

이벤트별 연령 제한 정보를 저장합니다. 현재 제약상 이벤트당 하나의 연령 제한만 저장됩니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `EventAgeRestriction` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `event_id`, `age_restriction_keyword_id` |
| Unique | `uk_event_age_restriction_event`: event |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 매핑 식별자입니다. |
| `event_id` | event | BINARY(16) | FK UNIQUE NOT NULL | `events.id`를 참조하며 테이블 내에서 유일합니다. |
| `age_restriction_keyword_id` | ageRestrictionKeyword | BIGINT | FK NOT NULL | `age_restriction_keywords.id`를 참조합니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## bookmarks

사용자가 관심 있는 이벤트를 저장한 북마크 목록입니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `Bookmark` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `user_id`, `event_id` |
| Unique | `uk_bookmark_user_event`: user + event |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 북마크 식별자입니다. |
| `user_id` | user | BINARY(16) | FK UK NOT NULL | `users.id`를 참조합니다. |
| `event_id` | event | BINARY(16) | FK UK NOT NULL | `events.id`를 참조합니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## reviews

사용자가 이벤트에 작성한 평점과 리뷰 내용을 저장합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `Review` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `user_id`, `event_id` |
| Unique | `uk_review_user_event`: user + event |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 리뷰 식별자입니다. |
| `user_id` | user | BINARY(16) | FK UK NOT NULL | `users.id`를 참조합니다. |
| `event_id` | event | BINARY(16) | FK UK NOT NULL | `events.id`를 참조합니다. |
| `rating` | rating | INT | NOT NULL | 사용자 평점입니다. 도메인 규칙상 1부터 5까지의 값을 기대합니다. |
| `content` | content | VARCHAR(200) | NOT NULL | 리뷰 본문입니다. |
| `verification_image_url` | verificationImageUrl | VARCHAR(500) | NULL | 관람 인증 이미지 URL입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## recommendations

사용자별 이벤트 추천 결과를 저장합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `Recommendation` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `user_id`, `event_id` |
| Unique | `uk_recommendation_user_event`: user + event |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 추천 결과 식별자입니다. |
| `user_id` | user | BINARY(16) | FK UK NOT NULL | `users.id`를 참조합니다. |
| `event_id` | event | BINARY(16) | FK UK NOT NULL | `events.id`를 참조합니다. |
| `match_score` | matchScore | DECIMAL(5,2) | NOT NULL | 사용자와 이벤트의 매칭 점수입니다. |
| `reason` | reason | VARCHAR(500) | NOT NULL | 사용자에게 보여줄 추천 사유 문구입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## recommendation_runs

추천 알고리즘 실행 이력과 입력 이벤트 목록을 저장합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `RecommendationRun` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Key | `user_id` → `users.id` |
| Referenced By | `recommendation_items` |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 추천 실행 식별자입니다. |
| `user_id` | user | BINARY(16) | FK NOT NULL | `users.id`를 참조합니다. |
| `algorithm` | algorithm | VARCHAR(50) | NOT NULL | 추천 알고리즘명입니다. |
| `model_version` | modelVersion | VARCHAR(50) | NOT NULL | 모델 또는 알고리즘 버전입니다. |
| `input_event_ids_json` | inputEventIdsJson | TEXT | NULL | 추천 입력 이벤트 ID 목록 JSON입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## recommendation_items

추천 실행별 이벤트 추천 항목과 점수, 사유, 상세 피처 점수를 저장합니다.

| 항목 | 내용 |
| --- | --- |
| Entity | `RecommendationItem` |
| Primary Key | `id` BIGINT auto increment |
| Foreign Keys | `run_id`, `event_id` |
| Unique | `uk_recommendation_item_run_event`: run + event |

| 컬럼 | Java 필드 | 타입 | 제약 | 설명 |
| --- | --- | --- | --- | --- |
| `id` | id | BIGINT | PK NOT NULL AUTO_INCREMENT | 추천 항목 식별자입니다. |
| `run_id` | run | BIGINT | FK UK NOT NULL | `recommendation_runs.id`를 참조합니다. |
| `event_id` | event | BINARY(16) | FK UK NOT NULL | `events.id`를 참조합니다. |
| `rank_no` | rankNo | INT | NOT NULL | 추천 순위입니다. |
| `score` | score | DECIMAL(8,4) | NOT NULL | 추천 점수입니다. |
| `reason` | reason | VARCHAR(300) | NOT NULL | 추천 사유입니다. |
| `feature_scores_json` | featureScoresJson | TEXT | NULL | 피처별 점수 JSON입니다. |
| `created_at` | createdAt | DATETIME(6) | NOT NULL | 생성 시각입니다. |
| `updated_at` | updatedAt | DATETIME(6) | NULL | 수정 시각입니다. |

## 주요 관계 요약

엔티티의 `@ManyToOne`, `@JoinColumn`, `@UniqueConstraint` 기준 관계입니다.

- `events.category_id` → `categories.id`: 이벤트는 하나의 카테고리에 속합니다.
- `user_categories.user_id` + `category_id`: 사용자별 선호 카테고리는 중복 저장되지 않습니다.
- `keywords.type` + `value`: 같은 유형 안에서 키워드 값은 중복 저장되지 않습니다.
- `user_keyword_weights.user_id` + `keyword_id`: 사용자별 키워드 가중치는 하나만 유지됩니다.
- `event_keywords.event_id` + `preference_keyword_id`: 이벤트별 취향 키워드는 중복 저장되지 않습니다.
- `event_keyword_weights.event_id` + `keyword_id`: 이벤트별 키워드 가중치는 하나만 유지됩니다.
- `event_classifications.event_id` + `classification_keyword_id`: 이벤트별 분류 키워드는 중복 저장되지 않습니다.
- `event_age_restrictions.event_id`: 이벤트당 하나의 연령 제한 정보만 저장됩니다.
- `bookmarks.user_id` + `event_id`: 사용자는 같은 이벤트를 한 번만 북마크할 수 있습니다.
- `reviews.user_id` + `event_id`: 사용자는 같은 이벤트에 리뷰를 하나만 작성할 수 있습니다.
- `recommendations.user_id` + `event_id`: 사용자별 동일 이벤트 추천 결과는 하나만 유지됩니다.
- `recommendation_runs.user_id` → `users.id`: 추천 실행 이력은 사용자에 속합니다.
- `recommendation_items.run_id` + `event_id`: 추천 실행 안에서 같은 이벤트 항목은 중복 저장되지 않습니다.

---

Generated from JPA entities in `D:\Project\Arbit`. This document does not modify API request or response contracts.
