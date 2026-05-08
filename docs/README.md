# Komga 프로젝트 문서

작성 기준: 2026-05-05

이 디렉토리는 현재 저장소 구조를 기준으로 Komga의 모듈 구성과 주요 기능을 빠르게 파악하기 위한 내부 문서입니다. API 명세 산출물은 기존 `komga/docs/openapi.json`에 있으며, 이 문서는 코드 탐색과 기능 이해를 목적으로 합니다.

## 문서 목록

- [프로젝트 구조](./project-structure.md): Gradle 모듈, 백엔드 계층, 프론트엔드 디렉토리, 데스크톱 트레이, 빌드/설정 구조
- [기능 문서](./features.md): 사용자 관점의 기능 영역과 해당 구현 위치
- [운영 및 개발 흐름](./development-flow.md): 실행, 빌드, 테스트, 백그라운드 작업, 데이터 저장소 흐름

## 프로젝트 한 줄 요약

Komga는 만화, 망가, BD, 잡지, eBook을 관리하고 읽을 수 있는 미디어 서버입니다. Spring Boot 기반 서버가 REST/OPDS/Kobo/KOReader API와 정적 웹 UI를 제공하고, Vue 2 기반 웹 UI가 브라우징, 관리, 리더 기능을 담당합니다. 별도 `komga-tray` 모듈은 서버를 데스크톱 트레이 앱 형태로 실행합니다.

## 핵심 모듈

| 모듈 | 역할 |
| --- | --- |
| `komga` | Spring Boot 백엔드 서버, API, 스캔/분석/메타데이터 처리, SQLite/Flyway/jOOQ 데이터 계층 |
| `komga-webui` | Vue 2 + Vuetify 프론트엔드, 라이브러리 탐색, 설정, 리더 화면 |
| `komga-tray` | Compose Desktop 기반 트레이 앱, 백엔드 서버를 데스크톱 앱처럼 구동 |

## 주요 기능 영역

- 라이브러리 등록, 파일 시스템 스캔, 주기 스캔
- 시리즈, 도서, 원샷, 컬렉션, 읽기목록 관리
- 이미지/EPUB/PDF 분석, 썸네일 생성, 메타데이터 추출과 집계
- 암호화된 EPUB/PDF/CBZ 파일 복호화 후 분석
- 웹 리더, EPUB 리더, 읽기 진행률 저장
- 사용자, 역할, API Key, OAuth2, 접근 제한
- 중복 파일/중복 페이지 탐지와 정리
- OPDS v1/v2, Kobo Sync, KOReader Sync
- 서버 설정, UI 설정, 공지, 릴리스 확인, 메트릭/이력 조회
