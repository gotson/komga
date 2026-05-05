# 기능 문서

작성 기준: 2026-05-05

이 문서는 현재 코드 구조에서 확인되는 주요 기능을 사용자 기능 단위로 정리합니다. 구현 위치는 대표 파일/패키지 기준입니다.

## 1. 라이브러리 관리와 스캔

Komga의 최상위 콘텐츠 단위는 `Library`입니다. 관리자는 파일 시스템 경로를 라이브러리로 등록하고, 스캔 옵션과 주기 스캔 간격을 설정합니다.

주요 기능:

- 라이브러리 생성, 조회, 수정, 삭제
- 라이브러리 루트 폴더 스캔
- 심층 스캔, 시작 시 스캔, 주기 스캔
- 라이브러리별 파일 해싱, 자동 변환, 로컬 아트워크, 메타데이터 import 옵션
- 휴지통 비우기

대표 구현:

- REST API: `komga/src/main/kotlin/org/gotson/komga/interfaces/api/rest/LibraryController.kt`
- 도메인 모델: `domain/model/Library.kt`, `domain/model/ScanResult.kt`
- 도메인 서비스: `domain/service/LibraryLifecycle.kt`, `domain/service/LibraryContentLifecycle.kt`, `domain/service/FileSystemScanner.kt`
- 스케줄러: `application/scheduler/LibraryScanScheduler.kt`, `interfaces/scheduler/PeriodicScannerController.kt`
- UI: `komga-webui/src/views/WelcomeView.vue`, `BrowseLibraries.vue`, `SettingsServer.vue`
- API 클라이언트: `komga-webui/src/services/komga-libraries.service.ts`

## 2. 시리즈 관리

시리즈는 라이브러리 내 도서를 묶는 핵심 단위입니다. 일반 시리즈와 원샷을 모두 지원하며, 메타데이터와 썸네일을 별도로 관리합니다.

주요 기능:

- 시리즈 목록, 상세, 알파벳 그룹, 신규/업데이트/최근 시리즈 조회
- 시리즈 내 도서 목록 조회
- 시리즈 메타데이터 수정, 새로고침, 집계
- 시리즈 썸네일 목록, 업로드, 선택, 삭제
- 시리즈 단위 읽기 진행률 업데이트/삭제
- 시리즈 파일 다운로드와 삭제
- 컬렉션 포함 여부 조회

대표 구현:

- REST API: `interfaces/api/rest/SeriesController.kt`
- 도메인 모델: `domain/model/Series.kt`, `SeriesMetadata.kt`, `SeriesSearch.kt`, `ThumbnailSeries.kt`
- 도메인 서비스: `domain/service/SeriesLifecycle.kt`, `SeriesMetadataLifecycle.kt`, `MetadataAggregator.kt`
- UI: `komga-webui/src/views/BrowseLibraries.vue`, `BrowseSeries.vue`, `BrowseOneshot.vue`
- API 클라이언트: `komga-webui/src/services/komga-series.service.ts`

## 3. 도서 관리

도서는 실제 파일과 분석된 미디어 정보를 연결하는 단위입니다. 파일 다운로드, 페이지 조회, 메타데이터 수정, 가져오기, 삭제 등을 제공합니다.

주요 기능:

- 도서 목록, 상세, 이전/다음 도서, 최신/이어보기/중복 도서 조회
- 도서 페이지 목록과 개별 페이지 이미지 제공
- 도서 파일 다운로드
- 도서 분석, 썸네일 생성, 메타데이터 새로고침
- 도서 메타데이터 단건/대량 수정
- 읽기 진행률 생성, 업데이트, 삭제
- 외부 파일을 시리즈 폴더로 가져오기
- 도서 파일 삭제

대표 구현:

- REST API: `interfaces/api/rest/BookController.kt`, `interfaces/api/CommonBookController.kt`
- 도메인 모델: `domain/model/Book.kt`, `BookMetadata.kt`, `Media.kt`, `BookPage.kt`, `ReadProgress.kt`
- 도메인 서비스: `domain/service/BookLifecycle.kt`, `BookAnalyzer.kt`, `BookMetadataLifecycle.kt`, `BookImporter.kt`, `BookConverter.kt`
- 미디어 추출: `infrastructure/mediacontainer`
- UI: `komga-webui/src/views/BrowseBooks.vue`, `BrowseBook.vue`, `ImportBooks.vue`
- API 클라이언트: `komga-webui/src/services/komga-books.service.ts`, `komga-transientbooks.service.ts`

