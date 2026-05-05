# Read Book Flow

작성 기준: 2026-05-05

이 문서는 사용자가 책을 읽을 때 문서 포맷별로 어떤 API 요청이 발생하고, 서버가 원본 파일 전체를 보내는지 또는 요청한 일부만 보내는지 정리한다.

## 공통 흐름

1. 클라이언트가 책 상세/리더 화면에 진입한다.
2. 서버는 스캔 시 DB에 저장한 `Media`, `BookPage`, `MediaFile`, EPUB 확장 정보 등을 조회한다.
3. 리더는 포맷에 맞는 manifest 또는 page/resource URL을 사용한다.
4. 실제 페이지/리소스 요청이 들어오면 서버는 원본 책 파일에서 필요한 항목을 읽어 응답한다.

스캔 시 저장되는 것은 분석 결과다. 원본 책 전체 또는 복호화된 전체 본문을 DB에 저장하지 않는다.

## EPUB

### Manifest 요청

EPUB 리더는 먼저 WebPub manifest를 요청한다.

```text
GET /api/v1/books/{bookId}/manifest/epub
```

서버는 DB에 저장된 `media.files`와 EPUB 확장 정보를 사용해 manifest를 만든다.

- `readingOrder`: EPUB spine의 HTML/XHTML 페이지 문서
- `resources`: 이미지, CSS, 폰트 등 EPUB asset
- `toc`, `landmarks`, `pageList`: 스캔 시 계산된 EPUB navigation 정보

manifest 응답은 책 전체 파일이 아니라 URL 목록과 메타데이터다.

### 리소스 요청

EPUB의 각 문서와 asset은 다음 API로 개별 요청된다.

```text
GET /api/v1/books/{bookId}/resource/{entryName}
```

다음 페이지로 이동할 때 XHTML/HTML 하나만 요청된다고 단정할 수 없다. 일반적으로 spine 문서가 먼저 요청되고, 그 문서가 참조하는 리소스가 추가 요청된다.

예:

```text
GET /api/v1/books/{bookId}/resource/chapter02.xhtml
GET /api/v1/books/{bookId}/resource/styles/book.css
GET /api/v1/books/{bookId}/resource/images/p023.jpg
GET /api/v1/books/{bookId}/resource/fonts/font.woff2
```

서버 응답은 요청한 ZIP entry 하나의 bytes다. EPUB 전체 파일을 응답하지 않는다.

### Pagination

EPUB의 pagination은 CBZ/PDF처럼 서버의 `pageNumber`와 1:1로 고정되지 않는다.

EPUB manifest의 `readingOrder`는 spine 문서 단위다. 보통 하나의 `readingOrder` 항목은 하나의 XHTML/HTML chapter 또는 section이다.

```text
readingOrder[0] -> /resource/titlepage.xhtml
readingOrder[1] -> /resource/chapter01.xhtml
readingOrder[2] -> /resource/chapter02.xhtml
```

reflowable EPUB에서는 실제 화면 페이지가 클라이언트에서 결정된다. 화면 크기, 폰트 크기, 줄 간격, margin, column count, scroll mode 같은 reader 설정에 따라 같은 XHTML 문서가 여러 화면 페이지로 나뉠 수 있다.

따라서 다음 화면 페이지로 넘길 때 항상 새 REST 요청이 발생하는 것은 아니다.

- 같은 XHTML 문서 안에서 다음 화면으로 이동: 이미 로드된 iframe/document 내부에서 pagination만 이동할 수 있다.
- 다음 spine 문서로 넘어감: 새 XHTML/HTML resource 요청이 발생한다.
- 새 문서가 이미지/CSS/font를 참조함: 해당 asset resource 요청이 추가로 발생할 수 있다.

서버는 EPUB 위치 계산을 위해 `/positions` API를 제공한다.

```text
GET /api/v1/books/{bookId}/positions
```

positions는 스캔 시 계산되어 DB에 저장된 `MediaExtensionEpub.positions`를 반환한다.

- fixed-layout EPUB: spine page 문서당 position 1개
- reflowable EPUB: Readium 방식으로 각 spine resource의 file size 기준 약 1024 bytes마다 position 생성
- KEPUB 또는 kepub 변환 가능 시: `koboSpan` 기반 위치 보정 가능

