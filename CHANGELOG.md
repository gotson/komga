## [0.16.3](https://github.com/gotson/komga/compare/v0.16.2...v0.16.3) (2020-02-28)


### Bug Fixes

* **webui:** remove border on preselect for multi-select ([a0bd2f9](https://github.com/gotson/komga/commit/a0bd2f9682530649ab2c91b447ba03833687c0c2))

## [0.16.2](https://github.com/gotson/komga/compare/v0.16.1...v0.16.2) (2020-02-28)


### Bug Fixes

* **webui:** better multi-select ([881806e](https://github.com/gotson/komga/commit/881806ed1c78b0a66ee46905acb9fef14a210bc2))

## [0.16.1](https://github.com/gotson/komga/compare/v0.16.0...v0.16.1) (2020-02-27)


### Bug Fixes

* **webui:** close edit series dialog on escape keypress ([7c0f55d](https://github.com/gotson/komga/commit/7c0f55deeb30ac406ffd4cd15395363cbb372951))
* **webui:** remove ripple effect on series card ([ce5594e](https://github.com/gotson/komga/commit/ce5594e82e265cc99421fb5a7489a642d34b1204))

# [0.16.0](https://github.com/gotson/komga/compare/v0.15.1...v0.16.0) (2020-02-27)


### Features

* **webui:** series multi-selection and edition ([cfce076](https://github.com/gotson/komga/commit/cfce0768ba688b396ba3f0aadbc91008ff0b3555))

## [0.15.1](https://github.com/gotson/komga/compare/v0.15.0...v0.15.1) (2020-02-25)


### Bug Fixes

* **webui:** responsive scaling for login page logo ([20720ae](https://github.com/gotson/komga/commit/20720ae7ccf44c4699d0154a0aa6ed3216813914))
* send proper JSON on API 404 instead of index.html ([fb147a4](https://github.com/gotson/komga/commit/fb147a447a4ba8aff73da57fff1c511a9d515b20))

# [0.15.0](https://github.com/gotson/komga/compare/v0.14.2...v0.15.0) (2020-02-25)


### Features

* **webui:** add series and book title in page title and reader overlay ([6b1998c](https://github.com/gotson/komga/commit/6b1998c1d9fa75db4a3971fb772ad18aaad3bdaf))
* **webui:** add the series and book title to page title ([23c10c2](https://github.com/gotson/komga/commit/23c10c231efaa5f409811e453c51287a9aca4a9b))

## [0.14.2](https://github.com/gotson/komga/compare/v0.14.1...v0.14.2) (2020-02-20)


### Bug Fixes

* webui works with baseUrl ([bb18382](https://github.com/gotson/komga/commit/bb183828a189ce5908d21304bbf1592ae074afc5))

## [0.14.1](https://github.com/gotson/komga/compare/v0.14.0...v0.14.1) (2020-02-14)


### Bug Fixes

* **webui:** make overlay buttons more responsive on smaller screens ([0c03950](https://github.com/gotson/komga/commit/0c0395060a7d6c3a2ef8feef79474d9149013e80))

# [0.14.0](https://github.com/gotson/komga/compare/v0.13.1...v0.14.0) (2020-02-05)


### Bug Fixes

* **api:** sort series properly ignoring case ([16dfe91](https://github.com/gotson/komga/commit/16dfe91140b1140c38a5b4e686bea6295a33dad0)), closes [#85](https://github.com/gotson/komga/issues/85)
* **webui:** hide filter menu after click ([2ded39f](https://github.com/gotson/komga/commit/2ded39f6d6b265b826aad67b9a8a05ba4fc93700))
* **webui:** missing data on back navigation with filters ([f1952ee](https://github.com/gotson/komga/commit/f1952eee4a4f920f4d1ec9f73f19cf0da9c32c16))
* **webui:** scrolling position was not restored properly ([be6a7fc](https://github.com/gotson/komga/commit/be6a7fc7174b0331d02af90bd6b8fd6d8d87225c))
* incorrect placeholder card height on xs and sm screens ([0f50a76](https://github.com/gotson/komga/commit/0f50a7690f609884abd9b902290a138cdb2f2f75))
* logout was broken after remember-me was added ([8b02471](https://github.com/gotson/komga/commit/8b02471be1f2eb9ea78ea112388a2ec003c180ba))


### Features

* add more series metadata fields ([8f08ce8](https://github.com/gotson/komga/commit/8f08ce82e1dd6f888d9e06eb09e29a13e84a6741))
* **api:** ability to filter series by status ([c96bf19](https://github.com/gotson/komga/commit/c96bf19048ba8de3d7b1370d68e2b52fc3c69f3f)), closes [#48](https://github.com/gotson/komga/issues/48)
* **webui:** add thumbnail and status on Series view ([0fc8b01](https://github.com/gotson/komga/commit/0fc8b0137feae0307b4d16ece4f135cdbec5aa13))
* **webui:** filter series by status ([c540e56](https://github.com/gotson/komga/commit/c540e56c088419b48e7fc686e0440b2a65b83dd9)), closes [#48](https://github.com/gotson/komga/issues/48)
* **webui):** edit series metadata ([5f0ccc5](https://github.com/gotson/komga/commit/5f0ccc5bfc35456908c7cc5435511589f23c0512))
* add Series Metadata status ([f522142](https://github.com/gotson/komga/commit/f5221420fd1388cbdd43b497c5470125577bbd2e)), closes [#48](https://github.com/gotson/komga/issues/48)

## [0.13.1](https://github.com/gotson/komga/compare/v0.13.0...v0.13.1) (2020-01-18)


### Bug Fixes

* trigger release ([b45a23c](https://github.com/gotson/komga/commit/b45a23c8c04b30af6d8a1df345aa29d8875a2b11))


### Reverts

* revert thumbnails library ([a685475](https://github.com/gotson/komga/commit/a6854753d2a40ea71868fc31ba170ca257495d6f))

# [0.13.0](https://github.com/gotson/komga/compare/v0.12.0...v0.13.0) (2020-01-18)


### Bug Fixes

* **admin rpc:** fix transaction issues on thumbnails regeneration ([af8e3ea](https://github.com/gotson/komga/commit/af8e3ea433944922eafb8ecc1490d332e27d6c0d))
* **thumbnails:** fix wrong color in thumbnails ([1d5500d](https://github.com/gotson/komga/commit/1d5500d5606c816fc94953adc263f9905c1b3881)), closes [#77](https://github.com/gotson/komga/issues/77)
* media comment was not reset properly ([b42eadf](https://github.com/gotson/komga/commit/b42eadf182a51c3cd073c6195c5da3fd6bcf0b7e))
* **web reader:** first/last display to full height in double pages mode ([a7548e2](https://github.com/gotson/komga/commit/a7548e298a24704780e4bed460656a8d78573749))
* **web reader:** remove blank space between images in double pages mode ([b65b009](https://github.com/gotson/komga/commit/b65b009e0d5e0a7c13ef63ab69030e3223730592)), closes [#72](https://github.com/gotson/komga/issues/72)


### Features

* **api:** search books by media status ([0790501](https://github.com/gotson/komga/commit/07905018e5bad44c8f29a3fd8da0c08326e674f5))
* **book analyzer:** partial handling of archives with errors ([2605b1d](https://github.com/gotson/komga/commit/2605b1d943148e1c71856b35028563be3ca86b26)), closes [#57](https://github.com/gotson/komga/issues/57)
* **browse book:** add button to read book when hovering on thumbnail ([c490e79](https://github.com/gotson/komga/commit/c490e799ba20e66c66f88d4381ad830eca3eaf77)), closes [#67](https://github.com/gotson/komga/issues/67)
* **security:** add remember-me option ([003452b](https://github.com/gotson/komga/commit/003452bd26a7a56387d563abca2f6ee5a7f62d13)), closes [#39](https://github.com/gotson/komga/issues/39)
* **web reader:** add 'original' fit option ([d030044](https://github.com/gotson/komga/commit/d030044df35b09522e5038254a9384c53cecd5ee)), closes [#71](https://github.com/gotson/komga/issues/71)
* **webui:** add Media Analysis screen showing all books in error ([27d46d5](https://github.com/gotson/komga/commit/27d46d57cbec468ac08e1cf061b77e0b616807d4)), closes [#26](https://github.com/gotson/komga/issues/26)

# [0.12.0](https://github.com/gotson/komga/compare/v0.11.0...v0.12.0) (2020-01-14)


### Bug Fixes

* **scanner:** compare file extensions with case insensitive ([91c9cdd](https://github.com/gotson/komga/commit/91c9cdd8323d02c792728ed20bb3a4589694bdf0)), closes [#59](https://github.com/gotson/komga/issues/59)
* **web reader:** conditional webp support ([ad21152](https://github.com/gotson/komga/commit/ad2115244aaf5dea0b2f5f667451d62d6c15069c)), closes [#65](https://github.com/gotson/komga/issues/65)


### Features

* **api:** add endpoints to get previous/next book of a book ([54f583f](https://github.com/gotson/komga/commit/54f583f0ce22e105a0bddde0287cc276dbbc12c9))
* **api:** on-th-fly thumbnail generation for any page ([7167f3e](https://github.com/gotson/komga/commit/7167f3ea24cd222ab023838d42231f8560a733e5))
* **web reader:** double page support ([77c9004](https://github.com/gotson/komga/commit/77c9004d5772bb623c27aebe764cbd155a9562cb)), closes [#61](https://github.com/gotson/komga/issues/61)
* **web reader:** remember fit and rtl ([78c181e](https://github.com/gotson/komga/commit/78c181e130b6085a45755aefec61ad181a0802be)), closes [#66](https://github.com/gotson/komga/issues/66)
* **web reader:** thumbnails explorer ([ec06955](https://github.com/gotson/komga/commit/ec06955e22f72e161ebe8c8b1f485b8139e03076)), closes [#62](https://github.com/gotson/komga/issues/62)
* better management of book analysis errors ([8c26a31](https://github.com/gotson/komga/commit/8c26a318fe84ced77fe763711dec78575073678f))

# [0.11.0](https://github.com/gotson/komga/compare/v0.10.1...v0.11.0) (2020-01-06)


### Bug Fixes

* **web reader:** remove webp as it's not supported in Safari ([6770107](https://github.com/gotson/komga/commit/6770107dc8532c9bd62e2dbcadc0124df151a385))
* add support for jpeg2000/jbig2 formats ([227975a](https://github.com/gotson/komga/commit/227975a79eecd7882e6f73c5e49910bc86eeba18)), closes [#50](https://github.com/gotson/komga/issues/50)


### Features

* handle archives without images ([70a2da5](https://github.com/gotson/komga/commit/70a2da532160a10ae56fda017478f2b0ecd92ef1)), closes [#56](https://github.com/gotson/komga/issues/56)
* **web reader:** rtl option ([98efa9b](https://github.com/gotson/komga/commit/98efa9b44894eccd3e38c6f2bc8b777fb206732f)), closes [#53](https://github.com/gotson/komga/issues/53)
* display version in UI ([4085f1f](https://github.com/gotson/komga/commit/4085f1fdaac973f549d11a9c2d62cc5f45abd747)), closes [#42](https://github.com/gotson/komga/issues/42)
* rescan library ([30208a2](https://github.com/gotson/komga/commit/30208a234044fad8d68902cfba8e601255665cac)), closes [#38](https://github.com/gotson/komga/issues/38)
* **api:** reAnalyze library ([fa65e94](https://github.com/gotson/komga/commit/fa65e94ae6397fb61e04ded733db10bc997d07de))
* **api:** reAnalyze series ([e80451f](https://github.com/gotson/komga/commit/e80451ffcc79c8bbadcbd452acd4644ff38773ff))
* **rest api:** ability to re-analyze a book ([8e81356](https://github.com/gotson/komga/commit/8e81356908e42d1b7b8edb935a1eaf133cff7035)), closes [#51](https://github.com/gotson/komga/issues/51)
* **rest api:** don't return hidden files for directory listings ([a478d90](https://github.com/gotson/komga/commit/a478d90a596749b5e7752a2a934ef5e6cf244f20))
* **webui:** add menu option to analyze book from browsing view ([64f542d](https://github.com/gotson/komga/commit/64f542d42fb18010fa4d602aaa1c2f5bace5e1e9)), closes [#51](https://github.com/gotson/komga/issues/51)
* **webui:** reAnalyze library ([b599b72](https://github.com/gotson/komga/commit/b599b72c48d6687d6679cce6b12407abe8cf7d86)), closes [#51](https://github.com/gotson/komga/issues/51)
* **webui:** reAnalyze series ([b997561](https://github.com/gotson/komga/commit/b9975618a30694c8bbd791e22794d1427231d837))

## [0.10.1](https://github.com/gotson/komga/compare/v0.10.0...v0.10.1) (2020-01-01)


### Bug Fixes

* **webui:** remove CDN usage for icons and fonts ([c88a27c](https://github.com/gotson/komga/commit/c88a27c10abdfecd5d9476ca74f382418922a546)), closes [#45](https://github.com/gotson/komga/issues/45)
* **webui:** show all books when browsing series ([85ca99d](https://github.com/gotson/komga/commit/85ca99d49aaeabed40b578dfff3e1d7f4c2e6bff))
* **zip extractor:** better handling of exotic charsets ([0254d7d](https://github.com/gotson/komga/commit/0254d7d8671a3743bfedbcd42472dbe974a76c98)), closes [#41](https://github.com/gotson/komga/issues/41)

<a name="v0.10.0"></a>
# [v0.10.0](https://github.com/gotson/komga/releases/tag/v0.10.0) - 31 Dec 2019

### Features

- **webui:** added **Web Reader** (#28)
- **webui:** display all books instead of only books in ready state
- **webui:** add 'Date updated' sort criteria for Series
- **webui:** add 'File size' sort criteria for Books
- **api:** handle the HTTP cache properly for dynamic resources: thumbnails and pages (#27)
- **api:** hide full path to non-admin users (for libraries, series and books)
- **scanner:** add configuration key to force the last modified time of directories (#37)

### Fixes

- **webui:** rework dashboard sliders to be more touch-friendly
- **webui:** better display on mobile and small screens
- remove regeneration of missing thumbnails at startup (don't remember why I added it in the first place ¯\_(ツ)_/¯ )

[Changes][v0.10.0]


<a name="v0.9.1"></a>
# [v0.9.1](https://github.com/gotson/komga/releases/tag/v0.9.1) - 18 Dec 2019

This release is focused on performance enhancements.

### Changes

- Hibernate lazy loading was not working because of Kotlin closed by default classes
- add Hibernate caches: second level, collections, query
- enhance code path to reduce number of database queries
- fix a bug where Series thumbnail would be retrieved by the first book in the collection, instead of the first by number
- remove (unused) bi-directional OneToOne relationship between Book and BookMetadata to reduce database fetch

[Changes][v0.9.1]


<a name="v0.9.0"></a>
# [v0.9.0](https://github.com/gotson/komga/releases/tag/v0.9.0) - 12 Dec 2019

### Features

- Web Interface enhancements (closes #18)
  - browse Libraries, Series, and Books
  - dashboard with recently added Series and Books
  - search Series and Books
  - remove browser pop-up for basic auth, use a dedicated login screen instead
  - http cache for static assets

### Changes

- retrieve real ip in audit logs (for example if behind a reverse proxy)
- remove Humio metrics
- libraries are sorted by name in the API by default

### Deprecation

- endpoints of the form `/series/{seriesId}/books/{bookId}/**`, use `/books/{bookId}/**` instead

### Known issues

- UI is extremely slow when browsing libraries/series with many items (75+) on Chrome Android

[Changes][v0.9.0]


<a name="v0.8.1"></a>
# [v0.8.1](https://github.com/gotson/komga/releases/tag/v0.8.1) - 05 Nov 2019

### Changes

- add metrics exporter for InfluxDB (disabled by default)

[Changes][v0.8.1]


<a name="v0.8.0"></a>
# [v0.8.0](https://github.com/gotson/komga/releases/tag/v0.8.0) - 30 Oct 2019

### Changes

- change docker base image to adoptopenjdk 11
- retrieve file size of books when scanning. Add file size in REST API and OPDS.

### Bug fixes

- Swagger is not showing Kotlin nullable types as optional (fixes #15)
- compare file modification time at millisecond. With JDK 9+ on Windows, time precision of the underlying clock would go over 6 digits, which is the precision of the timestamps in database, which would lead to loss of precision on saved timestamps, and failing comparisons of modification times at every library scan
- return only books in ready state via OPDS, else it would throw an error 500 because metadata is not ready
- prevent circular loop in the error resolver, which would complain in logs

[Changes][v0.8.0]


<a name="v0.7.1"></a>
# [v0.7.1](https://github.com/gotson/komga/releases/tag/v0.7.1) - 25 Oct 2019

### Bug fixes

- could not delete a library if it was specifically shared with any user

### Changes

- add build and git info in `/actuator/info` endpoint
- add `humio` metrics exporter (disabled, need to be enabled by configuration)

[Changes][v0.7.1]


<a name="v0.7.0"></a>
# [v0.7.0](https://github.com/gotson/komga/releases/tag/v0.7.0) - 22 Oct 2019

### :warning: Breaking changes

- `admin` and `user` users are deprecated and replaced by the User Management feature

### Features

- User management:
  - Automatic creation of an admin account at startup if no user exist in database, outputting the login and a random password in the logs
  - Ability to add/remove user accounts
  - Ability to manage roles: Admin/User
  - Ability to manage access to shared libraries per user
  - Ability for a user to change his/her password

### Changes

- OPDS entry links to file now include the full filename. This helps some (badly implemented) OPDS clients to correctly see the files.
- logs are written to disk, and available via the `/actuator/logfile` endpoint

### Bug fixes

- fix OPDS link for page streaming where an incorrect url escape in the `zero_based` query parameter would generate an error for the first page, shift all pages, and prevent the last page to be streamed

[Changes][v0.7.0]


<a name="v0.6.1"></a>
# [v0.6.1](https://github.com/gotson/komga/releases/tag/v0.6.1) - 12 Oct 2019

**Fixes**:

- Thumbnail media type was incorrect for OPDS feed
- `/series` endpoint library filter can accept a list instead of a single value
- `/filesystem` endpoint now sorts directories with case insensitive

[Changes][v0.6.1]


<a name="v0.6.0"></a>
# [v0.6.0](https://github.com/gotson/komga/releases/tag/v0.6.0) - 11 Oct 2019

**Changes**:

- change thumbnail format from PNG to JPEG to reduce size (I observed reduction by 10 of the database size)
- regenerate missing thumbnails on startup
- force one time regeneration of all thumbnails in this release to change the format of existing thumbnails

[Changes][v0.6.0]


<a name="v0.5.0"></a>
# [v0.5.0](https://github.com/gotson/komga/releases/tag/v0.5.0) - 10 Oct 2019

**Features**:

- Support for multiple libraries
- First version of the web interface, which supports addition/deletion of libraries
- OPDS feed supports browsing by library
- REST API `/series` endpoint can be filtered by `library_id`
- Ability to exclude directories from disk scan, to be configured via `komga.libraries-scan-directory-exclusions` configuration key

**Deprecations**:

- The configuration property `komga.root-folder` is deprecated. Use the web interface to add libraries instead.
- The configuration property `komga.root-folder-scan-cron` is deprecated. It is replaced by `komga.libraries-scan-cron`.

[Changes][v0.5.0]


<a name="v0.4.1"></a>
# [v0.4.1](https://github.com/gotson/komga/releases/tag/v0.4.1) - 30 Sep 2019

properly release open files after scan (fixes #9)
properly release open files after accessing PDF files (fixes #10)

[Changes][v0.4.1]


<a name="v0.4.0"></a>
# [v0.4.0](https://github.com/gotson/komga/releases/tag/v0.4.0) - 23 Sep 2019

support for OPDS feed with OpenSearch and Page Streaming Extension (https://vaemendis.net/opds-pse/)

[Changes][v0.4.0]


<a name="v0.3.4"></a>
# [v0.3.4](https://github.com/gotson/komga/releases/tag/v0.3.4) - 10 Sep 2019

the fix in v0.3.2 also increased the scan time, rolling back the change, and adding a one-time rescan to handle potential pdf files that were forgotten in mixed-content directories
subsequent addition of new supported file formats should force a rescan to ensure there are no missing files

[Changes][v0.3.4]


<a name="v0.3.3"></a>
# [v0.3.3](https://github.com/gotson/komga/releases/tag/v0.3.3) - 07 Sep 2019



[Changes][v0.3.3]


<a name="v0.3.2"></a>
# [v0.3.2](https://github.com/gotson/komga/releases/tag/v0.3.2) - 07 Sep 2019

fixed a side effect that could occur in mixed-content directories containing pdf files that were scanned before the support of pdf was introduced, where pdf files in those directories would never be rescanned

[Changes][v0.3.2]


<a name="v0.3.1"></a>
# [v0.3.1](https://github.com/gotson/komga/releases/tag/v0.3.1) - 07 Sep 2019



[Changes][v0.3.1]


<a name="v0.3.0"></a>
# [v0.3.0](https://github.com/gotson/komga/releases/tag/v0.3.0) - 06 Sep 2019

Support for WEBP image format to generate thumbnails
Better PDF handling

[Changes][v0.3.0]


<a name="v0.2.1"></a>
# [v0.2.1](https://github.com/gotson/komga/releases/tag/v0.2.1) - 04 Sep 2019

On the fly conversion of pages

[Changes][v0.2.1]


<a name="v0.2.0"></a>
# [v0.2.0](https://github.com/gotson/komga/releases/tag/v0.2.0) - 30 Aug 2019

PDF support

[Changes][v0.2.0]


<a name="v0.1.1"></a>
# [v0.1.1](https://github.com/gotson/komga/releases/tag/v0.1.1) - 28 Aug 2019



[Changes][v0.1.1]


<a name="v0.1.0"></a>
# [v0.1.0](https://github.com/gotson/komga/releases/tag/v0.1.0) - 27 Aug 2019

First release, support for `cbr` and `cbz` archives

[Changes][v0.1.0]


[v0.10.0]: https://github.com/gotson/komga/compare/v0.9.1...v0.10.0
[v0.9.1]: https://github.com/gotson/komga/compare/v0.9.0...v0.9.1
[v0.9.0]: https://github.com/gotson/komga/compare/v0.8.1...v0.9.0
[v0.8.1]: https://github.com/gotson/komga/compare/v0.8.0...v0.8.1
[v0.8.0]: https://github.com/gotson/komga/compare/v0.7.1...v0.8.0
[v0.7.1]: https://github.com/gotson/komga/compare/v0.7.0...v0.7.1
[v0.7.0]: https://github.com/gotson/komga/compare/v0.6.1...v0.7.0
[v0.6.1]: https://github.com/gotson/komga/compare/v0.6.0...v0.6.1
[v0.6.0]: https://github.com/gotson/komga/compare/v0.5.0...v0.6.0
[v0.5.0]: https://github.com/gotson/komga/compare/v0.4.1...v0.5.0
[v0.4.1]: https://github.com/gotson/komga/compare/v0.4.0...v0.4.1
[v0.4.0]: https://github.com/gotson/komga/compare/v0.3.4...v0.4.0
[v0.3.4]: https://github.com/gotson/komga/compare/v0.3.3...v0.3.4
[v0.3.3]: https://github.com/gotson/komga/compare/v0.3.2...v0.3.3
[v0.3.2]: https://github.com/gotson/komga/compare/v0.3.1...v0.3.2
[v0.3.1]: https://github.com/gotson/komga/compare/v0.3.0...v0.3.1
[v0.3.0]: https://github.com/gotson/komga/compare/v0.2.1...v0.3.0
[v0.2.1]: https://github.com/gotson/komga/compare/v0.2.0...v0.2.1
[v0.2.0]: https://github.com/gotson/komga/compare/v0.1.1...v0.2.0
[v0.1.1]: https://github.com/gotson/komga/compare/v0.1.0...v0.1.1
[v0.1.0]: https://github.com/gotson/komga/tree/v0.1.0

 <!-- Generated by changelog-from-release -->