## 4. 웹 리더와 읽기 진행률

웹 UI는 이미지 기반 도서와 EPUB 도서를 모두 읽을 수 있는 리더 화면을 제공합니다. 읽기 위치는 서버에 저장되어 다른 클라이언트와 연동됩니다.

주요 기능:

- 페이지형 리더와 연속 스크롤 리더
- EPUB 리더
- 페이지, 챕터, 진행률 기반 읽기 위치 저장
- Tachiyomi/Mihon 호환 읽기 진행률 API
- 사용자별 이어보기, 온덱/keep reading 성격의 목록 제공
- 키보드 단축키와 리더별 설정

대표 구현:

- 공통 도서 콘텐츠 API: `interfaces/api/CommonBookController.kt`
- 읽기 진행률 저장소: `domain/persistence/ReadProgressRepository.kt`, `interfaces/api/persistence/ReadProgressDtoRepository.kt`
- UI: `komga-webui/src/views/DivinaReader.vue`, `EpubReader.vue`
- 리더 컴포넌트: `komga-webui/src/components/readers/ContinuousReader.vue`, `PagedReader.vue`
- 리더 유틸리티: `komga-webui/src/functions/readium.ts`, `page.ts`, `book-progress.ts`, `shortcuts/*`

## 5. 컬렉션

컬렉션은 여러 시리즈를 사용자가 직접 묶는 기능입니다.

주요 기능:

- 컬렉션 생성, 조회, 수정, 삭제
- 컬렉션에 포함된 시리즈 조회
- 컬렉션 썸네일 업로드, 선택, 삭제
- 라이브러리별 컬렉션 목록 조회

대표 구현:

- REST API: `interfaces/api/rest/SeriesCollectionController.kt`
- 도메인 모델: `domain/model/SeriesCollection.kt`, `ThumbnailSeriesCollection.kt`
- 도메인 서비스: `domain/service/SeriesCollectionLifecycle.kt`
- UI: `komga-webui/src/views/BrowseCollections.vue`, `BrowseCollection.vue`
- API 클라이언트: `komga-webui/src/services/komga-collections.service.ts`

## 6. 읽기목록

읽기목록은 도서를 순서 있게 묶는 기능입니다. ComicRack `cbl` 가져오기와 매칭도 지원합니다.

주요 기능:

- 읽기목록 생성, 조회, 수정, 삭제
- 읽기목록 내 도서 목록과 이전/다음 도서 조회
- 읽기목록 파일 다운로드
- ComicRack `cbl` 읽기목록 매칭과 가져오기
- 읽기목록 썸네일 업로드, 선택, 삭제
- Tachiyomi/Mihon 호환 진행률 API

대표 구현:

- REST API: `interfaces/api/rest/ReadListController.kt`
- 도메인 모델: `domain/model/ReadList.kt`, `ReadListRequest.kt`, `ThumbnailReadList.kt`
- 도메인 서비스: `domain/service/ReadListLifecycle.kt`, `ReadListMatcher.kt`
- ComicRack provider: `infrastructure/metadata/comicrack/ReadListProvider.kt`
- UI: `komga-webui/src/views/BrowseReadLists.vue`, `BrowseReadList.vue`, `ImportReadList.vue`
- API 클라이언트: `komga-webui/src/services/komga-readlists.service.ts`

## 7. 메타데이터 추출과 집계

Komga는 도서 파일과 주변 파일에서 메타데이터를 추출하고, 시리즈 메타데이터를 도서 메타데이터에서 집계합니다.

주요 기능:

- ComicInfo.xml 기반 도서/시리즈 메타데이터 추출
- EPUB 메타데이터 추출
- Mylar 시리즈 메타데이터 추출
- ISBN 바코드 스캔
- 원샷 시리즈 메타데이터 보정
- 로컬 아트워크 탐색
- 도서 메타데이터를 시리즈 메타데이터로 집계

