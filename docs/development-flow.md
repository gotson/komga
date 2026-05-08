# 운영 및 개발 흐름

작성 기준: 2026-05-05

## 실행 흐름

### 백엔드 서버

`komga` 모듈의 `Application.kt`가 Spring Boot 애플리케이션을 시작합니다. 서버는 REST API, OPDS/Kobo/KOReader API, SSE, 웹 UI 정적 리소스를 함께 제공합니다.

기본 실행:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev,noclaim'
```

주요 프로파일:

| 프로파일 | 역할 |
| --- | --- |
| `dev` | 개발 로그, 인메모리 DB, CORS 허용, 개발 포트 |
| `localdb` | `./localdb` 기반 로컬 DB 사용 |
| `noclaim` | 초기 사용자가 없을 때 사용자 자동 생성 |
| `docker` | Docker 환경 설정 |

### 웹 UI 개발 서버

```bash
cd komga-webui
npm install
npm run serve
```

프론트 개발 서버는 기본 `8081` 포트를 사용합니다. 백엔드는 `dev` 프로파일로 실행해야 CORS가 허용됩니다.

### 통합 웹 UI 빌드

백엔드가 정적 웹 UI를 서빙하도록 빌드하려면 다음 Gradle 태스크를 사용합니다.

```bash
./gradlew :komga:prepareThymeLeaf
```

흐름:

1. `npmInstall`: `komga-webui/package.json` 기준 의존성 설치
2. `npmBuild`: `npm run build`
3. `copyWebDist`: `komga-webui/dist`를 `komga/src/main/resources/public`로 복사
4. `prepareThymeLeaf`: `index.html`에 Thymeleaf 리소스 경로 태그 적용

## 백그라운드 작업 흐름

무거운 작업은 요청 처리 중 직접 수행하지 않고 작업 큐에 넣습니다.

```text
Controller 또는 Scheduler
  -> TaskEmitter
  -> TasksRepository / tasks.sqlite
  -> TaskProcessor
  -> TaskHandler
  -> Domain Service
  -> Repository / File System / Lucene
```

`TaskProcessor`는 애플리케이션 시작 시 미완료 작업의 소유권을 초기화하고, 작업 추가 이벤트나 애플리케이션 준비 이벤트를 받아 처리합니다. 동시 처리량은 서버 설정의 task pool size에 의해 조정됩니다.

주요 작업 정의는 `application/tasks/Task.kt`, 실제 분기는 `TaskHandler.kt`에 있습니다.

## 라이브러리 스캔 흐름

```text
라이브러리 생성/수동 스캔/주기 스캔
  -> Task.ScanLibrary
  -> LibraryContentLifecycle.scanRootFolder
  -> FileSystemScanner
  -> Series/Book/Media 갱신
  -> 후속 작업 등록
```

스캔 후 자동으로 이어질 수 있는 후속 작업:

- 알 수 없거나 오래된 도서 분석
- 확장자 복구
- CBZ 변환 대상 탐색
- 누락된 페이지 해시 생성
- 자동 삭제 가능한 중복 페이지 탐색
- 파일 해시와 KOReader 해시 생성

## 도서 분석 흐름

```text
Task.AnalyzeBook
  -> BookLifecycle.analyzeAndPersist
  -> BookAnalyzer
  -> MediaContainer Extractor
  -> Media, Page, Dimension, Hash 정보 저장
  -> 썸네일/메타데이터 후속 작업 등록
```

컨테이너별 분석 구현은 `infrastructure/mediacontainer` 아래에 있습니다.

- `divina`: ZIP/RAR 기반 이미지 아카이브
- `epub`: EPUB
- `pdf`: PDF

암호화된 미디어 파일은 `MediaFileDecryptionService`가 일반 파일과 암호화 파일을 구분한 뒤 extractor가 읽을 수 있는 형태로 제공합니다.

- CBZ/ZIP: 기존 Komga처럼 원본 ZIP에서 요청한 이미지 entry를 읽고, crypto-kit magic header가 있으면 해당 entry bytes만 메모리에서 복호화합니다.
- PDF: PDFBox 로드 시 `InvalidPasswordException`이 발생하면 암호화 PDF로 보고 설정 비밀번호로 재시도합니다.
- EPUB: 기존 Komga처럼 원본 EPUB ZIP에서 요청한 entry를 읽습니다. 내부 HTML/XHTML 리소스 암호화는 JitPack `com.github.whitenight209:crypto-kit:v0.0.2`의 magic header로 판별합니다.
- crypto-kit password API 포맷은 `[magic header 5B][salt 16B][nonce 12B][ciphertext + GCM tag 16B]`입니다. magic header는 `CKIT` + 버전 바이트이며, `CryptoEngine.hasMagicHeader(bytes)` 기준으로 일반 HTML/XHTML과 암호문을 구분합니다.
- 비밀번호는 `komga.media-file-decryption.password`에서 읽습니다.
- 복호화된 ZIP/EPUB/PDF 임시파일은 만들지 않습니다.

## 메타데이터 흐름

```text
Task.RefreshBookMetadata
  -> BookMetadataLifecycle
  -> BookMetadataProvider 구현체
  -> BookMetadataPatch 적용
  -> Task.RefreshSeriesMetadata
  -> SeriesMetadataLifecycle
  -> SeriesMetadataFromBookProvider / SeriesMetadataProvider
  -> MetadataAggregator
```

주요 provider:

- ComicRack: `ComicInfoProvider`
- EPUB: `EpubMetadataProvider`
- Mylar: `MylarSeriesProvider`
- ISBN 바코드: `IsbnBarcodeProvider`
- 로컬 아트워크: `LocalArtworkProvider`
- 원샷: `OneShotSeriesProvider`

## 데이터 저장 흐름

| 데이터 | 위치/기술 |
| --- | --- |
| 도메인 데이터 | SQLite `database.sqlite` |
| 작업 큐 | SQLite `tasks.sqlite` |
| 검색 인덱스 | Lucene 디렉토리 |
| 웹 UI 빌드 산출물 | `komga/src/main/resources/public` |
| 설정 파일 | `${komga.config-dir}/application.yml`, `.yaml`, `.properties` |
| 로그 | `${komga.config-dir}/logs/komga.log` |

Flyway 마이그레이션은 SQLite vendor 경로를 사용합니다. jOOQ DSL은 Flyway로 생성한 SQLite schema를 기반으로 생성됩니다.

## 테스트와 품질 확인

백엔드 테스트:

```bash
./gradlew :komga:test
```

프론트엔드 단위 테스트:

```bash
cd komga-webui
npm run test:unit
```

프론트엔드 lint:

```bash
cd komga-webui
npm run lint
```

Kotlin formatting/lint는 루트 Gradle의 ktlint 플러그인 설정을 따릅니다.

## 패키징 흐름

Docker 이미지 생성 흐름:

```bash
./gradlew :komga:prepareThymeLeaf
./gradlew jreleaserPackage
```

JReleaser 설정은 루트 `build.gradle.kts`에 있으며, Docker 템플릿은 `komga/docker` 아래에 있습니다.

데스크톱 패키징은 `komga-tray`의 Compose Desktop/Conveyor 설정을 사용합니다.
