# 프로젝트 구조

작성 기준: 2026-05-05

## 루트 구조

```text
.
├── build.gradle.kts          # 전체 Gradle 플러그인, 릴리스, Docker/JReleaser 설정
├── settings.gradle           # Gradle 모듈 include
├── gradle/                   # Gradle wrapper 설정
├── komga/                    # 백엔드 서버 모듈
├── komga-webui/              # 웹 UI 모듈
├── komga-tray/               # 데스크톱 트레이 모듈
├── res/                      # 패키징/리소스 보조 파일
├── README.md                 # 사용자용 프로젝트 개요
├── DEVELOPING.md             # 개발 환경과 실행 가이드
└── docs/                     # 사람이 읽는 프로젝트 구조/기능 문서
```

`settings.gradle` 기준 Gradle 모듈은 `komga`, `komga-tray` 두 개입니다. `komga-webui`는 Gradle 서브프로젝트가 아니라, 백엔드 Gradle 태스크가 `npm install`, `npm run build`를 호출해서 빌드 산출물을 `komga/src/main/resources/public`로 복사하는 구조입니다.

## `komga` 백엔드 모듈

`komga`는 Kotlin + Spring Boot 서버입니다. 기본 서버 포트는 `25600`이며, `dev` 프로파일에서는 `8080`을 사용합니다.

```text
komga/
├── build.gradle.kts
├── docs/openapi.json
├── docker/
└── src/
    ├── main/kotlin/org/gotson/komga/
    │   ├── Application.kt
    │   ├── application/
    │   ├── domain/
    │   ├── infrastructure/
    │   ├── interfaces/
    │   └── language/
    ├── main/resources/
    │   ├── application.yml
    │   ├── application-dev.yml
    │   ├── application-docker.yml
    │   └── application-localdb.yml
    ├── flyway/
    └── test/
```

### 백엔드 계층

| 패키지 | 역할 |
| --- | --- |
| `org.gotson.komga.Application` | Spring Boot 진입점, 스케줄링 활성화 |
| `application` | 비동기 작업 큐, 작업 처리기, 라이브러리 스캔 스케줄러, 애플리케이션 이벤트 |
| `domain.model` | `Library`, `Series`, `Book`, `Media`, `ReadList`, `KomgaUser`, `ReadProgress`, `PageHash` 등 핵심 도메인 모델 |
| `domain.persistence` | 도메인 저장소 인터페이스 |
| `domain.service` | 라이브러리/시리즈/도서 라이프사이클, 분석, 메타데이터, 읽기목록, 사용자, 중복 페이지 처리 |
| `infrastructure` | SQLite/jOOQ, Flyway, Lucene 검색, 보안, 이미지 처리, 미디어 컨테이너 추출/복호화, 메타데이터 제공자, OpenAPI, 캐시 |
| `interfaces.api.rest` | REST API 컨트롤러와 DTO |
| `interfaces.api.opds` | OPDS v1/v2 API |
| `interfaces.api.kobo` | Kobo Sync API |
| `interfaces.api.kosync` | KOReader Sync API |
| `interfaces.mvc` | 웹 UI index 라우팅과 정적 리소스 처리 |
| `interfaces.sse` | 서버 이벤트 스트림 |
| `interfaces.scheduler` | 초기 사용자 생성, 주기 작업, 메트릭, 검색 인덱스 제어 |
| `interfaces.apprunner` | CLI성 실행기: 사용자 목록, 비밀번호 재설정 |

### 데이터와 저장소

- 주 데이터베이스: SQLite, 기본 위치는 `${komga.config-dir}/database.sqlite`
- 작업 데이터베이스: SQLite, 기본 위치는 `${komga.config-dir}/tasks.sqlite`
- 검색 인덱스: Lucene, 기본 위치는 `${komga.config-dir}/lucene`
- 폰트 저장소: 기본 위치는 `${komga.config-dir}/fonts`
- 기본 설정 디렉토리: `${user.home}/.komga`
- DB 마이그레이션: `komga/src/flyway/resources/db/migration/sqlite`
- 작업 DB 마이그레이션: `komga/src/flyway/resources/tasks/migration/sqlite`
- DB 접근 구현: `infrastructure/jooq/main`, `infrastructure/jooq/tasks`

## `komga-webui` 프론트엔드 모듈

`komga-webui`는 Vue 2, TypeScript, Vue Router, Vuex, Vuetify 기반의 SPA입니다. 개발 서버는 `npm run serve`로 실행하며 기본 포트는 `8081`입니다.