대표 구현:

- 도메인 서비스: `domain/service/BookMetadataLifecycle.kt`, `SeriesMetadataLifecycle.kt`, `MetadataApplier.kt`, `MetadataAggregator.kt`, `LocalArtworkLifecycle.kt`
- 제공자 인터페이스: `infrastructure/metadata/BookMetadataProvider.kt`, `SeriesMetadataProvider.kt`, `SeriesMetadataFromBookProvider.kt`
- 제공자 구현: `infrastructure/metadata/comicrack`, `epub`, `mylar`, `barcode`, `localartwork`, `oneshot`

## 8. 미디어 분석, 변환, 썸네일

도서 파일은 컨테이너별 extractor로 분석되고 페이지, 크기, 미디어 타입, 썸네일, 해시 정보가 생성됩니다.

주요 기능:

- ZIP/RAR 기반 만화 아카이브 분석
- EPUB, PDF 분석
- 암호화된 EPUB/PDF/CBZ 파일 복호화 후 분석
- 이미지 크기와 페이지 정보 수집
- 썸네일 생성과 모자이크 생성
- CBZ 변환과 파일 확장자 복구
- WebP, HEIF, JXL, JPEG2000 등 이미지 처리 런타임 지원

암호화 파일 처리:

- 공통 비밀번호 설정은 `komga.media-file-decryption.password`를 사용합니다.
- crypto-kit은 JitPack 의존성 `com.github.whitenight209:crypto-kit:v0.0.2`를 사용합니다.
- 일반 파일은 기존 추출 경로를 그대로 사용하고, 암호화된 entry payload만 메모리에서 복호화합니다.
- CBZ/ZIP 컨테이너 자체는 기존 Komga처럼 원본 ZIP에서 바로 읽습니다. 이미지 entry가 crypto-kit magic header로 시작할 때 해당 entry bytes만 복호화합니다.
- PDF는 PDFBox `Loader.loadPDF`를 먼저 비밀번호 없이 시도하고, `InvalidPasswordException` 발생 시 설정된 비밀번호로 다시 엽니다.
- EPUB 컨테이너 자체는 기존 Komga처럼 원본 EPUB ZIP에서 바로 읽습니다. EPUB 내부 리소스 암호화는 `.html`, `.xhtml` 엔트리만 대상으로 합니다. crypto-kit password API 출력은 `[magic header 5B][salt 16B][nonce 12B][ciphertext + GCM tag 16B]`이며, magic header는 `CKIT` + 버전 바이트입니다. HTML/XHTML 엔트리가 이 magic header로 시작할 때 crypto-kit 암호화 데이터로 판별합니다.
- Komga는 일반 파일과 암호화 파일을 명확히 구분하기 위해 magic header가 있는 HTML/XHTML만 복호화합니다.
- 복호화된 ZIP/EPUB/PDF 임시파일은 생성하지 않습니다.

대표 구현:

- 분석 서비스: `domain/service/BookAnalyzer.kt`, `BookLifecycle.kt`
- 컨테이너 추출: `infrastructure/mediacontainer/divina`, `epub`, `pdf`
- 복호화 보조 서비스: `infrastructure/mediacontainer/MediaFileDecryptionService.kt`
- 이미지 처리: `infrastructure/image/ImageAnalyzer.kt`, `ImageConverter.kt`, `MosaicGenerator.kt`
- 변환: `domain/service/BookConverter.kt`

## 9. 검색

검색은 Lucene 기반 인덱스를 사용합니다. 시리즈/도서 등 엔티티 인덱스를 비동기 작업으로 재생성하거나 업그레이드할 수 있습니다.

주요 기능:

- 시리즈/도서 검색 조건 모델
- 검색 인덱스 재생성/업그레이드 작업
- Lucene analyzer 설정
- 프론트엔드 검색 화면

대표 구현:

- 도메인 모델: `domain/model/SearchCondition.kt`, `SearchField.kt`, `SearchOperator.kt`, `SearchContext.kt`
- 인프라: `infrastructure/search`
- 작업: `application/tasks/Task.RebuildIndex`, `Task.UpgradeIndex`
- UI: `komga-webui/src/views/SearchView.vue`