읽기 진행률 저장은 `locator.href`, `progression`, `position`, `totalProgression`을 사용한다. 여기서 `href`는 보통 `/resource/{entryName}`의 entry path다.

### 암호화 EPUB

EPUB ZIP 컨테이너 자체는 기존 Komga와 동일하게 원본 파일에서 바로 읽는다. 별도 평문 ZIP 임시파일을 만들지 않는다.

지원하는 암호화 단위는 EPUB 내부 HTML/XHTML entry payload다.

```text
원본 EPUB
-> 요청한 entry 하나 읽기
-> entry가 .html/.xhtml이고 crypto-kit header가 있으면 메모리에서 복호화
-> 응답
```

`META-INF/container.xml`, OPF, CSS, 이미지, 폰트 같은 다른 entry는 기존처럼 ZIP entry bytes를 그대로 읽는다.

## CBZ/ZIP 이미지 책

### Manifest/page 목록 요청

페이지 목록은 스캔 시 저장한 `media.pages`를 사용한다.

```text
GET /api/v1/books/{bookId}/pages
```

이 응답은 페이지 번호, 파일명, media type, 크기, dimension 같은 메타데이터다. 원본 이미지 bytes는 포함하지 않는다.

### 페이지 요청

각 페이지 이미지는 페이지 번호로 요청된다.

```text
GET /api/v1/books/{bookId}/pages/{pageNumber}
```

서버는 스캔 시 저장한 page entry name을 찾고, ZIP/CBZ에서 해당 entry 하나를 읽어 응답한다. 원본 ZIP/CBZ 전체를 응답하지 않는다.

이미지 변환 요청이 있으면 해당 entry bytes를 읽은 뒤 서버에서 변환하여 응답한다.

```text
GET /api/v1/books/{bookId}/pages/{pageNumber}?convert=jpeg
```

### Pagination

CBZ/ZIP 이미지 책의 pagination은 서버가 스캔 시 정한다.

스캔 중 ZIP/CBZ entry 목록에서 이미지 entry를 골라 정렬하고, 각 이미지 entry를 `BookPage`로 저장한다. 클라이언트의 page number는 이 저장된 `media.pages` index에 대응한다.

```text
page 1 -> media.pages[0].fileName -> ZIP entry 하나
page 2 -> media.pages[1].fileName -> ZIP entry 하나
page 3 -> media.pages[2].fileName -> ZIP entry 하나
```

페이지를 넘길 때는 보통 다음 page number를 요청한다.

```text
GET /api/v1/books/{bookId}/pages/1
GET /api/v1/books/{bookId}/pages/2
GET /api/v1/books/{bookId}/pages/3
```

각 요청은 페이지 이미지 하나를 응답한다. 원본 ZIP/CBZ 전체를 보내지 않는다.

### 암호화 ZIP/CBZ

ZIP/CBZ 컨테이너 자체는 기존 Komga와 동일하게 원본 파일에서 바로 읽는다. 별도 평문 ZIP 임시파일을 만들지 않는다.

지원하는 암호화 단위는 ZIP 내부 이미지 entry payload다.

```text
원본 ZIP/CBZ
-> 요청한 이미지 entry 하나 읽기
-> 이미지 entry에 crypto-kit header가 있으면 메모리에서 복호화
-> 응답
```

즉 페이지 요청 하나는 ZIP 전체가 아니라 이미지 entry 하나만 읽고, 필요한 경우 그 entry bytes만 복호화한다.

## PDF

### Manifest/page 목록 요청

PDF도 WebPub manifest 또는 page 목록을 통해 페이지 URL을 제공한다.

```text
GET /api/v1/books/{bookId}/manifest/pdf
GET /api/v1/books/{bookId}/pages
```

이 응답들은 메타데이터와 페이지 URL 목록이다. PDF 전체 bytes를 포함하지 않는다.

### 페이지 요청

PDF 페이지는 두 방식이 있다.

이미지 렌더링:

```text
GET /api/v1/books/{bookId}/pages/{pageNumber}
```

raw PDF page:

```text
GET /api/v1/books/{bookId}/pages/{pageNumber}/raw
```

이미지 렌더링 경로는 PDFBox로 원본 PDF를 열고 해당 page를 이미지 bytes로 렌더링해 응답한다. PDF 전체를 응답하지 않는다.

raw PDF page 경로는 해당 페이지에 해당하는 PDF bytes를 만들어 응답한다. 책 전체 PDF 다운로드가 아니다.

암호화 PDF는 설정된 `komga.media-file-decryption.password`로 PDFBox가 연다. 별도 평문 PDF 파일을 생성하지 않는다.

### Pagination

PDF pagination은 PDF 문서의 실제 page index를 사용한다. 스캔 시 PDF page count와 page dimension을 읽고 `media.pages`에 저장한다.

```text
page 1 -> PDF page index 0
page 2 -> PDF page index 1
page 3 -> PDF page index 2
```

클라이언트가 페이지를 넘길 때는 다음 page number를 요청한다.

```text
GET /api/v1/books/{bookId}/pages/1
GET /api/v1/books/{bookId}/pages/2
GET /api/v1/books/{bookId}/pages/3
```

이미지 렌더링 경로에서는 요청한 PDF page만 이미지로 렌더링해 응답한다. raw 경로에서는 요청한 page에 해당하는 PDF bytes를 응답한다. 두 경우 모두 원본 PDF 전체를 읽을 수는 있지만, 응답으로 전체 PDF를 보내지는 않는다.

## 원본 파일 다운로드

책 원본 파일 다운로드는 읽기 페이지 요청과 다르다.

```text
GET /api/v1/books/{bookId}/file
```

이 경로는 원본 파일 전체를 streaming한다. 현재 복호화된 파일을 만들어 내려주지 않고, 저장된 원본 파일을 그대로 보낸다.

## 요약

| 포맷 | 읽기 요청 단위 | 응답이 전체 파일인가 | 복호화 데이터 수명 |
| --- | --- | --- | --- |
| EPUB | `/resource/{entryName}` | 아니오. 요청한 entry만 응답 | HTML/XHTML entry payload만 메모리에서 복호화 |
| CBZ/ZIP | `/pages/{pageNumber}` | 아니오. 요청한 페이지 entry만 응답 | 이미지 entry payload만 메모리에서 복호화 |
| PDF | `/pages/{pageNumber}` 또는 `/raw` | 아니오. 요청한 page만 응답 | PDFBox가 password로 열며 별도 평문 PDF 파일 없음 |
| 원본 다운로드 | `/file` | 예. 원본 파일 전체 streaming | 복호화 다운로드 아님 |

| 포맷 | Pagination 기준 | 다음 페이지 이동 시 요청 특징 |
| --- | --- | --- |
| EPUB reflowable | 클라이언트 layout + 서버 positions | 같은 XHTML 내부 이동은 추가 resource 요청이 없을 수 있음. 다음 spine 문서로 넘어가면 새 XHTML/asset 요청 |
| EPUB fixed-layout | spine page 문서 | 보통 page 문서 단위 resource 요청. 문서가 참조하는 이미지/CSS/font 추가 요청 가능 |
| CBZ/ZIP | 스캔 시 정렬된 이미지 entry index | page number마다 이미지 entry 하나 요청 |
| PDF | PDF page index | page number마다 해당 PDF page를 렌더링 또는 raw page로 응답 |

## 캐시 설계 시 고려점

EPUB과 ZIP/CBZ는 기존 Komga 흐름처럼 요청한 entry만 읽는다. 암호화가 적용되어도 전체 ZIP 컨테이너를 복호화하지 않고 entry payload만 복호화한다.

EPUB은 한 페이지 이동 시 HTML/XHTML뿐 아니라 이미지, CSS, 폰트가 추가 요청될 수 있으므로 캐시를 추가한다면 `bookId + entryName` 단위가 적합하다. 큰 이미지 entry는 메모리 사용량을 제한하기 위해 캐시 제외 또는 최대 entry size 제한을 둬야 한다.

PDF는 현재 별도 평문 파일을 만들지 않으므로, 복호화 캐시보다 렌더링된 page image 캐시가 성능 개선 후보에 가깝다.