```text
komga-webui/
├── package.json
├── public/
├── src/
│   ├── assets/
│   ├── components/
│   ├── functions/
│   ├── locales/
│   ├── plugins/
│   ├── services/
│   ├── styles/
│   ├── types/
│   ├── views/
│   ├── router.ts
│   └── store.ts
└── tests/unit/
```

### 프론트엔드 주요 디렉토리

| 경로 | 역할 |
| --- | --- |
| `src/views` | 라우트 단위 화면. 대시보드, 라이브러리 탐색, 리더, 설정, 관리 화면 |
| `src/components` | 공용 바, 다이얼로그, 메뉴, 아이콘, 리더 컴포넌트 |
| `src/components/readers` | `ContinuousReader`, `PagedReader` 등 도서 페이지 리더 컴포넌트 |
| `src/services` | 백엔드 REST API 호출 계층. 도서, 시리즈, 라이브러리, 사용자, 설정 등 기능별 서비스 |
| `src/types` | API DTO와 UI 타입 정의 |
| `src/functions` | 필터, 페이지 계산, 파일 다운로드, 리더 단축키, URL, 문자열 유틸리티 |
| `src/locales` | 다국어 번역 리소스 |
| `src/store.ts` | 전역 다이얼로그 상태, 공지, 릴리스, Actuator 정보 등 Vuex 상태 |
| `src/router.ts` | SPA 라우팅과 관리자/라이브러리 가드 |

### 주요 라우트

| 라우트 | 화면 |
| --- | --- |
| `/dashboard` | 대시보드, 추천/최근/이어보기성 콘텐츠 |
| `/libraries/:libraryId/books` | 라이브러리 도서 목록 |
| `/libraries/:libraryId/series` | 라이브러리 시리즈 목록 |
| `/libraries/:libraryId/collections` | 컬렉션 목록 |
| `/libraries/:libraryId/readlists` | 읽기목록 목록 |
| `/series/:seriesId` | 시리즈 상세 |
| `/book/:bookId` | 도서 상세 |
| `/book/:bookId/read` | 이미지 기반 웹 리더 |
| `/book/:bookId/read-epub` | EPUB 리더 |
| `/collections/:collectionId` | 컬렉션 상세 |
| `/readlists/:readListId` | 읽기목록 상세 |
| `/search` | 검색 |
| `/import/books` | 외부 도서 가져오기 |
| `/import/readlist` | ComicRack 읽기목록 가져오기 |
| `/settings/*` | 관리자 설정, 사용자, 서버, UI, 메트릭, 공지, 업데이트 |
| `/media-management/*` | 미디어 분석, 누락 포스터, 중복 파일/페이지 관리 |
| `/account/*` | 내 계정, API Key, UI 설정, 인증 활동 |

## `komga-tray` 데스크톱 트레이 모듈

`komga-tray`는 `komga` 서버 모듈에 의존하는 Compose Desktop 애플리케이션입니다. 서버를 headless가 아닌 데스크톱 앱으로 실행하고 시스템 트레이 메뉴를 제공합니다.

```text
komga-tray/
├── build.gradle.kts
└── src/main/
    ├── kotlin/org/gotson/komga/
    │   ├── DesktopApplication.kt
    │   ├── RB.kt
    │   ├── Utils.kt
    │   └── application/gui/
    └── resources/
        ├── application-mac.yml
        ├── application-windows.yml
        ├── icons/
        └── org/gotson/komga/messages*.properties
```

트레이 메뉴는 Komga 열기, 로그 파일 열기, 설정 디렉토리 열기, 종료를 제공합니다. 서버 포트 충돌 등 시작 오류는 데스크톱 오류 다이얼로그로 표시합니다.

## 빌드와 패키징

| 영역 | 내용 |
| --- | --- |
| JVM | Java 25 toolchain |
| Kotlin | Kotlin 2.3.0 |
| 백엔드 | Spring Boot, Spring Security, Web MVC/WebFlux, jOOQ, Flyway, SQLite, Lucene |
| 프론트엔드 | Vue 2, TypeScript, Vuetify, Vuex, Vue Router |
| 패키징 | JReleaser, Docker, Conveyor, Compose Desktop |
| OpenAPI | `komga/docs/openapi.json`, Springdoc 설정은 `komga/build.gradle.kts`와 `application.yml` |