## 10. 중복 파일과 중복 페이지 관리

Komga는 도서 파일 해시와 페이지 해시를 기반으로 중복 콘텐츠를 찾아 관리합니다.

주요 기능:

- 중복 도서/파일 조회
- 페이지 해시 생성
- 알려진/알 수 없는 페이지 해시 관리
- 동일 페이지 자동 삭제 후보 탐색
- 중복 페이지 전체 삭제 또는 매칭 삭제

대표 구현:

- REST API: `interfaces/api/rest/PageHashController.kt`, `BookController.kt`
- 도메인 모델: `domain/model/PageHash.kt`, `PageHashKnown.kt`, `PageHashUnknown.kt`, `PageHashMatch.kt`
- 도메인 서비스: `domain/service/PageHashLifecycle.kt`, `BookPageEditor.kt`
- UI: `komga-webui/src/views/DuplicateFiles.vue`, `DuplicatePagesKnown.vue`, `DuplicatePagesUnknown.vue`
- API 클라이언트: `komga-webui/src/services/komga-pagehashes.service.ts`

## 11. 사용자, 권한, 인증

Komga는 다중 사용자, 역할, 라이브러리별 접근 제어, 연령/라벨 제한, API Key, OAuth2를 지원합니다.

주요 기능:

- 사용자 생성, 조회, 수정, 삭제
- 비밀번호 변경과 비밀번호 재설정 runner
- 관리자/파일 다운로드/페이지 스트리밍/Kobo 역할
- 라이브러리 접근 제한, 연령 제한, 라벨 제한
- 인증 활동 이력
- API Key 발급과 폐기
- OAuth2 provider 조회와 계정 생성 옵션
- 서버 claim 흐름

대표 구현:

- REST API: `interfaces/api/rest/UserController.kt`, `ClaimController.kt`, `OAuth2Controller.kt`, `LoginController.kt`
- 도메인 모델: `domain/model/KomgaUser.kt`, `UserRoles.kt`, `ApiKey.kt`, `AuthenticationActivity.kt`, `ContentRestrictions.kt`
- 도메인 서비스: `domain/service/KomgaUserLifecycle.kt`
- 보안 인프라: `infrastructure/security`
- runner: `interfaces/apprunner/ListUsersRunner.kt`, `PasswordResetRunner.kt`
- UI: `komga-webui/src/views/SettingsUsers.vue`, `AccountView.vue`, `ApiKeys.vue`, `SelfAuthenticationActivity.vue`, `LoginView.vue`, `StartupView.vue`

## 12. OPDS v1/v2

OPDS는 외부 리더 앱이 Komga 라이브러리를 탐색하고 도서를 내려받거나 읽을 수 있게 하는 피드 API입니다.

주요 기능:

- OPDS v1 Atom/XML 피드
- OPDS v2 JSON 피드
- 카탈로그, 라이브러리, 시리즈, 컬렉션, 읽기목록, 출판사 탐색
- 최신 도서/시리즈, 이어보기, 온덱
- 검색
- OPDS 인증 문서와 책/페이지/커버 리소스

대표 구현:

- OPDS 공통: `interfaces/api/opds/OpdsCommonController.kt`
- OPDS v1: `interfaces/api/opds/v1/OpdsController.kt`
- OPDS v2: `interfaces/api/opds/v2/Opds2Controller.kt`
- DTO: `interfaces/api/opds/v1/dto`, `interfaces/api/opds/v2/dto`

## 13. Kobo Sync

Kobo API는 Kobo eReader와 Komga 라이브러리/읽기상태를 동기화하기 위한 호환 엔드포인트입니다.

주요 기능:

- Kobo 초기화, device auth, ping
- 라이브러리 sync
- 도서 메타데이터와 읽기 상태 조회/업데이트
- 다운로드 URL과 이미지 리소스 제공
- KEPUB 변환 경로 설정 지원

대표 구현:

- REST API: `interfaces/api/kobo/KoboController.kt`
- DTO: `interfaces/api/kobo/dto`
- 저장소: `interfaces/api/kobo/persistence/KoboDtoRepository.kt`
- 인프라: `infrastructure/kobo/KoboProxy.kt`, `KepubConverter.kt`, `KomgaSyncTokenGenerator.kt`

## 14. KOReader Sync

KOReader Sync API는 KOReader의 진행률 동기화 프로토콜과 호환되는 엔드포인트를 제공합니다.

주요 기능:

- 사용자 생성/인증 호환 엔드포인트
- 문서 hash 기반 진행률 조회
- 진행률 업데이트
- KOReader용 도서 해시 생성 작업

대표 구현:

- REST API: `interfaces/api/kosync/KoreaderSyncController.kt`
- DTO: `interfaces/api/kosync/dto`
- 해시: `infrastructure/hash/KoreaderHasher.kt`
- 작업: `application/tasks/Task.HashBookKoreader`

## 15. 백그라운드 작업

무거운 작업은 작업 DB에 저장되고 `TaskProcessor`가 우선순위와 thread pool 설정에 따라 처리합니다.

주요 작업:

- 라이브러리 스캔
- 도서 분석
- 썸네일 생성
- 도서/시리즈 메타데이터 새로고침과 집계
- 로컬 아트워크 갱신
- 도서 가져오기
- CBZ 변환, 확장자 복구
- 도서/페이지 해싱
- 중복 페이지 삭제
- 검색 인덱스 재생성/업그레이드
- 도서/시리즈 파일 삭제

대표 구현:

- 작업 모델: `application/tasks/Task.kt`
- 작업 등록: `application/tasks/TaskEmitter.kt`
- 작업 처리: `application/tasks/TaskProcessor.kt`, `TaskHandler.kt`
- 작업 저장소: `application/tasks/TasksRepository.kt`, `infrastructure/jooq/tasks/TasksDao.kt`
- 작업 취소 API: `interfaces/api/rest/TaskController.kt`

## 16. 서버 설정, UI 설정, 공지, 릴리스, 메트릭

관리자와 사용자는 서버/클라이언트 설정을 조회하거나 변경할 수 있습니다.

주요 기능:

- 서버 설정 조회/수정
- 전역/사용자별 클라이언트 설정
- 폰트 목록과 폰트 리소스 제공
- 공지 조회/읽음 처리
- GitHub 릴리스 조회와 업데이트 화면
- Actuator 기반 메트릭/정보 조회
- 역사 이벤트 조회
- SSE 이벤트 스트림

대표 구현:

- REST API: `SettingsController.kt`, `ClientSettingsController.kt`, `AnnouncementController.kt`, `ReleaseController.kt`, `HistoricalEventController.kt`, `FontsController.kt`
- SSE: `interfaces/sse/SseController.kt`
- 설정: `infrastructure/configuration`
- UI: `SettingsServer.vue`, `UISettings.vue`, `UIUserSettings.vue`, `AnnouncementsView.vue`, `UpdatesView.vue`, `MetricsView.vue`, `HistoryView.vue`
- API 클라이언트: `komga-webui/src/services/komga-settings.service.ts`, `komga-announcements.service.ts`, `komga-releases.service.ts`, `actuator.service.ts`

## 17. 데스크톱 트레이 앱

트레이 앱은 서버를 데스크톱 애플리케이션처럼 실행하기 위한 얇은 래퍼입니다.

주요 기능:

- Spring Boot 서버 실행
- 시스템 트레이 아이콘 표시
- Komga 열기
- 로그 파일 열기
- 설정 디렉토리 열기
- 종료
- 포트 충돌/예상치 못한 시작 오류 표시

대표 구현:

- 진입점: `komga-tray/src/main/kotlin/org/gotson/komga/DesktopApplication.kt`
- 트레이 UI: `komga-tray/src/main/kotlin/org/gotson/komga/application/gui/TrayIconRunner.kt`
- 오류 다이얼로그: `komga-tray/src/main/kotlin/org/gotson/komga/application/gui/ErrorDialog.kt`
- 리소스: `komga-tray/src/main/resources/icons`, `messages*.properties`
