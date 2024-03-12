# [1.10.4](https://github.com/gotson/komga/compare/1.10.3...1.10.4) (2024-03-12)
## 🐛 Fixes
**api**
- cap search results to avoid SQLITE_TOOBIG ([d34d4a5](https://github.com/gotson/komga/commits/d34d4a5)), closes [#1428](https://github.com/gotson/komga/issues/1428)
- book fileLastModified was in local time instead of UTC ([1648533](https://github.com/gotson/komga/commits/1648533)), closes [#1430](https://github.com/gotson/komga/issues/1430)

**unscoped**
- better compatibility for malformed epub files ([e09cbc4](https://github.com/gotson/komga/commits/e09cbc4)), closes [#1441](https://github.com/gotson/komga/issues/1441)
- some epub resources would not be detected properly ([f8e40ce](https://github.com/gotson/komga/commits/f8e40ce)), closes [#1441](https://github.com/gotson/komga/issues/1441) [#1442](https://github.com/gotson/komga/issues/1442)

## 🛠  Build

- migrate to gradle/actions/setup-gradle@v3 ([5466087](https://github.com/gotson/komga/commits/5466087))

## 🌐 Translation
**komga-tray**
- deleted translation using Weblate (Chinese (Simplified)) ([ed374a1](https://github.com/gotson/komga/commits/ed374a1))
- translated using Weblate (Chinese (Simplified)) ([4b7bfa0](https://github.com/gotson/komga/commits/4b7bfa0))
- translated using Weblate (Korean) ([05f02f5](https://github.com/gotson/komga/commits/05f02f5))
- translated using Weblate (Italian) ([e38dce2](https://github.com/gotson/komga/commits/e38dce2))
- added translation using Weblate (Portuguese) ([0288231](https://github.com/gotson/komga/commits/0288231))
- translated using Weblate (Turkish) ([0f189c1](https://github.com/gotson/komga/commits/0f189c1))

**webui**
- translated using Weblate (Chinese (Simplified)) ([38cdf70](https://github.com/gotson/komga/commits/38cdf70))
- translated using Weblate (Korean) ([ac3eb4e](https://github.com/gotson/komga/commits/ac3eb4e))
- translated using Weblate (Finnish) ([d98de06](https://github.com/gotson/komga/commits/d98de06))
- translated using Weblate (Italian) ([e9f1017](https://github.com/gotson/komga/commits/e9f1017))
- added translation using Weblate (Portuguese) ([5b70520](https://github.com/gotson/komga/commits/5b70520))
- translated using Weblate (Portuguese (Brazil)) ([6e915d6](https://github.com/gotson/komga/commits/6e915d6))
- translated using Weblate (Turkish) ([4e5cc0d](https://github.com/gotson/komga/commits/4e5cc0d))

## ⚙️ Dependencies
**ci**
- bump gradle/wrapper-validation-action from 1 to 2 ([5a21c5c](https://github.com/gotson/komga/commits/5a21c5c))
- bump hydraulic-software/conveyor from 13.0 to 13.1 ([c9ab1f7](https://github.com/gotson/komga/commits/c9ab1f7))
- bump peter-evans/dockerhub-description from 3.4.2 to 4.0.0 ([898bc19](https://github.com/gotson/komga/commits/898bc19))

**webui**
- bump sanitize-html from 2.11.0 to 2.12.1 in /komga-webui ([b0b457a](https://github.com/gotson/komga/commits/b0b457a))

# [1.10.3](https://github.com/gotson/komga/compare/1.10.2...1.10.3) (2024-01-23)
## 🐛 Fixes
**komga**
- add field alias for Mylar series.json ([bc5a73a](https://github.com/gotson/komga/commits/bc5a73a))

## 🔄️ Changes
**komga**
- remove deprecated property ([2ec3265](https://github.com/gotson/komga/commits/2ec3265))

## 🛠  Build
**release**
- stop publishing Conveyor old-site ([9d1b82a](https://github.com/gotson/komga/commits/9d1b82a))

# [1.10.2](https://github.com/gotson/komga/compare/1.10.1...1.10.2) (2024-01-22)
## 🐛 Fixes
**komga**
- ignore missing epub resources ([22c2131](https://github.com/gotson/komga/commits/22c2131)), closes [#1386](https://github.com/gotson/komga/issues/1386)
- add safeguards for malformed epub ([270a50c](https://github.com/gotson/komga/commits/270a50c)), closes [#1386](https://github.com/gotson/komga/issues/1386)

## 🔄️ Changes
**komga**
- remove unnecessary null checks ([98f20cb](https://github.com/gotson/komga/commits/98f20cb))
- replace deprecated TaskExecutorBuilder with ThreadPoolTaskExecutorBuilder ([864dba6](https://github.com/gotson/komga/commits/864dba6))
- use lazy logging statement ([479aff2](https://github.com/gotson/komga/commits/479aff2))

**unscoped**
- ktlint format ([d9bba60](https://github.com/gotson/komga/commits/d9bba60))

## 🛠  Build
**docker**
- use new Spring Boot jar launcher ([fbb2b6b](https://github.com/gotson/komga/commits/fbb2b6b))

**release**
- set conveyor consistency-checks to warn only ([966a5d3](https://github.com/gotson/komga/commits/966a5d3))
- more knobs to handle broken releases ([e730b74](https://github.com/gotson/komga/commits/e730b74))

**tests**
- ignore komga-tray translations ([814b94d](https://github.com/gotson/komga/commits/814b94d))

**unscoped**
- bump Gradle from 8.4 to 8.5 ([c5b9baa](https://github.com/gotson/komga/commits/c5b9baa))

## 🌐 Translation
**komga-tray**
- translated using Weblate (Japanese) ([9265c5a](https://github.com/gotson/komga/commits/9265c5a))
- translated using Weblate (German) ([d420648](https://github.com/gotson/komga/commits/d420648))
- added translation using Weblate (Galician) ([1c88058](https://github.com/gotson/komga/commits/1c88058))

**webui**
- translated using Weblate (Chinese (Simplified)) ([6b50968](https://github.com/gotson/komga/commits/6b50968))
- translated using Weblate (Spanish) ([6c7dedd](https://github.com/gotson/komga/commits/6c7dedd))
- translated using Weblate (Japanese) ([7e89f54](https://github.com/gotson/komga/commits/7e89f54))
- translated using Weblate (Bulgarian) ([99d94da](https://github.com/gotson/komga/commits/99d94da))
- translated using Weblate (Portuguese (Brazil)) ([ec988b4](https://github.com/gotson/komga/commits/ec988b4))
- translated using Weblate (Galician) ([e2f0dc2](https://github.com/gotson/komga/commits/e2f0dc2))

## ⚙️ Dependencies
**komga**
- bump Spring Boot to 3.2.2 ([3b25396](https://github.com/gotson/komga/commits/3b25396))
- bump nu.studer.jooq to 9.0 ([f9aafae](https://github.com/gotson/komga/commits/f9aafae))
- bump ksp to 1.9.21-1.0.16 ([fb2e05d](https://github.com/gotson/komga/commits/fb2e05d))
- bump mockk to 1.13.9 ([24e095a](https://github.com/gotson/komga/commits/24e095a))
- remove hawtio ([4128e96](https://github.com/gotson/komga/commits/4128e96))
- bump archunit-junit5 to 1.2.1 ([76586eb](https://github.com/gotson/komga/commits/76586eb))
- bump tsid-creator to 5.2.6 ([c49a80e](https://github.com/gotson/komga/commits/c49a80e))
- bump twelvemonkeys.imageio to 3.10.1 ([c54261b](https://github.com/gotson/komga/commits/c54261b))
- bump jsoup to 1.17.2 ([6de7a14](https://github.com/gotson/komga/commits/6de7a14))
- bump commons-compress to 1.25.0 ([c1259f3](https://github.com/gotson/komga/commits/c1259f3))
- bump icu4j to 74.2 ([a404ce6](https://github.com/gotson/komga/commits/a404ce6))
- bump commons-io to 2.15.1 ([71a6a48](https://github.com/gotson/komga/commits/71a6a48))
- bump commons-lang3 to 3.14.0 ([2af8293](https://github.com/gotson/komga/commits/2af8293))
- bump commons-validator to 1.8.0 ([78971a5](https://github.com/gotson/komga/commits/78971a5))
- bump sqlite-jdbc to 3.45.0.0 ([056101f](https://github.com/gotson/komga/commits/056101f))
- bump lucene to 9.9.1 ([7f3cc9c](https://github.com/gotson/komga/commits/7f3cc9c))
- bump org.apache.pdfbox:pdfbox from 2.0.28 to 3.0.1 ([38fcde9](https://github.com/gotson/komga/commits/38fcde9)), closes [#1334](https://github.com/gotson/komga/issues/1334)

**komga-tray**
- bump org.jetbrains.compose to 1.5.11 ([c7a695d](https://github.com/gotson/komga/commits/c7a695d))
- bump conveyor.gradle.plugin to 1.8 ([b141f21](https://github.com/gotson/komga/commits/b141f21))

**unscoped**
- bump Kotlin to 1.9.21 ([b87bec8](https://github.com/gotson/komga/commits/b87bec8))
- bump ktlint to 1.1.1 ([e01b324](https://github.com/gotson/komga/commits/e01b324))
- bump org.jlleitschuh.gradle.ktlint to 12.1.0 ([80ed686](https://github.com/gotson/komga/commits/80ed686))
- bump jreleaser to 1.10.0 ([446bb5a](https://github.com/gotson/komga/commits/446bb5a))
- bump com.github.ben-manes.versions from 0.48.0 to 0.50.0 ([1daa4b8](https://github.com/gotson/komga/commits/1daa4b8))
- bump io.github.microutils:kotlin-logging-jvm:3.0.5 to io.github.oshai:kotlin-logging-jvm:6.0.3 ([295bfaf](https://github.com/gotson/komga/commits/295bfaf))

# [1.10.1](https://github.com/gotson/komga/compare/1.10.0...1.10.1) (2024-01-12)
## 🚀 Features
**windows**
- add libarchive for RAR5 support ([3e11e90](https://github.com/gotson/komga/commits/3e11e90))

## 🐛 Fixes
**komga**
- detect older epub as fixed-layout ([e08cea8](https://github.com/gotson/komga/commits/e08cea8)), closes [#1385](https://github.com/gotson/komga/issues/1385)

## 🛠  Build
**release**
- fix missing upload to B2 ([5d85585](https://github.com/gotson/komga/commits/5d85585)), closes [#1388](https://github.com/gotson/komga/issues/1388)

**windows**
- replace native libraries with vcpkg-built ones ([834306f](https://github.com/gotson/komga/commits/834306f))

## ⚙️ Dependencies
**webui**
- bump follow-redirects from 1.15.2 to 1.15.4 in /komga-webui ([056b4e7](https://github.com/gotson/komga/commits/056b4e7))

# [1.10.0](https://github.com/gotson/komga/compare/1.9.2...1.10.0) (2024-01-08)
## 🚀 Features
**docker**
- add libarchive for RAR5 support ([6f8467a](https://github.com/gotson/komga/commits/6f8467a))

**komga**
- support for RAR5 via libarchive ([4c1301f](https://github.com/gotson/komga/commits/4c1301f))

**komga-tray**
- add libarchive for RAR5 support on macOS ([536d478](https://github.com/gotson/komga/commits/536d478))

## 🐛 Fixes
**api**
- mark progression fragment handling ([eb24470](https://github.com/gotson/komga/commits/eb24470))
- mark progression timezone handling ([ec97da6](https://github.com/gotson/komga/commits/ec97da6)), closes [#1363](https://github.com/gotson/komga/issues/1363)

**webui**
- webreader crash when reading one-shot from collection context ([bdf9ec9](https://github.com/gotson/komga/commits/bdf9ec9)), closes [#1374](https://github.com/gotson/komga/issues/1374)

## 🔄️ Changes

- ktlint ([8c4a5e3](https://github.com/gotson/komga/commits/8c4a5e3))

## 🛠  Build
**release**
- upload to B2 with conveyor ([cccbc39](https://github.com/gotson/komga/commits/cccbc39))
- limit conveyor cache size ([cabed41](https://github.com/gotson/komga/commits/cabed41))

## 🌐 Translation
**komga-tray**
- translated using Weblate (Chinese (Simplified)) ([58ca293](https://github.com/gotson/komga/commits/58ca293))
- translated using Weblate (Finnish) ([d955db2](https://github.com/gotson/komga/commits/d955db2))
- translated using Weblate (French) ([e623c46](https://github.com/gotson/komga/commits/e623c46))
- translated using Weblate (Spanish) ([5758f6d](https://github.com/gotson/komga/commits/5758f6d))
- translated using Weblate (Dutch) ([8fa8989](https://github.com/gotson/komga/commits/8fa8989))
- added translation using Weblate (Catalan) ([e28c8eb](https://github.com/gotson/komga/commits/e28c8eb))
- translated using Weblate (Russian) ([609368f](https://github.com/gotson/komga/commits/609368f))

**webui**
- translated using Weblate (Chinese (Simplified)) ([19000da](https://github.com/gotson/komga/commits/19000da))
- translated using Weblate (French) ([ea4bead](https://github.com/gotson/komga/commits/ea4bead))
- translated using Weblate (Dutch) ([86b2ec9](https://github.com/gotson/komga/commits/86b2ec9))
- translated using Weblate (Catalan) ([2150ddc](https://github.com/gotson/komga/commits/2150ddc))
- translated using Weblate (Russian) ([3bbe7b9](https://github.com/gotson/komga/commits/3bbe7b9))

## ⚙️ Dependencies
**ci**
- bump conveyor to 13.0 ([7dd05a5](https://github.com/gotson/komga/commits/7dd05a5))
- bump actions/upload-artifact from 3 to 4 ([6d7dfdd](https://github.com/gotson/komga/commits/6d7dfdd))

# [1.9.2](https://github.com/gotson/komga/compare/1.9.1...1.9.2) (2023-12-18)
## 🛠  Build
**release**
- re-enable npm cache ([d65ee39](https://github.com/gotson/komga/commits/d65ee39))

**webui**
- build would pull incorrect version of dependency ([de0e854](https://github.com/gotson/komga/commits/de0e854)), closes [#1353](https://github.com/gotson/komga/issues/1353)

# [1.9.1](https://github.com/gotson/komga/compare/1.9.0...1.9.1) (2023-12-18)
## 🐛 Fixes
**api**
- incorrect progression date check ([3daa194](https://github.com/gotson/komga/commits/3daa194))

**epubreader**
- ignore progression without locator ([d168c0a](https://github.com/gotson/komga/commits/d168c0a)), closes [#1352](https://github.com/gotson/komga/issues/1352)

## 🔄️ Changes

- remove announcement workaround for links ([c4d6868](https://github.com/gotson/komga/commits/c4d6868))

## 🛠  Build
**release**
- disable npm cache ([4f79fbf](https://github.com/gotson/komga/commits/4f79fbf)), closes [#1353](https://github.com/gotson/komga/issues/1353)

## 🌐 Translation
**komga-tray**
- use UTF-8 for properties ([4c922c4](https://github.com/gotson/komga/commits/4c922c4))

**webui**
- translated using Weblate (Chinese (Simplified)) ([b4c05a2](https://github.com/gotson/komga/commits/b4c05a2))
- translated using Weblate (Finnish) ([341b42b](https://github.com/gotson/komga/commits/341b42b))
- translated using Weblate (Spanish) ([754d016](https://github.com/gotson/komga/commits/754d016))
- translated using Weblate (Italian) ([744b6f5](https://github.com/gotson/komga/commits/744b6f5))
- translated using Weblate (German) ([5f96700](https://github.com/gotson/komga/commits/5f96700))

# [1.9.0](https://github.com/gotson/komga/compare/1.8.4...1.9.0) (2023-12-15)
## 🚀 Features
**api**
- increase PDF image resolution ([e856d4f](https://github.com/gotson/komga/commits/e856d4f))
- make pre-paginated epub containing only images compatible with divina profile ([c2a4d17](https://github.com/gotson/komga/commits/c2a4d17))
- add basic metadata for transient books ([1050f52](https://github.com/gotson/komga/commits/1050f52))
- add Readium Progression API ([20799ad](https://github.com/gotson/komga/commits/20799ad))
- add layout attribute on epub manifest ([5096364](https://github.com/gotson/komga/commits/5096364))
- add positions endpoint to get pre-computed positions of epub books ([eb8a644](https://github.com/gotson/komga/commits/eb8a644))

**epubreader**
- handle reading direction ([77b8a32](https://github.com/gotson/komga/commits/77b8a32))
- remove margin and dropshadow for pre-paginated publications ([10e2597](https://github.com/gotson/komga/commits/10e2597))
- mark read progress ([1172911](https://github.com/gotson/komga/commits/1172911))
- adjust display of current progress ([b234586](https://github.com/gotson/komga/commits/b234586))
- add navigation modes with click/tap, buttons, or both ([2f0f94d](https://github.com/gotson/komga/commits/2f0f94d))
- display percentage of total progression ([3a14990](https://github.com/gotson/komga/commits/3a14990))

**komga**
- relax epub validity controls ([fad93ad](https://github.com/gotson/komga/commits/fad93ad))

**komga-tray**
- display dialog with error detail on application startup failure ([0fdcb2a](https://github.com/gotson/komga/commits/0fdcb2a)), closes [#1336](https://github.com/gotson/komga/issues/1336)

**opds-v1**
- add OPDS-PSE links for divina-compatible EPUBs ([0c96054](https://github.com/gotson/komga/commits/0c96054))
- use static xml namespace prefix for opds-pse ([834b51d](https://github.com/gotson/komga/commits/834b51d))

**webui**
- use divina reader for compatible epub ([809a794](https://github.com/gotson/komga/commits/809a794)), closes [#1324](https://github.com/gotson/komga/issues/1324)
- autofill series and number from metadata during book import ([5b75345](https://github.com/gotson/komga/commits/5b75345)), closes [#998](https://github.com/gotson/komga/issues/998)

## 🐛 Fixes
**api**
- properly set layout property in webpub manifest ([e11ce46](https://github.com/gotson/komga/commits/e11ce46))
- cannot get last page of PDF ([721c5d1](https://github.com/gotson/komga/commits/721c5d1)), closes [#1341](https://github.com/gotson/komga/issues/1341)

**epubreader**
- properly handle clicks in iframes ([4db0542](https://github.com/gotson/komga/commits/4db0542))

**komga**
- empty generated thumbnails would be saved in DB ([15920b7](https://github.com/gotson/komga/commits/15920b7)), closes [#1338](https://github.com/gotson/komga/issues/1338)
- use an in-process lock for Lucene ([1a30bf9](https://github.com/gotson/komga/commits/1a30bf9))

**webui**
- pre-render page could overlap current page in some specific conditions ([7a8d50c](https://github.com/gotson/komga/commits/7a8d50c)), closes [#1339](https://github.com/gotson/komga/issues/1339)

## 🏎 Perf
**komga**
- convert BookMetadataProvider to property ([3843f77](https://github.com/gotson/komga/commits/3843f77))

## 🔄️ Changes
**komga**
- move date utils to language package ([fbc1034](https://github.com/gotson/komga/commits/fbc1034))

**komga-tray**
- catch any exception on startup ([b312847](https://github.com/gotson/komga/commits/b312847))

## 🌐 Translation
**komga-tray**
- translated using Weblate (Chinese (Simplified)) ([142326c](https://github.com/gotson/komga/commits/142326c))
- translated using Weblate (Finnish) ([d19e1a5](https://github.com/gotson/komga/commits/d19e1a5))
- translated using Weblate (Spanish) ([ad6a8c9](https://github.com/gotson/komga/commits/ad6a8c9))

**webui**
- fix language file name ([ac59fca](https://github.com/gotson/komga/commits/ac59fca)), closes [#1345](https://github.com/gotson/komga/issues/1345)
- translated using Weblate (Chinese (Simplified)) ([0274308](https://github.com/gotson/komga/commits/0274308))
- translated using Weblate (Spanish) ([37abfbf](https://github.com/gotson/komga/commits/37abfbf))
- translated using Weblate (Catalan) ([32e9a0c](https://github.com/gotson/komga/commits/32e9a0c))
- translated using Weblate (German) ([d2a91ac](https://github.com/gotson/komga/commits/d2a91ac))

# [1.8.4](https://github.com/gotson/komga/compare/1.8.3...1.8.4) (2023-12-06)
## 🐛 Fixes
**webui**
- force pre-render of images in the webreader ([7ab5eb3](https://github.com/gotson/komga/commits/7ab5eb3)), closes [#1323](https://github.com/gotson/komga/issues/1323)
- update browserlist ([0a0eaca](https://github.com/gotson/komga/commits/0a0eaca)), closes [#1253](https://github.com/gotson/komga/issues/1253)

## 🔄️ Changes
**webui**
- rename BookReader to DivinaReader ([c628807](https://github.com/gotson/komga/commits/c628807))

## 🛠  Build
**conveyor**
- disable update escape hatch for MS Store ([6e4a3ec](https://github.com/gotson/komga/commits/6e4a3ec))

## 🌐 Translation
**komga-tray**
- add empty translation files for all languages ([8de029d](https://github.com/gotson/komga/commits/8de029d))
- internationalization for desktop app tray menu ([1ad0cd2](https://github.com/gotson/komga/commits/1ad0cd2))

**webui**
- translated using Weblate (Chinese (Simplified)) ([2260da1](https://github.com/gotson/komga/commits/2260da1))
- translated using Weblate (Finnish) ([f04e03c](https://github.com/gotson/komga/commits/f04e03c))
- translated using Weblate (French) ([f7480ce](https://github.com/gotson/komga/commits/f7480ce))
- translated using Weblate (Spanish) ([af14751](https://github.com/gotson/komga/commits/af14751))

## ⚙️ Dependencies
**ci**
- bump actions/setup-java from 3 to 4 ([c97a322](https://github.com/gotson/komga/commits/c97a322))

# [1.8.3](https://github.com/gotson/komga/compare/1.8.2...1.8.3) (2023-11-30)
## 🐛 Fixes
**api**
- epub resources could not be retrieved if komga is running with a servlet context path ([0d94ae2](https://github.com/gotson/komga/commits/0d94ae2))

**komga**
- better handling of collection/readlist creation/update when using multiple threads ([a4384a6](https://github.com/gotson/komga/commits/a4384a6)), closes [#1317](https://github.com/gotson/komga/issues/1317)
- don't repair extension for broken EPUB files ([f41af61](https://github.com/gotson/komga/commits/f41af61))
- mark broken EPUB files as ERROR instead of falling back to CBZ ([acf080b](https://github.com/gotson/komga/commits/acf080b))
- use NightMonkeys WebP reader if present ([7b1a9e4](https://github.com/gotson/komga/commits/7b1a9e4)), closes [#1294](https://github.com/gotson/komga/issues/1294)
- better error handling during metadata refresh ([8832a0d](https://github.com/gotson/komga/commits/8832a0d)), closes [#1311](https://github.com/gotson/komga/issues/1311)

**opds**
- cannot retrieve full size poster for epub books ([5a71cf7](https://github.com/gotson/komga/commits/5a71cf7)), closes [#1312](https://github.com/gotson/komga/issues/1312)
- index out of bounds error ([d6246ed](https://github.com/gotson/komga/commits/d6246ed)), closes [#1309](https://github.com/gotson/komga/issues/1309)

## 🔄️ Changes
**komga**
- rename class ([74210f8](https://github.com/gotson/komga/commits/74210f8))

## 🛠  Build
**docker**
- add native webp library for docker amd64 and arm64 ([8cf8f47](https://github.com/gotson/komga/commits/8cf8f47)), closes [#1294](https://github.com/gotson/komga/issues/1294)

**komga-tray**
- add native heif library for Windows ([a3439dd](https://github.com/gotson/komga/commits/a3439dd))
- add native webp library for macOS application ([86f0fcd](https://github.com/gotson/komga/commits/86f0fcd))

**release**
- use BackBlaze B2 for binaries storage instead of Github ([f704685](https://github.com/gotson/komga/commits/f704685))

# [1.8.2](https://github.com/gotson/komga/compare/1.8.1...1.8.2) (2023-11-28)
## 🐛 Fixes
**epubreader**
- page cannot load because of incorrect css mime type ([dbc5b3d](https://github.com/gotson/komga/commits/dbc5b3d))

# [1.8.1](https://github.com/gotson/komga/compare/1.8.0...1.8.1) (2023-11-28)
## 🐛 Fixes
**api**
- set X-Frame-Options header to same origin for epubreader ([85cae8a](https://github.com/gotson/komga/commits/85cae8a))

# [1.8.0](https://github.com/gotson/komga/compare/1.7.2...1.8.0) (2023-11-28)
## 🚀 Features
**api**
- epub ebook support ([a7252f8](https://github.com/gotson/komga/commits/a7252f8)), closes [#221](https://github.com/gotson/komga/issues/221)

**webui**
- epubreader ([3d69e19](https://github.com/gotson/komga/commits/3d69e19)), closes [#221](https://github.com/gotson/komga/issues/221)

## 🐛 Fixes
**komga**
- better error handling of sse connections ([ab34781](https://github.com/gotson/komga/commits/ab34781))

## 🔄️ Changes
**komga**
- introduce media profile for PDF ([d6680a4](https://github.com/gotson/komga/commits/d6680a4))
- make Media.pageCount explicit ([21e3e7a](https://github.com/gotson/komga/commits/21e3e7a))

**webui**
- remove unused imports ([92b721c](https://github.com/gotson/komga/commits/92b721c))

## 🛠  Build
**dependabot**
- pr format ([35f9196](https://github.com/gotson/komga/commits/35f9196))

**komga**
- fix epub tests ([f3a0a32](https://github.com/gotson/komga/commits/f3a0a32))

**tests**
- only check conveyor modules on push ([06d0d4a](https://github.com/gotson/komga/commits/06d0d4a))

**webui**
- use node 18 ([dedb01f](https://github.com/gotson/komga/commits/dedb01f))

## 📝 Documentation

- update readme description ([c93760b](https://github.com/gotson/komga/commits/c93760b))

## 🌐 Translation

- translated using Weblate (Chinese (Simplified)) ([6b5eba1](https://github.com/gotson/komga/commits/6b5eba1))
- translated using Weblate (French) ([2bf2df9](https://github.com/gotson/komga/commits/2bf2df9))
- translated using Weblate (Japanese) ([d199684](https://github.com/gotson/komga/commits/d199684))

## ⚙️ Dependencies
**ci**
- bump dessant/lock-threads from 4 to 5 ([9db0f07](https://github.com/gotson/komga/commits/9db0f07))
- bump hydraulic-software/conveyor from 12.0 to 12.1 ([82e5887](https://github.com/gotson/komga/commits/82e5887))

**webui**
- bump axios from 1.5.0 to 1.6.0 in /komga-webui ([aa65180](https://github.com/gotson/komga/commits/aa65180))

# [1.7.2](https://github.com/gotson/komga/compare/1.7.1...1.7.2) (2023-11-09)
## 🐛 Fixes
**komga**
- check temp directory exists and is writable on startup ([4999edd](https://github.com/gotson/komga/commits/4999edd)), closes [#1283](https://github.com/gotson/komga/issues/1283)
- task FixThumbnailsWithoutMetadata could not fix sidecar covers ([9d2d007](https://github.com/gotson/komga/commits/9d2d007)), closes [#1287](https://github.com/gotson/komga/issues/1287)

## 🔄️ Changes
**komga**
- ktlint format ([ea52a5e](https://github.com/gotson/komga/commits/ea52a5e))

## 🛠  Build
**conveyor**
- remove space in windows installer name ([d51fdeb](https://github.com/gotson/komga/commits/d51fdeb))
- adjust JDK module list ([74d04d0](https://github.com/gotson/komga/commits/74d04d0))
- bump conveyor from 11.4 to 12.0 ([e30a93b](https://github.com/gotson/komga/commits/e30a93b))
- change windows installer name ([2c87700](https://github.com/gotson/komga/commits/2c87700))
- hard-code module list ([5911431](https://github.com/gotson/komga/commits/5911431))
- fix config file ([b80de03](https://github.com/gotson/komga/commits/b80de03))
- use the Apple notarization API ([0704e9f](https://github.com/gotson/komga/commits/0704e9f))

**jreleaser**
- add release introduction to the release notes ([d52ba95](https://github.com/gotson/komga/commits/d52ba95))

**release**
- upload conveyor logs ([e631313](https://github.com/gotson/komga/commits/e631313))
- fix secret decoding ([0995c26](https://github.com/gotson/komga/commits/0995c26))
- create secret folder before decoding ([4ddd51e](https://github.com/gotson/komga/commits/4ddd51e))

**tests**
- use conveyor detect config file ([0f6b7cc](https://github.com/gotson/komga/commits/0f6b7cc))
- add missing config for conveyor ([39825e4](https://github.com/gotson/komga/commits/39825e4))
- build jdk module list and compare with historical ([33e10f5](https://github.com/gotson/komga/commits/33e10f5))

## 🌐 Translation

- translated using Weblate (Chinese (Simplified)) ([091015e](https://github.com/gotson/komga/commits/091015e))
- translated using Weblate (Finnish) ([44a8db0](https://github.com/gotson/komga/commits/44a8db0))

## ⚙️ Dependencies
**komga**
- bump nightmonkeys imageio from 0.6.1 to 0.6.2 ([25eb6c0](https://github.com/gotson/komga/commits/25eb6c0)), closes [#1289](https://github.com/gotson/komga/issues/1289)

# [1.7.1](https://github.com/gotson/komga/compare/1.7.0...1.7.1) (2023-11-02)
## 🐛 Fixes
**docker**
- install gpg-agent ([895c59c](https://github.com/gotson/komga/commits/895c59c))
- add missing locales ([1af13e6](https://github.com/gotson/komga/commits/1af13e6))

**komga**
- thumbnail metadata fixer cannot open UNC path ([879366b](https://github.com/gotson/komga/commits/879366b)), closes [#1275](https://github.com/gotson/komga/issues/1275)

## 🔄️ Changes
**docker**
- remove --no-install-recommends ([d9f16ed](https://github.com/gotson/komga/commits/d9f16ed))

**unscoped**
- use property assignment instead of set ([01b96fd](https://github.com/gotson/komga/commits/01b96fd))

## 🛠  Build
**release**
- fix version computation with new tag pattern ([a65a7f1](https://github.com/gotson/komga/commits/a65a7f1))
- add 'current' parameter for bump type ([bec9f50](https://github.com/gotson/komga/commits/bec9f50))

**unscoped**
- use property assignment instead of set ([0e43eb0](https://github.com/gotson/komga/commits/0e43eb0))
- bump gradle from 8.3 to 8.4 ([5f36271](https://github.com/gotson/komga/commits/5f36271))

## 🌐 Translation

- translated using Weblate (Chinese (Simplified)) ([e3172b6](https://github.com/gotson/komga/commits/e3172b6))
- translated using Weblate (Spanish) ([7629dcc](https://github.com/gotson/komga/commits/7629dcc))
- translated using Weblate (Czech) ([1c5796d](https://github.com/gotson/komga/commits/1c5796d))

## ⚙️ Dependencies
**ci**
- bump actions/setup-node from 3 to 4 ([142b44b](https://github.com/gotson/komga/commits/142b44b))

**komga**
- bump jreleaser from 1.8.0 to 1.9.0 ([7935f1c](https://github.com/gotson/komga/commits/7935f1c))
- bump nightmonkeys.imageio from 0.6.0 to 0.6.1 ([9570101](https://github.com/gotson/komga/commits/9570101))
- bump tika-core from 2.9.0 to 2.9.1 ([8480806](https://github.com/gotson/komga/commits/8480806))
- bump springdoc.openapi-gradle-plugin from 1.7.0 to 1.8.0 ([602fd4e](https://github.com/gotson/komga/commits/602fd4e))
- bump twelvemonkeys.imageio from 3.9.4 to 3.10.0 ([894f9f1](https://github.com/gotson/komga/commits/894f9f1))
- bump sqlite-jdbc from 3.43.2.1 to 3.43.2.2 ([af0023b](https://github.com/gotson/komga/commits/af0023b))
- bump jsoup from 1.16.1 to 1.16.2 ([647fb40](https://github.com/gotson/komga/commits/647fb40))
- bump commons-io from 2.14.0 to 2.15.0 ([3f56e19](https://github.com/gotson/komga/commits/3f56e19))
- bump icu4j from 73.2 to 74.1 ([30a7408](https://github.com/gotson/komga/commits/30a7408))

**komga-tray**
- bump jetbrains compose from 1.5.2 to 1.5.10 ([9b195bf](https://github.com/gotson/komga/commits/9b195bf))

# [1.7.0](https://github.com/gotson/komga/compare/1.6.4...1.7.0) (2023-10-30)
## 🚀 Features
**api**
- configure server port and context path ([3f39037](https://github.com/gotson/komga/commits/3f39037)), closes [#1264](https://github.com/gotson/komga/issues/1264)

**komga-tray**
- add libjxl for Windows ([8171cb8](https://github.com/gotson/komga/commits/8171cb8))

**webui**
- add server port and context path to the server settings screen ([4196f08](https://github.com/gotson/komga/commits/4196f08)), closes [#1264](https://github.com/gotson/komga/issues/1264)

## 🐛 Fixes
**komga-tray**
- use workaround to open filesystem on Windows ([6059b85](https://github.com/gotson/komga/commits/6059b85))

## 🛠  Build
**docker**
- add support for AVIF on amd64/arm64 images ([a92db64](https://github.com/gotson/komga/commits/a92db64))

**komga-tray**
- add native image libraries for macOS ([d8dea84](https://github.com/gotson/komga/commits/d8dea84))
- use JDK 21 toolchain ([69eb24d](https://github.com/gotson/komga/commits/69eb24d))

**unscoped**
- change release tag format to enable delta updates in Conveyor ([c4cd4bd](https://github.com/gotson/komga/commits/c4cd4bd))
- install JDK 21 ([0267ba8](https://github.com/gotson/komga/commits/0267ba8))

## 📝 Documentation

- update DEVELOPING.md for necessary JDK ([5a66f2a](https://github.com/gotson/komga/commits/5a66f2a))

## 🌐 Translation

- translated using Weblate (Chinese (Traditional)) ([56b782e](https://github.com/gotson/komga/commits/56b782e))
- translated using Weblate (Finnish) ([d8fe2d7](https://github.com/gotson/komga/commits/d8fe2d7))
- translated using Weblate (Turkish) ([a519261](https://github.com/gotson/komga/commits/a519261))

## ⚙️ Dependencies
**komga**
- add imageio-heif:0.6.0 ([07cf63a](https://github.com/gotson/komga/commits/07cf63a)), closes [#942](https://github.com/gotson/komga/issues/942)

# [1.6.4](https://github.com/gotson/komga/compare/v1.6.3...v1.6.4) (2023-10-26)
## 🐛 Fixes
**komga**
- change the default value of TASK_POOL_SIZE to 1 ([7ebce00](https://github.com/gotson/komga/commits/7ebce00))
- FixThumbnailsWithoutMetadata could not rerun itself ([493a33b](https://github.com/gotson/komga/commits/493a33b))

**opds**
- disable content negociation in the pages API ([084997f](https://github.com/gotson/komga/commits/084997f)), closes [#1262](https://github.com/gotson/komga/issues/1262)

## 🏎 Perf
**komga**
- add some db indices to help with FixThumbnailsWithoutMetadata task ([517f035](https://github.com/gotson/komga/commits/517f035))

## 🛠  Build
**release**
- simplify reruns ([8e1e699](https://github.com/gotson/komga/commits/8e1e699))
- attempt to avoid worker out of space issue ([bcda81a](https://github.com/gotson/komga/commits/bcda81a))

## 🌐 Translation

- translated using Weblate (Chinese (Simplified)) ([2cd95f1](https://github.com/gotson/komga/commits/2cd95f1))
- translated using Weblate (Spanish) ([b9842f6](https://github.com/gotson/komga/commits/b9842f6))
- translated using Weblate (Russian) ([80dc7a9](https://github.com/gotson/komga/commits/80dc7a9))

## ⚙️ Dependencies
**komga**
- bump sqlite-jdbc from 3.42.0.0 to 3.43.2.1 ([c90b472](https://github.com/gotson/komga/commits/c90b472))

# [1.6.3](https://github.com/gotson/komga/compare/v1.6.2...v1.6.3) (2023-10-20)

# [1.6.2](https://github.com/gotson/komga/compare/v1.6.1...v1.6.2) (2023-10-20)
## 🚀 Features
**api**
- configure number of task processing threads ([9ef319b](https://github.com/gotson/komga/commits/9ef319b))

**webui**
- configure number of task processing threads from server settings ([a837988](https://github.com/gotson/komga/commits/a837988))

## 🐛 Fixes
**api**
- prevent setting series poster for oneshots ([d8af496](https://github.com/gotson/komga/commits/d8af496))

**komga**
- sometimes tasks would not be seen as available ([528eddb](https://github.com/gotson/komga/commits/528eddb))
- do not cache PDF documents ([6d5d695](https://github.com/gotson/komga/commits/6d5d695))

**webreader**
- hide setting series poster menu for oneshots ([ff06e06](https://github.com/gotson/komga/commits/ff06e06))

## 🔄️ Changes
**webui**
- reorder server settings components ([67ee547](https://github.com/gotson/komga/commits/67ee547))

## 🛠  Build
**dependabot**
- attempt to fix dependabot config ([0636790](https://github.com/gotson/komga/commits/0636790))

**unscoped**
- cleanup conveyor output to reduce disk space ([4f4cad2](https://github.com/gotson/komga/commits/4f4cad2))

## ⚙️ Dependencies
**webui-dev**
- bump @babel/traverse from 7.22.17 to 7.23.2 ([3467d90](https://github.com/gotson/komga/commits/3467d90))

# [1.6.1](https://github.com/gotson/komga/compare/v1.6.0...v1.6.1) (2023-10-18)
## 🐛 Fixes
**komga-tray**
- missing jdk modules preventing SSL connections ([0d9184e](https://github.com/gotson/komga/commits/0d9184e))
- some tasks would not be run on startup ([a47eddb](https://github.com/gotson/komga/commits/a47eddb))

## 🔄️ Changes
**komga-tray**
- add more logs around open explorer action ([db06919](https://github.com/gotson/komga/commits/db06919))

# [1.6.0](https://github.com/gotson/komga/compare/v1.5.1...v1.6.0) (2023-10-18)
## 🚀 Features
**api**
- new endpoint to regenerate thumbnails conditionally ([796745a](https://github.com/gotson/komga/commits/796745a))
- add thumbnail size server setting ([5fa789b](https://github.com/gotson/komga/commits/5fa789b)), closes [#861](https://github.com/gotson/komga/issues/861) [#1031](https://github.com/gotson/komga/issues/1031)
- add thumbnail metadata ([5b6e9e3](https://github.com/gotson/komga/commits/5b6e9e3))

**komga**
- generate mosaic thumbnails with the quality configured in server settings ([b19e799](https://github.com/gotson/komga/commits/b19e799))
- prevent image resizing scale up ([84fe3b7](https://github.com/gotson/komga/commits/84fe3b7))
- store filesize, media type and dimensions for thumbnails ([bb13c0c](https://github.com/gotson/komga/commits/bb13c0c))

**webui**
- dialog to regenerate thumbnails if size has changed ([ac1c824](https://github.com/gotson/komga/commits/ac1c824))
- configure thumbnail size from server settings ([f0b1abe](https://github.com/gotson/komga/commits/f0b1abe))
- display thumbnail metadata in edit poster dialog ([64fddbd](https://github.com/gotson/komga/commits/64fddbd))

## 🐛 Fixes
**komga**
- incorrect counts when getting thumbnails without metadata ([553016c](https://github.com/gotson/komga/commits/553016c))

## 🏎 Perf
**komga**
- submit tasks in bulk ([5fe4e3e](https://github.com/gotson/komga/commits/5fe4e3e))
- replace Artemis for background task handling ([b7aa120](https://github.com/gotson/komga/commits/b7aa120)), closes [#1038](https://github.com/gotson/komga/issues/1038)
- better handling of Lucene index when reading and updating ([487b439](https://github.com/gotson/komga/commits/487b439))

## 🔄️ Changes
**komga**
- remove Serializable ([6e6f8b7](https://github.com/gotson/komga/commits/6e6f8b7))
- simplify some background tasks ([77ccb9e](https://github.com/gotson/komga/commits/77ccb9e))
- use extension function ([9abb261](https://github.com/gotson/komga/commits/9abb261))
- add support for multiple data sources ([cafe669](https://github.com/gotson/komga/commits/cafe669))
- convert Task.uniqueId to a property ([ae32b85](https://github.com/gotson/komga/commits/ae32b85))
- add functions to send multiple tasks in TaskEmitter ([12a786b](https://github.com/gotson/komga/commits/12a786b))
- replace Artemis with Spring events for domain event publishing ([545a314](https://github.com/gotson/komga/commits/545a314))
- remove empty file ([da184c8](https://github.com/gotson/komga/commits/da184c8))
- rename task ([266f692](https://github.com/gotson/komga/commits/266f692))
- add backing fields for server settings ([e35d468](https://github.com/gotson/komga/commits/e35d468))

**opds**
- remove chunky specific code as chunky doesn't work anymore with Komga ([9d0a533](https://github.com/gotson/komga/commits/9d0a533))

**unscoped**
- ktlint ([7057c28](https://github.com/gotson/komga/commits/7057c28))

## 🛠  Build
**changelog**
- group dependencies in separate category ([0b3748a](https://github.com/gotson/komga/commits/0b3748a))

**komga**
- fix gradle task dependencies ([1fcef0e](https://github.com/gotson/komga/commits/1fcef0e))
- disable AuthenticationActivityCleanupController during tests ([572a176](https://github.com/gotson/komga/commits/572a176))
- fix tests following previous changes ([72cf68b](https://github.com/gotson/komga/commits/72cf68b))
- coding rule test for no field injection ([e7fcf23](https://github.com/gotson/komga/commits/e7fcf23))

## 📝 Documentation

- fix faq link in issue report ([31c0bb0](https://github.com/gotson/komga/commits/31c0bb0))

## 🌐 Translation

- translated using Weblate (Chinese (Simplified)) ([e032f94](https://github.com/gotson/komga/commits/e032f94))
- translated using Weblate (Spanish) ([4caaa34](https://github.com/gotson/komga/commits/4caaa34))

# [1.5.1](https://github.com/gotson/komga/compare/v1.5.0...v1.5.1) (2023-10-05)
## 🐛 Fixes
**sse**
- sse connections would not be closed properly ([8ed5726](https://github.com/gotson/komga/commits/8ed5726))

**webui**
- add missing hint on remember-me regenerate checkbox ([18bffa9](https://github.com/gotson/komga/commits/18bffa9))

## 🛠  Build
**komga-deps**
- bump ben-manes version from 0.46.0 to 0.48.0 ([37ec436](https://github.com/gotson/komga/commits/37ec436))
- bump jreleaser from 1.7.0 to 1.8.0 ([72d848d](https://github.com/gotson/komga/commits/72d848d))

**unscoped**
- split ci into tests and release workflows ([c6128ee](https://github.com/gotson/komga/commits/c6128ee))
- add inputs to replay parts of the release workflow when it fails ([a8b37c7](https://github.com/gotson/komga/commits/a8b37c7))
- bump conveyor from 11.3 to 11.4 ([d4f23b8](https://github.com/gotson/komga/commits/d4f23b8))
- fix step for ms-store submission ([dd2edba](https://github.com/gotson/komga/commits/dd2edba))

# [1.5.0](https://github.com/gotson/komga/compare/v1.4.0...v1.5.0) (2023-10-04)
## 🚀 Features
**api**
- move some configuration keys to API and database ([48e9d32](https://github.com/gotson/komga/commits/48e9d32)), closes [#815](https://github.com/gotson/komga/issues/815)
- configure scan directory exclusions at library level ([b518473](https://github.com/gotson/komga/commits/b518473))
- configure scan interval, startup, and file types at library level ([e068485](https://github.com/gotson/komga/commits/e068485)), closes [#877](https://github.com/gotson/komga/issues/877)
- add limited content negotiation for getBookPage API ([a2761f1](https://github.com/gotson/komga/commits/a2761f1)), closes [#1219](https://github.com/gotson/komga/issues/1219)

**desktop**
- add menu items to locate log file and configuration directory ([80b5a33](https://github.com/gotson/komga/commits/80b5a33))

**komga**
- support webp cover sidecars ([4b435be](https://github.com/gotson/komga/commits/4b435be)), closes [#1238](https://github.com/gotson/komga/issues/1238)
- replace configuration komga.session-timeout with the standard server.servlet.session.timeout ([0364621](https://github.com/gotson/komga/commits/0364621))

**webreader**
- detect browser support for AVIF ([b5236c6](https://github.com/gotson/komga/commits/b5236c6))

**webui**
- unread filter only shows unread elements ([8ff6694](https://github.com/gotson/komga/commits/8ff6694)), closes [#1234](https://github.com/gotson/komga/issues/1234)
- add server settings configuration screen, add remember-me checkbox for login ([505b54c](https://github.com/gotson/komga/commits/505b54c))
- add scan directory exclusions to the library edit dialog ([364df50](https://github.com/gotson/komga/commits/364df50))
- add new scanner options to the library edit dialog ([c6c7c89](https://github.com/gotson/komga/commits/c6c7c89))

## 🐛 Fixes
**api**
- missing validation ([6270986](https://github.com/gotson/komga/commits/6270986))
- unknown API endpoints now return 404 instead of redirecting to index ([7315df5](https://github.com/gotson/komga/commits/7315df5))

**desktop-windows**
- configure msix manifest to use transparent icon background ([a249221](https://github.com/gotson/komga/commits/a249221))

**webui**
- show number of displayed elements instead of total when browsing collection/readlist ([d117fc1](https://github.com/gotson/komga/commits/d117fc1))
- manifest.json would have incorrect path when running in a subfolder ([854098c](https://github.com/gotson/komga/commits/854098c))

## 🏎 Perf
**komga**
- only rescan library on update when relevant ([b48c113](https://github.com/gotson/komga/commits/b48c113))

## 🔄️ Changes
**komga**
- replace gradle deprecations ([c057d09](https://github.com/gotson/komga/commits/c057d09))

**webui**
- remove commented code ([7c20909](https://github.com/gotson/komga/commits/7c20909))
- rename Metrics component ([b9629ed](https://github.com/gotson/komga/commits/b9629ed))

**unscoped**
- relocate ResourceNotFoundController ([2d404d3](https://github.com/gotson/komga/commits/2d404d3))

## 🛠  Build
**deps**
- bump hydraulic-software/conveyor from 11.1 to 11.2 ([029dea9](https://github.com/gotson/komga/commits/029dea9))
- bump docker/setup-buildx-action from 2 to 3 ([28f46d4](https://github.com/gotson/komga/commits/28f46d4))
- bump docker/setup-qemu-action from 2 to 3 ([c923ff8](https://github.com/gotson/komga/commits/c923ff8))
- bump docker/login-action from 2 to 3 ([b92ad63](https://github.com/gotson/komga/commits/b92ad63))

**desktop**
- bump hydraulic-software/conveyor from 11.2 to 11.3 ([3736490](https://github.com/gotson/komga/commits/3736490)), closes [#1220](https://github.com/gotson/komga/issues/1220)

**docker**
- bump libjxl from 0.8.1 to 0.8.2 ([a519cd0](https://github.com/gotson/komga/commits/a519cd0))

**komga-deps**
- bump dependencies ([7168af4](https://github.com/gotson/komga/commits/7168af4))
- bump springdoc from 2.1.0 to 2.2.0 ([165ba2a](https://github.com/gotson/komga/commits/165ba2a))
- bump springdoc gradle plugin from 1.6.0 to 1.7.0 ([5705531](https://github.com/gotson/komga/commits/5705531))
- bump spring boot from 3.1.1 to 3.1.4 ([1372aad](https://github.com/gotson/komga/commits/1372aad))

**komga-tray-deps**
- bump jetbrains compose gradle plugin from 1.4.3 to 1.5.2 ([d429e03](https://github.com/gotson/komga/commits/d429e03))
- bump conveyor gradle plugin from 1.5 to 1.6 ([75a2f59](https://github.com/gotson/komga/commits/75a2f59))

**webui**
- increase memory for ForkTsCheckerWebpackPlugin ([01a00a7](https://github.com/gotson/komga/commits/01a00a7))
- fix typescript config and issues after upgrading ([d19c9d3](https://github.com/gotson/komga/commits/d19c9d3))
- normalize eslint config for unit tests ([e7374e0](https://github.com/gotson/komga/commits/e7374e0))
- update browserlist ([40b6dfc](https://github.com/gotson/komga/commits/40b6dfc))
- upgrade vue cli to v5 ([9ef46b3](https://github.com/gotson/komga/commits/9ef46b3))

**webui-deps**
- upgrade typescript ([0c0f004](https://github.com/gotson/komga/commits/0c0f004))
- fix resolve conflicts ([1b1a423](https://github.com/gotson/komga/commits/1b1a423))
- bump deps ([110e5a7](https://github.com/gotson/komga/commits/110e5a7))
- bump axios to 1.5.0 ([b1a6ab6](https://github.com/gotson/komga/commits/b1a6ab6))
- bump @saekitominaga/isbn-verify to 2.0.1 ([63e0598](https://github.com/gotson/komga/commits/63e0598))

**unscoped**
- bump gradle from 8.1.1 to 8.3 ([00278e9](https://github.com/gotson/komga/commits/00278e9))
- increase node memory ([90d1a8e](https://github.com/gotson/komga/commits/90d1a8e))
- add missing environment variable ([5f1436a](https://github.com/gotson/komga/commits/5f1436a))
- remove unused config ([265b543](https://github.com/gotson/komga/commits/265b543))
- rework release steps ([a31e3da](https://github.com/gotson/komga/commits/a31e3da))
- fix release commit step ordering ([332cb2c](https://github.com/gotson/komga/commits/332cb2c))
- remove cache directive from setup-java ([de6c17a](https://github.com/gotson/komga/commits/de6c17a))
- setup msstore update ([88983a8](https://github.com/gotson/komga/commits/88983a8))
- fix jreleaser output job ([ed97553](https://github.com/gotson/komga/commits/ed97553))

## 📝 Documentation

- update README ([9e57596](https://github.com/gotson/komga/commits/9e57596))
- update DEVELOPING.md ([ceef94a](https://github.com/gotson/komga/commits/ceef94a))
- update CHANGELOG.md ([a8274d3](https://github.com/gotson/komga/commits/a8274d3))

## 🌐 Translation

- translated using Weblate (Chinese (Simplified)) ([e1a582c](https://github.com/gotson/komga/commits/e1a582c))
- translated using Weblate (Chinese (Traditional)) ([f56e760](https://github.com/gotson/komga/commits/f56e760))
- translated using Weblate (Spanish) ([6693c0d](https://github.com/gotson/komga/commits/6693c0d))
- translated using Weblate (Japanese) ([a91637b](https://github.com/gotson/komga/commits/a91637b))
- translated using Weblate (German) ([8412003](https://github.com/gotson/komga/commits/8412003))
- translated using Weblate (Chinese (Simplified)) ([229d1aa](https://github.com/gotson/komga/commits/229d1aa))
- translated using Weblate (Finnish) ([33d86ad](https://github.com/gotson/komga/commits/33d86ad))
- translated using Weblate (Swedish) ([749cc7e](https://github.com/gotson/komga/commits/749cc7e))
- translated using Weblate (French) ([97a243e](https://github.com/gotson/komga/commits/97a243e))
- translated using Weblate (Polish) ([6e16102](https://github.com/gotson/komga/commits/6e16102))
- translated using Weblate (Japanese) ([4a26b82](https://github.com/gotson/komga/commits/4a26b82))

# [1.4.0](https://github.com/gotson/komga/compare/v1.3.1...v1.4.0) (2023-09-11)
## 🚀 Features
**api**
- new endpoint to get pages in raw format ([2c33b3e](https://github.com/gotson/komga/commits/2c33b3e))

**desktop**
- new desktop application project ([4da12ae](https://github.com/gotson/komga/commits/4da12ae))

**opds**
- add alternate link to opds2 ([974bf30](https://github.com/gotson/komga/commits/974bf30))
- links with absolute url ([f751e9b](https://github.com/gotson/komga/commits/f751e9b))
- opds v2 support ([d1cb58b](https://github.com/gotson/komga/commits/d1cb58b))

**opds2**
- generate PDF profile webpub manifest ([7205b13](https://github.com/gotson/komga/commits/7205b13))

## 🐛 Fixes
**opds2**
- fix some incorrect urls ([15da160](https://github.com/gotson/komga/commits/15da160))

**webui**
- duplicate unavailable indicator on One-Shot browse view ([afe3233](https://github.com/gotson/komga/commits/afe3233))

**unscoped**
- ensure database parent directory is always created ([fdd6fff](https://github.com/gotson/komga/commits/fdd6fff))
- strip accents from series sort title in metadata providers ([e568dd0](https://github.com/gotson/komga/commits/e568dd0)), closes [#1199](https://github.com/gotson/komga/issues/1199)
- use Epub cover for thumbnail ([8bdc4d8](https://github.com/gotson/komga/commits/8bdc4d8))
- better handling of oneshot file deletion ([9b650cc](https://github.com/gotson/komga/commits/9b650cc)), closes [#1192](https://github.com/gotson/komga/issues/1192)

## 🔄️ Changes
**api**
- provide thumbnails in resources instead of images for webpub manifest ([e50591f](https://github.com/gotson/komga/commits/e50591f))

**opds2**
- rework publication feed ([37e7d55](https://github.com/gotson/komga/commits/37e7d55))

**webpub**
- cleanup webpub manifest acquisition links ([32ef331](https://github.com/gotson/komga/commits/32ef331))

**unscoped**
- ktlint format ([92fce54](https://github.com/gotson/komga/commits/92fce54))
- remove unused property number in BookPageContent ([cb5177d](https://github.com/gotson/komga/commits/cb5177d))

## 🛠  Build
**deps**
- bump actions/checkout from 3 to 4 ([7544b8f](https://github.com/gotson/komga/commits/7544b8f))

**desktop**
- use conveyor to build the desktop apps ([96020fd](https://github.com/gotson/komga/commits/96020fd))

**komga**
- fix gradle task ordering ([8015965](https://github.com/gotson/komga/commits/8015965))

**unscoped**
- remove jreleaser dryrun flag ([5ea40c5](https://github.com/gotson/komga/commits/5ea40c5))
- bump conveyor action to 11.1 ([8d29f4d](https://github.com/gotson/komga/commits/8d29f4d))
- move jreleaser to root project ([9222d09](https://github.com/gotson/komga/commits/9222d09))
- align jooq versions ([e2eff20](https://github.com/gotson/komga/commits/e2eff20))
- add Conveyor ([903993e](https://github.com/gotson/komga/commits/903993e))

## 📝 Documentation

- add privacy policy ([099276c](https://github.com/gotson/komga/commits/099276c))

# [1.3.1](https://github.com/gotson/komga/compare/v1.3.0...v1.3.1) (2023-08-08)
## 🐛 Fixes
**docker**
- change installation method for libjxl ([ad1d1da](https://github.com/gotson/komga/commits/ad1d1da))

**unscoped**
- series picker does not return any series in CBL import screen ([f8e65b0](https://github.com/gotson/komga/commits/f8e65b0)), closes [#1186](https://github.com/gotson/komga/issues/1186)

## 🌐 Translation

- translated using Weblate (Swedish) ([d47ae13](https://github.com/gotson/komga/commits/d47ae13))

# [1.3.0](https://github.com/gotson/komga/compare/v1.2.1...v1.3.0) (2023-08-03)
## 🚀 Features
**api**
- add support for oneshots directory in libraries ([739eeca](https://github.com/gotson/komga/commits/739eeca))

**cli**
- add command line interface commands ([f6cc958](https://github.com/gotson/komga/commits/f6cc958)), closes [#1175](https://github.com/gotson/komga/issues/1175)

**docker**
- enable jpeg-xl support for linux/amd64 ([466e980](https://github.com/gotson/komga/commits/466e980))

**webui**
- oneshots handling ([2b238cc](https://github.com/gotson/komga/commits/2b238cc))

## 🐛 Fixes
**api**
- http header containing non-ascii character gets stripped ([419cb5a](https://github.com/gotson/komga/commits/419cb5a)), closes [#1161](https://github.com/gotson/komga/issues/1161) [#1176](https://github.com/gotson/komga/issues/1176)

**webui**
- invalid series language code can prevent some views to render ([8a03c50](https://github.com/gotson/komga/commits/8a03c50)), closes [#1173](https://github.com/gotson/komga/issues/1173)
- better filenames when downloading book pages ([19abdce](https://github.com/gotson/komga/commits/19abdce))

**unscoped**
- normalize language codes ([1a2acac](https://github.com/gotson/komga/commits/1a2acac)), closes [#1173](https://github.com/gotson/komga/issues/1173)

## 🏎 Perf
**webui**
- remove redundant API call in EditSeriesDialog ([11f8aaf](https://github.com/gotson/komga/commits/11f8aaf))

## 🔄️ Changes
**webui**
- pass book or series id to reusable dialog instead of full DTO ([39e7ae9](https://github.com/gotson/komga/commits/39e7ae9))

**unscoped**
- ktlint format ([c2224f1](https://github.com/gotson/komga/commits/c2224f1))
- harden BCP47 functions ([82dd23a](https://github.com/gotson/komga/commits/82dd23a))

## 🛠  Build

- fix tests for oneshots ([5a8f686](https://github.com/gotson/komga/commits/5a8f686))
- add bestbefore to prevent shipping deprecated code on major versions ([7f1fded](https://github.com/gotson/komga/commits/7f1fded))

## 📝 Documentation

- update website url in issue templates ([f7b6e0d](https://github.com/gotson/komga/commits/f7b6e0d)), closes [#1178](https://github.com/gotson/komga/issues/1178)

## 🌐 Translation

- translated using Weblate (Indonesian) ([de01b70](https://github.com/gotson/komga/commits/de01b70))
- translated using Weblate (Portuguese (Brazil)) ([aa92b69](https://github.com/gotson/komga/commits/aa92b69))
- translated using Weblate (Polish) ([33d5a56](https://github.com/gotson/komga/commits/33d5a56))
- translated using Weblate (Indonesian) ([cb4ce72](https://github.com/gotson/komga/commits/cb4ce72))
- translated using Weblate (Japanese) ([9a6a75c](https://github.com/gotson/komga/commits/9a6a75c))
- translated using Weblate (Korean) ([7f71355](https://github.com/gotson/komga/commits/7f71355))
- translated using Weblate (Finnish) ([e3f66d3](https://github.com/gotson/komga/commits/e3f66d3))
- translated using Weblate (Spanish) ([4ce904e](https://github.com/gotson/komga/commits/4ce904e))
- translated using Weblate (German) ([8920415](https://github.com/gotson/komga/commits/8920415))

# [1.2.1](https://github.com/gotson/komga/compare/v1.2.0...v1.2.1) (2023-07-17)
## 🐛 Fixes
**api**
- show values for actuator /env and /config-props endpoints ([e6eaf2f](https://github.com/gotson/komga/commits/e6eaf2f))

**webui**
- metrics pie charts show library name in tooltip ([318a444](https://github.com/gotson/komga/commits/318a444)), closes [#1159](https://github.com/gotson/komga/issues/1159)

## 📝 Documentation

- fix link in DOCKERHUB.md ([5e1755c](https://github.com/gotson/komga/commits/5e1755c))

## 🌐 Translation

- translated using Weblate (Indonesian) ([ba6b723](https://github.com/gotson/komga/commits/ba6b723))
- translated using Weblate (Chinese (Simplified)) ([cd1ac38](https://github.com/gotson/komga/commits/cd1ac38))
- translated using Weblate (Chinese (Traditional)) ([c010b33](https://github.com/gotson/komga/commits/c010b33))
- translated using Weblate (Tamil) ([bd8b819](https://github.com/gotson/komga/commits/bd8b819))
- translated using Weblate (Swedish) ([42230ed](https://github.com/gotson/komga/commits/42230ed))
- translated using Weblate (Spanish) ([e0159b6](https://github.com/gotson/komga/commits/e0159b6))

# [1.2.0](https://github.com/gotson/komga/compare/v1.1.0...v1.2.0) (2023-07-13)
## 🚀 Features
**api**
- add sharing_label criteria to full text search for series ([040556e](https://github.com/gotson/komga/commits/040556e)), closes [#1146](https://github.com/gotson/komga/issues/1146)
- search series by sharing label ([7a21fe0](https://github.com/gotson/komga/commits/7a21fe0)), closes [#1146](https://github.com/gotson/komga/issues/1146)

**webui**
- add buttons for auto and manual deletion of remaining page hashes ([e9135fb](https://github.com/gotson/komga/commits/e9135fb)), closes [#1147](https://github.com/gotson/komga/issues/1147)
- add sharing label to filter panel ([737bf1b](https://github.com/gotson/komga/commits/737bf1b)), closes [#1146](https://github.com/gotson/komga/issues/1146)

**unscoped**
- display komga.org website announcements within the app ([72c1e8d](https://github.com/gotson/komga/commits/72c1e8d)), closes [#1149](https://github.com/gotson/komga/issues/1149)

## 🐛 Fixes
**api**
- missing metrics if library name contains specific characters ([5ec7fa1](https://github.com/gotson/komga/commits/5ec7fa1)), closes [#1156](https://github.com/gotson/komga/issues/1156)

**webui**
- scan all libraries from Server Settings does not work ([a2f0c3d](https://github.com/gotson/komga/commits/a2f0c3d)), closes [#1155](https://github.com/gotson/komga/issues/1155)
- better button alignment on smaller screens on login view ([223aea5](https://github.com/gotson/komga/commits/223aea5))
- make logo on login view the same size even if server is unclaimed ([806d2b6](https://github.com/gotson/komga/commits/806d2b6))
- startup logo fits small screens ([ecaf8d0](https://github.com/gotson/komga/commits/ecaf8d0))

## 🏎 Perf
**webui**
- load metrics with promises instead of await ([66dd1c2](https://github.com/gotson/komga/commits/66dd1c2))

## 🔄️ Changes
**webui**
- missing null safety ([5438444](https://github.com/gotson/komga/commits/5438444))
- add missing uppercase class on some elements ([ea0a85c](https://github.com/gotson/komga/commits/ea0a85c))
- convert dates using axios interceptor ([58c8187](https://github.com/gotson/komga/commits/58c8187))

## 🛠  Build

- remove redundant annotation ([438c40d](https://github.com/gotson/komga/commits/438c40d))

## 🌐 Translation

- translated using Weblate (Chinese (Simplified)) ([447f9c1](https://github.com/gotson/komga/commits/447f9c1))
- translated using Weblate (Italian) ([a442439](https://github.com/gotson/komga/commits/a442439))
- translated using Weblate (Hebrew) ([e4bf708](https://github.com/gotson/komga/commits/e4bf708))
- translated using Weblate (Bulgarian) ([5d32ef0](https://github.com/gotson/komga/commits/5d32ef0))
- translated using Weblate (Swedish) ([b751604](https://github.com/gotson/komga/commits/b751604))
- translated using Weblate (Spanish) ([d7ba805](https://github.com/gotson/komga/commits/d7ba805))

# [1.1.0](https://github.com/gotson/komga/compare/v1.0.0...v1.1.0) (2023-06-29)
## 🚀 Features
**webui**
- add button to ignore all remaining duplicate pages at once ([da79569](https://github.com/gotson/komga/commits/da79569)), closes [#1136](https://github.com/gotson/komga/issues/1136)

**unscoped**
- library deep scan is now a parameter of the scan API ([63e3e7a](https://github.com/gotson/komga/commits/63e3e7a)), closes [#1137](https://github.com/gotson/komga/issues/1137)

## 🐛 Fixes
**webui**
- library action menu scan would not work properly ([0fef983](https://github.com/gotson/komga/commits/0fef983))
- disable match button on PageHashKnownCard if there's no matches ([f3a4319](https://github.com/gotson/komga/commits/f3a4319))
- edit books dialog would not open on some browsers ([308a068](https://github.com/gotson/komga/commits/308a068)), closes [#1139](https://github.com/gotson/komga/issues/1139)

# [1.0.0](https://github.com/gotson/komga/compare/v0.165.0...v1.0.0) (2023-06-28)
## 🚀 Features
**webui**
- book release date field is editable ([902f700](https://github.com/gotson/komga/commits/902f700)), closes [#1097](https://github.com/gotson/komga/issues/1097)

**unscoped**
- display matched series release year in reading list import view ([0f89cf3](https://github.com/gotson/komga/commits/0f89cf3)), closes [#1114](https://github.com/gotson/komga/issues/1114)
- add match count sort for known duplicate pages ([bf1903b](https://github.com/gotson/komga/commits/bf1903b)), closes [#825](https://github.com/gotson/komga/issues/825)
- identify duplicate pages by hash only ([2d95679](https://github.com/gotson/komga/commits/2d95679))
- use XXH128 for hashing ([4f8dee7](https://github.com/gotson/komga/commits/4f8dee7))
- 🚨 change default port from 8080 to 25600 ([9b519e4](https://github.com/gotson/komga/commits/9b519e4))

## 🐛 Fixes
**api**
- paging and sort could be incorrect when searching for books ([c4cdd7a](https://github.com/gotson/komga/commits/c4cdd7a))
- recently updated series endpoint would incorrectly return created series ([a876132](https://github.com/gotson/komga/commits/a876132))

**opds**
- provide correct thumbnails in full and small size ([4a3e394](https://github.com/gotson/komga/commits/4a3e394)), closes [#1099](https://github.com/gotson/komga/issues/1099)

**webui**
- apply chosen locale to the date picker dialog ([7b3534a](https://github.com/gotson/komga/commits/7b3534a)), closes [#1113](https://github.com/gotson/komga/issues/1113)

**unscoped**
- oauth2 login would not work ([3eaab0f](https://github.com/gotson/komga/commits/3eaab0f))
- comicInfo.xml SeriesGroup is now split by comma ([fb13529](https://github.com/gotson/komga/commits/fb13529)), closes [#1122](https://github.com/gotson/komga/issues/1122)
- mark books with missing page file size as outdated ([a03bda9](https://github.com/gotson/komga/commits/a03bda9))

## 🔄️ Changes

- make SeriesMetadataPatch#collections a Set ([85fb3fa](https://github.com/gotson/komga/commits/85fb3fa))
- replace deprecated methods in build.gradle.kts ([c70cab4](https://github.com/gotson/komga/commits/c70cab4))
- replace deprecations ([32cb52e](https://github.com/gotson/komga/commits/32cb52e))
- specify session creation policy explicitly ([c202614](https://github.com/gotson/komga/commits/c202614))
- replace HttpTrace by HttpExchange ([0112fcd](https://github.com/gotson/komga/commits/0112fcd))
- make property private ([9c0905e](https://github.com/gotson/komga/commits/9c0905e))
- 🚨 remove deprecated classes: */api/v1/users and /api/v1/series/{seriesId}/read-progress/tachiyomi have been removed* ([2a5ce25](https://github.com/gotson/komga/commits/2a5ce25))
- remove unnecessary brackets ([d4aa113](https://github.com/gotson/komga/commits/d4aa113))
- adapt SecurityConfiguration for Spring Security 6 ([cf7fb45](https://github.com/gotson/komga/commits/cf7fb45))
- replace deprecated configuration keys ([1b69cd0](https://github.com/gotson/komga/commits/1b69cd0))
- hide jooq tips on startup ([7a3c1d8](https://github.com/gotson/komga/commits/7a3c1d8))
- replace private property with method ([3e2d2cf](https://github.com/gotson/komga/commits/3e2d2cf))
- replace artemis deprecated usage ([50524b9](https://github.com/gotson/komga/commits/50524b9))
- replace javax imports by jakarta ([b88b4c4](https://github.com/gotson/komga/commits/b88b4c4))

## 🛠  Build
**dependabot**
- remove unused dependabot configuration for root folder ([432bbb8](https://github.com/gotson/komga/commits/432bbb8))

**deps**
- bump peter-evans/dockerhub-description from 3.4.1 to 3.4.2 ([7209667](https://github.com/gotson/komga/commits/7209667))
- bump lucene from 9.6.0 to 9.7.0 ([28c1f88](https://github.com/gotson/komga/commits/28c1f88))
- bump icu4j from 73.1 to 73.2 ([a27f91c](https://github.com/gotson/komga/commits/a27f91c))
- bump hawtio-springboot from 2.17.2 to 2.17.4 ([27c8270](https://github.com/gotson/komga/commits/27c8270))
- bump commons-io from 2.12.0 to 2.13.0 ([0308d82](https://github.com/gotson/komga/commits/0308d82))
- bump jooq plugin from 8.2 to 8.2.1 ([f8a018c](https://github.com/gotson/komga/commits/f8a018c))
- bump Spring Boot from 3.1.0 to 3.1.1 ([1485f5f](https://github.com/gotson/komga/commits/1485f5f))
- bump Kotlin from 1.8.21 to 1.8.22 ([c5cc9a2](https://github.com/gotson/komga/commits/c5cc9a2))
- bump ktlint gradle to 11.4.2 ([b7cb89d](https://github.com/gotson/komga/commits/b7cb89d))
- bump commons-io to 2.12.0 ([93f138d](https://github.com/gotson/komga/commits/93f138d))
- bump tika-core to 2.8.0 ([3471446](https://github.com/gotson/komga/commits/3471446))
- bump pdfbox to 2.0.28 ([37514f0](https://github.com/gotson/komga/commits/37514f0))
- bump mockk to 1.13.5 ([ac91116](https://github.com/gotson/komga/commits/ac91116))
- bump ben-manes.versions to 0.46.0 ([5e89431](https://github.com/gotson/komga/commits/5e89431))
- bump kotlinx-coroutines-core to 1.7.1 ([714334e](https://github.com/gotson/komga/commits/714334e))
- bump springmockk to 4.0.2 ([dba0907](https://github.com/gotson/komga/commits/dba0907))
- bump tsid-creator to 5.2.4 ([c86302d](https://github.com/gotson/komga/commits/c86302d))
- bump jsoup to 1.16.1 ([2472bb6](https://github.com/gotson/komga/commits/2472bb6))
- bump commons-compress to 1.23.0 ([9dd1074](https://github.com/gotson/komga/commits/9dd1074))
- bump icu4j to 73.1 ([0fa540d](https://github.com/gotson/komga/commits/0fa540d))
- bump lucene to 9.6.0 ([c01e774](https://github.com/gotson/komga/commits/c01e774))
- bump hawtio to 2.17.2 ([aa31c74](https://github.com/gotson/komga/commits/aa31c74))
- bump jreleaser to 1.6.0 ([528ff62](https://github.com/gotson/komga/commits/528ff62))
- bump joog gradle plugin to 8.2 ([9c977d3](https://github.com/gotson/komga/commits/9c977d3))
- bump gradle to 8.1.1 ([cb7c150](https://github.com/gotson/komga/commits/cb7c150))
- bump springdoc to 2.1.0 ([77c1bb7](https://github.com/gotson/komga/commits/77c1bb7))
- bump sqlite-jdbc to 3.42.0.0 ([b7f4849](https://github.com/gotson/komga/commits/b7f4849))
- bump kotlin to 1.8.21 ([d5f3423](https://github.com/gotson/komga/commits/d5f3423))
- bump springboot to 3.1.0 ([1617fdf](https://github.com/gotson/komga/commits/1617fdf))
- bump hawtio-springboot to 2.17.0 ([2bf5dc3](https://github.com/gotson/komga/commits/2bf5dc3))
- bump springmockk to 4.0.0 ([c84a1d3](https://github.com/gotson/komga/commits/c84a1d3))
- bump springdoc to 2.0.2 ([c75d870](https://github.com/gotson/komga/commits/c75d870))
- bump mockk to 1.13.4 ([fb22bfd](https://github.com/gotson/komga/commits/fb22bfd))
- bump Lucene to 9.5.0 ([e6ba346](https://github.com/gotson/komga/commits/e6ba346))
- drop micrometer-registry-influx ([bae1ddf](https://github.com/gotson/komga/commits/bae1ddf))
- bump spring-session-caffeine to 2.0.0 ([70b6def](https://github.com/gotson/komga/commits/70b6def))
- bump archunit-junit5 to 1.0.1 ([992c68f](https://github.com/gotson/komga/commits/992c68f))
- bump tika-core to 2.7.0 ([471a759](https://github.com/gotson/komga/commits/471a759))
- bump kotlin-logging-jvm to 3.0.5 ([a0bf86e](https://github.com/gotson/komga/commits/a0bf86e))
- bump flyway's gradle plugin to 9.7.0 ([9104578](https://github.com/gotson/komga/commits/9104578))
- bump jooq's gradle plugin to 8.1 ([03929e4](https://github.com/gotson/komga/commits/03929e4))
- bump Spring Boot to 3.0.2 ([7828edc](https://github.com/gotson/komga/commits/7828edc))
- bump peter-evans/dockerhub-description from 3.3.0 to 3.4.1 ([8716996](https://github.com/gotson/komga/commits/8716996))

**webui**
- update browserslist ([272b52d](https://github.com/gotson/komga/commits/272b52d))
- fix .editorconfig ([e1c8780](https://github.com/gotson/komga/commits/e1c8780))

**unscoped**
- fix missing gradle tasks dependencies ([4d95e9d](https://github.com/gotson/komga/commits/4d95e9d))
- fix openapi generator configuration ([65be72a](https://github.com/gotson/komga/commits/65be72a))
- try to fix gradle error in CI ([9f44fc8](https://github.com/gotson/komga/commits/9f44fc8))
- try to fix gradle error in CI ([49c2695](https://github.com/gotson/komga/commits/49c2695))
- drop java below 17 ([9d7e193](https://github.com/gotson/komga/commits/9d7e193))
- use property access syntax ([d449ef1](https://github.com/gotson/komga/commits/d449ef1))
- remove SpykBean that would create flaky tests ([54c2e35](https://github.com/gotson/komga/commits/54c2e35))
- add REST API test for get claim status ([5336477](https://github.com/gotson/komga/commits/5336477))
- add OAuth2 REST API tests ([35be71e](https://github.com/gotson/komga/commits/35be71e))
- add Actuator REST API tests ([f9b1351](https://github.com/gotson/komga/commits/f9b1351))
- 🚨 target JDK 17: *Java 17 or above is now required* ([9569c5b](https://github.com/gotson/komga/commits/9569c5b))

## 📝 Documentation

- update development documentation for port change ([985f04f](https://github.com/gotson/komga/commits/985f04f))

## 🌐 Translation

- translated using Weblate (Japanese) ([28b8aff](https://github.com/gotson/komga/commits/28b8aff))
- translated using Weblate (Chinese (Simplified)) ([05ad407](https://github.com/gotson/komga/commits/05ad407))
- translated using Weblate (Italian) ([4154924](https://github.com/gotson/komga/commits/4154924))
- translated using Weblate (Chinese (Traditional)) ([965a041](https://github.com/gotson/komga/commits/965a041))
- translated using Weblate (Turkish) ([b5c9152](https://github.com/gotson/komga/commits/b5c9152))
- translated using Weblate (Bulgarian) ([28c0234](https://github.com/gotson/komga/commits/28c0234))
- translated using Weblate (Swedish) ([326c06e](https://github.com/gotson/komga/commits/326c06e))
- translated using Weblate (Czech) ([ab8d748](https://github.com/gotson/komga/commits/ab8d748))
- translated using Weblate (French) ([8e8a8ff](https://github.com/gotson/komga/commits/8e8a8ff))
- translated using Weblate (Spanish) ([3cc7386](https://github.com/gotson/komga/commits/3cc7386))
- translated using Weblate (German) ([dc5bf55](https://github.com/gotson/komga/commits/dc5bf55))

# [0.165.0](https://github.com/gotson/komga/compare/v0.164.0...v0.165.0) (2023-03-17)
## 🚀 Features
**metadata**
- import Comicinfo.xml's GTIN element as ISBN ([a51bf46](https://github.com/gotson/komga/commits/a51bf46))

**webui**
- use a datepicker to select book release date ([14c0bf4](https://github.com/gotson/komga/commits/14c0bf4)), closes [#1094](https://github.com/gotson/komga/issues/1094)

**unscoped**
- better handling of read progress when a book file changed ([5050a4e](https://github.com/gotson/komga/commits/5050a4e)), closes [#1093](https://github.com/gotson/komga/issues/1093)

## 🛠  Build

- better handling of temp directories ([c0d6bf2](https://github.com/gotson/komga/commits/c0d6bf2))
- add language injection for better readability ([c0d00cc](https://github.com/gotson/komga/commits/c0d00cc))
- inline xml and json files for better readability ([867f895](https://github.com/gotson/komga/commits/867f895))

# [0.164.0](https://github.com/gotson/komga/compare/v0.163.0...v0.164.0) (2023-03-14)
## 🚀 Features
**webui**
- allow CBL import with partial matching ([3a7d305](https://github.com/gotson/komga/commits/3a7d305)), closes [#1086](https://github.com/gotson/komga/issues/1086)

## 🏎 Perf
**api**
- 🚨 faster readlist matching for cbl: *removed api/v1/readlists/import* ([2461c83](https://github.com/gotson/komga/commits/2461c83))

**webui**
- reduce amount of API requests when matching cbl ([e3d9cb7](https://github.com/gotson/komga/commits/e3d9cb7))

## 🛠  Build
**deps**
- bump jreleaser to 1.5.1 ([df59ba6](https://github.com/gotson/komga/commits/df59ba6))

**unscoped**
- add JReleaser configuration for i18n commit type ([a3ebbdf](https://github.com/gotson/komga/commits/a3ebbdf))

## 🌐 Translation

- ttranslated using Weblate  ([0075499](https://github.com/gotson/komga/commits/0075499))
- remove unused error codes ([094d03f](https://github.com/gotson/komga/commits/094d03f))

# [0.163.0](https://github.com/gotson/komga/compare/v0.162.0...v0.163.0) (2023-03-08)
## 🚀 Features
**opds**
- add support for OPDS-PSE 1.2 ([29e71b0](https://github.com/gotson/komga/commits/29e71b0))

**webui**
- display series release year in series picker dialog ([1d7c89a](https://github.com/gotson/komga/commits/1d7c89a)), closes [#1076](https://github.com/gotson/komga/issues/1076)
- display series release year in search box results ([e95304e](https://github.com/gotson/komga/commits/e95304e))

**unscoped**
- comicrack read list matching will look for series with and without volume in brackets ([ac1e956](https://github.com/gotson/komga/commits/ac1e956)), closes [#1075](https://github.com/gotson/komga/issues/1075)

## 🐛 Fixes
**api**
- incorrect time conversion for BookMetadataAggregationDto ([9a015c4](https://github.com/gotson/komga/commits/9a015c4))
- better error handling for read list matching ([1961efe](https://github.com/gotson/komga/commits/1961efe))
- user restrictions checks were missing ([c661a88](https://github.com/gotson/komga/commits/c661a88))

**opds**
- user restrictions checks were missing ([471895c](https://github.com/gotson/komga/commits/471895c))

**webui**
- better error handling for read list matching ([53b1137](https://github.com/gotson/komga/commits/53b1137))

**unscoped**
- translated using Weblate ([38f1e0b](https://github.com/gotson/komga/commits/38f1e0b))
- regenerate thumbnail if first page is removed as duplicate ([cf2a5a2](https://github.com/gotson/komga/commits/cf2a5a2)), closes [#1078](https://github.com/gotson/komga/issues/1078)

## 🔄️ Changes
**webui**
- fix some warnings in javascript console ([88abfcc](https://github.com/gotson/komga/commits/88abfcc))
- missing i18n strings ([13444f8](https://github.com/gotson/komga/commits/13444f8))

**unscoped**
- use BookAction for analyzeAndPersist return type ([4d906f8](https://github.com/gotson/komga/commits/4d906f8))

## 🛠  Build
**deps**
- bump peter-evans/dockerhub-description from 3.1.2 to 3.3.0 ([683b663](https://github.com/gotson/komga/commits/683b663))

**unscoped**
- jreleaser continue if docker fails ([2f1af32](https://github.com/gotson/komga/commits/2f1af32))
- more JReleaser workaround ([e2e6d6e](https://github.com/gotson/komga/commits/e2e6d6e))
- always upload JReleaser output [skip ci] ([b2d3b3b](https://github.com/gotson/komga/commits/b2d3b3b))

## 📝 Documentation

- clarify docker steps ([74dbe92](https://github.com/gotson/komga/commits/74dbe92))

# [0.162.0](https://github.com/gotson/komga/compare/v0.161.0...v0.162.0) (2023-03-03)
## 🚀 Features
**api**
- new endpoint to match a CBL file without creating the readlist ([400f7ba](https://github.com/gotson/komga/commits/400f7ba))

**webui**
- interactive readlist import ([648ebb4](https://github.com/gotson/komga/commits/648ebb4))

**unscoped**
- read lists books can be sorted by release date ([e3bf906](https://github.com/gotson/komga/commits/e3bf906)), closes [#846](https://github.com/gotson/komga/issues/846)

## 🐛 Fixes
**api**
- filter readlist's books according to user's content restrictions ([a6895e1](https://github.com/gotson/komga/commits/a6895e1))
- ignore name case when updating a readlist or collection ([ae17d9c](https://github.com/gotson/komga/commits/ae17d9c))

**webui**
- dismissible alert on Book Import view ([7f8a7f3](https://github.com/gotson/komga/commits/7f8a7f3))
- perform case insensitive comparison for existing readlist or collection name ([2f97395](https://github.com/gotson/komga/commits/2f97395))
- history view could not load deleted page thumbnail in some conditions ([0211650](https://github.com/gotson/komga/commits/0211650))
- bulk edit books dialog incorrect validation for numberSort set to 0 ([4385f05](https://github.com/gotson/komga/commits/4385f05)), closes [#1057](https://github.com/gotson/komga/issues/1057)

**unscoped**
- translated using Weblate  ([fbc2ac1](https://github.com/gotson/komga/commits/fbc2ac1))
- translated using Weblate ([22b1ab0](https://github.com/gotson/komga/commits/22b1ab0))

## 🔄️ Changes
**api**
- change variable case ([aec3d3d](https://github.com/gotson/komga/commits/aec3d3d))

**webui**
- remove unused references ([b7ad808](https://github.com/gotson/komga/commits/b7ad808))
- incorrect type ([9860d4e](https://github.com/gotson/komga/commits/9860d4e))
- missing i18n field ([9e424b5](https://github.com/gotson/komga/commits/9e424b5))
- export readlist types ([0984d9f](https://github.com/gotson/komga/commits/0984d9f))

**unscoped**
- remove semantic-release files ([b5e83ac](https://github.com/gotson/komga/commits/b5e83ac))
- move specific ktlint configuration to subproject ([cb18b6b](https://github.com/gotson/komga/commits/cb18b6b))
- apply ktlint format ([1d6485c](https://github.com/gotson/komga/commits/1d6485c))

## 🛠  Build
**deps**
- bump minimist and mkdirp in /komga-webui ([c3ba85a](https://github.com/gotson/komga/commits/c3ba85a))
- bump thumbnailator to 0.4.19 ([4821cdb](https://github.com/gotson/komga/commits/4821cdb))
- bump sqlite-jdbc to 3.40.1.0 ([4614997](https://github.com/gotson/komga/commits/4614997))
- bump tsid-creator to 5.2.3 ([0e4a331](https://github.com/gotson/komga/commits/0e4a331))
- bump twelvemonkeys.imageio to 3.9.4 ([035a18a](https://github.com/gotson/komga/commits/035a18a))
- bump ben-manes.versions to 0.45.0 ([c0cc6b5](https://github.com/gotson/komga/commits/c0cc6b5))
- bump Kotlin to 1.7.22 ([e476d73](https://github.com/gotson/komga/commits/e476d73))

**unscoped**
- jreleaser workaround for JRELEASER_DOCKER_DEFAULT_PASSWORD ([cb332a6](https://github.com/gotson/komga/commits/cb332a6))
- gradle task dependency workaround ([b7a28c8](https://github.com/gotson/komga/commits/b7a28c8))
- release using JReleaser ([a1b058e](https://github.com/gotson/komga/commits/a1b058e))
- add jreleaser ([3581e57](https://github.com/gotson/komga/commits/3581e57))
- fix workflow file ([6583334](https://github.com/gotson/komga/commits/6583334))
- only trigger release job on master ([d21a7a3](https://github.com/gotson/komga/commits/d21a7a3))
- disable ktlint multiline-if-else rule ([1dc46be](https://github.com/gotson/komga/commits/1dc46be))
- bump gradle.ktlint to 11.1.0 amd ktlint to 0.48.2 ([c8dd291](https://github.com/gotson/komga/commits/c8dd291))

## 📝 Documentation

- update DEVELOPING.md instructions ([cd3687d](https://github.com/gotson/komga/commits/cd3687d))
- update CHANGELOG.md to match JReleaser format ([8666b94](https://github.com/gotson/komga/commits/8666b94))

# [0.161.0](https://github.com/gotson/komga/compare/v0.160.0...v0.161.0) (2023-01-27)


## Bug Fixes

* **api:** multiple tag or author filters could generate duplicate book results ([88aa7ad](https://github.com/gotson/komga/commit/88aa7adaad97cbf7637012f65b6faa9c25133fe8)), closes [#1052](https://github.com/gotson/komga/issues/1052)
* NPE when email_verified claim is missing in OIDC request ([72e5fd9](https://github.com/gotson/komga/commit/72e5fd9c9adde5cfeda7e2dee8d2edf4f57e2599)), closes [#1054](https://github.com/gotson/komga/issues/1054)
* translated using Weblate ([faf95f2](https://github.com/gotson/komga/commit/faf95f2c69913236529fbd81e463155b8b813c45))
* **webui:** multi-select bar delete button doesn't work on some views ([94fc5fa](https://github.com/gotson/komga/commit/94fc5fa4d5816b0c9c09783392a873d4a0676947)), closes [#1056](https://github.com/gotson/komga/issues/1056)


## Features

* add configuration option to skip email verification in OIDC login flow ([273b7d2](https://github.com/gotson/komga/commit/273b7d266cd1667f6a3c44b7d1ca479e0edb874a)), closes [#1054](https://github.com/gotson/komga/issues/1054)

# [0.160.0](https://github.com/gotson/komga/compare/v0.159.1...v0.160.0) (2023-01-19)


## Features

* **webui:** add button to reset filters on Series view if there are no results ([17ca7f7](https://github.com/gotson/komga/commit/17ca7f74eb7e3068400e5be984864b05f712ad9f))
* **webui:** add pagination to readlist/collection browse view ([ff70fea](https://github.com/gotson/komga/commit/ff70fea71a114e1a60a41bbe08d96fecf58b23d3)), closes [#817](https://github.com/gotson/komga/issues/817)


## Performance Improvements

* **webui:** readlist/collection expansion panels load data by page ([0b57dc9](https://github.com/gotson/komga/commit/0b57dc9c96fed24e6db440834f257aa4cf854f18)), closes [#817](https://github.com/gotson/komga/issues/817)

# [0.159.1](https://github.com/gotson/komga/compare/v0.159.0...v0.159.1) (2023-01-18)


## Bug Fixes

* **webui:** edit series dialog shows wrong tabs when editing multiple items ([12d2cbc](https://github.com/gotson/komga/commit/12d2cbcd09865471c447de9b71e24e8deb3b3675)), closes [#1049](https://github.com/gotson/komga/issues/1049)

# [0.159.0](https://github.com/gotson/komga/compare/v0.158.0...v0.159.0) (2023-01-17)


## Bug Fixes

* **api:** include Z in datetime formats ([bb7b7fd](https://github.com/gotson/komga/commit/bb7b7fd8f01a7114a2966619a48b22e547938743))
* **api:** some dates were not returned as UTC ([cdfb8e3](https://github.com/gotson/komga/commit/cdfb8e377f1eeef86b89305e5d8ec7ccb50f44eb))
* translated using Weblate ([6ad0188](https://github.com/gotson/komga/commit/6ad0188bd3816c9f2f34bf08ebbbc980d5eaa334))


## Features

* add links field in series metadata ([f9f02a3](https://github.com/gotson/komga/commit/f9f02a395b1d92b5dd1b76a7fe0187d784da75d9)), closes [#938](https://github.com/gotson/komga/issues/938)
* series metadata supports alternate titles ([8e0655f](https://github.com/gotson/komga/commit/8e0655f29a24fa9d887d7022d40ba2642edb3199)), closes [#878](https://github.com/gotson/komga/issues/878)
* **webui:** display datetime when hovering date on item card ([4d97172](https://github.com/gotson/komga/commit/4d9717287acb277a146594d134d6a8c7fe475d82))
* **webui:** display number of pages left on book details view ([77424b1](https://github.com/gotson/komga/commit/77424b1b1c1371d00036ea0bb08c59c543605020)), closes [#1012](https://github.com/gotson/komga/issues/1012)
* **webui:** display read date on book details view ([c30c755](https://github.com/gotson/komga/commit/c30c755f63acc210267d79dfb6388a46c992a02a)), closes [#757](https://github.com/gotson/komga/issues/757)
* **webui:** increment/decrement numberSort in bulk ([e7fbe57](https://github.com/gotson/komga/commit/e7fbe57e44e9850db6f68cefc6b1dde36c665d0c)), closes [#628](https://github.com/gotson/komga/issues/628)

# [0.158.0](https://github.com/gotson/komga/compare/v0.157.5...v0.158.0) (2023-01-12)


## Bug Fixes

* close SSE connections during shutdown ([3f773d1](https://github.com/gotson/komga/commit/3f773d1ed74fb265ea53f181cc22f71f1855c76e)), closes [#1028](https://github.com/gotson/komga/issues/1028)
* translated using Weblate  ([f06c680](https://github.com/gotson/komga/commit/f06c68067faefeef51d67381c7527b10ff8f30a1))


## Features

* library option to append ComicInfo Volume to Series title ([63b3c83](https://github.com/gotson/komga/commit/63b3c83ce2cf6dbd43a7d437a876731b35321afa))
* **webui:** navigate back to collection from series ([db56a38](https://github.com/gotson/komga/commit/db56a38476d8c8680e32dcb38c368ea50dd4af40)), closes [#1042](https://github.com/gotson/komga/issues/1042)
* **webui:** order collections/readlists by most recently modified in the Add To dialog ([592f87c](https://github.com/gotson/komga/commit/592f87ca794e52e1d374ffc1d6b1c5c4c231274b)), closes [#862](https://github.com/gotson/komga/issues/862)

# [0.157.5](https://github.com/gotson/komga/compare/v0.157.4...v0.157.5) (2022-11-22)


## Bug Fixes

* exception if user agent is null ([cb07581](https://github.com/gotson/komga/commit/cb07581daa850cc38902304e4d2ac8717e1f8696))
* translated using Weblate ([1f504f4](https://github.com/gotson/komga/commit/1f504f4a7d6b2f1adf176f884d9403e0046bf7d3))
* **webui:** sort genre on Browse Series screen ([665ce18](https://github.com/gotson/komga/commit/665ce1804c915a3d4783ede102c3a03bda9a48f6))

# [0.157.4](https://github.com/gotson/komga/compare/v0.157.3...v0.157.4) (2022-11-02)


## Bug Fixes

* set image type when generating thumbnails to avoid missing channels ([40f3e16](https://github.com/gotson/komga/commit/40f3e1623db7b9dafc29667c36c56c098910c77c)), closes [#976](https://github.com/gotson/komga/issues/976)


## Performance Improvements

* optimize task FindDuplicatePagesToDelete ([59a0048](https://github.com/gotson/komga/commit/59a0048385aa8d2bb8c5b75920eb48950956704f))

# [0.157.3](https://github.com/gotson/komga/compare/v0.157.2...v0.157.3) (2022-10-30)


## Bug Fixes

* **api:** allow unauthorized access to health endpoint ([751d347](https://github.com/gotson/komga/commit/751d3472310e76618e59902e4c0ba7d62f9c5b4b)), closes [#992](https://github.com/gotson/komga/issues/992)
* translated using Weblate ([3f4aa33](https://github.com/gotson/komga/commit/3f4aa33e49210172f68db908d023f6993a70a2b9))
* **webui:** add debouncer when searching for author in Edit Book dialog ([da3d283](https://github.com/gotson/komga/commit/da3d2835031a97d43089cbf4b177c06c0c6b573c)), closes [#960](https://github.com/gotson/komga/issues/960)

# [0.157.2](https://github.com/gotson/komga/compare/v0.157.1...v0.157.2) (2022-09-13)


## Bug Fixes

* cannot parse series.json ([122f0c9](https://github.com/gotson/komga/commit/122f0c92adef008357bfd783ce71f700311ebd01)), closes [#961](https://github.com/gotson/komga/issues/961)
* **webui:** bottom pagination hidden by navigation bar on SM breakpoint ([234dae0](https://github.com/gotson/komga/commit/234dae0841159b33b5527720ea6475d1f11d77c4)), closes [#953](https://github.com/gotson/komga/issues/953)

# [0.157.1](https://github.com/gotson/komga/compare/v0.157.0...v0.157.1) (2022-08-18)


## Bug Fixes

* **api:** prevent SQLITE_TOOBIG book search returns many matches ([f8cc3cd](https://github.com/gotson/komga/commit/f8cc3cd4ca3236b201c66640d1f58967a8293947)), closes [#940](https://github.com/gotson/komga/issues/940)
* encode filenames in UTF-8 when downloading ([#941](https://github.com/gotson/komga/issues/941)) ([cf98e69](https://github.com/gotson/komga/commit/cf98e69374ccc2713790cea94d77a4b79227f004))
* translated using Weblate ([c85f267](https://github.com/gotson/komga/commit/c85f267c10fb8e594ae43c34bf51b01c30531336))
* **webui:** iOS icon without black edges ([#949](https://github.com/gotson/komga/issues/949)) ([a3929e2](https://github.com/gotson/komga/commit/a3929e2e1ff0a0abfe78d78ebf21c9efee5fb91c))
* **webui:** search for collection/readlist in the "add to" dialog should ignore accents ([ac67924](https://github.com/gotson/komga/commit/ac67924fbaa8163396a779c26be86338624316df)), closes [#944](https://github.com/gotson/komga/issues/944)

# [0.157.0](https://github.com/gotson/komga/compare/v0.156.0...v0.157.0) (2022-07-28)


## Bug Fixes

* add configuration to set the database pool size ([76e6241](https://github.com/gotson/komga/commit/76e624140d83a6dcad3f58043f50b0ba4c4b64d5))
* default the max pool size to 1 ([c962f8a](https://github.com/gotson/komga/commit/c962f8a7ab0e3c132435fc1b3b18262135e023c6))
* translated using Weblate ([2cbd124](https://github.com/gotson/komga/commit/2cbd124d6174733afbbbde40aeff0ac4a3fac7db))


## Features

* **api:** allow readlist custom sorting ([b9e69a1](https://github.com/gotson/komga/commit/b9e69a1c9a194c9c0fd60ebfe2b8f954e6f9ca04))

# [0.156.0](https://github.com/gotson/komga/compare/v0.155.3...v0.156.0) (2022-07-26)


## Bug Fixes

* translated using Weblate ([85236d9](https://github.com/gotson/komga/commit/85236d9e9305f084e71389fb160d4c360cd47fe3))
* **webui:** missing i18n strings ([775dd5c](https://github.com/gotson/komga/commit/775dd5c666752d2e40bc18d36bbe82beb1d6c7e8))
* **webui:** series name not showing on card if it starts with '<' ([599b605](https://github.com/gotson/komga/commit/599b605d923c65fa6976d7811edee5feba484a31)), closes [#930](https://github.com/gotson/komga/issues/930)


## Features

* expose sqlite pragma configuration ([3c51430](https://github.com/gotson/komga/commit/3c5143071cd82890668df17f1520907f206e3ccd))

# [0.155.3](https://github.com/gotson/komga/compare/v0.155.2...v0.155.3) (2022-07-22)


## Bug Fixes

* expose configuration for transaction mode ([218e300](https://github.com/gotson/komga/commit/218e3006f9c6209ea7b29e70ef2e01aa1e9f31b5))

# [0.155.2](https://github.com/gotson/komga/compare/v0.155.1...v0.155.2) (2022-07-20)


## Bug Fixes

* translated using Weblate ([11bdf0e](https://github.com/gotson/komga/commit/11bdf0ebf7240d0121490e8cbefa0b1f76e6e483))


## Performance Improvements

* database connection pooling ([58fde3e](https://github.com/gotson/komga/commit/58fde3e7aa7a1cc622556a93bc7259f9cdec736f))
* remove distinct on BookDtoDao ([3256f3f](https://github.com/gotson/komga/commit/3256f3f300e0021ffdcc57587ac8f594c37a6165))
* retrieve one to many collections in bulk ([8e9d93f](https://github.com/gotson/komga/commit/8e9d93f6f947dfa4422f95c184f97ef499a453ac))

# [0.155.1](https://github.com/gotson/komga/compare/v0.155.0...v0.155.1) (2022-07-15)


## Bug Fixes

* translated using Weblate ([21c2be4](https://github.com/gotson/komga/commit/21c2be4c4fdc28c99439b2620ce55f119f5d247f))


## Performance Improvements

* add database indices for faster querying ([0af5f5c](https://github.com/gotson/komga/commit/0af5f5c4d94014e939ca806dbbcbb83cc75b400f))
* don't sort books and series by default ([31c89fc](https://github.com/gotson/komga/commit/31c89fc29836e9c5af27c92d6a85c8ef98f4a165))

# [0.155.0](https://github.com/gotson/komga/compare/v0.154.4...v0.155.0) (2022-06-30)


## Bug Fixes

* re-enable content length header for book downloads ([535c6d7](https://github.com/gotson/komga/commit/535c6d7eca2caf48bb58df1b9039f56c9f10c302))
* use zip64 for archive downloads ([73949d5](https://github.com/gotson/komga/commit/73949d514ec7ccb185821ce5a99bd3b70d3270c4))


## Features

* enable more providers for actuator info ([61f519a](https://github.com/gotson/komga/commit/61f519a6a0f2158397bdc5adbd9067fe22888070))

# [0.154.4](https://github.com/gotson/komga/compare/v0.154.3...v0.154.4) (2022-06-16)


## Bug Fixes

* files generated with removed pages could have incorrect permissions ([b3e3a4d](https://github.com/gotson/komga/commit/b3e3a4d7640e09547de1f7afd41c743d054771a0))
* translated using Weblate ([2511879](https://github.com/gotson/komga/commit/251187918faf577f653027a9481a55098a1a77f6))

# [0.154.3](https://github.com/gotson/komga/compare/v0.154.2...v0.154.3) (2022-06-10)


## Bug Fixes

* cannot delete read list with custom cover ([b73b869](https://github.com/gotson/komga/commit/b73b8690a42a7e6e6919c5f0e56c1c02959d15c1))
* error when downloading book file over 2GB ([ad82d99](https://github.com/gotson/komga/commit/ad82d99dbeb70b5f40a920de40664b18e50fe3c9)), closes [#897](https://github.com/gotson/komga/issues/897)
* translated using Weblate ([3f69206](https://github.com/gotson/komga/commit/3f692060641cfe7b25a679c16cb2a0a5a32f60d7))

# [0.154.2](https://github.com/gotson/komga/compare/v0.154.1...v0.154.2) (2022-05-31)


## Bug Fixes

* handle both cid and comicId for Mylar's series.json ([0bbe543](https://github.com/gotson/komga/commit/0bbe5438d0a3a389a2b2a6e0762835a5c2b6920a)), closes [#890](https://github.com/gotson/komga/issues/890) [#889](https://github.com/gotson/komga/issues/889)
* translated using Weblate ([6581ffd](https://github.com/gotson/komga/commit/6581ffd4cd00294c45cffb10678a742cf0191998))
* **webui:** display release date without timezone adjustment ([#875](https://github.com/gotson/komga/issues/875)) ([d0da11f](https://github.com/gotson/komga/commit/d0da11f23b1cb38e3f918a34b69ec9f12121cde2))

# [0.154.1](https://github.com/gotson/komga/compare/v0.154.0...v0.154.1) (2022-04-25)


## Bug Fixes

* read lists imported from cbl are not added to search index ([63e3d8a](https://github.com/gotson/komga/commit/63e3d8a6ae2924c6c12369c426410ca53f52e963)), closes [#868](https://github.com/gotson/komga/issues/868)
* rebuild search index for readlists ([03de229](https://github.com/gotson/komga/commit/03de229da50831a0f9c2b88c4631101e5ef3099e)), closes [#868](https://github.com/gotson/komga/issues/868)
* translated using Weblate ([a0c0daf](https://github.com/gotson/komga/commit/a0c0daf43f34d5bf5ce3b77d4c842834c6707348))

# [0.154.0](https://github.com/gotson/komga/compare/v0.153.2...v0.154.0) (2022-04-22)


## Bug Fixes

* translated using Weblate ([18b181b](https://github.com/gotson/komga/commit/18b181bf8fbde86a6a652d9dc7f134d8e28cbc3a))
* **webui:** extract translation strings for DropZone.vue ([2966c85](https://github.com/gotson/komga/commit/2966c854492157bb889ae8aff877e950a3bf67f3)), closes [#856](https://github.com/gotson/komga/issues/856)
* **webui:** read button on card would not always work ([4dd1e7a](https://github.com/gotson/komga/commit/4dd1e7af768a7e4e4e80be18e9adb6289e137b28)), closes [#830](https://github.com/gotson/komga/issues/830)


## Features

* read support for jpeg xl ([dd5b7d0](https://github.com/gotson/komga/commit/dd5b7d03633a2302dbef625c6658921971d83b8c)), closes [#831](https://github.com/gotson/komga/issues/831)
* **webui:** set the currently viewed page as poster for book/series/readlist ([49b7f59](https://github.com/gotson/komga/commit/49b7f592cbdb0e25687de26e13a1568ac7463a13)), closes [#838](https://github.com/gotson/komga/issues/838)

# [0.153.2](https://github.com/gotson/komga/compare/v0.153.1...v0.153.2) (2022-04-01)


## Bug Fixes

* translated using Weblate ([52136db](https://github.com/gotson/komga/commit/52136dbeb3c29dc6c7c1b405a321f3e805a86012))
* **webui:** show the full title of book/series on hover in cards ([59a6f7d](https://github.com/gotson/komga/commit/59a6f7d3cf189713e901c2f9eaab7b9328633844)), closes [#836](https://github.com/gotson/komga/issues/836)

# [0.153.1](https://github.com/gotson/komga/compare/v0.153.0...v0.153.1) (2022-03-14)


## Bug Fixes

* **api:** return created thumbnail ([dd1ffbe](https://github.com/gotson/komga/commit/dd1ffbe54ade732dafb3db00bdb829274276b1e6))
* translated using Weblate ([b778a2a](https://github.com/gotson/komga/commit/b778a2afc217c35277a23967c932f0261cd5a232))
* translated using Weblate ([6449f22](https://github.com/gotson/komga/commit/6449f2247d84a160596608b617c518c823222179))

# [0.153.0](https://github.com/gotson/komga/compare/v0.152.0...v0.153.0) (2022-03-03)


## Bug Fixes

* **api:** expired sessions would not be destroyed ([5ecc9c6](https://github.com/gotson/komga/commit/5ecc9c6785ae1e672b25f141339cca2fa9a91218))
* ignore Qnap @Recycle directories by default ([2621500](https://github.com/gotson/komga/commit/2621500666b7e7fcdf69298faabb7c56697815d9))
* **webui:** books selection bar wouldn't hide the toolbar on Dashboard ([02d51b9](https://github.com/gotson/komga/commit/02d51b96c8a6e4826109ec355698d9ce1d59b657))
* **webui:** display library navigation as bottom bar for sm screens ([70a546f](https://github.com/gotson/komga/commit/70a546f19c7a4c085ec43fb4a54e49aac45960dd))
* **webui:** display release date without timezone adjustment ([d343740](https://github.com/gotson/komga/commit/d343740f3021686bae9a41fd73c2102a51239faa)), closes [#818](https://github.com/gotson/komga/issues/818)


## Features

* **api:** add /api/v2/users and deprecate /api/v1/users ([fa04d95](https://github.com/gotson/komga/commit/fa04d9511a394ca6003a8177b97997814ac57674))
* **api:** manage restrictions for users ([e345d6f](https://github.com/gotson/komga/commit/e345d6f9ef0a0d2aa1e71bb08509b5c605cb169e))
* **api:** restrict content according to user's restrictions ([b0d6314](https://github.com/gotson/komga/commit/b0d6314ec9c8eec049034daab1f65b8463d20fcb))
* **api:** retrieve all sharing labels ([562c57c](https://github.com/gotson/komga/commit/562c57ccc8f3c39d494dd8b929e65db975370a1b))
* **api:** update series sharing labels ([769b0e6](https://github.com/gotson/komga/commit/769b0e6a0ca3c5c9dbbceb115c9e9d579a820bde))
* persist user content restriction ([f1ab136](https://github.com/gotson/komga/commit/f1ab136b5e9b3d35a67247814de8d74e757527ad))
* restrict content by labels ([8d4eb68](https://github.com/gotson/komga/commit/8d4eb68f7d4d294fe7a5379d7c94c63fff5b3ec8))
* sharing labels for series ([496ebb0](https://github.com/gotson/komga/commit/496ebb0aac88e981d1bb1e2ac36776a72f167303))
* **webui:** edit user restrictions ([37dfa92](https://github.com/gotson/komga/commit/37dfa923e91d2a6b272936d2741fcbee87bc5c15))
* **webui:** logout when session expired ([093610e](https://github.com/gotson/komga/commit/093610e18687feaf93d92de41a34c4e338156972))
* **webui:** update series sharing labels ([c7c5592](https://github.com/gotson/komga/commit/c7c5592c50490741d6824c64d276ac5c26b8b306))

# [0.152.0](https://github.com/gotson/komga/compare/v0.151.2...v0.152.0) (2022-02-18)


## Features

* **api:** new history endpoint to retrieve historical events ([88f7f57](https://github.com/gotson/komga/commit/88f7f57a5d8d4d68c6be47db48602a779e2dac53))
* **webui:** history view ([f8bea23](https://github.com/gotson/komga/commit/f8bea23b2a0a7eeda22c2840bb57c09b8464d6e6))
* **webui:** move some views into media management section ([90caee9](https://github.com/gotson/komga/commit/90caee988eaf0048688f278f8f45dedd425fd228))


## Performance Improvements

* **webui:** reduce duplicate api calls for components with datatables ([1a82497](https://github.com/gotson/komga/commit/1a8249732db89f9346d47cc21a6189a7e88f4529))

# [0.151.2](https://github.com/gotson/komga/compare/v0.151.1...v0.151.2) (2022-02-17)


## Bug Fixes

* **webui:** links not showing underline on hover on dark theme ([e476d78](https://github.com/gotson/komga/commit/e476d78770086330fb58f770efa266cf38e2f8c3))
* **webui:** restore library alphabetical navigation from url ([1faaf12](https://github.com/gotson/komga/commit/1faaf12de4f000244547b6917b791df8b983b377))


## Performance Improvements

* convert to cbz on first scan ([b724f20](https://github.com/gotson/komga/commit/b724f205cd065d614319e98856f5b1cbf29d7568))
* don't recompute book hash during scan if filesize is different ([33cd19a](https://github.com/gotson/komga/commit/33cd19ae04ec06c216091ac0ef7ee2c79eb3c8ad))
* hash and delete pages in a single scan ([b436e90](https://github.com/gotson/komga/commit/b436e90a8c429749b5c10a52ae39821eb939d5a0))

# [0.151.1](https://github.com/gotson/komga/compare/v0.151.0...v0.151.1) (2022-02-15)


## Bug Fixes

* **opds:** better titles for entries ([9f2808d](https://github.com/gotson/komga/commit/9f2808dfdc6d91b947743386849c42594df3d9b4))
* translated using Weblate ([6975728](https://github.com/gotson/komga/commit/6975728974e16e0508e1df112ece3870fda12bdf))
* **webui:** clearer item card display ([052f3d3](https://github.com/gotson/komga/commit/052f3d38049ee0d9decd6eaff1f9a6f91a4c1cef))

# [0.151.0](https://github.com/gotson/komga/compare/v0.150.0...v0.151.0) (2022-02-14)


## Bug Fixes

* **metrics:** configure step for 24h ([55df968](https://github.com/gotson/komga/commit/55df968651d2d27183bed70deca7737a947a073f))
* remove caching of ZipFile ([57082bd](https://github.com/gotson/komga/commit/57082bd990dfc450f23014f57ccbb862e40dacfb)), closes [#801](https://github.com/gotson/komga/issues/801)
* **webui:** don't show metrics if there's no data ([9fe50fd](https://github.com/gotson/komga/commit/9fe50fd5ceb9fd657d50eaa678dc6c13f46023d1))
* **webui:** page hash matches dialog would not reset properly ([c0c7b09](https://github.com/gotson/komga/commit/c0c7b09faa904ea29d39bde7966b8208403bd72b))
* **webui:** properly set known hash card button initial status ([17c76de](https://github.com/gotson/komga/commit/17c76de3083a92c8b2a3064d0a29904492f98503))


## Features

* **webui:** adapt card content depending on context ([35bf05e](https://github.com/gotson/komga/commit/35bf05eb39fc05b6d36beaedca5d64aace81302e)), closes [#679](https://github.com/gotson/komga/issues/679)
* **webui:** show series for books in search bar ([1463078](https://github.com/gotson/komga/commit/14630783670a1a19a692c600468209e6ce659ba8)), closes [#678](https://github.com/gotson/komga/issues/678)
* **webui:** show series title within read list navigation ([f388e9b](https://github.com/gotson/komga/commit/f388e9bf7687c8d49ee55aa7ee3623763952385b))


## Performance Improvements

* **webui:** reduce number of API calls on app initial load ([f7dc98a](https://github.com/gotson/komga/commit/f7dc98aa7e52b22a94a53ec4df62d519d953505c))
* **webui:** reduce number of API calls on book details screen ([5f8894d](https://github.com/gotson/komga/commit/5f8894d47a89f299445d6bc779fc3ba90944a9a5))

# [0.150.0](https://github.com/gotson/komga/compare/v0.149.2...v0.150.0) (2022-02-10)


## Bug Fixes

* translated using Weblate ([5d253a0](https://github.com/gotson/komga/commit/5d253a0fc507d85ec0a5bcc01fd9f06c315510bb))
* **webui:** reset duplicate match table when hash is changed ([b051528](https://github.com/gotson/komga/commit/b051528cba8884338cc00dd91aa656bf6579b1ef))
* **webui:** reset hash card when hash is changed ([745fe09](https://github.com/gotson/komga/commit/745fe09ec255a7ef24c04c5ac8527bf78029a235))


## Features

* **api:** publish business metrics ([78174db](https://github.com/gotson/komga/commit/78174db6fb74541c99c517df89a96b9db0011547))
* **webui:** metrics dashboard in server settings ([9467c93](https://github.com/gotson/komga/commit/9467c93b88c78f7610bdf11cfdf95b761fa33263))
* **webui:** page size selection for new duplicate pages view ([960546a](https://github.com/gotson/komga/commit/960546a1e0a7e15a4308e5cdc3fa58c49ce9cb88))

# [0.149.2](https://github.com/gotson/komga/compare/v0.149.1...v0.149.2) (2022-02-08)


## Bug Fixes

* release 0.149.0 could wipe some database entries ([5f2ce0f](https://github.com/gotson/komga/commit/5f2ce0fb30c1fc22a70406693756d8b672ab9383))
* **webui:** known hash card button would not reset properly ([ce66f87](https://github.com/gotson/komga/commit/ce66f8778b4389189893a441e6f7c9735b5b71e9))

# [0.149.1](https://github.com/gotson/komga/compare/v0.149.0...v0.149.1) (2022-02-08)


## Bug Fixes

* **api:** cannot delete duplicate page match if it is unknown ([a6dece2](https://github.com/gotson/komga/commit/a6dece2b8158deec940198879f7902de6694d972))
* **webui:** display issue for duplicate page card without size ([a9556d9](https://github.com/gotson/komga/commit/a9556d9069b1758e4cdb0edc3d08ef700d7854b1))

# [0.149.0](https://github.com/gotson/komga/compare/v0.148.3...v0.149.0) (2022-02-08)


## Bug Fixes

* book conversion will conserve page hashes ([83a59b8](https://github.com/gotson/komga/commit/83a59b8bd32d3d20f8930a8d846394992db316b2))
* soft delete after deleting files instead of triggering a scan ([dabe398](https://github.com/gotson/komga/commit/dabe3982745bec5eb6cc8c945ac4ae7ef2313029))
* translated using Weblate ([4ec8f32](https://github.com/gotson/komga/commit/4ec8f327fdc23f0c29f53c3c771d68d1638b54f1))
* **webreader:** don't pad landscape covers in double page mode ([352f9a8](https://github.com/gotson/komga/commit/352f9a852528f325971eb1e251a503379281f41e))


## Features

* delete duplicate page match individually ([b53fbc7](https://github.com/gotson/komga/commit/b53fbc7217bc014211a017280cd60272b9343991))
* deletion of duplicate pages ([c080f43](https://github.com/gotson/komga/commit/c080f433af72505399f9fe6adce3acc25530c253))
* page hashing enhancement ([a96335d](https://github.com/gotson/komga/commit/a96335dbeec3fc4379d0668f5aadf6f92141e6a1))

# [0.148.3](https://github.com/gotson/komga/compare/v0.148.2...v0.148.3) (2022-02-04)


## Bug Fixes

* a TooManyRowsException may crash the scan ([0368060](https://github.com/gotson/komga/commit/036806016691e748ee99decec7c7f3ea41405125))
* filtering may not work with some unicode characters ([656f433](https://github.com/gotson/komga/commit/656f433b9d1d1d7c9b33e6fc48ebf5a2b92fe00a)), closes [#789](https://github.com/gotson/komga/issues/789)
* **opds:** incorrect url encode for next/previous links ([288858c](https://github.com/gotson/komga/commit/288858cf6416f8e7a08c92940feae91c53d53198)), closes [#792](https://github.com/gotson/komga/issues/792)
* translated using Weblate ([f0d2833](https://github.com/gotson/komga/commit/f0d2833bd6601b772b388a2c8b9f0d73975f7654))
* use temp files for PDF when getting entries ([deb8e5f](https://github.com/gotson/komga/commit/deb8e5fff44c8f95ecd13a6174516f488d77e263))
* **webui:** better display of readlist context information for small screen ([add2574](https://github.com/gotson/komga/commit/add2574859847c47f9b36c04a96f17b15e03d2a2)), closes [#791](https://github.com/gotson/komga/issues/791)
* **webui:** don't reload series card thumbnail on every book update ([8bd36b4](https://github.com/gotson/komga/commit/8bd36b45afc84186afc58472e6cc5bf5fe4fd0f1))

# [0.148.2](https://github.com/gotson/komga/compare/v0.148.1...v0.148.2) (2022-01-31)


## Bug Fixes

* **api:** request param conflict ([160c767](https://github.com/gotson/komga/commit/160c767c4016a16e6496e9a27d84941891b9a8f5))
* synchronous cache eviction when closing PDDocument ([f25c74f](https://github.com/gotson/komga/commit/f25c74f759f3ac3f172d8f2ada517bb8987688d8))
* translated using Weblate ([2d554af](https://github.com/gotson/komga/commit/2d554afec4b67359244ce3921a8b6fb023929b4f))
* **webui:** duplicate pages card size ([fedaa33](https://github.com/gotson/komga/commit/fedaa338554958d7b4c91a9387ebf0a4e684cbab))


## Performance Improvements

* use temp files for PDF streams ([8da2489](https://github.com/gotson/komga/commit/8da2489671b4a1f00675b126abe71f6f2f57ea49))

# [0.148.1](https://github.com/gotson/komga/compare/v0.148.0...v0.148.1) (2022-01-27)


## Bug Fixes

* **webui:** duplicate page card action bar alignment ([7a3f80c](https://github.com/gotson/komga/commit/7a3f80ce92aead63f2e8310301fef402b885d964))
* **webui:** duplicate page matches show filenames ([388c4f5](https://github.com/gotson/komga/commit/388c4f5f305fdadb3f23e45a307159a0ee2c6e60))
* **webui:** duplicate page matches were not showing exact matches only ([5844521](https://github.com/gotson/komga/commit/58445212865da65c70d92ebe94f3b1ebe4be983d))
* **webui:** duplicate pages filters ([1120f19](https://github.com/gotson/komga/commit/1120f1943ad201805104b485cc497316e92d3899))
* **webui:** duplicate pages show total size saving ([e9bf064](https://github.com/gotson/komga/commit/e9bf064cb2accd47545f16fe6359c1f8e4858dae))

# [0.148.0](https://github.com/gotson/komga/compare/v0.147.0...v0.148.0) (2022-01-26)


## Features

* **api:** wip version of the page-hashes endpoints ([5777952](https://github.com/gotson/komga/commit/5777952c05246f8e1ab8408dbd11c130b70ef113))
* **webui:** view duplicate pages ([79d265c](https://github.com/gotson/komga/commit/79d265c85211a2430d0bae784bad91522eba36aa))

# [0.147.0](https://github.com/gotson/komga/compare/v0.146.0...v0.147.0) (2022-01-25)


## Bug Fixes

* **webui:** display clickable links for duplicate files ([4453c03](https://github.com/gotson/komga/commit/4453c0320036fd9b62ce5c451dc77b8c0c16fbe0))
* **webui:** drop poster from browser images ([7cb2a87](https://github.com/gotson/komga/commit/7cb2a87a7f274f5695ffdd14d083e824c818c1f6))


## Features

* **webui:** add refresh button and unavailable status to media analysis view ([a116d17](https://github.com/gotson/komga/commit/a116d1788315718fddeef62853ca05db97f4dc42))
* **webui:** delete book from duplicate view ([b1fd257](https://github.com/gotson/komga/commit/b1fd257a149682a16b3505e48a550524abd7d829)), closes [#764](https://github.com/gotson/komga/issues/764)

# [0.146.0](https://github.com/gotson/komga/compare/v0.145.1...v0.146.0) (2022-01-24)


## Features

* **webui:** custom cover upload ([2a56fff](https://github.com/gotson/komga/commit/2a56fffa9af46b5852976131c04f2f8cf7207080)), closes [#473](https://github.com/gotson/komga/issues/473)

# [0.145.1](https://github.com/gotson/komga/compare/v0.145.0...v0.145.1) (2022-01-24)


## Bug Fixes

* **opds:** links are missing the publisher parameter ([9963665](https://github.com/gotson/komga/commit/996366528d6c4ddabcc75b8fabdba87294c5567d)), closes [#777](https://github.com/gotson/komga/issues/777)
* translated using Weblate ([853a988](https://github.com/gotson/komga/commit/853a9886a8c1cc9d2257e30e945ad50e6edbca1b))
* **webreader:** mark read could miss the last page in double pages ([ecbba65](https://github.com/gotson/komga/commit/ecbba653bf7045ffa781b70b80baf77a47ba2651)), closes [#772](https://github.com/gotson/komga/issues/772)

# [0.145.0](https://github.com/gotson/komga/compare/v0.144.0...v0.145.0) (2022-01-19)


## Features

* check changed book hash before resetting during scan ([39f686b](https://github.com/gotson/komga/commit/39f686bebeb35a9bcf61de461cfa958b4fd40a7d))
* import ComicInfo Tags element ([d3daaf1](https://github.com/gotson/komga/commit/d3daaf1f9cba544510a501162c19a2bb832d58ec)), closes [#541](https://github.com/gotson/komga/issues/541)
* tasks concurrency (configurable) ([2fd95e5](https://github.com/gotson/komga/commit/2fd95e5a7fce0995f499b9dcd2f2a30afb1fdbd8))

# [0.144.0](https://github.com/gotson/komga/compare/v0.143.1...v0.144.0) (2022-01-13)


## Bug Fixes

* change artemis port ([4fb7dc7](https://github.com/gotson/komga/commit/4fb7dc75ecfff93ed0ea6ba2c2ac407c10180e9d))


## Features

* configurable config directory ([c643d55](https://github.com/gotson/komga/commit/c643d55ee84bd901da59ee0aa804203b3d501b95))

# [0.143.1](https://github.com/gotson/komga/compare/v0.143.0...v0.143.1) (2022-01-10)


## Bug Fixes

* find duplicate books by hash and file size ([4e055f0](https://github.com/gotson/komga/commit/4e055f037a9fc480efb6377bfcafc6198dfac912)), closes [#771](https://github.com/gotson/komga/issues/771)
* improper json deserialization when missing mandatory fields ([594194f](https://github.com/gotson/komga/commit/594194fafd7101a8ec848d72851030bad545c4af))

# [0.143.0](https://github.com/gotson/komga/compare/v0.142.0...v0.143.0) (2022-01-06)


## Features

* add library options for hashing and dimensions analysis ([5d4ec94](https://github.com/gotson/komga/commit/5d4ec94e01832774cdde2cf475d91d9d4a14496d)), closes [#645](https://github.com/gotson/komga/issues/645)
* get file size for pages during analysis ([432ed4e](https://github.com/gotson/komga/commit/432ed4e14c01d66fecf4f333994875f2edea38cb))
* hash pages to detect duplicates ([195ec29](https://github.com/gotson/komga/commit/195ec29d6d8ad2598f55e0c7009687abf231522e))
* **webui:** display page size in book import dialog ([6f26c2c](https://github.com/gotson/komga/commit/6f26c2c9268fdf8c07fe71226727e3c616ae3a61))

# [0.142.0](https://github.com/gotson/komga/compare/v0.141.0...v0.142.0) (2021-12-31)


## Features

* **api:** get duplicate books by filehash ([3c97c20](https://github.com/gotson/komga/commit/3c97c204810b7739044bbd6bf978a187a4f5e68a))
* **webui:** new duplicates tab in server settings ([38ad00c](https://github.com/gotson/komga/commit/38ad00c30723a3ecbea02d76c830ff9fb469078d)), closes [#590](https://github.com/gotson/komga/issues/590)

# [0.141.0](https://github.com/gotson/komga/compare/v0.140.0...v0.141.0) (2021-12-31)


## Bug Fixes

* translated using Weblate ([f06d967](https://github.com/gotson/komga/commit/f06d9677b964f2e3f4ed56bf70e4e9ba752b2e30))
* **webui:** edit book dialog would not save or reset properly ([e4b912e](https://github.com/gotson/komga/commit/e4b912e60783b4f2812fdc579dd065664c7cea91))


## Features

* **api:** search series by completeness ([494bdf2](https://github.com/gotson/komga/commit/494bdf28a110c566843e20ae558181fb4e0b4a32))
* **webui:** filter series by completeness ([c3a3fa3](https://github.com/gotson/komga/commit/c3a3fa343b921ef151552ea364d865ef7b11beeb)), closes [#590](https://github.com/gotson/komga/issues/590)

# [0.140.0](https://github.com/gotson/komga/compare/v0.139.0...v0.140.0) (2021-12-29)


## Bug Fixes

* translated using Weblate ([b6f2696](https://github.com/gotson/komga/commit/b6f269641ebee83c412a79fc07ce31202251b09e))
* **webui:** adjust import views display on small screens ([394123d](https://github.com/gotson/komga/commit/394123d26382439a235b07258489b3f942c93b6b))


## Features

* parse translator field from epub ([4f6f85c](https://github.com/gotson/komga/commit/4f6f85c474397baa04f1c1d018403cb25d4674c3))
* parse Web element from ComicInfo.xml ([5a464fd](https://github.com/gotson/komga/commit/5a464fd13e24c881dd3cebb63ea07954e4449aa7)), closes [#750](https://github.com/gotson/komga/issues/750)
* **webui:** display book links ([ff38516](https://github.com/gotson/komga/commit/ff38516b11a15b66c94bedaaf67c47cf94799878)), closes [#750](https://github.com/gotson/komga/issues/750)
* **webui:** edit book links ([7195547](https://github.com/gotson/komga/commit/719554766cb96dde6bcd0f42a335c8a5e840ade4))

# [0.139.0](https://github.com/gotson/komga/compare/v0.138.0...v0.139.0) (2021-12-23)


## Bug Fixes

* translated using Weblate ([5c69c05](https://github.com/gotson/komga/commit/5c69c058fe731ba97ab3a7efbee29fa29b0371cb))
* upgrade lucene version ([4ee30dd](https://github.com/gotson/komga/commit/4ee30dde96b1a9e8170e44512d5f041290b72892))


## Features

* detect JPEG XL images ([610c51f](https://github.com/gotson/komga/commit/610c51fd5d206ce3089f7caba95a91ad5bd6d578))
* **webreader:** add fullscreen keyboard shortcut ([a72a3ba](https://github.com/gotson/komga/commit/a72a3bab627a80df5c29ac6b1d055c96216e52a7))
* **webreader:** detect browser support for JPEG XL ([27f5ba6](https://github.com/gotson/komga/commit/27f5ba6348e7d252f08bcb698666baac7c5461b9))
* **webui:** move read lists import view in import menu ([2529a96](https://github.com/gotson/komga/commit/2529a9623a2bf1aca7df5b7bd6854faa0cdc3af7))

# [0.138.0](https://github.com/gotson/komga/compare/v0.137.0...v0.138.0) (2021-12-22)


## Bug Fixes

* **api:** library scan is always highest priority ([e60acb8](https://github.com/gotson/komga/commit/e60acb86ff4bc156f5b907e9688156cf2b2f9480))
* **webui:** increase height of activity bar ([5fc4013](https://github.com/gotson/komga/commit/5fc4013934965f06f4e136afa3fa25d0fb4d469c))


## Features

* added translation using Weblate (Slovenian) ([7d4d811](https://github.com/gotson/komga/commit/7d4d811af8f181023db850e1eb7fad8c492b77ef))
* **api:** cover upload for books, read lists and collections ([31ad351](https://github.com/gotson/komga/commit/31ad351144fd88ed59ac606af1156c6b66694d3a))
* series and book files deletion ([e626ff8](https://github.com/gotson/komga/commit/e626ff850f0687e227df0fd26c336705c024b273)), closes [#731](https://github.com/gotson/komga/issues/731)

# [0.137.0](https://github.com/gotson/komga/compare/v0.136.0...v0.137.0) (2021-12-14)


## Features

* import Translator from ComicInfo.xml ([fe8c21d](https://github.com/gotson/komga/commit/fe8c21d1ad6fc3ee552bdc17a4cd60397ec3d827)), closes [#740](https://github.com/gotson/komga/issues/740)

# [0.136.0](https://github.com/gotson/komga/compare/v0.135.1...v0.136.0) (2021-12-14)


## Features

* **webreader:** align double pages according to reading direction ([218fd79](https://github.com/gotson/komga/commit/218fd79e517670e2f3f1d9ddb14e649432e10075)), closes [#670](https://github.com/gotson/komga/issues/670)

# [0.135.1](https://github.com/gotson/komga/compare/v0.135.0...v0.135.1) (2021-12-13)


## Bug Fixes

* change default rolling policy for logs ([c1cc96c](https://github.com/gotson/komga/commit/c1cc96cefe6ac512e82b4b9231533d0e46a04418)), closes [#745](https://github.com/gotson/komga/issues/745)
* change default scan interval to every 8 hours ([8eef8d9](https://github.com/gotson/komga/commit/8eef8d99c997a9b42f9af76f5deb0cecdebde3b4))

# [0.135.0](https://github.com/gotson/komga/compare/v0.134.1...v0.135.0) (2021-12-10)


## Features

* **opds:** acquisition feeds are paginated ([734403a](https://github.com/gotson/komga/commit/734403a3663f8b0a5858b85cd389255c5086b6a7)), closes [#572](https://github.com/gotson/komga/issues/572)
* **opds:** add Keep Reading and On Deck ([c9d12d0](https://github.com/gotson/komga/commit/c9d12d042b3463779cc20c39aa52720e7404365e)), closes [#737](https://github.com/gotson/komga/issues/737)
* **opds:** support lastRead attribute on OPSD PSE links ([ce51373](https://github.com/gotson/komga/commit/ce513733446ff4ddbef1fab2cefa14a84bac7a6c))


## Reverts

* remove the OPDS markread feature ([e55f493](https://github.com/gotson/komga/commit/e55f493632f441a51372317988894a0cf333e00e))

# [0.134.1](https://github.com/gotson/komga/compare/v0.134.0...v0.134.1) (2021-12-06)


## Bug Fixes

* exceptions SQLITE_TOOBIG could arise when deleting many books or series ([adf9e14](https://github.com/gotson/komga/commit/adf9e14fb26936f307177886171fa442dae1aec0))

# [0.134.0](https://github.com/gotson/komga/compare/v0.133.0...v0.134.0) (2021-12-02)


## Features

* **opds:** optionally mark progress when streaming pages ([f17bbd5](https://github.com/gotson/komga/commit/f17bbd50769c36038d4fc73aa5cac7b5014e10f6)), closes [#710](https://github.com/gotson/komga/issues/710)

# [0.133.0](https://github.com/gotson/komga/compare/v0.132.4...v0.133.0) (2021-11-30)


## Features

* automatic oauth2 user creation ([fed2294](https://github.com/gotson/komga/commit/fed2294b844133f5763ecfce4b2f6a902d65f2cc)), closes [#716](https://github.com/gotson/komga/issues/716)

# [0.132.4](https://github.com/gotson/komga/compare/v0.132.3...v0.132.4) (2021-11-28)


## Bug Fixes

* **api:** only mark unread book as read for tachiyomi readlist ([6609e4c](https://github.com/gotson/komga/commit/6609e4ce04fa7f9c8b61268d0c8edb127671985d))
* translated using Weblate ([884573e](https://github.com/gotson/komga/commit/884573e20de1b905515d6e3ae5d9fbef55485fd1))

# [0.132.3](https://github.com/gotson/komga/compare/v0.132.2...v0.132.3) (2021-11-22)


## Bug Fixes

* use more specific error code when file is not found during analysis ([ebfc5e1](https://github.com/gotson/komga/commit/ebfc5e13764ee49cfab897f872b3a26e22654b14))
* **webui:** series cover uploads ([#729](https://github.com/gotson/komga/issues/729)) ([32f4d7f](https://github.com/gotson/komga/commit/32f4d7f651ee0243a42e516f7c891d6888fa50db))
* added translation using Weblate (Romanian, Tigrinya) ([9199407](https://github.com/gotson/komga/commit/91994076448bca56600a4d62774548cdcff31384))

# [0.132.2](https://github.com/gotson/komga/compare/v0.132.1...v0.132.2) (2021-11-16)


## Bug Fixes

* **webreader:** jump to previous or next book via keyboard ([ac3d969](https://github.com/gotson/komga/commit/ac3d969a0f79bc1888be8d356caf364dd1e568cb)), closes [#722](https://github.com/gotson/komga/issues/722)

# [0.132.1](https://github.com/gotson/komga/compare/v0.132.0...v0.132.1) (2021-11-07)


## Bug Fixes

* remove session concurrency ([ac05560](https://github.com/gotson/komga/commit/ac0556044f98b70f7bbc737f05dd342c73afae98)), closes [#717](https://github.com/gotson/komga/issues/717)
* series titleSort not updated when folder is renamed ([b972601](https://github.com/gotson/komga/commit/b9726018565f07b067aa65c9c05aeba20bcaa05e)), closes [#718](https://github.com/gotson/komga/issues/718)

# [0.132.0](https://github.com/gotson/komga/compare/v0.131.0...v0.132.0) (2021-10-06)


## Features

* ignore accents and multiple whitespace when sorting books ([cebdef1](https://github.com/gotson/komga/commit/cebdef1e5823b18890741e95d97cb5d032715347)), closes [s#702](https://github.com/s/issues/702)
* remember-me validity can be configured using duration notation ([f592a9e](https://github.com/gotson/komga/commit/f592a9eda2e7209a8d776af6239cbe5dabdc5684))
* **api:** more flexible session management ([a85b5f8](https://github.com/gotson/komga/commit/a85b5f8d2874660c04973a539039ddf7943bcc9f))
* **webui:** accept xAuthToken as query param and convert to session cookie ([e088c76](https://github.com/gotson/komga/commit/e088c76c4e2e5f9e765c299e1e3d2aaa9ab116d4))

# [0.131.0](https://github.com/gotson/komga/compare/v0.130.1...v0.131.0) (2021-10-05)


## Features

* **api:** download read list as zip ([7a176f2](https://github.com/gotson/komga/commit/7a176f23078d14e82981bdea86032f113581f5d2))
* **webui:** download read list as zip ([acdea3d](https://github.com/gotson/komga/commit/acdea3daf1f1667508457b7964e0427ec8ef2591)), closes [#411](https://github.com/gotson/komga/issues/411)
* translated using Weblate (Czech) ([#677](https://github.com/gotson/komga/issues/677)) ([5313cbc](https://github.com/gotson/komga/commit/5313cbced4c84b24ccf59804d1c059812b83c3ca))

# [0.130.1](https://github.com/gotson/komga/compare/v0.130.0...v0.130.1) (2021-09-29)


## Bug Fixes

* **webui:** open oauth2 login in popup ([0a07250](https://github.com/gotson/komga/commit/0a0725058322d6a6f92a8435091644ec4bb1727e))

# [0.130.0](https://github.com/gotson/komga/compare/v0.129.0...v0.130.0) (2021-09-27)


## Features

* **webui:** oauth2 login ([73d8dab](https://github.com/gotson/komga/commit/73d8dab60c92e508a12c037b2bc54c1b19d69770))
* oauth2 login ([7438bf4](https://github.com/gotson/komga/commit/7438bf4c95ec44ef473ee1e8cc9336bb43f26811)), closes [#143](https://github.com/gotson/komga/issues/143)

# [0.129.0](https://github.com/gotson/komga/compare/v0.128.4...v0.129.0) (2021-09-24)


## Features

* **webui:** add link to changelog on version number ([eb67356](https://github.com/gotson/komga/commit/eb67356472887a08eb103bb293241233c06efefd)), closes [#691](https://github.com/gotson/komga/issues/691)
* docker image can load application.yml from /config mounted folder ([8fa0ba6](https://github.com/gotson/komga/commit/8fa0ba6bc21e9c8eb9bec2a8b346f049e4ee3869))

# [0.128.4](https://github.com/gotson/komga/compare/v0.128.3...v0.128.4) (2021-09-23)


## Bug Fixes

* search with only NOT conditions doesn't return results ([ff6861c](https://github.com/gotson/komga/commit/ff6861c5545f4e90d0948c723af8f84b1f8dd1d1)), closes [#694](https://github.com/gotson/komga/issues/694)
* **webreader:** always hide scrollbars ([85ddae4](https://github.com/gotson/komga/commit/85ddae4f4801f0fd0be66fce130b8951fec18b8c)), closes [#640](https://github.com/gotson/komga/issues/640)
* **webreader:** ignore key presses with modifiers ([7898190](https://github.com/gotson/komga/commit/7898190ac49b34afab9e7250e7361405b6168554))
* **webreader:** incorrect page when going to next book ([13760c5](https://github.com/gotson/komga/commit/13760c5ef3117749966e90db9612674f5ab68b33))
* **webui:** hide scrollbar on Firefox for horizontal-scroller ([5d7fdb3](https://github.com/gotson/komga/commit/5d7fdb355794af344f5f3deb7bb0e99e8c9bbc06))

# [0.128.3](https://github.com/gotson/komga/compare/v0.128.2...v0.128.3) (2021-09-21)


## Bug Fixes

* **webui:** more logs in the frontend ([cefd3f0](https://github.com/gotson/komga/commit/cefd3f0ee8b24724ccaf41f00fdf382c0c74a513))

# [0.128.2](https://github.com/gotson/komga/compare/v0.128.1...v0.128.2) (2021-09-20)


## Bug Fixes

* **webui:** add logs in the frontend ([9b2c971](https://github.com/gotson/komga/commit/9b2c971ae77900a21006060a41d3002f66630648))

# [0.128.1](https://github.com/gotson/komga/compare/v0.128.0...v0.128.1) (2021-09-17)


## Bug Fixes

* **api:** missing error messages ([bc774d9](https://github.com/gotson/komga/commit/bc774d961548c6cec3c89d6ba51a30d217bc8a6e))

# [0.128.0](https://github.com/gotson/komga/compare/v0.127.0...v0.128.0) (2021-09-17)


## Bug Fixes

* **webui:** recompute scrollability on mutation ([d2212c6](https://github.com/gotson/komga/commit/d2212c63fc775d702b6f54a658fbbe5704daa883)), closes [#680](https://github.com/gotson/komga/issues/680)


## Features

* **webui:** edit series thumbnails ([6757acf](https://github.com/gotson/komga/commit/6757acfd24b40e8b8258c5a4f9a90164f3a8c1c1))

# [0.127.0](https://github.com/gotson/komga/compare/v0.126.0...v0.127.0) (2021-09-16)


## Bug Fixes

* **api:** add maxNumberSort to TachiyomiReadProgressV2Dto.kt ([5e9cb43](https://github.com/gotson/komga/commit/5e9cb43710a1263aead9fe3479a7a3c4c532535e))


## Features

* **api:** cancel all tasks ([aff4418](https://github.com/gotson/komga/commit/aff4418256bd437d1da40e0bcc612ecfa9312d0a))
* **webui:** cancel all tasks from Server Settings ([3bbb521](https://github.com/gotson/komga/commit/3bbb521bd6284e44cb396cf5a58b189813207744)), closes [#658](https://github.com/gotson/komga/issues/658)

# [0.126.0](https://github.com/gotson/komga/compare/v0.125.4...v0.126.0) (2021-09-15)


## Bug Fixes

* disable http session in database for now ([3448140](https://github.com/gotson/komga/commit/3448140f2d16d35e208d83b65f524aa37b70b7f0))


## Features

* index ngrams to allow partial search ([6e0c51e](https://github.com/gotson/komga/commit/6e0c51ed1d88417b78cb95c01ca17102806726bb))
* index titleSort for series ([817c293](https://github.com/gotson/komga/commit/817c2939b038efa548bb0a348d0dcad60b0e6909)), closes [#626](https://github.com/gotson/komga/issues/626)
* index updater facility on startup ([a7204e8](https://github.com/gotson/komga/commit/a7204e85b986e890f367be558b144d872205990a))

# [0.125.4](https://github.com/gotson/komga/compare/v0.125.3...v0.125.4) (2021-09-14)


## Bug Fixes

* translated using Weblate ([9205951](https://github.com/gotson/komga/commit/92059518cba627fa3253b21966589b5d3dfc0f81))
* **webui:** dashboard would not reload when series read progress changed ([62d378c](https://github.com/gotson/komga/commit/62d378c7175f7e6faf71c625c7d5b164bdcf03e2))
* don't update read progress data upon upgrade or restore ([72d3451](https://github.com/gotson/komga/commit/72d34511409678cc6b4f7a5d8ea9f5d8159565d9))
* don't update read progress for already read books when series is marked as read ([a6164da](https://github.com/gotson/komga/commit/a6164dadb7dfe78b550b5bfcceca516afe5259c1))
* insert batch in chunks ([1d0c578](https://github.com/gotson/komga/commit/1d0c57854c4b4ca63f66228193e64bcf8af2c091)), closes [#654](https://github.com/gotson/komga/issues/654)

# [0.125.3](https://github.com/gotson/komga/compare/v0.125.2...v0.125.3) (2021-09-10)


## Bug Fixes

* **webui:** dashboard and search results pages would reload entirely upon events ([043c4d3](https://github.com/gotson/komga/commit/043c4d3a73498e6a9178e35cfaecc308baa329e4))
* scan fails because of duplicate items by URL ([45d4421](https://github.com/gotson/komga/commit/45d4421cd68c2137cc378d3ade454038a04e8837)), closes [#663](https://github.com/gotson/komga/issues/663)

# [0.125.2](https://github.com/gotson/komga/compare/v0.125.1...v0.125.2) (2021-09-10)


## Bug Fixes

* **webui:** dashboard infinite scroll not working for on deck and new/updated series ([4153603](https://github.com/gotson/komga/commit/415360387931fe01efa305aac4002224efcffac0))

# [0.125.1](https://github.com/gotson/komga/compare/v0.125.0...v0.125.1) (2021-09-10)


## Bug Fixes

* don't update read progress data upon upgrade or restore ([eb9d505](https://github.com/gotson/komga/commit/eb9d505e179311a410291757487dd9a81582853e))
* translated using Weblate ([1eb16b4](https://github.com/gotson/komga/commit/1eb16b4c2ada9b5b2da433a44ec46cc303475ef4))
* **webui:** latest books on dashboard not sorted properly ([b691f30](https://github.com/gotson/komga/commit/b691f3098bda0fce1bb30b01208881527060a730))

# [0.125.0](https://github.com/gotson/komga/compare/v0.124.0...v0.125.0) (2021-09-10)


## Bug Fixes

* **api:** incorrect page information when searching ([24b564a](https://github.com/gotson/komga/commit/24b564a707b24a33112bde3158e03f74f6189a18))
* **api:** page.sort had incorrect value ([57f601b](https://github.com/gotson/komga/commit/57f601b8dd13f8d5c9fb20e2fd9dd71cb24d1c08))


## Features

* **webui:** horizontal scroller infinite scroll on dashboard and search results ([fe78f17](https://github.com/gotson/komga/commit/fe78f17e5e7c0502867e7b08e4f927801b0d26f4)), closes [#605](https://github.com/gotson/komga/issues/605)

# [0.124.0](https://github.com/gotson/komga/compare/v0.123.0...v0.124.0) (2021-09-08)


## Bug Fixes

* **webui:** dashboard could show no data while still loading ([16c35ab](https://github.com/gotson/komga/commit/16c35ab6ff26ccc3ef7c8139830b42779a4242a7))
* **webui:** series card thumbnail flicker when new book thumbnails are added ([0101473](https://github.com/gotson/komga/commit/010147306715638252a88ef8a22aa16e672754aa))


## Features

* store sessions in database ([4568914](https://github.com/gotson/komga/commit/4568914ef716a164a4443ef98269a1a1fa8f7957))

# [0.123.0](https://github.com/gotson/komga/compare/v0.122.0...v0.123.0) (2021-09-06)


## Bug Fixes

* make SeriesMetadata language tag lowercase ([fe2b756](https://github.com/gotson/komga/commit/fe2b756c2f37a022cbc8fdc7b7b530296950abcc)), closes [#661](https://github.com/gotson/komga/issues/661)
* **webui:** display custom roles in book edit dialog ([055fb1c](https://github.com/gotson/komga/commit/055fb1c82721dd018aa343d54585d5791e9f30a7))
* **webui:** display custom roles on book view ([2d14e69](https://github.com/gotson/komga/commit/2d14e698e6fa849bae806a4e7dde470371131621))


## Features

* **webreader:** fit width (shrink only) scale type ([9e9033d](https://github.com/gotson/komga/commit/9e9033dc0eef0947913ed4edb4b484384b0221ec)), closes [#584](https://github.com/gotson/komga/issues/584)
* **webui:** add 'translator' author role ([8ff6c86](https://github.com/gotson/komga/commit/8ff6c8620efeae57516413213c44131916846c3a)), closes [#614](https://github.com/gotson/komga/issues/614)
* **webui:** add custom roles in the book edit dialog ([e7c5c1a](https://github.com/gotson/komga/commit/e7c5c1af39c5d60ee291dfc3e5ed02f811af3848)), closes [#639](https://github.com/gotson/komga/issues/639)

# [0.122.0](https://github.com/gotson/komga/compare/v0.121.0...v0.122.0) (2021-09-06)


## Bug Fixes

* translated using Weblate ([8859241](https://github.com/gotson/komga/commit/8859241885be130e70cf6c1c98c57e52704d8479))


## Features

* **api:** support custom covers for series ([d7470dd](https://github.com/gotson/komga/commit/d7470dd7db785c1393c1a6d3da61446f42205f7f))
* **webreader:** added gray backround option ([1ba6822](https://github.com/gotson/komga/commit/1ba6822fd55ebedf78ec6132aff5338ea2aaa8da))

# [0.121.0](https://github.com/gotson/komga/compare/v0.120.3...v0.121.0) (2021-09-04)


## Features

* **api:** read progress v2 for Tachiyomi ([9d92b25](https://github.com/gotson/komga/commit/9d92b2594d5b32df1963671871185fb76b4e7d30))

# [0.120.3](https://github.com/gotson/komga/compare/v0.120.2...v0.120.3) (2021-09-02)


## Bug Fixes

* scan could fail because of sql statement too long ([19204df](https://github.com/gotson/komga/commit/19204df1c71b185c01195462caf4bd0a147b5c2e)), closes [#650](https://github.com/gotson/komga/issues/650)

# [0.120.2](https://github.com/gotson/komga/compare/v0.120.1...v0.120.2) (2021-09-01)


## Bug Fixes

* **api:** prevent retrieving own user activity in demo profile ([59c187f](https://github.com/gotson/komga/commit/59c187f3b769550e788a5fbf8031fc59fa33b41b)), closes [#643](https://github.com/gotson/komga/issues/643)
* **webui:** catch exception during authentication activity retrieval ([e4b136b](https://github.com/gotson/komga/commit/e4b136b28d794136b60d513e166ff8014cd9cf82))
* **webui:** don't display nothing to show while still loading ([dce4dde](https://github.com/gotson/komga/commit/dce4dde6b8a87a5b5a910181dcf23cf8fa3ba3a1)), closes [#648](https://github.com/gotson/komga/issues/648)

# [0.120.1](https://github.com/gotson/komga/compare/v0.120.0...v0.120.1) (2021-08-31)


## Bug Fixes

* **webui:** support pl pluralization ([20afd83](https://github.com/gotson/komga/commit/20afd83356d1a153290b73cc7750e4140d27a34f))

# [0.120.0](https://github.com/gotson/komga/compare/v0.119.2...v0.120.0) (2021-08-31)


## Bug Fixes

* scan could fail with latest sqlite library ([f8cd7df](https://github.com/gotson/komga/commit/f8cd7dfcf7bafe80a196fa8054575562d2cca1ad)), closes [#644](https://github.com/gotson/komga/issues/644)


## Features

* translated using Weblate (Indonesian) ([#619](https://github.com/gotson/komga/issues/619)) ([bde858f](https://github.com/gotson/komga/commit/bde858f58e17e6f1151469127b7b8767951b43d9))

# [0.119.2](https://github.com/gotson/komga/compare/v0.119.1...v0.119.2) (2021-08-30)


## Bug Fixes

* better unicode sorting ([773858e](https://github.com/gotson/komga/commit/773858eddd67dd242774c88d3387e1493e730173))

# [0.119.1](https://github.com/gotson/komga/compare/v0.119.0...v0.119.1) (2021-08-24)


## Bug Fixes

* **webui:** better image loading for cards ([f0476ab](https://github.com/gotson/komga/commit/f0476ab890825ed730cf50a488f45d33822f9030))

# [0.119.0](https://github.com/gotson/komga/compare/v0.118.0...v0.119.0) (2021-08-24)


## Features

* **webui:** display library name in book/series screen, empty all trash ([1d3f4e4](https://github.com/gotson/komga/commit/1d3f4e42472cae446e53ceacbbe181f48e88a54b))

# [0.118.0](https://github.com/gotson/komga/compare/v0.117.0...v0.118.0) (2021-08-20)


## Bug Fixes

* skip duplicate books during reading list import ([b528b3d](https://github.com/gotson/komga/commit/b528b3d56d02eb837adbe8bd19b21128f0eaf884)), closes [#622](https://github.com/gotson/komga/issues/622)


## Features

* **api:** batch update book metadata ([ae9a19a](https://github.com/gotson/komga/commit/ae9a19af62d35309854b69de6e6e2b4cafcedae4))
* **importer:** delete sidecars when upgrading book ([9443f7e](https://github.com/gotson/komga/commit/9443f7e3c715b3da0ea801ea67ff4cf700958469)), closes [#624](https://github.com/gotson/komga/issues/624)
* **webui:** bulk edit book metadata ([4bab0c6](https://github.com/gotson/komga/commit/4bab0c61c7ce7b353d9e06da3d92c3f68cf9b361)), closes [#476](https://github.com/gotson/komga/issues/476)

# [0.117.0](https://github.com/gotson/komga/compare/v0.116.3...v0.117.0) (2021-08-16)


## Features

* **importer:** import sidecars alongside books ([a3c3a48](https://github.com/gotson/komga/commit/a3c3a48038ecde6a1be5c4795047f8a37fbb6e11)), closes [#611](https://github.com/gotson/komga/issues/611)

# [0.116.3](https://github.com/gotson/komga/compare/v0.116.2...v0.116.3) (2021-08-16)


## Bug Fixes

* **scanner:** fail scan if root folder is unavailable ([871ec60](https://github.com/gotson/komga/commit/871ec60869fe57d2c823b79006a6f5d38c5b9372)), closes [#617](https://github.com/gotson/komga/issues/617)
* **webui:** display series/books as unavailable if library is unavailable ([82cf82d](https://github.com/gotson/komga/commit/82cf82df384b82479837fabf917ee840a84d4dba)), closes [#617](https://github.com/gotson/komga/issues/617)

# [0.116.2](https://github.com/gotson/komga/compare/v0.116.1...v0.116.2) (2021-08-16)


## Bug Fixes

* **scanner:** don't ignore files with reparse point ([d4b9f5d](https://github.com/gotson/komga/commit/d4b9f5d7c453f677e76092b569be16ae76e20e5b)), closes [#616](https://github.com/gotson/komga/issues/616)

# [0.116.1](https://github.com/gotson/komga/compare/v0.116.0...v0.116.1) (2021-08-12)


## Bug Fixes

* empty trash is not library specific ([d68f70a](https://github.com/gotson/komga/commit/d68f70acb3dfff94756e6d92a5363b79dac66a68)), closes [#612](https://github.com/gotson/komga/issues/612)
* translated using Weblate ([c86a99e](https://github.com/gotson/komga/commit/c86a99e315fd4f45078aace78e7ce221b8d7f883))

# [0.116.0](https://github.com/gotson/komga/compare/v0.115.1...v0.116.0) (2021-08-09)


## Features

* better full text search ([5aa9a95](https://github.com/gotson/komga/commit/5aa9a95ca5a88739705e879349787fb8622b275d)), closes [#592](https://github.com/gotson/komga/issues/592) [#597](https://github.com/gotson/komga/issues/597)

# [0.115.1](https://github.com/gotson/komga/compare/v0.115.0...v0.115.1) (2021-08-08)


## Bug Fixes

* support for armv6 ([eb1fc4a](https://github.com/gotson/komga/commit/eb1fc4a49f17a225b7f5b6095cc8e8dfecf98f00)), closes [#398](https://github.com/gotson/komga/issues/398)

# [0.115.0](https://github.com/gotson/komga/compare/v0.114.0...v0.115.0) (2021-08-06)


## Bug Fixes

* **webui:** better add to home screen support ([43733fb](https://github.com/gotson/komga/commit/43733fbec3a01bd25295d806e0a38af0db858bd3))


## Features

* **webreader:** fullscreen support ([0a1761d](https://github.com/gotson/komga/commit/0a1761d37e0cbf14bc84f048ca9e8f8517ac93c0))

# [0.114.0](https://github.com/gotson/komga/compare/v0.113.0...v0.114.0) (2021-08-02)


## Features

* **webui:** full screen support when adding to home on mobile ([20bccb5](https://github.com/gotson/komga/commit/20bccb57ec5c65e53425c46afec174ebacc8d9a2)), closes [#565](https://github.com/gotson/komga/issues/565)

# [0.113.0](https://github.com/gotson/komga/compare/v0.112.2...v0.113.0) (2021-07-30)


## Features

* **api:** add total book count field for series metadata ([87c1432](https://github.com/gotson/komga/commit/87c14329848667ff3d223e15e29070eb5f15d2c8)), closes [#298](https://github.com/gotson/komga/issues/298)
* **api:** filter read lists books by library, read status, tags, or authors ([c06f89f](https://github.com/gotson/komga/commit/c06f89ffc2c876b1ef3bffd3ed20697c5f822f5a))
* **api:** search authors and tags by read list ([422876a](https://github.com/gotson/komga/commit/422876ae2fd676459ae84f54d9bd6878828d714d))
* **api:** summary field for read lists ([1148e46](https://github.com/gotson/komga/commit/1148e46d9043df134549bc6f3911674f1898b3e0)), closes [#558](https://github.com/gotson/komga/issues/558)
* **webui:** add filter panel in BrowseReadList view ([b8effd4](https://github.com/gotson/komga/commit/b8effd44531de86b6392b24bf7ed0ea2eb25756f)), closes [#580](https://github.com/gotson/komga/issues/580)
* **webui:** add summary for read list browsing and edit dialog ([883fed3](https://github.com/gotson/komga/commit/883fed39408b8f55920937075a05d1939e5c008f)), closes [#558](https://github.com/gotson/komga/issues/558)
* **webui:** display and filter series aggregated book tags ([bb1eb36](https://github.com/gotson/komga/commit/bb1eb36daec6d983300702487163d49211b94543)), closes [#513](https://github.com/gotson/komga/issues/513)
* aggregate book tags at series level ([0c9a063](https://github.com/gotson/komga/commit/0c9a063cc3d28a6ada2eedec0ffd6e72eee378ba)), closes [#513](https://github.com/gotson/komga/issues/513)
* import total book count from ComicInfo.xml and Mylar series.json ([64acfef](https://github.com/gotson/komga/commit/64acfeff99a732d3819bb9b012f020369cece325)), closes [#450](https://github.com/gotson/komga/issues/450)
* **webui:** show and edit total book count for series ([6090e3f](https://github.com/gotson/komga/commit/6090e3f0c5ac9dffa7768fd5f3948c23c6a5f8a9)), closes [#298](https://github.com/gotson/komga/issues/298)

# [0.112.2](https://github.com/gotson/komga/compare/v0.112.1...v0.112.2) (2021-07-30)


## Bug Fixes

* **api:** ignore search parameter when blank ([48637e2](https://github.com/gotson/komga/commit/48637e26e4f8a17a3501a15bcc6f96ac4473243b))

# [0.112.1](https://github.com/gotson/komga/compare/v0.112.0...v0.112.1) (2021-07-29)


## Bug Fixes

* skip old file hashing tasks if feature is disabled ([01a16b7](https://github.com/gotson/komga/commit/01a16b718ae732509ac58135e323cee941b9435e)), closes [#585](https://github.com/gotson/komga/issues/585)

# [0.112.0](https://github.com/gotson/komga/compare/v0.111.0...v0.112.0) (2021-07-29)


## Bug Fixes

* translated using Weblate (German) ([#579](https://github.com/gotson/komga/issues/579)) ([dd63a0f](https://github.com/gotson/komga/commit/dd63a0f1bbb64e7b5df65f7582d295e318b0436b))


## Features

* sort series by release date ([d15c130](https://github.com/gotson/komga/commit/d15c130530890398d39fd03c282cf005caefad96)), closes [#582](https://github.com/gotson/komga/issues/582)

# [0.111.0](https://github.com/gotson/komga/compare/v0.110.0...v0.111.0) (2021-07-29)


## Features

* **api:** full text search ([8f27faf](https://github.com/gotson/komga/commit/8f27faf4172bcdd733709241be6a67028d0ae737)), closes [#24](https://github.com/gotson/komga/issues/24) [#496](https://github.com/gotson/komga/issues/496)

# [0.110.0](https://github.com/gotson/komga/compare/v0.109.1...v0.110.0) (2021-07-26)


## Bug Fixes

* translated using Weblate ([770f2fa](https://github.com/gotson/komga/commit/770f2fa2162599669f455766f788dd4775cd9396))


## Features

* **webui:** add library option to choose series cover ([afe2b64](https://github.com/gotson/komga/commit/afe2b64a15ab68f8db10f13827c5c41ed6ca902a)), closes [#312](https://github.com/gotson/komga/issues/312)
* **webui:** add recently read books section on dashboard ([f4527ec](https://github.com/gotson/komga/commit/f4527ec1fa9c4436d19178245b6f7566f19308be))
* add library option to choose series cover ([8e94b8e](https://github.com/gotson/komga/commit/8e94b8e4448b7600433ceb6850b02d97afb4414a)), closes [#312](https://github.com/gotson/komga/issues/312)

# [0.109.1](https://github.com/gotson/komga/compare/v0.109.0...v0.109.1) (2021-07-26)


## Bug Fixes

* **api:** accent insensitive search ([30c349a](https://github.com/gotson/komga/commit/30c349afaffdcd307a422653895e4fe91dc47f29))
* **api:** accent insensitive sort ([af837c6](https://github.com/gotson/komga/commit/af837c68130b40872b04b75f7ed7c74729369706)), closes [#500](https://github.com/gotson/komga/issues/500)
* **webui:** clearer error message on login error ([ca054f6](https://github.com/gotson/komga/commit/ca054f642f6119552234656aaf3a7ede7bd8d655))
* **webui:** incorrect data reload on received events ([44bd09a](https://github.com/gotson/komga/commit/44bd09ac0b2d8420b25d84f355860046cc978188))

# [0.109.0](https://github.com/gotson/komga/compare/v0.108.0...v0.109.0) (2021-07-21)


## Features

* **api:** count series by first letter ([199c619](https://github.com/gotson/komga/commit/199c6190a3c7fa8d0aa91935333aa23fa20ff42e))
* **api:** search series by regex ([1fe5580](https://github.com/gotson/komga/commit/1fe55809a1e58c299151433c17889b9d4eb81a81))
* **webui:** alphabetical navigation for libraries ([5d747d2](https://github.com/gotson/komga/commit/5d747d2cd3431410eb84554a8afd55086662eb51)), closes [#186](https://github.com/gotson/komga/issues/186)

# [0.108.0](https://github.com/gotson/komga/compare/v0.107.0...v0.108.0) (2021-07-19)


## Bug Fixes

* **webui:** add hint to language field in EditSeriesDialog.vue ([fa8b4c3](https://github.com/gotson/komga/commit/fa8b4c34770769cb2c8a3891a165d02934228964))


## Features

* **api:** add released_after query parameter for /books endpoint ([f6dc546](https://github.com/gotson/komga/commit/f6dc546fd99b5e68fd560616b1ddca530dbbaab1))
* **webui:** show recently released books in the dashboard ([320450a](https://github.com/gotson/komga/commit/320450a7507e901246072be091152ebfd14aee5d)), closes [#569](https://github.com/gotson/komga/issues/569)
* import mylar metadata ([528f676](https://github.com/gotson/komga/commit/528f676ce0fca3a4a1162aebf35076e247154741)), closes [#550](https://github.com/gotson/komga/issues/550)
* import StoryArcNumber from ComicInfo.xml ([2012f8b](https://github.com/gotson/komga/commit/2012f8be98561d834009a4d73a0245c7b2da753c)), closes [#573](https://github.com/gotson/komga/issues/573)
* refresh series metadata if series.json file changed ([5cc14b5](https://github.com/gotson/komga/commit/5cc14b57f84e1f817388b788b687e0bf1117da48))
* **webui:** library options to import mylar metadata ([e6602c6](https://github.com/gotson/komga/commit/e6602c60be9fc04c0faeea6d732028deb66bd985))

# [0.107.0](https://github.com/gotson/komga/compare/v0.106.1...v0.107.0) (2021-07-19)


## Bug Fixes

* **scanner:** better detect changes even if file last modified have not change after a rename ([7b8cf7a](https://github.com/gotson/komga/commit/7b8cf7ae25e51d8f0871fefbd2fc586618233538))


## Features

* configuration property to disable file hashing ([52db0a1](https://github.com/gotson/komga/commit/52db0a168628a38a0d3f1e4a5c26adabd260e57a))
* restore books and series from trash bin ([bc25c11](https://github.com/gotson/komga/commit/bc25c119900f15293253c95021d230ea41a42159)), closes [#217](https://github.com/gotson/komga/issues/217) [#383](https://github.com/gotson/komga/issues/383)
* **api:** add endpoint to empty trash per library ([4dac73e](https://github.com/gotson/komga/commit/4dac73ea9f154e9f3644d3c042ec7f1e63939416))
* **api:** add query parameter to filter on deleted series or books ([c1d34e4](https://github.com/gotson/komga/commit/c1d34e430cb0b7138742928cbfd5da05d22a0c59))
* **opds:** do not show soft deleted books/series ([5b6b817](https://github.com/gotson/komga/commit/5b6b8170856ea85f487041ad003eb80b9b36996d))
* **scanner:** soft delete series and books ([f0664e9](https://github.com/gotson/komga/commit/f0664e97918752d2d6f795017a5e0379d305b8c2))
* **webui:** display an outdated badge on book details view ([c955bed](https://github.com/gotson/komga/commit/c955bed79d1a309ada585972008b5ce68e0708d1))
* **webui:** library option to automatically empty trash after scan ([fc06b4a](https://github.com/gotson/komga/commit/fc06b4a987ed43c4754b2e3691493ed2093ac64a))
* generate file hash for books ([7ad738a](https://github.com/gotson/komga/commit/7ad738a64551b69b1a657119456f69feb9e99f30))
* library option to automatically empty trash after scan ([21781a3](https://github.com/gotson/komga/commit/21781a3a23074a942e571e745ff5e614a7291799))
* **webui:** add library action menu item to empty trash ([31fbf2a](https://github.com/gotson/komga/commit/31fbf2a829b4fc0f6a246178935ffc2598df10b0))
* **webui:** show soft deleted books/series as unavailable ([d946600](https://github.com/gotson/komga/commit/d946600a646b67638226905a2359502165892ed1))

# [0.106.1](https://github.com/gotson/komga/compare/v0.106.0...v0.106.1) (2021-07-08)


## Bug Fixes

* **webui:** show different empty state if library is empty ([68a9f7a](https://github.com/gotson/komga/commit/68a9f7a79c8d45e0ed47b44dd3561de7aa44e2ba))

# [0.106.0](https://github.com/gotson/komga/compare/v0.105.2...v0.106.0) (2021-07-06)


## Features

* **sse:** publish breakdown of task count by task type ([eda767a](https://github.com/gotson/komga/commit/eda767aeb5c9291bab69bebb9cb14591247344ed))
* **webui:** display task count by type in activity bar tooltip ([6b690bc](https://github.com/gotson/komga/commit/6b690bcdfb25080f2a6c6df37b03d53d2c868b94))

# [0.105.2](https://github.com/gotson/komga/compare/v0.105.1...v0.105.2) (2021-07-05)


## Bug Fixes

* translated using Weblate (Danish) ([9f2e2dc](https://github.com/gotson/komga/commit/9f2e2dca14ace0b65a5a570cef763e2dc5d57a9a))
* **webui:** clear searchbox results on blur ([e1a2cec](https://github.com/gotson/komga/commit/e1a2cecce409cb236d5aa4bf375bfe2bea49b7c9))
* **webui:** close notification when clicking on primary action ([403ab0a](https://github.com/gotson/komga/commit/403ab0a04f7a6e619fe3f94fd3ecea865c4b24c5))
* **webui:** logo alignment on startup page ([1de8a94](https://github.com/gotson/komga/commit/1de8a944bdc2db99dc644866aea1793857e09c45))
* **webui:** show empty state if there's not books to import ([7ddbac9](https://github.com/gotson/komga/commit/7ddbac95be63a3867d0d544e3d722b257cf1d19e))

# [0.105.1](https://github.com/gotson/komga/compare/v0.105.0...v0.105.1) (2021-07-02)


## Bug Fixes

* **webui:** disable some UI elements for non-admins ([ada1438](https://github.com/gotson/komga/commit/ada1438921446d81c56a403c0f2083b5781a0f02))
* **webui:** properly reload series when book read progress is updated ([92954e0](https://github.com/gotson/komga/commit/92954e091675b121fb53a1846a559fa5fed92a3b))

# [0.105.0](https://github.com/gotson/komga/compare/v0.104.2...v0.105.0) (2021-07-02)


## Bug Fixes

* **webui:** more emphasis on main button in dialogs ([f0d32d9](https://github.com/gotson/komga/commit/f0d32d98a582c6dfa8178ee5ed81b3f97d3000e5))


## Features

* **webui:** confirmation dialog for library analysis and refresh ([9923cea](https://github.com/gotson/komga/commit/9923cea244476ce5743beb1ce3872a76575d4436))

# [0.104.2](https://github.com/gotson/komga/compare/v0.104.1...v0.104.2) (2021-07-02)


## Bug Fixes

* **scanner:** crash if library root is filesystem root ([d739166](https://github.com/gotson/komga/commit/d7391669f696e7de61b2a52cef3bf12c36072227)), closes [#564](https://github.com/gotson/komga/issues/564)
* **swagger:** swagger-ui hangs on some calls ([76caa4d](https://github.com/gotson/komga/commit/76caa4de39e1c7c2c3a8e3440a4c4eeec2d0bd7f)), closes [#566](https://github.com/gotson/komga/issues/566)

# [0.104.1](https://github.com/gotson/komga/compare/v0.104.0...v0.104.1) (2021-06-30)


## Performance Improvements

* optimize database transactions to avoid locking ([39dcf59](https://github.com/gotson/komga/commit/39dcf5969ed435fd68ed9d439b6538a2bf832315))

# [0.104.0](https://github.com/gotson/komga/compare/v0.103.0...v0.104.0) (2021-06-29)


## Features

* **api:** get latest activity by user ([b118959](https://github.com/gotson/komga/commit/b11895977544c94aa89c74b6a6a94d6c556c344e))
* **sse:** publish event when series is completely marked as read or unread ([11c5802](https://github.com/gotson/komga/commit/11c5802255b2fbf7f4f03db2393bd4db1b4c834c))
* **webui:** display badge if some books are in error or unsupported ([101c6fd](https://github.com/gotson/komga/commit/101c6fd92d4cf707f023ea6c8a6af1255534da02))
* **webui:** display latest user connection in settings ([58478c2](https://github.com/gotson/komga/commit/58478c21ff59a4239dfcd24ced427be911c3e25d))
* **webui:** multi-select collections and read lists ([19e3f18](https://github.com/gotson/komga/commit/19e3f18cad28de14a23eba81745d73d069059f29))
* **webui:** select all option in the selection bar ([8545574](https://github.com/gotson/komga/commit/8545574d387e7242291f2a5eb8f9ce81bbda3d73))
* **webui:** select multiple items using shift+click ([f69a31e](https://github.com/gotson/komga/commit/f69a31eaf1ed62ba2a173ee78116008a4cef0ec1))


## Performance Improvements

* **webui:** throttle reloads, remove legacy reload logic ([e8a7eaf](https://github.com/gotson/komga/commit/e8a7eaf999b7cd4d239fec665c90eece29546f53))

# [0.103.0](https://github.com/gotson/komga/compare/v0.102.0...v0.103.0) (2021-06-28)


## Bug Fixes

* **api:** total item count incorrect for /me/authentication-activity ([a56c2f1](https://github.com/gotson/komga/commit/a56c2f1d2d2daa17bbd7080afda4ab30194ba86b))


## Features

* **api:** admin can change password for any user ([30303a6](https://github.com/gotson/komga/commit/30303a6df3f1d37db0cbf38de734542a1eaa2876)), closes [#503](https://github.com/gotson/komga/issues/503)
* **webui:** enable grouping for authentication activity table ([2a19399](https://github.com/gotson/komga/commit/2a1939934776ec9d3cc2d98c8d4b5446454bcde3))
* **webui:** password change from user settings screen ([668331e](https://github.com/gotson/komga/commit/668331eb00f64f430b0bdf627184b05bcff8ad78)), closes [#503](https://github.com/gotson/komga/issues/503)

# [0.102.0](https://github.com/gotson/komga/compare/v0.101.2...v0.102.0) (2021-06-25)


## Features

* **api:** store authentication activity ([de96e0d](https://github.com/gotson/komga/commit/de96e0dcef1312aaed0d253574734a9e027722ca)), closes [#160](https://github.com/gotson/komga/issues/160)
* **webui:** display authentication activity ([9d33602](https://github.com/gotson/komga/commit/9d33602873b15668fe978dd66148287d4f9744dd)), closes [#160](https://github.com/gotson/komga/issues/160)

# [0.101.2](https://github.com/gotson/komga/compare/v0.101.1...v0.101.2) (2021-06-24)


## Bug Fixes

* deleting series could fail with foreign key error ([97b53e5](https://github.com/gotson/komga/commit/97b53e5c962cf848cf7d62fb4fe0ede94479a554))
* fix potential database inconsistencies ([75ecbe1](https://github.com/gotson/komga/commit/75ecbe15baee9c91d209675cd20f488fff907fce))
* use spring transactions instead of jooq transactions ([73931f0](https://github.com/gotson/komga/commit/73931f0bf80e39e1806e877c3f97d40e583735a7))
* **webui:** sort by size in media analysis table ([4d2393f](https://github.com/gotson/komga/commit/4d2393f500fafc9dd7cfdd08b7830089936bc30c))

# [0.101.1](https://github.com/gotson/komga/compare/v0.101.0...v0.101.1) (2021-06-23)


## Bug Fixes

* **webui:** add contain property to thumbnail in searchbox ([15bad4b](https://github.com/gotson/komga/commit/15bad4b20a8213dc489ba83affc57a00763e468c))
* **webui:** add thumbnail to series picker dialog ([cb096e2](https://github.com/gotson/komga/commit/cb096e21175f83ea73ca0bbc5a4391a5b684355d))
* **webui:** don't reload card thumbnail if one was successfully loaded ([10cdedc](https://github.com/gotson/komga/commit/10cdedcf5d6e35c0a5928d0e92761832f699b099))

# [0.101.0](https://github.com/gotson/komga/compare/v0.100.3...v0.101.0) (2021-06-23)


## Features

* **webui:** remove early feature warning (extension repair, convert to cbz) ([6af6f5b](https://github.com/gotson/komga/commit/6af6f5be405ace295d704deab3a12b769397ece5))
* **webui:** remove early feature warning in book import screen ([3d159d1](https://github.com/gotson/komga/commit/3d159d16b27e41dbc53d4267ba0527ae90e11dc8))
* **webui:** show library name on series picker dialog (import) ([626ef3e](https://github.com/gotson/komga/commit/626ef3eb40c82f8107f465d90fa9854504547a94)), closes [#559](https://github.com/gotson/komga/issues/559)

# [0.100.3](https://github.com/gotson/komga/compare/v0.100.2...v0.100.3) (2021-06-23)


## Bug Fixes

* **epub:** better isbn parsing ([e1741c3](https://github.com/gotson/komga/commit/e1741c34fa88d188f703fa2d85f005b50d0c475e))
* **epub:** incorrect genres parsing when empty ([8a5abf2](https://github.com/gotson/komga/commit/8a5abf24fb9d6679507f8ce6599450e6997c6ead))
* **epub:** parse multiple dc:subjects for series genre ([c25b9cf](https://github.com/gotson/komga/commit/c25b9cf5f91e8cca404c53fbc2f481264b60dcaa))
* **epub:** series title not always parsed properly ([a6c19f6](https://github.com/gotson/komga/commit/a6c19f6eee9a965ad470d7426a07f3a32b3c3890)), closes [#556](https://github.com/gotson/komga/issues/556)

# [0.100.2](https://github.com/gotson/komga/compare/v0.100.1...v0.100.2) (2021-06-22)


## Bug Fixes

* better date parsing for epub metadata ([ed7fa1f](https://github.com/gotson/komga/commit/ed7fa1fcb8c077f14b42df86ecf7c4674114a73b))
* better isbn parsing for epub metadata ([b3bcd23](https://github.com/gotson/komga/commit/b3bcd233fda5e209377c8452917655d83906e6f7))
* do not split authors by comma in epub metadata ([13b18c0](https://github.com/gotson/komga/commit/13b18c0b457dfee81a824328093b52a8e7a0b777)), closes [#556](https://github.com/gotson/komga/issues/556)
* epub parsing namespace issue ([7a56632](https://github.com/gotson/komga/commit/7a566326b089e6ce91fc5c660b2c03a374306616))
* ignore html tags in epub description ([52d81ef](https://github.com/gotson/komga/commit/52d81ef1d7ab5d9ae813b5693e6861321a7f96ff)), closes [#556](https://github.com/gotson/komga/issues/556)
* retrieve series from epub correctly ([fbd8655](https://github.com/gotson/komga/commit/fbd8655c51b752eeba939a13fca3d7dc96dfb40e)), closes [#556](https://github.com/gotson/komga/issues/556)
* some images could be missing from epub files ([6c34794](https://github.com/gotson/komga/commit/6c34794a505ca1fbfdc25593caced45037b85cb9)), closes [#556](https://github.com/gotson/komga/issues/556)

# [0.100.1](https://github.com/gotson/komga/compare/v0.100.0...v0.100.1) (2021-06-21)


## Bug Fixes

* **webui:** query param doesn't work with values containing comma ([b447101](https://github.com/gotson/komga/commit/b447101bccda6b4b31a80c048288346026e8292c)), closes [#557](https://github.com/gotson/komga/issues/557)

# [0.100.0](https://github.com/gotson/komga/compare/v0.99.4...v0.100.0) (2021-06-21)


## Features

* **sse:** publish server-sent events ([691c7f0](https://github.com/gotson/komga/commit/691c7f0071009d26dd6ec2073d4922aa9a7e60d0))
* **webui:** the UI is now dynamic to events from the server ([a707fd3](https://github.com/gotson/komga/commit/a707fd359490d032bc65782cc1009d1e40e4b51a)), closes [#124](https://github.com/gotson/komga/issues/124)

# [0.99.4](https://github.com/gotson/komga/compare/v0.99.3...v0.99.4) (2021-06-18)


## Bug Fixes

* some tasks could be executed with the wrong priority ([2b6f534](https://github.com/gotson/komga/commit/2b6f534f848f5b8fc926558d03edc3a594a9f2e2))

# [0.99.3](https://github.com/gotson/komga/compare/v0.99.2...v0.99.3) (2021-06-17)


## Bug Fixes

* sidecars would not be deleted with library ([49f83b7](https://github.com/gotson/komga/commit/49f83b78ace92aeb6a42e1a66f2042655a3b41cd))

# [0.99.2](https://github.com/gotson/komga/compare/v0.99.1...v0.99.2) (2021-06-17)


## Bug Fixes

* local artwork could be refreshed at every scan ([4ac9fe9](https://github.com/gotson/komga/commit/4ac9fe96bed2c55ba3584853cc47f75de6601bee))

# [0.99.1](https://github.com/gotson/komga/compare/v0.99.0...v0.99.1) (2021-06-10)


## Bug Fixes

* translated using Weblate (French) ([#549](https://github.com/gotson/komga/issues/549)) ([37a66fe](https://github.com/gotson/komga/commit/37a66feb27a48befb2a4297eb300306c807e3d37))
* **webui:** incorrect spacing in dialog ([8e1ef16](https://github.com/gotson/komga/commit/8e1ef165a810145d5f4a7b2d231e51e24fc5a016))

# [0.99.0](https://github.com/gotson/komga/compare/v0.98.0...v0.99.0) (2021-06-10)


## Bug Fixes

* **api:** publisher and authors were not sorted lowercase ([78c6d23](https://github.com/gotson/komga/commit/78c6d23295b8e81425c321186f2cda71eb02eb6e))


## Features

* detect change in sidecar files during scan ([4244bcd](https://github.com/gotson/komga/commit/4244bcd9ae58b696f6d004025285fddf329c4c93))

# [0.98.0](https://github.com/gotson/komga/compare/v0.97.3...v0.98.0) (2021-06-08)


## Features

* delete empty read lists and collections ([159c767](https://github.com/gotson/komga/commit/159c767929be90ea326086a2354008ab7b52d290)), closes [#551](https://github.com/gotson/komga/issues/551)

# [0.97.3](https://github.com/gotson/komga/compare/v0.97.2...v0.97.3) (2021-06-03)


## Bug Fixes

* translated using Weblate ([45f2bb4](https://github.com/gotson/komga/commit/45f2bb47f902ad579b85799927033261daf2f56d))
* **api:** updating read progress from tachiyomi would mess up On Deck ([480871c](https://github.com/gotson/komga/commit/480871c966eba64ad74356254765cbd09c8682b8))

# [0.97.2](https://github.com/gotson/komga/compare/v0.97.1...v0.97.2) (2021-06-01)


## Bug Fixes

* **webui:** replace searchbox for authors in filter panel ([14e6718](https://github.com/gotson/komga/commit/14e6718252be074deae4e32dfd17d47e7b498cdf))

# [0.97.1](https://github.com/gotson/komga/compare/v0.97.0...v0.97.1) (2021-06-01)


## Bug Fixes

* **webui:** favicon for iOS, Android and Windows 10 ([#547](https://github.com/gotson/komga/issues/547)) ([da99052](https://github.com/gotson/komga/commit/da990529548a596f90b91fc3806946270c06c3b6))

# [0.97.0](https://github.com/gotson/komga/compare/v0.96.5...v0.97.0) (2021-05-31)


## Bug Fixes

* series would be updated at each scan even if not modified ([d05237f](https://github.com/gotson/komga/commit/d05237f5ed6b845f0ff05527a505ed7bea7a2698))
* **webui:** fix combobox search value remaining after list selection ([d114b0e](https://github.com/gotson/komga/commit/d114b0efb277f98b0361acc413f94d384654f6f2))


## Features

* **api:** search authors by name and role ([a45a73c](https://github.com/gotson/komga/commit/a45a73c8bd33667a8f7f254b43c8c69f5a76b19f))
* **webui:** filter series by read and in progress status ([4195ecb](https://github.com/gotson/komga/commit/4195ecbb9a4d53d8ef37ec5820b77fd0ae568ffe))
* **webui:** in progress and read filter for series and collection ([b41499d](https://github.com/gotson/komga/commit/b41499d77593aa3b32154959658669edb76c592e))
* **webui:** reset filter button ([3219dc4](https://github.com/gotson/komga/commit/3219dc4bb2c721b92d25861edb281c5501ceba83))
* **webui:** search authors in filters ([b908ac1](https://github.com/gotson/komga/commit/b908ac140b1d942b3e45c76b2094ab0d7f598f0d))


## Performance Improvements

* **webui:** load background data in parallel when possible ([c0d7be9](https://github.com/gotson/komga/commit/c0d7be9627923c52516054ffd00b9d3f339b0beb))

# [0.96.5](https://github.com/gotson/komga/compare/v0.96.4...v0.96.5) (2021-05-27)


## Bug Fixes

* **webui:** show recent books before series ([7f49bfa](https://github.com/gotson/komga/commit/7f49bfa05e20ae4adb5ab40244ae9b770c1c2348))


## Performance Improvements

* add db index to speed up webui dashboard ([ecc1dd4](https://github.com/gotson/komga/commit/ecc1dd412dcae1409bba8e5d4cf42471f7f303a5))
* precompute series book counts ([c3b352a](https://github.com/gotson/komga/commit/c3b352aca03a7a859ffdb8f4a95c504b91981348))

# [0.96.4](https://github.com/gotson/komga/compare/v0.96.3...v0.96.4) (2021-05-25)


## Performance Improvements

* missing database index ([8b5ae64](https://github.com/gotson/komga/commit/8b5ae64dd5ae62c3562405f1b280e9eb91e4a6e8))
* remove subquery ([b162cde](https://github.com/gotson/komga/commit/b162cdeba39b1a28af0188de24b429a1a15cbab0))

# [0.96.3](https://github.com/gotson/komga/compare/v0.96.2...v0.96.3) (2021-05-24)


## Performance Improvements

* page streaming performance ([8de01a6](https://github.com/gotson/komga/commit/8de01a6fd760a8bd80aa75ff2055a3138f3476bf))

# [0.96.2](https://github.com/gotson/komga/compare/v0.96.1...v0.96.2) (2021-05-22)


## Bug Fixes

* cors filter causing issues ([0708ce7](https://github.com/gotson/komga/commit/0708ce750c9d003fbd3430e864b1571cebbabb5c)), closes [#543](https://github.com/gotson/komga/issues/543)

# [0.96.1](https://github.com/gotson/komga/compare/v0.96.0...v0.96.1) (2021-05-21)


## Bug Fixes

* **webreader:** incorrect tooltip for read incognito button ([d1616a9](https://github.com/gotson/komga/commit/d1616a98cd96a81e996752c7127720f162404e54))
* **webui:** count in progress books as unread in series filter and card ([fa9d40f](https://github.com/gotson/komga/commit/fa9d40f84c7d02fa164472c86ac549ae3999c418))

# [0.96.0](https://github.com/gotson/komga/compare/v0.95.5...v0.96.0) (2021-05-20)


## Bug Fixes

* **webui:** add autofocus on some dialogs ([339570e](https://github.com/gotson/komga/commit/339570e17c76355eff1c13c9084958d41ce512a2))
* **webui:** display file browser errors in snackbar ([80a520c](https://github.com/gotson/komga/commit/80a520cfe5ac73792b3815080b267d67399d71c8))
* remove the library path tooltip ([1285ae4](https://github.com/gotson/komga/commit/1285ae4a7e5ad5635cc22d409c71df55b13ec66f))
* translated using Weblate (Italian) ([#539](https://github.com/gotson/komga/issues/539)) ([7200046](https://github.com/gotson/komga/commit/7200046ea5603e6d14fa76eaf0540f4194cf6fc8))


## Features

* **webui:** add back button on book view ([d03acea](https://github.com/gotson/komga/commit/d03aceae1a9195885a7890a121dbfad24f4ff3d6))
* **webui:** book import path is saved to local storage ([2473090](https://github.com/gotson/komga/commit/2473090c36bf12cc04b5c457020f54ae046e58e5))
* **webui:** change default theme to system ([b7a371b](https://github.com/gotson/komga/commit/b7a371b7eacae68f850a5862db3c6ecfab7edee6))
* **webui:** show the library name on the search drop down ([f85c60b](https://github.com/gotson/komga/commit/f85c60bd1ab56a4f2dd2a3e7994aa44700774454))

# [0.95.5](https://github.com/gotson/komga/compare/v0.95.4...v0.95.5) (2021-05-20)


## Bug Fixes

* allow cors configuration ([f435b9d](https://github.com/gotson/komga/commit/f435b9dc784b91077e632b68b1f3124373f1ea1f)), closes [#540](https://github.com/gotson/komga/issues/540)

# [0.95.4](https://github.com/gotson/komga/compare/v0.95.3...v0.95.4) (2021-05-17)


## Bug Fixes

* book conversion would fail if page dimensions were missing ([e8e46a0](https://github.com/gotson/komga/commit/e8e46a0834f1ace429183973266fb25d573d0694))

# [0.95.3](https://github.com/gotson/komga/compare/v0.95.2...v0.95.3) (2021-05-17)


## Bug Fixes

* fix rar books with potential issues ([8befde0](https://github.com/gotson/komga/commit/8befde0ea8e8aba007fd9c0374a133e0f3a753ef))

# [0.95.2](https://github.com/gotson/komga/compare/v0.95.1...v0.95.2) (2021-05-14)


## Bug Fixes

* **api:** better handling of tachiyomi tracking ([a7ab0da](https://github.com/gotson/komga/commit/a7ab0da0253bb42b23db2d5b711dbe58eadca437))

# [0.95.1](https://github.com/gotson/komga/compare/v0.95.0...v0.95.1) (2021-05-11)


## Bug Fixes

* **webui:** import details dialog not updated correctly ([33cb083](https://github.com/gotson/komga/commit/33cb0836ba5b7c9dec53a6941b9dc6d4a25bd34b))

# [0.95.0](https://github.com/gotson/komga/compare/v0.94.0...v0.95.0) (2021-05-10)


## Features

* **api:** mark read progress for read lists with Tachiyomi format ([0177ee3](https://github.com/gotson/komga/commit/0177ee3e08ad08eb061bed668cb312e70c0b0858))
* **api:** mark read progress for series with Tachiyomi format ([82af4b3](https://github.com/gotson/komga/commit/82af4b3bbfb2a7dcb91b46032dc7a8e22e5d2e90))

# [0.94.0](https://github.com/gotson/komga/compare/v0.93.0...v0.94.0) (2021-05-09)


## Features

* added translation using Weblate (Vietnamese) ([648f0d4](https://github.com/gotson/komga/commit/648f0d40ff7d0ee6af26d294513914af2edb8915))

# [0.93.0](https://github.com/gotson/komga/compare/v0.92.1...v0.93.0) (2021-05-06)


## Features

* added translation using Weblate (Hungarian) ([3abc8bd](https://github.com/gotson/komga/commit/3abc8bde92db420128fe509680a73707f59d3dd7))

# [0.92.1](https://github.com/gotson/komga/compare/v0.92.0...v0.92.1) (2021-05-06)


## Bug Fixes

* priority tasks could be executed out of order ([55b2883](https://github.com/gotson/komga/commit/55b288388f26e223b09c3ea14d05f893b3a4cba6))

# [0.92.0](https://github.com/gotson/komga/compare/v0.91.3...v0.92.0) (2021-05-05)


## Features

* **webui:** library dialog options for extension repair ([f6ad0f0](https://github.com/gotson/komga/commit/f6ad0f0012b7d67a4cdbb95bad11cc5dcc8e2e9f))
* repair file extensions ([39cd31c](https://github.com/gotson/komga/commit/39cd31cbb6f8b50e36620c36f394d6d06822a3a2))
* **webui:** show in progress books as unread ([db95544](https://github.com/gotson/komga/commit/db955447f95e5946062a5aa91c5dbd356d8e0f82)), closes [#526](https://github.com/gotson/komga/issues/526)

# [0.91.3](https://github.com/gotson/komga/compare/v0.91.2...v0.91.3) (2021-05-05)


## Bug Fixes

* rar extractor could mis-analyze some images ([bd15aec](https://github.com/gotson/komga/commit/bd15aecccc507675aa16c5943fb91009bddf56dc))

# [0.91.2](https://github.com/gotson/komga/compare/v0.91.1...v0.91.2) (2021-05-05)


## Bug Fixes

* book conversion could fail because of different path separators ([e577e4a](https://github.com/gotson/komga/commit/e577e4a78f0586262fecfcb673af3201422435eb))

# [0.91.1](https://github.com/gotson/komga/compare/v0.91.0...v0.91.1) (2021-05-04)


## Bug Fixes

* **opds:** acquisition link is not a valid url ([00ade7f](https://github.com/gotson/komga/commit/00ade7fb362bc8ba30364d5353c857290838ccf3)), closes [#525](https://github.com/gotson/komga/issues/525)

# [0.91.0](https://github.com/gotson/komga/compare/v0.90.3...v0.91.0) (2021-05-04)


## Features

* **webui:** automatic book conversion in library settings ([1c3e853](https://github.com/gotson/komga/commit/1c3e853202b9fd5a4d3d7e7686304b20aa8abe8b))
* automatic book conversion to cbz ([dc2663e](https://github.com/gotson/komga/commit/dc2663ecb78b70c06a8ba1e364986191b802e638))

# [0.90.3](https://github.com/gotson/komga/compare/v0.90.2...v0.90.3) (2021-05-04)


## Bug Fixes

* **webreader:** browser back button would not honor page in query param ([dc4bd44](https://github.com/gotson/komga/commit/dc4bd440d9be77385815f98c4824384c152f2088))

# [0.90.2](https://github.com/gotson/komga/compare/v0.90.1...v0.90.2) (2021-05-04)


## Bug Fixes

* priority tasks ([6ee968b](https://github.com/gotson/komga/commit/6ee968be924b497bda482a13e876407a84d789b8))

# [0.90.1](https://github.com/gotson/komga/compare/v0.90.0...v0.90.1) (2021-05-03)


## Bug Fixes

* better stream handling for rar and zip files ([ebc8df2](https://github.com/gotson/komga/commit/ebc8df2053e270f420c6752619854d125041ef4e))

# [0.90.0](https://github.com/gotson/komga/compare/v0.89.3...v0.90.0) (2021-04-30)


## Features

* added translation using Weblate (Dutch) ([e15f174](https://github.com/gotson/komga/commit/e15f174ff124c14d753779d6f53e7a92e64b3b94))

# [0.89.3](https://github.com/gotson/komga/compare/v0.89.2...v0.89.3) (2021-04-29)


## Bug Fixes

* **webreader:** progress not marked correctly ([f3c541c](https://github.com/gotson/komga/commit/f3c541cd1452496469431439f85ec160564224d6)), closes [#518](https://github.com/gotson/komga/issues/518)

# [0.89.2](https://github.com/gotson/komga/compare/v0.89.1...v0.89.2) (2021-04-28)


## Bug Fixes

* add configuration to unload native webp library ([64a805e](https://github.com/gotson/komga/commit/64a805e0194357622ef7e18efc74d97a301ccdb3))
* native webp library excludes support for armv7l ([731e07a](https://github.com/gotson/komga/commit/731e07a070a6c2046e0db30bbc90bef0270d4687)), closes [#488](https://github.com/gotson/komga/issues/488)

# [0.89.1](https://github.com/gotson/komga/compare/v0.89.0...v0.89.1) (2021-04-28)


## Bug Fixes

* **webreader:** incorrect double pages display ([ece9813](https://github.com/gotson/komga/commit/ece981345fe78afe4e1ef82103aa2e9fbfc54244))
* **webui:** all libraries should show browse view by default ([3e1bf89](https://github.com/gotson/komga/commit/3e1bf89bdba60e60188960e462de4d335e23b946))
* **webui:** dashboard would not reload properly ([18e3e21](https://github.com/gotson/komga/commit/18e3e21f06d9e1a810eae98aaf2f03a97d2b91a4))

# [0.89.0](https://github.com/gotson/komga/compare/v0.88.1...v0.89.0) (2021-04-27)


## Bug Fixes

* **webui:** home link would not show as active ([4c6b3f5](https://github.com/gotson/komga/commit/4c6b3f5e5e4cc99bb952263d9a6b155063da5704))
* **webui:** wrong comparison could show error in javascript console ([b345c30](https://github.com/gotson/komga/commit/b345c30d1232fb8529c6e822b5a2e99565d12b4e))


## Features

* **webui:** display library navigation within toolbar ([a6252ff](https://github.com/gotson/komga/commit/a6252ff2e8e1664bab2492216667fc46546d7f64)), closes [#234](https://github.com/gotson/komga/issues/234)
* **webui:** remember view within library ([b1931aa](https://github.com/gotson/komga/commit/b1931aa892a71d6718c12ae00a917ef1e7d8fcb4)), closes [#367](https://github.com/gotson/komga/issues/367)
* Added a 'Recommended' tab in the library views for a library specific dashboard like the home page ([b26559d](https://github.com/gotson/komga/commit/b26559dc47a76220e20599caa807fe9a6b008d44))

# [0.88.1](https://github.com/gotson/komga/compare/v0.88.0...v0.88.1) (2021-04-26)


## Bug Fixes

* replace java webp library ([f658f9a](https://github.com/gotson/komga/commit/f658f9abe04896cbce6590bae0783836f4e59f74))

# [0.88.0](https://github.com/gotson/komga/compare/v0.87.5...v0.88.0) (2021-04-26)


## Features

* **webui:** ability to read incognito ([dc0cc13](https://github.com/gotson/komga/commit/dc0cc1380793bf870e11444debf1872a6cefc95c))

# [0.87.5](https://github.com/gotson/komga/compare/v0.87.4...v0.87.5) (2021-04-26)


## Bug Fixes

* **webui:** double pages could show duplicate pages ([76ba55a](https://github.com/gotson/komga/commit/76ba55a1242cfc90bceaddef8520719ac1165854))
* **webui:** mark read progress instantly ([97cc3e0](https://github.com/gotson/komga/commit/97cc3e043d15255bc53641bdf0b5cd53cb5c14c4)), closes [#475](https://github.com/gotson/komga/issues/475)
* **webui:** scroll continuous reader to top on book change ([d27828d](https://github.com/gotson/komga/commit/d27828de13fdbe348055fe4c23d129a29ad974e8)), closes [#475](https://github.com/gotson/komga/issues/475)

# [0.87.4](https://github.com/gotson/komga/compare/v0.87.3...v0.87.4) (2021-04-21)


## Bug Fixes

* **api:** filter referential data by access rights ([09fa5e9](https://github.com/gotson/komga/commit/09fa5e95a5a59f2136a7992468ca77a257a267a5)), closes [#492](https://github.com/gotson/komga/issues/492)

# [0.87.3](https://github.com/gotson/komga/compare/v0.87.2...v0.87.3) (2021-04-21)


## Bug Fixes

* error importing reading list with non-numerical number ([3ef0240](https://github.com/gotson/komga/commit/3ef02409bac07ee64c0136e34aac6afcf7c1aba4)), closes [#499](https://github.com/gotson/komga/issues/499)
* **webui:** stored filters are not validated ([fbb4171](https://github.com/gotson/komga/commit/fbb4171d9bd3727d3826e650fe97822ec146bd4d)), closes [#504](https://github.com/gotson/komga/issues/504)

# [0.87.2](https://github.com/gotson/komga/compare/v0.87.1...v0.87.2) (2021-04-20)


## Bug Fixes

* translated using Weblate (Esperanto) ([19ff72f](https://github.com/gotson/komga/commit/19ff72f9e34fce599b3f9691dcaa78b8a21909ef))
* **importer:** keep metadata when upgrading book ([a3b8866](https://github.com/gotson/komga/commit/a3b88667d0364edf68f5a906053af0977c176135))
* prevent transient scanning of directories that are part of existing libraries ([8a92b84](https://github.com/gotson/komga/commit/8a92b84fd06c7b6b8ae3ab6975f7802708598de3))
* **importer:** prevent import of files that are in an existing library ([b0170c7](https://github.com/gotson/komga/commit/b0170c7c8840089811108e275e76cf4bb0350d8b))

# [0.87.1](https://github.com/gotson/komga/compare/v0.87.0...v0.87.1) (2021-04-20)


## Bug Fixes

* **importer:** hardlink fails inside docker ([24cf181](https://github.com/gotson/komga/commit/24cf1819b02dd2625d7a6327a17ef2081cf52e64))

# [0.87.0](https://github.com/gotson/komga/compare/v0.86.0...v0.87.0) (2021-04-19)


## Bug Fixes

* translated using Weblate (Esperanto) ([#506](https://github.com/gotson/komga/issues/506)) ([248f474](https://github.com/gotson/komga/commit/248f47458cb82d777e0f5823ddbe044ca76092c7))


## Features

* **api:** import books ([d41dcef](https://github.com/gotson/komga/commit/d41dcefd3efd4f9844d5b3b1d336a246c320a1ec))
* **api:** support for transient books ([02b0893](https://github.com/gotson/komga/commit/02b08932babd27b5b309b3038279885ac65d0821))
* **webui:** import books ([13b304d](https://github.com/gotson/komga/commit/13b304dd147f3102345c2edb85d41f87ccae1871))
* added translation using Weblate (Esperanto, Polish) ([f3cc6f6](https://github.com/gotson/komga/commit/f3cc6f6e916862741cd7ff3aafa98a4c587653c6))

# [0.86.0](https://github.com/gotson/komga/compare/v0.85.1...v0.86.0) (2021-04-07)


## Bug Fixes

* **webui:** series year incorrectly formatted ([d166207](https://github.com/gotson/komga/commit/d16620791243201f2e2eb0910201f73e2c2975f7))


## Features

* added translation using Weblate (Finnish) ([81142ab](https://github.com/gotson/komga/commit/81142ab570ea9ce1cfd964e7c3205d0c1a9ead7a))

# [0.85.1](https://github.com/gotson/komga/compare/v0.85.0...v0.85.1) (2021-03-31)


## Bug Fixes

* **webreader:** settings are not persisted ([d202dc2](https://github.com/gotson/komga/commit/d202dc239a1d7f8b440ec09fa509ebe0686bc695)), closes [#489](https://github.com/gotson/komga/issues/489)

# [0.85.0](https://github.com/gotson/komga/compare/v0.84.1...v0.85.0) (2021-03-29)


## Bug Fixes

* **webui:** page size chooser would not always reflect the actual value ([5ee4230](https://github.com/gotson/komga/commit/5ee423030f2775754a9e556fcef776b9517eb902))


## Features

* **webui:** change/restore theme even on login page ([7f7c6c3](https://github.com/gotson/komga/commit/7f7c6c3e6f7c75b90aa15666f3af5b657e5764bd))

# [0.84.1](https://github.com/gotson/komga/compare/v0.84.0...v0.84.1) (2021-03-24)


## Bug Fixes

* download current page not working on mobile ([f98d791](https://github.com/gotson/komga/commit/f98d7915c599580a39c4aa77e9b491a16af79e3c))

# [0.84.0](https://github.com/gotson/komga/compare/v0.83.0...v0.84.0) (2021-03-24)


## Features

* download current page from webreader ([93cec4e](https://github.com/gotson/komga/commit/93cec4e4e593a0bdbe3b3c324dda28d843dbdf49)), closes [#469](https://github.com/gotson/komga/issues/469)

# [0.83.0](https://github.com/gotson/komga/compare/v0.82.1...v0.83.0) (2021-03-22)


## Bug Fixes

* refresh series metadata and aggregation after book deletion ([9e44437](https://github.com/gotson/komga/commit/9e4443765b77f503177871cfc68d3252dd0e4206))
* skip ISBN barcode provider if not enabled in library ([9cb63b5](https://github.com/gotson/komga/commit/9cb63b57985afb192555ab89feb1aa8163207000))


## Features

* import ISBN from epub metadata, split authors on comma ([52fceec](https://github.com/gotson/komga/commit/52fceecaa9c15fb9d75844b6f56ed948f0e4b947))


## Performance Improvements

* only refresh relevant metadata providers on series sort ([3dccb9a](https://github.com/gotson/komga/commit/3dccb9a6a7e6b51cbdbd85fc12fc4b001998864a))

# [0.82.1](https://github.com/gotson/komga/compare/v0.82.0...v0.82.1) (2021-03-22)


## Bug Fixes

* **webui:** display status instead of pages on cards for books not ready ([dcf065f](https://github.com/gotson/komga/commit/dcf065f0054088063d0c2e2773043e9df063a9c8))
* translated using Weblate ([a01b764](https://github.com/gotson/komga/commit/a01b764b07f49022e74ccc65b4ab9cfe92d1b46d))

# [0.82.0](https://github.com/gotson/komga/compare/v0.81.1...v0.82.0) (2021-03-19)


## Features

* **webui:** import ComicRack lists as read lists ([8b0dac3](https://github.com/gotson/komga/commit/8b0dac312589dbd5fd9a6c972d61802147022b2f))
* added translation using Weblate (Italian)  ([e5343d7](https://github.com/gotson/komga/commit/e5343d7ab40b95eaed861a0cb65282911093a0aa))
* import ComicRack lists as read lists ([c1e4357](https://github.com/gotson/komga/commit/c1e435762c732d38a4e074f4927fa17445d89709)), closes [#464](https://github.com/gotson/komga/issues/464)

# [0.81.1](https://github.com/gotson/komga/compare/v0.81.0...v0.81.1) (2021-03-16)


## Bug Fixes

* prevent crash during h2 cleanup if parent folder is null ([bd20ff6](https://github.com/gotson/komga/commit/bd20ff67cc9b1973323d603b384783ae533eaed7))
* translated using Weblate (French) ([a5f7dc5](https://github.com/gotson/komga/commit/a5f7dc5dafe59ba43a1fc5442a58a0adaee98f18))
* translated using Weblate (Japanese) ([7c95fff](https://github.com/gotson/komga/commit/7c95fffccf6d2e1dc161121aa9e1880e699534e2))
* translated using Weblate (Swedish) ([1b72135](https://github.com/gotson/komga/commit/1b72135beff2e82c1df885d9d1e5580444056397))

# [0.81.0](https://github.com/gotson/komga/compare/v0.80.0...v0.81.0) (2021-03-15)


## Features

* remove H2 dependencies ([50248e7](https://github.com/gotson/komga/commit/50248e72332c493b070d1a200eec994661fd6b7d)), closes [#455](https://github.com/gotson/komga/issues/455)
* sort series by books count ([b51e491](https://github.com/gotson/komga/commit/b51e4917e1636c83a121952b1b69bd3ab881a279)), closes [#459](https://github.com/gotson/komga/issues/459)

# [0.80.0](https://github.com/gotson/komga/compare/v0.79.1...v0.80.0) (2021-03-11)


## Features

* localize server side errors ([cbe47ea](https://github.com/gotson/komga/commit/cbe47ea5931427193bc853e80b8faae40135ec1a))


## Performance Improvements

* prevent failing tasks ([07cec50](https://github.com/gotson/komga/commit/07cec50417da93d299f59734d6e6139e8e037589))

# [0.79.1](https://github.com/gotson/komga/compare/v0.79.0...v0.79.1) (2021-03-10)


## Performance Improvements

* reduce disk usage during filesystem scan ([e154583](https://github.com/gotson/komga/commit/e154583d30c49f6e88473b7a6594e39365498d34))

# [0.79.0](https://github.com/gotson/komga/compare/v0.78.0...v0.79.0) (2021-03-10)


## Bug Fixes

* **api:** some metadata fields would not unset if set to null ([1996071](https://github.com/gotson/komga/commit/1996071794dddc87c67d99a676f901c725870043))
* **webui:** only show writers and pencillers on series screen ([6101e83](https://github.com/gotson/komga/commit/6101e83292b2e238508559db28fe249bf9fb0fff))


## Features

* **webui:** group checkbox for advanced library options ([6fa0324](https://github.com/gotson/komga/commit/6fa0324666050c80f5247a3313d20ea349a08754))
* **webui:** manage isbn barcode import for libraries ([ee1a0a2](https://github.com/gotson/komga/commit/ee1a0a2aa0909f79dfe99da484ba519e6daaac7d))
* **webui:** show and edit ISBN for book ([65c16f1](https://github.com/gotson/komga/commit/65c16f109f2a1172128b91e0d7f015206a523c48))
* read ISBN from barcode ([6431b1f](https://github.com/gotson/komga/commit/6431b1f0008fc43c1997dec966d93c5b9e1531d0)), closes [#380](https://github.com/gotson/komga/issues/380) [#381](https://github.com/gotson/komga/issues/381)

# [0.78.0](https://github.com/gotson/komga/compare/v0.77.3...v0.78.0) (2021-03-09)


## Bug Fixes

* **webui:** adjust read and download button icon size ([6cdc777](https://github.com/gotson/komga/commit/6cdc777be9d62116859e956057c8a3396f2d0277))


## Features

* add Japanese translation ([8e5748d](https://github.com/gotson/komga/commit/8e5748d97309b5821d38d04c0285f3240a087e7b))

# [0.77.3](https://github.com/gotson/komga/compare/v0.77.2...v0.77.3) (2021-03-05)


## Bug Fixes

* translated using Weblate (Swedish) ([04c1dde](https://github.com/gotson/komga/commit/04c1dde4eefcc4abaccce1eefb7d6129954d8433))

# [0.77.2](https://github.com/gotson/komga/compare/v0.77.1...v0.77.2) (2021-03-03)


## Bug Fixes

* **webui:** keyboard navigation in searchbox results ([604ccf1](https://github.com/gotson/komga/commit/604ccf11928e8f80b4a9470e92272770bb08ed96)), closes [#250](https://github.com/gotson/komga/issues/250)
* **webui:** rearrange browse series and books views ([a5c7b17](https://github.com/gotson/komga/commit/a5c7b17829b73cb11e6e365c0949ccb624e6e0c3))

# [0.77.1](https://github.com/gotson/komga/compare/v0.77.0...v0.77.1) (2021-03-02)


## Bug Fixes

* **webui:** show only author roles present ([847b704](https://github.com/gotson/komga/commit/847b7044a0730683bf57b5907799fa7dffed0f13))

# [0.77.0](https://github.com/gotson/komga/compare/v0.76.0...v0.77.0) (2021-03-02)


## Features

* **webui:** show read status on Books and Series when searching  ([234997c](https://github.com/gotson/komga/commit/234997c27d86d93c23bc26e989954e6da384865d)), closes [#433](https://github.com/gotson/komga/issues/433)

# [0.76.0](https://github.com/gotson/komga/compare/v0.75.2...v0.76.0) (2021-03-02)


## Features

* **webui:** make authors chips clickable ([9fed50e](https://github.com/gotson/komga/commit/9fed50e4056a13570f90a1bc289117b27b253eda)), closes [#431](https://github.com/gotson/komga/issues/431)

# [0.75.2](https://github.com/gotson/komga/compare/v0.75.1...v0.75.2) (2021-03-01)


## Bug Fixes

* better email validation ([97871f7](https://github.com/gotson/komga/commit/97871f7fbc3b615b02ffbaf1f81359cce03a57c1)), closes [#434](https://github.com/gotson/komga/issues/434)

# [0.75.1](https://github.com/gotson/komga/compare/v0.75.0...v0.75.1) (2021-02-26)


## Bug Fixes

* **webui:** validation on user login screen ([392b3b8](https://github.com/gotson/komga/commit/392b3b87fe9ff4778086a8f6b34e144e0582c772)), closes [#429](https://github.com/gotson/komga/issues/429)

# [0.75.0](https://github.com/gotson/komga/compare/v0.74.0...v0.75.0) (2021-02-26)


## Bug Fixes

* **webui:** filter panel would not display values properly ([0c5a744](https://github.com/gotson/komga/commit/0c5a7447f8b7d76e5ad5af3a482dae7e64289261))
* translated using Weblate (Chinese (Simplified)) ([#430](https://github.com/gotson/komga/issues/430)) ([b68e3e5](https://github.com/gotson/komga/commit/b68e3e54b86ee258f2b4cae7ab0d6a9641f041d1))


## Features

* **api:** filter series and books by authors ([bd64381](https://github.com/gotson/komga/commit/bd64381a8e869b26c84168722a100b1e9572c505)), closes [#339](https://github.com/gotson/komga/issues/339)
* **api:** revamp search authors ([f549067](https://github.com/gotson/komga/commit/f549067a8ae3e0e5a44eac97aa8002712b0d20b1))
* **webui:** filter series and books by authors ([c2c2f58](https://github.com/gotson/komga/commit/c2c2f58f1a4fc24a46bc9377768d75fa5f73c6ac)), closes [#339](https://github.com/gotson/komga/issues/339)

# [0.74.0](https://github.com/gotson/komga/compare/v0.73.2...v0.74.0) (2021-02-25)


## Features

* **webui:** make publisher, status, language, ageRating chips clickable ([8bf805e](https://github.com/gotson/komga/commit/8bf805e1e414a0f9464d121b0de53515f11697f1))

# [0.73.2](https://github.com/gotson/komga/compare/v0.73.1...v0.73.2) (2021-02-24)


## Bug Fixes

* translated using Weblate (Chinese (Simplified)) ([#425](https://github.com/gotson/komga/issues/425)) ([e270034](https://github.com/gotson/komga/commit/e270034c8023b46b89d4dd830ef7781ed4a736e0))

# [0.73.1](https://github.com/gotson/komga/compare/v0.73.0...v0.73.1) (2021-02-24)


## Bug Fixes

* **webui:** properly restore query params on page reload ([01f9317](https://github.com/gotson/komga/commit/01f9317b89e211bbc69512d4965a1fee09fcc9e6))

# [0.73.0](https://github.com/gotson/komga/compare/v0.72.0...v0.73.0) (2021-02-24)


## Bug Fixes

* **webui:** incorrect display of html in library delete dialog ([fd6fee1](https://github.com/gotson/komga/commit/fd6fee19d886c397d6c79d0a6013a4bdae2ed097))
* **webui:** remove hash prefix for book numbers ([114f55f](https://github.com/gotson/komga/commit/114f55fe864d70a6fae40a5a0406f9d9111b504a))
* **webui:** text overlapping in some locales ([0ac9f24](https://github.com/gotson/komga/commit/0ac9f24245133214fba6f2e64616eaf81de6344e))


## Features

* **webui:** sort books by file name ([55f0647](https://github.com/gotson/komga/commit/55f06476f205f3c5ce60bba18cb5ce68db4251e4)), closes [#420](https://github.com/gotson/komga/issues/420)
* sort series by folder name ([6ba5cd4](https://github.com/gotson/komga/commit/6ba5cd483218644bdc09e9cdfe573f22c1389963))
* **webui:** Duplicate pagination links at bottom of content lists ([22cdd28](https://github.com/gotson/komga/commit/22cdd28f93bffe814877f68487ebd6eceb8b3fa8))
* add simplified chinese translation ([b76907f](https://github.com/gotson/komga/commit/b76907fe4be29dcb1fa20903ef9ea380df3cd284))

# [0.72.0](https://github.com/gotson/komga/compare/v0.71.7...v0.72.0) (2021-02-22)


## Features

* **api:** enable actuator shutdown endpoint ([8f5ba00](https://github.com/gotson/komga/commit/8f5ba00f1ab64a406bf8aefb752f5b5678f44b4c))
* **webui:** new server management screen with shutdown button ([fd81e17](https://github.com/gotson/komga/commit/fd81e17dedada691d92871be2d77794f29a3c250)), closes [#416](https://github.com/gotson/komga/issues/416)

# [0.71.7](https://github.com/gotson/komga/compare/v0.71.6...v0.71.7) (2021-02-22)


## Bug Fixes

* translated using Weblate (French) ([f5fdf5c](https://github.com/gotson/komga/commit/f5fdf5c8a925ecfc993b23d50364a88481250f4f))
* translated using Weblate (Norwegian Bokmål) ([33fd19b](https://github.com/gotson/komga/commit/33fd19befb10464f49af28fc2d0fc94140a929ff))

# [0.71.6](https://github.com/gotson/komga/compare/v0.71.5...v0.71.6) (2021-02-22)


## Bug Fixes

* **webui:** missing i18n for vuetify datatable ([4fc4e32](https://github.com/gotson/komga/commit/4fc4e32d00d390f438d2564a518433d635c5e440))

# [0.71.5](https://github.com/gotson/komga/compare/v0.71.4...v0.71.5) (2021-02-22)


## Bug Fixes

* **webui:** add i18n for Vuetify datatable ([add6160](https://github.com/gotson/komga/commit/add6160eacffc0dd5f242ecca08e9fedc6e0587f))
* **webui:** add i18n media analysis media status ([6d5b50e](https://github.com/gotson/komga/commit/6d5b50e3c5992f7814600e781bf17f28ecf31dc3))
* **webui:** adjust summary text font size ([cd8805c](https://github.com/gotson/komga/commit/cd8805c434c2608f5ff19cde9d0c9e7f503d2b72))
* **webui:** change grid layout for browse book ([c8543a9](https://github.com/gotson/komga/commit/c8543a95e1993bc4f3d75f419276b55652d77f13))
* **webui:** missing i18n for Settings Users screen ([8a3b16d](https://github.com/gotson/komga/commit/8a3b16db8f6fd163949b96ea36c74d563debc2b2))
* **webui:** missing i18n on collection delete dialog ([f57b949](https://github.com/gotson/komga/commit/f57b949de26917414f6637455671560663ce89f7))
* **webui:** remove text capitalization for some i18n strings ([4c0b24b](https://github.com/gotson/komga/commit/4c0b24b9ac91854c31824fe0d28f7b923556c43b))

# [0.71.4](https://github.com/gotson/komga/compare/v0.71.3...v0.71.4) (2021-02-20)


## Bug Fixes

* **webui:** translations update from Weblate ([#415](https://github.com/gotson/komga/issues/415)) ([71a280a](https://github.com/gotson/komga/commit/71a280a1b0c6c0ee201f1b85302cac1829c69d9e))

# [0.71.3](https://github.com/gotson/komga/compare/v0.71.2...v0.71.3) (2021-02-19)


## Bug Fixes

* **webui:** filter panel would not show values properly ([a63daaf](https://github.com/gotson/komga/commit/a63daafcce8975df46b9d65be83e9d1aac4fb374))
* **webui:** incorrect rtl icon ([98a8f61](https://github.com/gotson/komga/commit/98a8f61a488856e038451a866184b0b1e3402f12))

# [0.71.2](https://github.com/gotson/komga/compare/v0.71.1...v0.71.2) (2021-02-19)


## Bug Fixes

* **webui:** right to left compatibility ([9420010](https://github.com/gotson/komga/commit/9420010caeba3053fd22e0579c2224c42dd08326))

# [0.71.1](https://github.com/gotson/komga/compare/v0.71.0...v0.71.1) (2021-02-19)


## Bug Fixes

* handle query parameters with square brackets ([245dea9](https://github.com/gotson/komga/commit/245dea906c52245ada916c7282cc5e579b4b8a87))

# [0.71.0](https://github.com/gotson/komga/compare/v0.70.0...v0.71.0) (2021-02-17)


## Features

* series download ([e44bc7b](https://github.com/gotson/komga/commit/e44bc7b4915e8d8d55ad3c89c6148bdaba33278f))

# [0.70.0](https://github.com/gotson/komga/compare/v0.69.2...v0.70.0) (2021-02-16)


## Features

* **webui:** support for translations ([efe6476](https://github.com/gotson/komga/commit/efe6476a9057ce610c61278a9e402c250ee61365)), closes [#187](https://github.com/gotson/komga/issues/187)


## Reverts

* update sqlite library to handle armv6 ([e39a2ac](https://github.com/gotson/komga/commit/e39a2ac6bac77f29d29828e2e58f0d97033cc15d))

# [0.69.2](https://github.com/gotson/komga/compare/v0.69.1...v0.69.2) (2021-01-29)


## Bug Fixes

* update sqlite library to handle armv6 ([33a10b3](https://github.com/gotson/komga/commit/33a10b3f7b8b2f40ecb05a905d1a6157fac2b075)), closes [#398](https://github.com/gotson/komga/issues/398)

# [0.69.1](https://github.com/gotson/komga/compare/v0.69.0...v0.69.1) (2021-01-25)


## Bug Fixes

* **api:** sort series release years descending ([3cea176](https://github.com/gotson/komga/commit/3cea17612e82606503a5e8900cd8fc190a0eb707)), closes [#395](https://github.com/gotson/komga/issues/395)

# [0.69.0](https://github.com/gotson/komga/compare/v0.68.2...v0.69.0) (2021-01-25)


## Features

* **api:** filter series by release year ([9133f3a](https://github.com/gotson/komga/commit/9133f3a441a6a3e3dab80cbbc701049df060beda))
* **api:** get all release years for series ([c68951b](https://github.com/gotson/komga/commit/c68951be849a85b2852afabdf49e060974be6f96))
* **webui:** filter series by release year ([33ecb72](https://github.com/gotson/komga/commit/33ecb72f57cecdd63a7f5a2d37de95b3b965eeaf)), closes [#374](https://github.com/gotson/komga/issues/374)

# [0.68.2](https://github.com/gotson/komga/compare/v0.68.1...v0.68.2) (2021-01-24)


## Bug Fixes

* **scanner:** prevent library deletion when inaccessible ([2a262cc](https://github.com/gotson/komga/commit/2a262ccad1c276bf8bc7f489305dac9cf1c295c6)), closes [#392](https://github.com/gotson/komga/issues/392)

# [0.68.1](https://github.com/gotson/komga/compare/v0.68.0...v0.68.1) (2021-01-11)


## Bug Fixes

* **webui:** truncate summary and authors when too long ([9071ad5](https://github.com/gotson/komga/commit/9071ad59ef9f52482c73849732ca2967617a6e68))

# [0.68.0](https://github.com/gotson/komga/compare/v0.67.1...v0.68.0) (2021-01-11)


## Features

* **webui:** display book aggregated data on series screen ([e642174](https://github.com/gotson/komga/commit/e642174c8450fa9e6f6707e59f1c3830e283a47f)), closes [#181](https://github.com/gotson/komga/issues/181)
* aggregate book information at series level ([eb029d9](https://github.com/gotson/komga/commit/eb029d9bb5506775fac03a5fd2eca93d32678f57))

# [0.67.1](https://github.com/gotson/komga/compare/v0.67.0...v0.67.1) (2021-01-09)


## Bug Fixes

* scanner should ignore hidden files ([2d72b47](https://github.com/gotson/komga/commit/2d72b47cdfc03b90554e01259dadd8a5b37ab85b))
* scanner would not ignore hidden directories properly ([1cbcdaa](https://github.com/gotson/komga/commit/1cbcdaa0a193bc3440e9b25fc9b7f1db83808885))

# [0.67.0](https://github.com/gotson/komga/compare/v0.66.1...v0.67.0) (2021-01-06)


## Features

* **webui:** filter collections/readlists in Add To dialog ([1b422a2](https://github.com/gotson/komga/commit/1b422a2086113dce613121c46a79272d13e6b114)), closes [#378](https://github.com/gotson/komga/issues/378)

# [0.66.1](https://github.com/gotson/komga/compare/v0.66.0...v0.66.1) (2021-01-06)


## Bug Fixes

* **webui:** context navigation messed up ([ba61660](https://github.com/gotson/komga/commit/ba6166031bf02eabd246187ec5af09e28ee38362))

# [0.66.0](https://github.com/gotson/komga/compare/v0.65.0...v0.66.0) (2021-01-06)


## Bug Fixes

* **api:** endpoint for books in readlist is not filtered properly ([cdca78b](https://github.com/gotson/komga/commit/cdca78b38f1071e592d458cf9bc29c057d44197d))


## Features

* **api:** find previous/next book in readlist ([bcfb203](https://github.com/gotson/komga/commit/bcfb203f74e16fcc9df53e69720680a7cb328eb0))
* **webreader:** navigate between books of a readlist ([3a3d85c](https://github.com/gotson/komga/commit/3a3d85c3fc83f1a0c74adc091a843870b59bcc12)), closes [#310](https://github.com/gotson/komga/issues/310)
* **webui:** navigate between books of a readlist ([88d4342](https://github.com/gotson/komga/commit/88d4342ef5a17bb65f966b77620cd8dfbd7e42b9))

# [0.65.0](https://github.com/gotson/komga/compare/v0.64.8...v0.65.0) (2021-01-05)


## Features

* **api:** filter /series endpoint by library_id ([1603a96](https://github.com/gotson/komga/commit/1603a96de6a0bb4fdcadad01220498f63a8fd729)), closes [#360](https://github.com/gotson/komga/issues/360)

# [0.64.8](https://github.com/gotson/komga/compare/v0.64.7...v0.64.8) (2020-12-31)


## Bug Fixes

* fix sqlite library detection for aarch64 ([10df531](https://github.com/gotson/komga/commit/10df5316b3381183e32de10c0db83c72534da881))

# [0.64.7](https://github.com/gotson/komga/compare/v0.64.6...v0.64.7) (2020-12-18)


## Bug Fixes

* **opds:** duplicate series if in multiple collections ([c7e6209](https://github.com/gotson/komga/commit/c7e62097656b435bd3e728e95038a7986f226525)), closes [#363](https://github.com/gotson/komga/issues/363)

# [0.64.6](https://github.com/gotson/komga/compare/v0.64.5...v0.64.6) (2020-12-03)


## Bug Fixes

* use bionic base image ([85fe674](https://github.com/gotson/komga/commit/85fe674e6a3f4977450f089b83c91f22e1f6f390)), closes [#349](https://github.com/gotson/komga/issues/349)

# [0.64.5](https://github.com/gotson/komga/compare/v0.64.4...v0.64.5) (2020-11-23)


## Bug Fixes

* enhanced logging for sqlite native lib loading ([f1a894d](https://github.com/gotson/komga/commit/f1a894db9eb9c6720353039a9dc79c021856cabd))

# [0.64.4](https://github.com/gotson/komga/compare/v0.64.3...v0.64.4) (2020-11-02)


## Bug Fixes

* **api:** support accept header in getBookPage ([#352](https://github.com/gotson/komga/issues/352)) ([e123d38](https://github.com/gotson/komga/commit/e123d386928c07c69915fcaa903c1a9b5373b8c6)), closes [#350](https://github.com/gotson/komga/issues/350)

# [0.64.3](https://github.com/gotson/komga/compare/v0.64.2...v0.64.3) (2020-10-29)


## Bug Fixes

* don't append volume to series title if equals to 1 ([1cb9ae6](https://github.com/gotson/komga/commit/1cb9ae6aad85900db1a1dc12e14a7a88d4a430a9)), closes [#347](https://github.com/gotson/komga/issues/347)

# [0.64.2](https://github.com/gotson/komga/compare/v0.64.1...v0.64.2) (2020-10-25)


## Bug Fixes

* native webp library ([15caea3](https://github.com/gotson/komga/commit/15caea3b99b76162aa636bcd691032a4510f32ff)), closes [#279](https://github.com/gotson/komga/issues/279)

# [0.64.1](https://github.com/gotson/komga/compare/v0.64.0...v0.64.1) (2020-10-24)


## Bug Fixes

* use own distrib of sqlite with freebsd support ([3d2793a](https://github.com/gotson/komga/commit/3d2793a768cb220265246fabc16ec3474e0af935)), closes [#319](https://github.com/gotson/komga/issues/319)

# [0.64.0](https://github.com/gotson/komga/compare/v0.63.3...v0.64.0) (2020-10-22)


## Features

* append volume to series name ([2567796](https://github.com/gotson/komga/commit/25677967db255efe39834ce31fe0f048481a3067)), closes [#343](https://github.com/gotson/komga/issues/343) [#132](https://github.com/gotson/komga/issues/132)

# [0.63.3](https://github.com/gotson/komga/compare/v0.63.2...v0.63.3) (2020-10-20)


## Bug Fixes

* scanner crashes if folders are not readable ([f43a386](https://github.com/gotson/komga/commit/f43a3864f869d254e75b18c00c4a1585ed69fd25)), closes [#342](https://github.com/gotson/komga/issues/342)

# [0.63.2](https://github.com/gotson/komga/compare/v0.63.1...v0.63.2) (2020-10-05)


## Bug Fixes

* **api:** incorrect parameter type ([342f04c](https://github.com/gotson/komga/commit/342f04c117ee0ad2d26782f7ee65ec14247f741e))

# [0.63.1](https://github.com/gotson/komga/compare/v0.63.0...v0.63.1) (2020-09-29)


## Bug Fixes

* **opds:** url encode publisher query param ([8347fdc](https://github.com/gotson/komga/commit/8347fdc44d5610159599ba9946ae28d26779e65b)), closes [#335](https://github.com/gotson/komga/issues/335)

# [0.63.0](https://github.com/gotson/komga/compare/v0.62.6...v0.63.0) (2020-09-28)


## Features

* **opds:** browse series by publishers ([f3fcab4](https://github.com/gotson/komga/commit/f3fcab41e49cd3fbe861ec8110fa9f2ee0972ec1)), closes [#332](https://github.com/gotson/komga/issues/332)
* **opds:** recently added books ([dd7bc9c](https://github.com/gotson/komga/commit/dd7bc9cbf63c51a7b9655d26d7c69c005f13417c)), closes [#327](https://github.com/gotson/komga/issues/327)
* order books by release date ([e3bb8bc](https://github.com/gotson/komga/commit/e3bb8bc7aee48154a4798620baa3b5f409f6d2a5)), closes [#318](https://github.com/gotson/komga/issues/318)

# [0.62.6](https://github.com/gotson/komga/compare/v0.62.5...v0.62.6) (2020-09-12)


## Bug Fixes

* **webreader:** images smaller than viewport would not fill width ([81d9a00](https://github.com/gotson/komga/commit/81d9a001850191b6ce2f6891caf296cc015a8dc6)), closes [#311](https://github.com/gotson/komga/issues/311)

# [0.62.5](https://github.com/gotson/komga/compare/v0.62.4...v0.62.5) (2020-09-06)


## Bug Fixes

* trim and lowercase tags and genres in metadata ([656f23a](https://github.com/gotson/komga/commit/656f23a64abd5cd22de6d670e64352df005a628e)), closes [#302](https://github.com/gotson/komga/issues/302)
* **opds:** cannot download file with semicolon in filename ([e1e251e](https://github.com/gotson/komga/commit/e1e251ec2f384892d7705ab590c318cf0e3232bb)), closes [#309](https://github.com/gotson/komga/issues/309)

# [0.62.4](https://github.com/gotson/komga/compare/v0.62.3...v0.62.4) (2020-09-05)


## Bug Fixes

* **api:** openAPI spec fields marked as required ([8f228db](https://github.com/gotson/komga/commit/8f228dbb1868c17133c61b85375d027dbce532f6)), closes [#308](https://github.com/gotson/komga/issues/308)

# [0.62.3](https://github.com/gotson/komga/compare/v0.62.2...v0.62.3) (2020-09-01)


## Bug Fixes

* **webui:** clear selection after action performed ([e0cff70](https://github.com/gotson/komga/commit/e0cff70ff513a35cdeb509a168e0663219f89090)), closes [#303](https://github.com/gotson/komga/issues/303)
* **webui:** edit series dialog could incorrectly display MIXED placeholder ([5d3f061](https://github.com/gotson/komga/commit/5d3f061a810dd9deefec710ebeec45c0ed1f5b18))
* **webui:** filter values could be duplicated ([e91954f](https://github.com/gotson/komga/commit/e91954f6bd0072d5316e716e9ff687225684d549))
* **webui:** theme is not restored properly ([432dc91](https://github.com/gotson/komga/commit/432dc9156293bfaabbdde01472229cf2951ff504)), closes [#304](https://github.com/gotson/komga/issues/304)

# [0.62.2](https://github.com/gotson/komga/compare/v0.62.1...v0.62.2) (2020-08-31)


## Bug Fixes

* **opds:** latest series sort inverted ([bddc008](https://github.com/gotson/komga/commit/bddc00855202d4c3694f7069837178b029ebf026)), closes [#301](https://github.com/gotson/komga/issues/301)
* **webui:** compute scrollability on mount, fix right chevron when using display scaling ([8928959](https://github.com/gotson/komga/commit/8928959fdb8adf9c555379c67a13a657fc291e05))

# [0.62.1](https://github.com/gotson/komga/compare/v0.62.0...v0.62.1) (2020-08-28)


## Bug Fixes

* series genre is not imported from metadata ([58fac36](https://github.com/gotson/komga/commit/58fac365ad046fa652ea825ba7b0c57b24b8d141))

# [0.62.0](https://github.com/gotson/komga/compare/v0.61.0...v0.62.0) (2020-08-28)


## Bug Fixes

* **webui:** correct label in library edit dialog ([c532e50](https://github.com/gotson/komga/commit/c532e50489aa5c873cc3c64da9918cd48f1d9ff7))
* **webui:** restore library filter for language and age rating ([dc064f2](https://github.com/gotson/komga/commit/dc064f20ecd671e35b399b79263b180214ce6498))


## Features

* **api:** filter collection's series ([cfa06a9](https://github.com/gotson/komga/commit/cfa06a9d2bd890e37bf46ba70c8494e7a27a1a15))
* **api:** get all age ratings ([be80d86](https://github.com/gotson/komga/commit/be80d86d6cba564293053f883c0ceda186652fe0))
* **api:** get referential data by collection ([d7fd296](https://github.com/gotson/komga/commit/d7fd2964928530e221be800d4cafe4334dbbf368))
* **api:** search series by age rating ([f51d575](https://github.com/gotson/komga/commit/f51d575bda49a89ff565009aec4a0b866095e2d9))
* **webui:** filter collection content ([fc905ef](https://github.com/gotson/komga/commit/fc905ef9b5bda44e1da67299e9e87865a04652ab)), closes [#270](https://github.com/gotson/komga/issues/270)
* **webui:** filter series by age rating ([01eef83](https://github.com/gotson/komga/commit/01eef838a20ee01b179bf3ad5dcd03b94f0e7a8b))

# [0.61.0](https://github.com/gotson/komga/compare/v0.60.0...v0.61.0) (2020-08-27)


## Bug Fixes

* **webui:** contextual filters ([f515819](https://github.com/gotson/komga/commit/f5158197ded703d57c3c0fb7eb6c31dae1629d03)), closes [#290](https://github.com/gotson/komga/issues/290)
* **webui:** disable filter panels with no values ([57cc6c4](https://github.com/gotson/komga/commit/57cc6c48d3249f80756bb2a8c4a79b590c514a42))


## Features

* **api:** filter referential data by library or series ([982983e](https://github.com/gotson/komga/commit/982983e3db0ca0e4fa143ef510f4a61ff2bd0d57)), closes [#290](https://github.com/gotson/komga/issues/290)
* **webreader:** add double page no cover layout ([5fe015e](https://github.com/gotson/komga/commit/5fe015ede0329ed85b39b78f11d55150214f65bf)), closes [#103](https://github.com/gotson/komga/issues/103)

# [0.60.0](https://github.com/gotson/komga/compare/v0.59.2...v0.60.0) (2020-08-27)


## Bug Fixes

* ignore blank metadata fields ([c8c5df2](https://github.com/gotson/komga/commit/c8c5df27013719f533554ed8d2fe2d51b5ed9972)), closes [#288](https://github.com/gotson/komga/issues/288)
* series genre is not imported from metadata ([d0815dd](https://github.com/gotson/komga/commit/d0815dd80ec246ad1c3c5dca70f49ed165fddf1f))


## Features

* handle multiple story arcs in ComicInfoProvider ([f4451bf](https://github.com/gotson/komga/commit/f4451bfd413f78598fe5760345b9c2a4864b3054)), closes [#282](https://github.com/gotson/komga/issues/282)

# [0.59.2](https://github.com/gotson/komga/compare/v0.59.1...v0.59.2) (2020-08-27)


## Bug Fixes

* potential incorrect read progress for series ([7b90244](https://github.com/gotson/komga/commit/7b90244bdda9a06bc1a40f9aa5f6a719b54f5169))


## Performance Improvements

* add sql indexes ([41f3e8d](https://github.com/gotson/komga/commit/41f3e8dc638178846f37da21f17d2a314692b33e))
* lazy sql joins ([6eb7669](https://github.com/gotson/komga/commit/6eb76698583255ce408ec9c660ef697bb4f05f5a))

# [0.59.1](https://github.com/gotson/komga/compare/v0.59.0...v0.59.1) (2020-08-25)


## Bug Fixes

* incorrect read progress for books ([107d7db](https://github.com/gotson/komga/commit/107d7db3946012c4d634300e75e3118cc3d90cf2)), closes [#286](https://github.com/gotson/komga/issues/286)

# [0.59.0](https://github.com/gotson/komga/compare/v0.58.1...v0.59.0) (2020-08-25)


## Bug Fixes

* **api:** do not return empty language ([2ab3c9c](https://github.com/gotson/komga/commit/2ab3c9ca957b1ec578d2fec8dd0d3afbb94416dd))
* **webui:** field should not display if empty ([968e297](https://github.com/gotson/komga/commit/968e297af0e0ee4ffaf7e76e52071f19699df223))


## Features

* **api:** get all languages ([115ad42](https://github.com/gotson/komga/commit/115ad42d9c463d4489f00936372dc580e8b1013b))
* **api:** get all publishers ([1929061](https://github.com/gotson/komga/commit/1929061393d97324fd61139929b6011114810021))
* **api:** search books by tag ([f46f1a0](https://github.com/gotson/komga/commit/f46f1a0e9655247fe70a8b3b874d045b75ec9d53))
* **api:** search series by genre ([da4a0aa](https://github.com/gotson/komga/commit/da4a0aa10b27fb1ca24c9c848de3f6ae3ac418e4))
* **api:** search series by language ([723d7c1](https://github.com/gotson/komga/commit/723d7c1fe9bb877b4e4b446196095e0d138830b6))
* **api:** search series by publisher ([e4b0b2d](https://github.com/gotson/komga/commit/e4b0b2d1f7890a93371e534cf2b4836ed0a72a1d))
* **api:** search series by tag ([7bd1de6](https://github.com/gotson/komga/commit/7bd1de6f78a768a605a47bac7a966ea22d4236af))
* **api:** search series' books by tag ([940d5d3](https://github.com/gotson/komga/commit/940d5d341087965f0e307ce8418e1e14f338a984))
* **webui:** color coded chips for series status ([fb8a8c4](https://github.com/gotson/komga/commit/fb8a8c4228048e37ab3638f1fe44c39e514cab16))
* **webui:** more filter criteria ([4d22d9c](https://github.com/gotson/komga/commit/4d22d9c8e88108afac6d82991aa51fd4f0fed88b)), closes [#283](https://github.com/gotson/komga/issues/283) [#34](https://github.com/gotson/komga/issues/34)
* **webui:** navigation drawer for sort/filter ([28598cb](https://github.com/gotson/komga/commit/28598cbef585bc644518270dbae5d99f32779ee9)), closes [#283](https://github.com/gotson/komga/issues/283)

# [0.58.1](https://github.com/gotson/komga/compare/v0.58.0...v0.58.1) (2020-08-24)


## Bug Fixes

* database migration failure ([2ca6077](https://github.com/gotson/komga/commit/2ca607708e21513fdf00f43b05b840dba0e07560))

# [0.58.0](https://github.com/gotson/komga/compare/v0.57.0...v0.58.0) (2020-08-24)


## Bug Fixes

* **webui:** update option title in library dialog ([29d9726](https://github.com/gotson/komga/commit/29d97266137a4f38497e75f1a1f24fbf9ae96f46))


## Features

* **webui:** handle new metadata fields ([5567adc](https://github.com/gotson/komga/commit/5567adc9460a20373e3a63f48995d1bbaca5bf50)), closes [#276](https://github.com/gotson/komga/issues/276)
* add/rearrange metadata fields ([9e406e3](https://github.com/gotson/komga/commit/9e406e33167284f3803beb45243bf11992ec7e83)), closes [#276](https://github.com/gotson/komga/issues/276)

# [0.57.0](https://github.com/gotson/komga/compare/v0.56.1...v0.57.0) (2020-08-20)


## Features

* **webui:** read lists ([27edf17](https://github.com/gotson/komga/commit/27edf17424ff7a53ccfb272b877cf452c95f1158)), closes [#106](https://github.com/gotson/komga/issues/106)
* read lists ([f0c864f](https://github.com/gotson/komga/commit/f0c864f4eba8c70bdac8f92171bfb03edabcea53)), closes [#106](https://github.com/gotson/komga/issues/106)

# [0.56.1](https://github.com/gotson/komga/compare/v0.56.0...v0.56.1) (2020-08-16)


## Bug Fixes

* database migration could fail ([1c9f3d0](https://github.com/gotson/komga/commit/1c9f3d0e804631a8a3437473b181f389a7d73814))

# [0.56.0](https://github.com/gotson/komga/compare/v0.55.1...v0.56.0) (2020-08-14)


## Bug Fixes

* **webui:** change grid size for sm screens ([18280be](https://github.com/gotson/komga/commit/18280beb95b44bb9dfeec9f81cfe04b97c225fdd))


## Features

* **api:** unpaged queries for books ([8f8054e](https://github.com/gotson/komga/commit/8f8054e1d859928cc249cb5631283da498be908d))
* **webui:** library option to import local artwork ([cba0a2e](https://github.com/gotson/komga/commit/cba0a2e701e4d508d2a1b68da7ae7e6c5673846c))
* **webui:** navigate to series books from book details screen ([48e92c7](https://github.com/gotson/komga/commit/48e92c7580f93e01d9e90a43870d3c1ed04c66cf)), closes [#272](https://github.com/gotson/komga/issues/272)
* library option to import local artwork ([eefb802](https://github.com/gotson/komga/commit/eefb80213a89903a01ca0114d319feb1e83bb041))
* **webui:** scanner option per library ([fca0180](https://github.com/gotson/komga/commit/fca018033dd094e0ddf6b0ca3a447b65d492aaa4))
* scanner option per library ([4da6ff9](https://github.com/gotson/komga/commit/4da6ff9fd44d2283c31adc5a3185e0ad642255f3))
* **api:** http eTag caching for all API calls ([fe22cb5](https://github.com/gotson/komga/commit/fe22cb5ce6157a23d9b01cd6fb2139555d5a2da5))

# [0.55.1](https://github.com/gotson/komga/compare/v0.55.0...v0.55.1) (2020-08-14)


## Bug Fixes

* incorrect book number after adding book in series ([3b1eb36](https://github.com/gotson/komga/commit/3b1eb368ca022268be7beed7aa8c9d8e1d89657d)), closes [#269](https://github.com/gotson/komga/issues/269)

# [0.55.0](https://github.com/gotson/komga/compare/v0.54.0...v0.55.0) (2020-08-13)


## Features

* **webreader:** side padding, original scaling for Webtoon ([6499788](https://github.com/gotson/komga/commit/6499788543e9487c90f2b38b9b2eb354aaf802c2)), closes [#266](https://github.com/gotson/komga/issues/266) [#264](https://github.com/gotson/komga/issues/264)

# [0.54.0](https://github.com/gotson/komga/compare/v0.53.0...v0.54.0) (2020-08-12)


## Features

* custom thumbnails for series ([f5f423f](https://github.com/gotson/komga/commit/f5f423f05f3d3bf824bc10c9d1dcc712fbaa98fe)), closes [#63](https://github.com/gotson/komga/issues/63)

# [0.53.0](https://github.com/gotson/komga/compare/v0.52.3...v0.53.0) (2020-08-12)


## Features

* sidecar thumbnails for books ([d01b29f](https://github.com/gotson/komga/commit/d01b29f28098102804c0bddd29c6e0867c560c01))

# [0.52.3](https://github.com/gotson/komga/compare/v0.52.2...v0.52.3) (2020-08-07)


## Bug Fixes

* **api:** large file download ([d100db2](https://github.com/gotson/komga/commit/d100db22eb6505a182b6778d4d52de13ad8653eb))

# [0.52.2](https://github.com/gotson/komga/compare/v0.52.1...v0.52.2) (2020-08-07)


## Bug Fixes

* some PDF pages are blurry ([9ad1cfe](https://github.com/gotson/komga/commit/9ad1cfe80975639a349a99a45e874efd3aba7e1b)), closes [#260](https://github.com/gotson/komga/issues/260)
* **webreader:** incorrect display on safari and firefox ([22b0b7e](https://github.com/gotson/komga/commit/22b0b7e7480d45728c92eb0f418b8143fb523de7)), closes [#262](https://github.com/gotson/komga/issues/262)
* **webui:** missing settings icon in sidebar ([b9e4bea](https://github.com/gotson/komga/commit/b9e4bea71b3a1168312961e104109669feef8078))
* **webui:** thumbnails explorer navigation misaligned ([2ffed28](https://github.com/gotson/komga/commit/2ffed282d705bb74336e191bb28708b1a0337da4))

# [0.52.1](https://github.com/gotson/komga/compare/v0.52.0...v0.52.1) (2020-08-06)


## Bug Fixes

* **webreader:** webtoon reader not loading images ([1a030b4](https://github.com/gotson/komga/commit/1a030b4db558b02e996246670f6ce86094f00495)), closes [#259](https://github.com/gotson/komga/issues/259)

# [0.52.0](https://github.com/gotson/komga/compare/v0.51.1...v0.52.0) (2020-08-06)


## Bug Fixes

* **webui:** missing settings icon in webreader ([9693829](https://github.com/gotson/komga/commit/969382988dbd40448a68fdf1b2586c527f2209f6))


## Features

* **webreader:** display reading direction on book opening ([ebf2aac](https://github.com/gotson/komga/commit/ebf2aac0ac44097da00a628e25ceb446b2fba9e4))
* **webreader:** webtoon reader, fit to screen ([44c814a](https://github.com/gotson/komga/commit/44c814a5baeea9e6b715a63c31285b33cd2e6872)), closes [#81](https://github.com/gotson/komga/issues/81) [#145](https://github.com/gotson/komga/issues/145)

# [0.51.1](https://github.com/gotson/komga/compare/v0.51.0...v0.51.1) (2020-08-03)


## Bug Fixes

* **metadata:** recognize ComicInfo Day tag and "MA15+" rating ([3ad438d](https://github.com/gotson/komga/commit/3ad438d53fadb4766d9167fbc45937455af95ddb))

# [0.51.0](https://github.com/gotson/komga/compare/v0.50.1...v0.51.0) (2020-07-31)


## Bug Fixes

* **webreader:** cycle image fit would not persist setting ([e5272d2](https://github.com/gotson/komga/commit/e5272d224383d9afdc183dfc626937ba87fc27cf))


## Features

* **webreader:** better display of landscape images ([09984a4](https://github.com/gotson/komga/commit/09984a4284c6ee9a50c7457ea9271fc36c58b770)), closes [#123](https://github.com/gotson/komga/issues/123)
* get images dimension during media analysis ([f9d55ec](https://github.com/gotson/komga/commit/f9d55ecfd0743a3c0e07f5f63725d25e6183af0c)), closes [#123](https://github.com/gotson/komga/issues/123)

# [0.50.1](https://github.com/gotson/komga/compare/v0.50.0...v0.50.1) (2020-07-30)


## Bug Fixes

* **docker:** docker image doesn't start ([8bce80b](https://github.com/gotson/komga/commit/8bce80b408143416335a8afb356b8ea404b22369))

# [0.50.0](https://github.com/gotson/komga/compare/v0.49.0...v0.50.0) (2020-07-30)


## Features

* **webui:** add system theme option ([8f22f01](https://github.com/gotson/komga/commit/8f22f01b3bdbfc8951bb291ef9c78f13dc61cbc0))

# [0.49.0](https://github.com/gotson/komga/compare/v0.48.1...v0.49.0) (2020-07-25)


## Features

* **webreader:** add shortcut information menu ([1885f32](https://github.com/gotson/komga/commit/1885f32416635611e62795bdf22420006bb577ad))
* **webui:** dark theme ([efe2021](https://github.com/gotson/komga/commit/efe2021bdcb28cbeac93cee8add6dc80a5708c60)), closes [#179](https://github.com/gotson/komga/issues/179)

# [0.48.1](https://github.com/gotson/komga/compare/v0.48.0...v0.48.1) (2020-07-21)


## Bug Fixes

* fix database migration errors ([f84ba17](https://github.com/gotson/komga/commit/f84ba17079d1ba4bf320d923b971468d19f0aa1d)), closes [#239](https://github.com/gotson/komga/issues/239) [#238](https://github.com/gotson/komga/issues/238) [#237](https://github.com/gotson/komga/issues/237) [#240](https://github.com/gotson/komga/issues/240)

# [0.48.0](https://github.com/gotson/komga/compare/v0.47.0...v0.48.0) (2020-07-16)


## Features

* change database from H2 to SQLite ([20b2b39](https://github.com/gotson/komga/commit/20b2b39d364a04a4b73b8f8bea2f86ae4c6e0304)), closes [#218](https://github.com/gotson/komga/issues/218)

# [0.47.0](https://github.com/gotson/komga/compare/v0.46.0...v0.47.0) (2020-07-14)


## Features

* **webui:** change internals for shortcuts. add new shortcuts for settings ([b402817](https://github.com/gotson/komga/commit/b402817edbface6d622d92c98347f6fe1914bf88)), closes [#172](https://github.com/gotson/komga/issues/172)

# [0.46.0](https://github.com/gotson/komga/compare/v0.45.2...v0.46.0) (2020-07-13)


## Features

* **webui:** add simple next/previous book buttons to reader toolbar ([e428115](https://github.com/gotson/komga/commit/e4281156f5398d478257ee699d7016c1aa5ee81c)), closes [#233](https://github.com/gotson/komga/issues/233)

# [0.45.2](https://github.com/gotson/komga/compare/v0.45.1...v0.45.2) (2020-07-05)


## Bug Fixes

* **webui:** browse collection should show all series ([8629ea9](https://github.com/gotson/komga/commit/8629ea9936ecfcf118be82d7cbf9ad5c2db8035f)), closes [#226](https://github.com/gotson/komga/issues/226)

# [0.45.1](https://github.com/gotson/komga/compare/v0.45.0...v0.45.1) (2020-07-05)


## Bug Fixes

* **webui:** read button link underline on hover ([863258d](https://github.com/gotson/komga/commit/863258d8be037c983c75cd70224970f70d8212a5))

# [0.45.0](https://github.com/gotson/komga/compare/v0.44.0...v0.45.0) (2020-07-05)


## Features

* **api:** claim status ([47dd2f6](https://github.com/gotson/komga/commit/47dd2f66e0049e04cfda0f8d62ff9b7c2f207873)), closes [#207](https://github.com/gotson/komga/issues/207)
* **webui:** claim server from login screen ([d4810bd](https://github.com/gotson/komga/commit/d4810bdc809d87cd94027566b0cbee10d6673a33)), closes [#207](https://github.com/gotson/komga/issues/207)

# [0.44.0](https://github.com/gotson/komga/compare/v0.43.4...v0.44.0) (2020-07-03)


## Bug Fixes

* **webui:** make card title as link ([d6e4b80](https://github.com/gotson/komga/commit/d6e4b807dbcaa9c5ce91ed72ba88d8d8f3434f7e)), closes [#224](https://github.com/gotson/komga/issues/224)
* **webui:** reset edit book dialog to first tab on reopen ([5760a06](https://github.com/gotson/komga/commit/5760a06b7a3496d9ec910697122abd51e2c5f9bd))


## Features

* generate collections from ComicInfo SeriesGroup ([277cdcd](https://github.com/gotson/komga/commit/277cdcd4e39c2ad1da84522de78d693a30c89788)), closes [#210](https://github.com/gotson/komga/issues/210)
* **api:** metadata import settings per library ([6824212](https://github.com/gotson/komga/commit/6824212514d8586bdb47ac2c38d6e121be8db489)), closes [#199](https://github.com/gotson/komga/issues/199)
* **webui:** metadata import settings per library ([521cc42](https://github.com/gotson/komga/commit/521cc42858415abde9a4eb378e5276578bdbed59)), closes [#199](https://github.com/gotson/komga/issues/199)

# [0.43.4](https://github.com/gotson/komga/compare/v0.43.3...v0.43.4) (2020-07-01)


## Bug Fixes

* properly expand home dir in config ([5d86d3e](https://github.com/gotson/komga/commit/5d86d3ea0b3558d8bca2e7994049c1daed4d3fd6)), closes [#195](https://github.com/gotson/komga/issues/195) [#203](https://github.com/gotson/komga/issues/203)

# [0.43.3](https://github.com/gotson/komga/compare/v0.43.2...v0.43.3) (2020-06-30)


## Bug Fixes

* **api:** add missing sort fields for Books ([797535d](https://github.com/gotson/komga/commit/797535d71ff59f37ab49afc5383ed270ab45ac9b))

# [0.43.2](https://github.com/gotson/komga/compare/v0.43.1...v0.43.2) (2020-06-30)


## Bug Fixes

* **api:** exception on unpaged empty content ([2cc27f2](https://github.com/gotson/komga/commit/2cc27f244ccf886b4c5d538d9cfdf61ec92f5478))

# [0.43.1](https://github.com/gotson/komga/compare/v0.43.0...v0.43.1) (2020-06-29)


## Bug Fixes

* **webui:** clear selection when reloading search results ([7d808c5](https://github.com/gotson/komga/commit/7d808c5dee753bcef8987ddeb66bb7086c4cb708))

# [0.43.0](https://github.com/gotson/komga/compare/v0.42.0...v0.43.0) (2020-06-29)


## Bug Fixes

* **webui:** lazy load collections on browse series ([d89533d](https://github.com/gotson/komga/commit/d89533ded69f58a96a2c1062067858b589857b00))


## Features

* **webui:** sort/filter settings are persisted per library ([bf737de](https://github.com/gotson/komga/commit/bf737de91065e56fe1fe11b9ef27cfa913f0ab77)), closes [#190](https://github.com/gotson/komga/issues/190)

# [0.42.0](https://github.com/gotson/komga/compare/v0.41.1...v0.42.0) (2020-06-29)


## Bug Fixes

* **webui:** incorrect count of collections in browse collections screen ([2254929](https://github.com/gotson/komga/commit/22549291d8798f62da4c387e75cec1ad23817c5a))
* **webui:** lazy http call for CollectionEditDialog ([4716a1b](https://github.com/gotson/komga/commit/4716a1b22b873549e07e13f4c76b0caa0c597078))


## Features

* **webui:** collection edition on card in browse collections ([e45389d](https://github.com/gotson/komga/commit/e45389d77b8277fb4850be3f9349a1aa89ac97ca))
* **webui:** collection edition on card in search results ([68fe4fd](https://github.com/gotson/komga/commit/68fe4fd23d4836b40be834201f84d70fb5ec2e7a))
* **webui:** enable edit on books and series cards in search screen ([32dad62](https://github.com/gotson/komga/commit/32dad627773341fb4c070146a3eda4898409fc4a))
* **webui:** multi-select in dashboard page ([8e33be7](https://github.com/gotson/komga/commit/8e33be78e22fa0b622d92606ec976d5756770445))
* **webui:** multi-select in search results page ([c5417ac](https://github.com/gotson/komga/commit/c5417ac8da1dde26237e69561cc718a5882c2baf)), closes [#213](https://github.com/gotson/komga/issues/213)

# [0.41.1](https://github.com/gotson/komga/compare/v0.41.0...v0.41.1) (2020-06-27)


## Bug Fixes

* **api:** pagination for collection's series ([6dd0704](https://github.com/gotson/komga/commit/6dd070436be7458eb3b3019d6286b8dd32d1b27d))
* **opds:** prepend position for series in ordered collection ([7e5a141](https://github.com/gotson/komga/commit/7e5a1412ca1a0278f2f5169fa55d5e25fbb75480))

# [0.41.0](https://github.com/gotson/komga/compare/v0.40.1...v0.41.0) (2020-06-26)


## Bug Fixes

* **api:** collection without element would return incorrect dto ([0891981](https://github.com/gotson/komga/commit/08919814d361aa11083ed7a5c3169d449e744d10))
* **api:** incorrect number of books per series ([bf2d0ba](https://github.com/gotson/komga/commit/bf2d0ba1fad8cf193cc60d2680ad90226c49e93f))
* **webui:** adjust padding for grids ([02e9168](https://github.com/gotson/komga/commit/02e916898e90db49b64bb59e90d152c92553b69c))
* **webui:** display collection after adding in series browsing screen ([8ce5a39](https://github.com/gotson/komga/commit/8ce5a391e53b34c7c46e0c568e7438603c01302c))
* **webui:** library navigation not highlighted correctly ([e28c070](https://github.com/gotson/komga/commit/e28c070e364a3fbae8836ad6b246ebe364c6271c))


## Features

* **api:** collections are pageable ([449a27e](https://github.com/gotson/komga/commit/449a27e13618815cbfd0cf8124dc00779f4d86ac)), closes [#216](https://github.com/gotson/komga/issues/216)
* **api:** search series by collection ids ([ca91af7](https://github.com/gotson/komga/commit/ca91af7792559c54a9e1843368a29b7b9d7db3de))
* **opds:** browse by collection ([15f9c82](https://github.com/gotson/komga/commit/15f9c8257eb120cb74d37226e37f6025cb9242a6))
* **webui:** action menu on item cards ([37d790d](https://github.com/gotson/komga/commit/37d790d1fc222018f8d17344c03c26d71dcefae1))
* **webui:** allow direct input of library path in add dialog ([6ece7b1](https://github.com/gotson/komga/commit/6ece7b12be582b97d82ae56dea73d29ae6bade11)), closes [#88](https://github.com/gotson/komga/issues/88)
* **webui:** better handling of library deletion ([0297210](https://github.com/gotson/komga/commit/0297210dc2bc420fcfdd3d59bbd76dd254335c5b))
* **webui:** display collections in search results and search box ([82aec45](https://github.com/gotson/komga/commit/82aec456606589d8d012715ad45a7c10d1b9ed51)), closes [#212](https://github.com/gotson/komga/issues/212)
* **webui:** hide pagination when there is only 1 page ([675b4a1](https://github.com/gotson/komga/commit/675b4a16afdfad89be5391cafa32fdd430f49c48))
* **webui:** pagination for collections ([50b516d](https://github.com/gotson/komga/commit/50b516d0c5772a15acdb69091ace788ed67fad13)), closes [#216](https://github.com/gotson/komga/issues/216)
* **webui:** refresh browse collection screen on action menu actions ([8f2c453](https://github.com/gotson/komga/commit/8f2c4534c888e79ee377d0041390fa3fb1cfb90f))
* **webui:** refresh dashboard on action menu actions ([b6bd735](https://github.com/gotson/komga/commit/b6bd735bdbce7c60f2e489b9f416b28669576228))

# [0.40.1](https://github.com/gotson/komga/compare/v0.40.0...v0.40.1) (2020-06-19)


## Bug Fixes

* **webui:** drag series by handle when editing collection ([e72f4ab](https://github.com/gotson/komga/commit/e72f4ab57e26405d9117da1c02c4efa6d4a6fa23)), closes [#214](https://github.com/gotson/komga/issues/214)
* **webui:** wider display for collection expansion panel ([96c95ea](https://github.com/gotson/komga/commit/96c95ea0745d3bed9b0f618cb2aebdf8bde34a7f))

# [0.40.0](https://github.com/gotson/komga/compare/v0.39.0...v0.40.0) (2020-06-19)


## Bug Fixes

* **api:** sort libraries case insensitive ([1650aec](https://github.com/gotson/komga/commit/1650aec75bd3f6b8cd1e58d2110761f9b59f5e42))


## Features

* **api:** collections management ([c2f9403](https://github.com/gotson/komga/commit/c2f940336ac455a7f7623d4367033b6b77cc23d9)), closes [#30](https://github.com/gotson/komga/issues/30)
* **webui:** collections management ([2f8255a](https://github.com/gotson/komga/commit/2f8255a05fa0f477af764e72f95f6c28fe4ebfe3)), closes [#30](https://github.com/gotson/komga/issues/30)

# [0.39.0](https://github.com/gotson/komga/compare/v0.38.0...v0.39.0) (2020-06-10)


## Features

* **api:** restrict page streaming and file download with roles ([6291dab](https://github.com/gotson/komga/commit/6291dab86472be0f7d564c43914d20baec566c85)), closes [#146](https://github.com/gotson/komga/issues/146)
* **webui:** restrict page streaming and file download per user ([381b196](https://github.com/gotson/komga/commit/381b1960330c97681c84b136d4a670c9dd295c46)), closes [#146](https://github.com/gotson/komga/issues/146)

# [0.38.0](https://github.com/gotson/komga/compare/v0.37.0...v0.38.0) (2020-06-09)


## Bug Fixes

* **webui:** simplify unread filter ([bb60f10](https://github.com/gotson/komga/commit/bb60f10d49b41ac2288ad7fe6765cb2d783b41c1))


## Features

* **webui:** search results page ([89039a4](https://github.com/gotson/komga/commit/89039a4170b674adb0453e16e12ea54577abf473)), closes [#29](https://github.com/gotson/komga/issues/29)

# [0.37.0](https://github.com/gotson/komga/compare/v0.36.0...v0.37.0) (2020-06-08)


## Bug Fixes

* **scanner:** add TRACE logs for file update times ([5433567](https://github.com/gotson/komga/commit/54335674eed6fccd626329248b532b791a133f60)), closes [#159](https://github.com/gotson/komga/issues/159)
* order of pages is not loaded correctly from database ([d2288dd](https://github.com/gotson/komga/commit/d2288dda72b053821be5226e64dd460f5a97f2d0)), closes [#189](https://github.com/gotson/komga/issues/189)


## Features

* automatic database backup ([bbb9f7c](https://github.com/gotson/komga/commit/bbb9f7ce06a546768f662309bb635713aff70cb9)), closes [#138](https://github.com/gotson/komga/issues/138)

# [0.36.0](https://github.com/gotson/komga/compare/v0.35.2...v0.36.0) (2020-06-08)


## Bug Fixes

* strip accented characters for title sort when creating series ([6f1e36e](https://github.com/gotson/komga/commit/6f1e36e7dc3087445a55c0b5a360a932c2a3778f)), closes [#188](https://github.com/gotson/komga/issues/188)
* **webui:** prevent cropping on book thumbnails ([e038857](https://github.com/gotson/komga/commit/e038857887f180ed4fa147064cad84febf647567)), closes [#191](https://github.com/gotson/komga/issues/191)


## Features

* **api:** on deck books ([1b6a030](https://github.com/gotson/komga/commit/1b6a030ab568a657e9d7b3c89292ebd8e05d6611)), closes [#131](https://github.com/gotson/komga/issues/131)
* **webui:** add On Deck section on dashboard ([37c935e](https://github.com/gotson/komga/commit/37c935ec9a0d5ada86eb0ee8f9b201422a532e6d)), closes [#131](https://github.com/gotson/komga/issues/131)

# [0.35.2](https://github.com/gotson/komga/compare/v0.35.1...v0.35.2) (2020-06-08)


## Bug Fixes

* **analysis:** page number was not persisted ([99f800c](https://github.com/gotson/komga/commit/99f800ce8f40833588d3e937abba2e838a205218)), closes [#189](https://github.com/gotson/komga/issues/189)

# [0.35.1](https://github.com/gotson/komga/compare/v0.35.0...v0.35.1) (2020-06-05)


## Bug Fixes

* **api:** books could disappear for users if read by others ([3d1f0e0](https://github.com/gotson/komga/commit/3d1f0e0d058ba2d740e31c9185ff8c9e60e03131))

# [0.35.0](https://github.com/gotson/komga/compare/v0.34.1...v0.35.0) (2020-06-05)


## Bug Fixes

* **api:** add count of in progress books in SeriesDto ([0d2713a](https://github.com/gotson/komga/commit/0d2713a090257cf427cc71700fd2a9d42978001c)), closes [#25](https://github.com/gotson/komga/issues/25)


## Features

* **api:** add read_status parameter to series books ([fc5c502](https://github.com/gotson/komga/commit/fc5c50240e5311d476d235adff638b91e8826605)), closes [#25](https://github.com/gotson/komga/issues/25)
* **api:** read progress as search criteria for Series ([885c891](https://github.com/gotson/komga/commit/885c89126700d6a5a2a153ffee346a1324d04e41)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** add read status filter when browsing Series ([cc39ce8](https://github.com/gotson/komga/commit/cc39ce8b44f847501ac3600cd70b9503193dee67)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** read progress filter for Series when browsing Libraries ([0c046a7](https://github.com/gotson/komga/commit/0c046a767db2bde801ea31ebfbbb2df7d6fac3c6)), closes [#25](https://github.com/gotson/komga/issues/25)

# [0.34.1](https://github.com/gotson/komga/compare/v0.34.0...v0.34.1) (2020-06-04)


## Bug Fixes

* **webui:** filter status not reset properly when changing library ([fea5431](https://github.com/gotson/komga/commit/fea54313d3f7e635d131d0bd339c59123cfc8422))

# [0.34.0](https://github.com/gotson/komga/compare/v0.33.1...v0.34.0) (2020-06-04)


## Bug Fixes

* **webui:** adjust series unread count when marking books ([31e21fe](https://github.com/gotson/komga/commit/31e21fed45603c0e5ad6706e25aac5a863e42675))
* **webui:** series grid not expanding to full width ([2f7d2a4](https://github.com/gotson/komga/commit/2f7d2a447f91a8af71a1a18d3d114035964c9ee4))


## Features

* **analysis:** handle read progress during book analysis ([1fc893e](https://github.com/gotson/komga/commit/1fc893ecb31138c3529ffb80a1c8fc05ea62bb07))
* **api:** add read/unread books count in SeriesDto ([3ca50d7](https://github.com/gotson/komga/commit/3ca50d7b34c3fad99a162c09d417bd6dc42e9d54)), closes [#25](https://github.com/gotson/komga/issues/25)
* **api:** search books by read status ([7f3c492](https://github.com/gotson/komga/commit/7f3c49280b48b38e9308025e4dd95b6f1cc921c0)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** display unread count on series card ([4962f17](https://github.com/gotson/komga/commit/4962f170284c8d43961798a77b7c27a103977821)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** keep reading section in dashboard ([7984cef](https://github.com/gotson/komga/commit/7984cef06664f84d1cb1f1336b34338f90c1c1a0)), closes [#131](https://github.com/gotson/komga/issues/131)
* **webui:** read button on item card ([a59f263](https://github.com/gotson/komga/commit/a59f26365f660fbf0ca6ffa70df029bf14f07ba9)), closes [#133](https://github.com/gotson/komga/issues/133)

# [0.33.1](https://github.com/gotson/komga/compare/v0.33.0...v0.33.1) (2020-06-03)


## Bug Fixes

* **api:** incorrect number of books returned ([544c873](https://github.com/gotson/komga/commit/544c8731416276cc97125851e503008be3ea8769)), closes [#177](https://github.com/gotson/komga/issues/177)
* **webui:** unread tick not showing properly on books ([0d0b998](https://github.com/gotson/komga/commit/0d0b9985c01764e707893fa66f1288f08276477b))

# [0.33.0](https://github.com/gotson/komga/compare/v0.32.0...v0.33.0) (2020-06-03)


## Bug Fixes

* **webui:** action menu not showing for non-admin ([e60666c](https://github.com/gotson/komga/commit/e60666caa39e6f18446ebea2b7d910687da60ac1))
* **webui:** replace mark as read/unread buttons with icons ([709ee1e](https://github.com/gotson/komga/commit/709ee1e29b7f98e733b5b2070fdabef3f6d4d171))
* **webui:** use pagination for browsing screens ([5867db7](https://github.com/gotson/komga/commit/5867db77f5dc20181671fb5bf409941cf42591f7)), closes [#91](https://github.com/gotson/komga/issues/91)


## Features

* **api:** mark all books in series as read or unread ([75b7216](https://github.com/gotson/komga/commit/75b72164fe1b3784c5b40ba6aa923fe071f72b56)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** mark series as read/unread ([4d7e243](https://github.com/gotson/komga/commit/4d7e243d3aa8729139d3311a8e670e101b3a1bbb)), closes [#25](https://github.com/gotson/komga/issues/25)

# [0.32.0](https://github.com/gotson/komga/compare/v0.31.0...v0.32.0) (2020-06-02)


## Features

* **api:** manage book read progress per user ([17c80cd](https://github.com/gotson/komga/commit/17c80cd1a1407de2de8a8341374f92dd2fe72cc7)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webreader:** mark progress while reading ([10895a3](https://github.com/gotson/komga/commit/10895a37f503503ad23a24632ad28eaeaf8db1de)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** mark books as read or unread ([24c994f](https://github.com/gotson/komga/commit/24c994f840df95920e5a8d501e58a916eaea04cc)), closes [#25](https://github.com/gotson/komga/issues/25)

# [0.31.0](https://github.com/gotson/komga/compare/v0.30.0...v0.31.0) (2020-06-01)


## Features

* migrate DAO from Hibernate to jOOQ ([75e1079](https://github.com/gotson/komga/commit/75e1079992fdecfe51b0175ed0c19351cd7b3672))

# [0.30.0](https://github.com/gotson/komga/compare/v0.29.1...v0.30.0) (2020-05-28)


## Features

* switch to JRE for docker image ([3206495](https://github.com/gotson/komga/commit/320649594f72563e41f0052fef26493f518fff55))

# [0.29.1](https://github.com/gotson/komga/compare/v0.29.0...v0.29.1) (2020-05-12)


## Bug Fixes

* **opds:** use servlet context path to build links ([d82eefe](https://github.com/gotson/komga/commit/d82eefe19a95950df72d1070fdf4c5194b377392)), closes [#156](https://github.com/gotson/komga/issues/156)

# [0.29.0](https://github.com/gotson/komga/compare/v0.28.6...v0.29.0) (2020-05-08)


## Features

* **webreader:** add setting to disable swipe navigation ([32170d4](https://github.com/gotson/komga/commit/32170d4d690fc047444e48ef3c71c640f3c45024))
* **webreader:** do not always show reading direction pop ([dc7dc75](https://github.com/gotson/komga/commit/dc7dc75e317c904727045f9c9b818eb8dbd4d422)), closes [#151](https://github.com/gotson/komga/issues/151)

# [0.28.6](https://github.com/gotson/komga/compare/v0.28.5...v0.28.6) (2020-05-08)


## Bug Fixes

* ignore alpha channel on image conversion ([9556ae5](https://github.com/gotson/komga/commit/9556ae51f262470b1fbb5fd9af2d8caeb3c1fa64)), closes [#153](https://github.com/gotson/komga/issues/153)

# [0.28.5](https://github.com/gotson/komga/compare/v0.28.4...v0.28.5) (2020-05-08)


## Bug Fixes

* **opds:** do not always convert images ([6430c92](https://github.com/gotson/komga/commit/6430c92bcb11ef598e4c26eb61ad9fe6733a1d0d)), closes [#153](https://github.com/gotson/komga/issues/153)

# [0.28.4](https://github.com/gotson/komga/compare/v0.28.3...v0.28.4) (2020-05-06)


## Bug Fixes

* **tasks:** background tasks would stop running if disk is over 90% full ([dc33fb1](https://github.com/gotson/komga/commit/dc33fb1d10a72d0fc3bd6715f7fcd0c1dfead90b))

# [0.28.3](https://github.com/gotson/komga/compare/v0.28.2...v0.28.3) (2020-05-05)


## Bug Fixes

* **epub:** epub not loading correctly on windows (again) ([8535f46](https://github.com/gotson/komga/commit/8535f46848287e03e5e36493481d25e9dfcc8a04))

# [0.28.2](https://github.com/gotson/komga/compare/v0.28.1...v0.28.2) (2020-05-04)


## Bug Fixes

* **epub:** epub were not loading properly on Windows ([eca453c](https://github.com/gotson/komga/commit/eca453c5344ce7c0144f30b6c1c08aa9d2c79038)), closes [#149](https://github.com/gotson/komga/issues/149)

# [0.28.1](https://github.com/gotson/komga/compare/v0.28.0...v0.28.1) (2020-05-04)


## Bug Fixes

* **webui:** refactor Cards to a single dynamic component ([#148](https://github.com/gotson/komga/issues/148)) ([74a9f7e](https://github.com/gotson/komga/commit/74a9f7e628de95cd78902d539f858737bb5c1d61))

# [0.28.0](https://github.com/gotson/komga/compare/v0.27.7...v0.28.0) (2020-05-03)


## Bug Fixes

* **rar:** unsupported rar archives are marked as such ([6c0ebbe](https://github.com/gotson/komga/commit/6c0ebbeee126a77299d4198bb5ab1f03391e774f)), closes [#147](https://github.com/gotson/komga/issues/147)


## Features

* add hawt.io support ([dfa2405](https://github.com/gotson/komga/commit/dfa24057bbad382a2da54471b91db523f18545ba))
* **docker:** persist artemis data in /config ([08e7f5d](https://github.com/gotson/komga/commit/08e7f5dbf26995fcf2b39818561c0259c473fd30))
* **scan:** add configuration to disable startup scan ([37590dd](https://github.com/gotson/komga/commit/37590dd9129174c6be5f9e2e1509f79555bed207))
* **tasks:** background tasks persistency ([1a31c89](https://github.com/gotson/komga/commit/1a31c8971269380d9fabef60dce881aee957c230))
* **tasks:** replace background tasks management ([60ce87a](https://github.com/gotson/komga/commit/60ce87a25dd62a2d357ce3b62a03f03bbce43f1c))

# [0.27.7](https://github.com/gotson/komga/compare/v0.27.6...v0.27.7) (2020-04-20)


## Bug Fixes

* **swagger:** fix incorrect Pageable schema ([14bee56](https://github.com/gotson/komga/commit/14bee566b3b32659080e5b8f19e9b52f2ba8c4d1))

# [0.27.6](https://github.com/gotson/komga/compare/v0.27.5...v0.27.6) (2020-04-18)


## Bug Fixes

* **swagger:** better pageable support ([6401dc9](https://github.com/gotson/komga/commit/6401dc9b9b051ed49b1341891383e6015fd77fc9))

# [0.27.5](https://github.com/gotson/komga/compare/v0.27.4...v0.27.5) (2020-04-18)


## Bug Fixes

* set default forward headers strategy to framework ([b24fbe3](https://github.com/gotson/komga/commit/b24fbe3a1e206e35ea0da1223483d625eacaa323))

# [0.27.4](https://github.com/gotson/komga/compare/v0.27.3...v0.27.4) (2020-04-16)


## Bug Fixes

* **analyzer:** better rar5 detection ([b07e9b9](https://github.com/gotson/komga/commit/b07e9b9728f387c15fb22f970a42c5e484f22b7a))
* **swagger:** correct response type for thumbnails and file ([07f634e](https://github.com/gotson/komga/commit/07f634e658b8ce74486dc5654518f65832602e99))

# [0.27.3](https://github.com/gotson/komga/compare/v0.27.2...v0.27.3) (2020-04-16)


## Bug Fixes

* **analyzer:** regression in RAR handling ([b8462f3](https://github.com/gotson/komga/commit/b8462f3568a8d8428b88bb45f55a7e7fdf22497e))

# [0.27.2](https://github.com/gotson/komga/compare/v0.27.1...v0.27.2) (2020-04-15)


## Bug Fixes

* **webreader:** stretched images on Safari with fit to Width ([74f9305](https://github.com/gotson/komga/commit/74f9305e166482003f4ef3fc12f2a003d5ec468c))

# [0.27.1](https://github.com/gotson/komga/compare/v0.27.0...v0.27.1) (2020-04-15)


## Bug Fixes

* **swagger:** fix duplicated mapping key error ([face321](https://github.com/gotson/komga/commit/face321b0841b93b24b8e51bcce49f391a9aaeb8))

# [0.27.0](https://github.com/gotson/komga/compare/v0.26.3...v0.27.0) (2020-04-13)


## Bug Fixes

* **api:** libraries are not sorted for restricted users ([1d58322](https://github.com/gotson/komga/commit/1d583229eeb4eb279c545b433e6160612df7a0b7)), closes [#140](https://github.com/gotson/komga/issues/140)


## Features

* **swagger:** update to OpenAPI 3 ([c9de7c8](https://github.com/gotson/komga/commit/c9de7c8074ec15a3a69db808c31bd3415c17ba15))

# [0.26.3](https://github.com/gotson/komga/compare/v0.26.2...v0.26.3) (2020-04-12)


## Bug Fixes

* **epub:** add support for opf:role for authors ([30bf7f4](https://github.com/gotson/komga/commit/30bf7f469f0d9895646ade1b9b050975fc7228c5)), closes [#139](https://github.com/gotson/komga/issues/139)
* **epub:** match artist and illustrator roles to penciller ([b7fe4bd](https://github.com/gotson/komga/commit/b7fe4bde749bb6fff2c8e81d9881cf70acb04d76))

# [0.26.2](https://github.com/gotson/komga/compare/v0.26.1...v0.26.2) (2020-04-11)


## Bug Fixes

* **swagger:** paging and sorting parameters showing incorrectly ([1e9407c](https://github.com/gotson/komga/commit/1e9407cfe25a6104150efe2b7892a9973878ef0a))

# [0.26.1](https://github.com/gotson/komga/compare/v0.26.0...v0.26.1) (2020-04-10)


## Bug Fixes

* **swagger:** parameters were not showing for claim method ([e5a0841](https://github.com/gotson/komga/commit/e5a084144dab96209ad55172cd4fe59fea08efe5))

# [0.26.0](https://github.com/gotson/komga/compare/v0.25.1...v0.26.0) (2020-04-10)


## Bug Fixes

* **comicinfo:** avoid reading the file twice ([1dab826](https://github.com/gotson/komga/commit/1dab826622d6d4bc1121862bbc0acbdff7ff743b))
* **epub:** some EPUB files would not be parsed correctly ([f0ff785](https://github.com/gotson/komga/commit/f0ff785d66f15bc93d27bb1ee6314e7940bbaa1c))
* **swagger:** ignore non-functional parameters ([d1c8b23](https://github.com/gotson/komga/commit/d1c8b23f447e5b3bd196159692d43794d28cc44b))


## Features

* **metadata:** retrieve EPUB metadata ([a4f5015](https://github.com/gotson/komga/commit/a4f50154351818e42506cc31f414b51e06c14e23))
* **scan:** force rescan of existing series to pickup EPUB files ([854f090](https://github.com/gotson/komga/commit/854f090415f47ff10704c714e69026a70b3d7bce))
* support for EPUB format (comics only) ([0a06a6f](https://github.com/gotson/komga/commit/0a06a6f7995cf540d6cdabc8f8c058eef82e81e1)), closes [#119](https://github.com/gotson/komga/issues/119)

# [0.25.1](https://github.com/gotson/komga/compare/v0.25.0...v0.25.1) (2020-04-06)


## Bug Fixes

* **api:** thumbnails not updating properly ([a5bd908](https://github.com/gotson/komga/commit/a5bd9087df1e43be9bef5dea1039e1720f0f9541))
* **opds:** prepend issue number for book titles for Chunky ([42cad8b](https://github.com/gotson/komga/commit/42cad8b4d572ee587b8b908662863f83f15f6349))

# [0.25.0](https://github.com/gotson/komga/compare/v0.24.10...v0.25.0) (2020-04-03)


## Bug Fixes

* **api:** book file download uses streaming ([74be1f0](https://github.com/gotson/komga/commit/74be1f0face9b910cf9fa25c2379b3c8eaefaab6))


## Features

* **webui:** action menu to refresh metadata ([6ad59c4](https://github.com/gotson/komga/commit/6ad59c4efb28517d9aabc1c67e240d0939493cf9))
* retrieve metadata from ComicInfo.xml ([af01d25](https://github.com/gotson/komga/commit/af01d25ede29feb5159e8a800b789f200e6e7e5c))

# [0.24.10](https://github.com/gotson/komga/compare/v0.24.9...v0.24.10) (2020-03-25)


## Bug Fixes

* **opds:** add summary and authors to book entries ([2414792](https://github.com/gotson/komga/commit/241479292ae1adc50ab45b03e39ef27d25f7847a))
* **webui:** properly display newline in book summary ([15d95ac](https://github.com/gotson/komga/commit/15d95ac442bba89e2fb98bf064995526d9dd6725))

# [0.24.9](https://github.com/gotson/komga/compare/v0.24.8...v0.24.9) (2020-03-24)


## Bug Fixes

* **webui:** redirect to initial page after login ([5f61597](https://github.com/gotson/komga/commit/5f6159712515d0df128443dbec5cc01b9dadff07)), closes [#122](https://github.com/gotson/komga/issues/122)

# [0.24.8](https://github.com/gotson/komga/compare/v0.24.7...v0.24.8) (2020-03-23)


## Bug Fixes

* **api:** check if authors field is set ([9218e6b](https://github.com/gotson/komga/commit/9218e6bb265b32fbd28d61ef797e7a1d93cb9a1c)), closes [#120](https://github.com/gotson/komga/issues/120)

# [0.24.7](https://github.com/gotson/komga/compare/v0.24.6...v0.24.7) (2020-03-23)


## Bug Fixes

* **webui:** fix metadata dialogs on xs screens ([112837a](https://github.com/gotson/komga/commit/112837a163a2a123b421cf8cebef15d431f67367))

# [0.24.6](https://github.com/gotson/komga/compare/v0.24.5...v0.24.6) (2020-03-21)


## Bug Fixes

* **api:** use etag on book thumbnails ([871a92a](https://github.com/gotson/komga/commit/871a92a7835333ea11014ac06ead5b434a44c23d)), closes [#117](https://github.com/gotson/komga/issues/117)

# [0.24.5](https://github.com/gotson/komga/compare/v0.24.4...v0.24.5) (2020-03-20)


## Bug Fixes

* **api:** find book siblings by metadata.numberSort ([1902e72](https://github.com/gotson/komga/commit/1902e72f86c1fcaca8492d828a8fc19dbc0fac9c))
* **api:** series thumbnail is of first book by metadata.numberSort ([0721f31](https://github.com/gotson/komga/commit/0721f31e2faa0123b1c581afe28c042947bd8e0b))
* **opds:** books are ordered by metadata.numberSort ([60edbe1](https://github.com/gotson/komga/commit/60edbe1090f99575bc209a3c439f41158398b7a6))
* **opds:** display book metadata title instead of name ([d06da57](https://github.com/gotson/komga/commit/d06da572b64caca6fbed00606c6aae73fa8c40f8))
* **opds:** search series by metadata.title ([f4466b4](https://github.com/gotson/komga/commit/f4466b45bfc374724d48008c642cad295e444b4d))

# [0.24.4](https://github.com/gotson/komga/compare/v0.24.3...v0.24.4) (2020-03-20)


## Bug Fixes

* **edit series dialog:** better form handling ([c225829](https://github.com/gotson/komga/commit/c2258294ced569310d21b5c8e03e455408b18d22))

# [0.24.3](https://github.com/gotson/komga/compare/v0.24.2...v0.24.3) (2020-03-20)


## Bug Fixes

* **webui:** change page title on book change ([2d0e21b](https://github.com/gotson/komga/commit/2d0e21b0b8d36a81e17417db01545cc3e51f61b2))

# [0.24.2](https://github.com/gotson/komga/compare/v0.24.1...v0.24.2) (2020-03-19)


## Bug Fixes

* **webui:** use metadata title for display name ([64c3356](https://github.com/gotson/komga/commit/64c33565c5648154a7ede69f7f5f8de69b6697c3))

# [0.24.1](https://github.com/gotson/komga/compare/v0.24.0...v0.24.1) (2020-03-19)


## Bug Fixes

* **api:** search books by metadata title ([487b18d](https://github.com/gotson/komga/commit/487b18d15b04e86eacf55bfda5b286456d36e563))
* **api:** search series by metadata title ([51dd917](https://github.com/gotson/komga/commit/51dd91724976a14a029d69c04f2483b7072778b6))
* **book card:** use book metadata ([ea1bdc6](https://github.com/gotson/komga/commit/ea1bdc646af70be647dc9b1709817dcfd657aa15))
* **browse series:** hide toolbar on selection ([e6d014f](https://github.com/gotson/komga/commit/e6d014f1ac29c6e02537eaf0c4be636785f69ab5))
* **searchbox:** display book metadata title instead of name ([26d37a0](https://github.com/gotson/komga/commit/26d37a0fc91c476e246ba09a7ae9f8c71f0b82b5))

# [0.24.0](https://github.com/gotson/komga/compare/v0.23.0...v0.24.0) (2020-03-18)


## Bug Fixes

* **add library:** special characters handling ([15afa93](https://github.com/gotson/komga/commit/15afa9343155aecee4132cb0ceedbfb5bf22b229))


## Features

* **book reader:** set reading direction from metadata ([30e766b](https://github.com/gotson/komga/commit/30e766be16dff121577ebb24d11881f73450c5cd))
* **book reader:** vertical reading mode ([ca03111](https://github.com/gotson/komga/commit/ca03111b0b4246239bad84b79d7d179975d722aa))

# [0.23.0](https://github.com/gotson/komga/compare/v0.22.2...v0.23.0) (2020-03-18)


## Features

* **book reader:** background color settings ([2c87e7b](https://github.com/gotson/komga/commit/2c87e7bba6f33c52313d5b28690cf179eaac8c85)), closes [#113](https://github.com/gotson/komga/issues/113)
* **book reader:** pressing ESC will close the toolbars ([791f5df](https://github.com/gotson/komga/commit/791f5dff0878a72a5d34f8e201090e656492784c))

# [0.22.2](https://github.com/gotson/komga/compare/v0.22.1...v0.22.2) (2020-03-17)


## Bug Fixes

* **webui:** change page size on media analysis screen ([afc2cd4](https://github.com/gotson/komga/commit/afc2cd4e7002aeea440477203af0ac9d67cbdbb3))

# [0.22.1](https://github.com/gotson/komga/compare/v0.22.0...v0.22.1) (2020-03-17)


## Bug Fixes

* use JDBC update statements for database migration ([f68e035](https://github.com/gotson/komga/commit/f68e0352fdfd6d4683d4015c4b21bad6bcd070dc))

# [0.22.0](https://github.com/gotson/komga/compare/v0.21.0...v0.22.0) (2020-03-17)


## Features

* support for book metadata ([6a53e8f](https://github.com/gotson/komga/commit/6a53e8fd6b53533020b9e06a14028f9776e641af)), closes [#48](https://github.com/gotson/komga/issues/48) [#43](https://github.com/gotson/komga/issues/43)

# [0.21.0](https://github.com/gotson/komga/compare/v0.20.0...v0.21.0) (2020-03-10)


## Bug Fixes

* **scanner:** follow symlinks when scanning libraries ([1044262](https://github.com/gotson/komga/commit/1044262a1c2c7be47002a1128b2374051fa4f7fc)), closes [#96](https://github.com/gotson/komga/issues/96)


## Features

* docker multi-arch images ([d54c67b](https://github.com/gotson/komga/commit/d54c67b3df0d0c4d753e0057d3349720e819eca5))

# [0.20.0](https://github.com/gotson/komga/compare/v0.19.0...v0.20.0) (2020-03-05)


## Features

* add default location for database ([ce50403](https://github.com/gotson/komga/commit/ce50403a86cb14de5edba34a0ac9f34b1b586af5))

# [0.19.0](https://github.com/gotson/komga/compare/v0.18.0...v0.19.0) (2020-03-05)


## Features

* demo profile ([24b2125](https://github.com/gotson/komga/commit/24b21250be81789ffd2897cb95c56959bf1ee0af))

# [0.18.0](https://github.com/gotson/komga/compare/v0.17.0...v0.18.0) (2020-03-03)


## Features

* add claim profile ([b7eeb4c](https://github.com/gotson/komga/commit/b7eeb4c6cbac9207578e6d303b361dad70a1a646)), closes [#104](https://github.com/gotson/komga/issues/104)

# [0.17.0](https://github.com/gotson/komga/compare/v0.16.5...v0.17.0) (2020-03-03)


## Bug Fixes

* **webreader:** defaults to LTR and fix touch ([2eae83f](https://github.com/gotson/komga/commit/2eae83f561ef2fc0009770d1fbc21ff2fff56b78))


## Features

* **webreader:** add 's' keyboard shortcut to show settings ([99b14cb](https://github.com/gotson/komga/commit/99b14cb80c65c42a1955b53df012cb1e213ffc48))
* **webreader:** escape closes dialogs ([9e44571](https://github.com/gotson/komga/commit/9e44571af53673c5acbee2fced22f047bad3316e))
* **webui:** redesign reader to follow material design ([7f0ab5f](https://github.com/gotson/komga/commit/7f0ab5fde3119abff1b0ba88b6e958a0c5645369)), closes [#74](https://github.com/gotson/komga/issues/74)

# [0.16.5](https://github.com/gotson/komga/compare/v0.16.4...v0.16.5) (2020-03-02)


## Bug Fixes

* prevent user self-deletion ([3d9b78d](https://github.com/gotson/komga/commit/3d9b78d364c07b4c7f9bd1923c36943bbe5147f6)), closes [#100](https://github.com/gotson/komga/issues/100)

# [0.16.4](https://github.com/gotson/komga/compare/v0.16.3...v0.16.4) (2020-02-29)


## Bug Fixes

* swagger-ui and h2-console work again ([626f047](https://github.com/gotson/komga/commit/626f04769630a8b02f04e796ffb299c12168686d)), closes [#99](https://github.com/gotson/komga/issues/99)

# [0.16.3](https://github.com/gotson/komga/compare/v0.16.2...v0.16.3) (2020-02-28)


## Bug Fixes

* **webui:** remove border on preselect for multi-select ([a0bd2f9](https://github.com/gotson/komga/commit/a0bd2f9682530649ab2c91b447ba03833687c0c2))

# [0.16.2](https://github.com/gotson/komga/compare/v0.16.1...v0.16.2) (2020-02-28)


## Bug Fixes

* **webui:** better multi-select ([881806e](https://github.com/gotson/komga/commit/881806ed1c78b0a66ee46905acb9fef14a210bc2))

# [0.16.1](https://github.com/gotson/komga/compare/v0.16.0...v0.16.1) (2020-02-27)


## Bug Fixes

* **webui:** close edit series dialog on escape keypress ([7c0f55d](https://github.com/gotson/komga/commit/7c0f55deeb30ac406ffd4cd15395363cbb372951))
* **webui:** remove ripple effect on series card ([ce5594e](https://github.com/gotson/komga/commit/ce5594e82e265cc99421fb5a7489a642d34b1204))

# [0.16.0](https://github.com/gotson/komga/compare/v0.15.1...v0.16.0) (2020-02-27)


## Features

* **webui:** series multi-selection and edition ([cfce076](https://github.com/gotson/komga/commit/cfce0768ba688b396ba3f0aadbc91008ff0b3555))

# [0.15.1](https://github.com/gotson/komga/compare/v0.15.0...v0.15.1) (2020-02-25)


## Bug Fixes

* **webui:** responsive scaling for login page logo ([20720ae](https://github.com/gotson/komga/commit/20720ae7ccf44c4699d0154a0aa6ed3216813914))
* send proper JSON on API 404 instead of index.html ([fb147a4](https://github.com/gotson/komga/commit/fb147a447a4ba8aff73da57fff1c511a9d515b20))

# [0.15.0](https://github.com/gotson/komga/compare/v0.14.2...v0.15.0) (2020-02-25)


## Features

* **webui:** add series and book title in page title and reader overlay ([6b1998c](https://github.com/gotson/komga/commit/6b1998c1d9fa75db4a3971fb772ad18aaad3bdaf))
* **webui:** add the series and book title to page title ([23c10c2](https://github.com/gotson/komga/commit/23c10c231efaa5f409811e453c51287a9aca4a9b))

# [0.14.2](https://github.com/gotson/komga/compare/v0.14.1...v0.14.2) (2020-02-20)


## Bug Fixes

* webui works with baseUrl ([bb18382](https://github.com/gotson/komga/commit/bb183828a189ce5908d21304bbf1592ae074afc5))

# [0.14.1](https://github.com/gotson/komga/compare/v0.14.0...v0.14.1) (2020-02-14)


## Bug Fixes

* **webui:** make overlay buttons more responsive on smaller screens ([0c03950](https://github.com/gotson/komga/commit/0c0395060a7d6c3a2ef8feef79474d9149013e80))

# [0.14.0](https://github.com/gotson/komga/compare/v0.13.1...v0.14.0) (2020-02-05)


## Bug Fixes

* **api:** sort series properly ignoring case ([16dfe91](https://github.com/gotson/komga/commit/16dfe91140b1140c38a5b4e686bea6295a33dad0)), closes [#85](https://github.com/gotson/komga/issues/85)
* **webui:** hide filter menu after click ([2ded39f](https://github.com/gotson/komga/commit/2ded39f6d6b265b826aad67b9a8a05ba4fc93700))
* **webui:** missing data on back navigation with filters ([f1952ee](https://github.com/gotson/komga/commit/f1952eee4a4f920f4d1ec9f73f19cf0da9c32c16))
* **webui:** scrolling position was not restored properly ([be6a7fc](https://github.com/gotson/komga/commit/be6a7fc7174b0331d02af90bd6b8fd6d8d87225c))
* incorrect placeholder card height on xs and sm screens ([0f50a76](https://github.com/gotson/komga/commit/0f50a7690f609884abd9b902290a138cdb2f2f75))
* logout was broken after remember-me was added ([8b02471](https://github.com/gotson/komga/commit/8b02471be1f2eb9ea78ea112388a2ec003c180ba))


## Features

* add more series metadata fields ([8f08ce8](https://github.com/gotson/komga/commit/8f08ce82e1dd6f888d9e06eb09e29a13e84a6741))
* **api:** ability to filter series by status ([c96bf19](https://github.com/gotson/komga/commit/c96bf19048ba8de3d7b1370d68e2b52fc3c69f3f)), closes [#48](https://github.com/gotson/komga/issues/48)
* **webui:** add thumbnail and status on Series view ([0fc8b01](https://github.com/gotson/komga/commit/0fc8b0137feae0307b4d16ece4f135cdbec5aa13))
* **webui:** filter series by status ([c540e56](https://github.com/gotson/komga/commit/c540e56c088419b48e7fc686e0440b2a65b83dd9)), closes [#48](https://github.com/gotson/komga/issues/48)
* **webui):** edit series metadata ([5f0ccc5](https://github.com/gotson/komga/commit/5f0ccc5bfc35456908c7cc5435511589f23c0512))
* add Series Metadata status ([f522142](https://github.com/gotson/komga/commit/f5221420fd1388cbdd43b497c5470125577bbd2e)), closes [#48](https://github.com/gotson/komga/issues/48)

# [0.13.1](https://github.com/gotson/komga/compare/v0.13.0...v0.13.1) (2020-01-18)


## Bug Fixes

* trigger release ([b45a23c](https://github.com/gotson/komga/commit/b45a23c8c04b30af6d8a1df345aa29d8875a2b11))


## Reverts

* revert thumbnails library ([a685475](https://github.com/gotson/komga/commit/a6854753d2a40ea71868fc31ba170ca257495d6f))

# [0.13.0](https://github.com/gotson/komga/compare/v0.12.0...v0.13.0) (2020-01-18)


## Bug Fixes

* **admin rpc:** fix transaction issues on thumbnails regeneration ([af8e3ea](https://github.com/gotson/komga/commit/af8e3ea433944922eafb8ecc1490d332e27d6c0d))
* **thumbnails:** fix wrong color in thumbnails ([1d5500d](https://github.com/gotson/komga/commit/1d5500d5606c816fc94953adc263f9905c1b3881)), closes [#77](https://github.com/gotson/komga/issues/77)
* media comment was not reset properly ([b42eadf](https://github.com/gotson/komga/commit/b42eadf182a51c3cd073c6195c5da3fd6bcf0b7e))
* **web reader:** first/last display to full height in double pages mode ([a7548e2](https://github.com/gotson/komga/commit/a7548e298a24704780e4bed460656a8d78573749))
* **web reader:** remove blank space between images in double pages mode ([b65b009](https://github.com/gotson/komga/commit/b65b009e0d5e0a7c13ef63ab69030e3223730592)), closes [#72](https://github.com/gotson/komga/issues/72)


## Features

* **api:** search books by media status ([0790501](https://github.com/gotson/komga/commit/07905018e5bad44c8f29a3fd8da0c08326e674f5))
* **book analyzer:** partial handling of archives with errors ([2605b1d](https://github.com/gotson/komga/commit/2605b1d943148e1c71856b35028563be3ca86b26)), closes [#57](https://github.com/gotson/komga/issues/57)
* **browse book:** add button to read book when hovering on thumbnail ([c490e79](https://github.com/gotson/komga/commit/c490e799ba20e66c66f88d4381ad830eca3eaf77)), closes [#67](https://github.com/gotson/komga/issues/67)
* **security:** add remember-me option ([003452b](https://github.com/gotson/komga/commit/003452bd26a7a56387d563abca2f6ee5a7f62d13)), closes [#39](https://github.com/gotson/komga/issues/39)
* **web reader:** add 'original' fit option ([d030044](https://github.com/gotson/komga/commit/d030044df35b09522e5038254a9384c53cecd5ee)), closes [#71](https://github.com/gotson/komga/issues/71)
* **webui:** add Media Analysis screen showing all books in error ([27d46d5](https://github.com/gotson/komga/commit/27d46d57cbec468ac08e1cf061b77e0b616807d4)), closes [#26](https://github.com/gotson/komga/issues/26)

# [0.12.0](https://github.com/gotson/komga/compare/v0.11.0...v0.12.0) (2020-01-14)


## Bug Fixes

* **scanner:** compare file extensions with case insensitive ([91c9cdd](https://github.com/gotson/komga/commit/91c9cdd8323d02c792728ed20bb3a4589694bdf0)), closes [#59](https://github.com/gotson/komga/issues/59)
* **web reader:** conditional webp support ([ad21152](https://github.com/gotson/komga/commit/ad2115244aaf5dea0b2f5f667451d62d6c15069c)), closes [#65](https://github.com/gotson/komga/issues/65)


## Features

* **api:** add endpoints to get previous/next book of a book ([54f583f](https://github.com/gotson/komga/commit/54f583f0ce22e105a0bddde0287cc276dbbc12c9))
* **api:** on-th-fly thumbnail generation for any page ([7167f3e](https://github.com/gotson/komga/commit/7167f3ea24cd222ab023838d42231f8560a733e5))
* **web reader:** double page support ([77c9004](https://github.com/gotson/komga/commit/77c9004d5772bb623c27aebe764cbd155a9562cb)), closes [#61](https://github.com/gotson/komga/issues/61)
* **web reader:** remember fit and rtl ([78c181e](https://github.com/gotson/komga/commit/78c181e130b6085a45755aefec61ad181a0802be)), closes [#66](https://github.com/gotson/komga/issues/66)
* **web reader:** thumbnails explorer ([ec06955](https://github.com/gotson/komga/commit/ec06955e22f72e161ebe8c8b1f485b8139e03076)), closes [#62](https://github.com/gotson/komga/issues/62)
* better management of book analysis errors ([8c26a31](https://github.com/gotson/komga/commit/8c26a318fe84ced77fe763711dec78575073678f))

# [0.11.0](https://github.com/gotson/komga/compare/v0.10.1...v0.11.0) (2020-01-06)


## Bug Fixes

* **web reader:** remove webp as it's not supported in Safari ([6770107](https://github.com/gotson/komga/commit/6770107dc8532c9bd62e2dbcadc0124df151a385))
* add support for jpeg2000/jbig2 formats ([227975a](https://github.com/gotson/komga/commit/227975a79eecd7882e6f73c5e49910bc86eeba18)), closes [#50](https://github.com/gotson/komga/issues/50)


## Features

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

# [0.10.1](https://github.com/gotson/komga/compare/v0.10.0...v0.10.1) (2020-01-01)


## Bug Fixes

* **webui:** remove CDN usage for icons and fonts ([c88a27c](https://github.com/gotson/komga/commit/c88a27c10abdfecd5d9476ca74f382418922a546)), closes [#45](https://github.com/gotson/komga/issues/45)
* **webui:** show all books when browsing series ([85ca99d](https://github.com/gotson/komga/commit/85ca99d49aaeabed40b578dfff3e1d7f4c2e6bff))
* **zip extractor:** better handling of exotic charsets ([0254d7d](https://github.com/gotson/komga/commit/0254d7d8671a3743bfedbcd42472dbe974a76c98)), closes [#41](https://github.com/gotson/komga/issues/41)

<a name="v0.10.0"></a>
# [v0.10.0](https://github.com/gotson/komga/releases/tag/v0.10.0) - 31 Dec 2019

## Features

- **webui:** added **Web Reader** (#28)
- **webui:** display all books instead of only books in ready state
- **webui:** add 'Date updated' sort criteria for Series
- **webui:** add 'File size' sort criteria for Books
- **api:** handle the HTTP cache properly for dynamic resources: thumbnails and pages (#27)
- **api:** hide full path to non-admin users (for libraries, series and books)
- **scanner:** add configuration key to force the last modified time of directories (#37)

## Fixes

- **webui:** rework dashboard sliders to be more touch-friendly
- **webui:** better display on mobile and small screens
- remove regeneration of missing thumbnails at startup (don't remember why I added it in the first place ¯\_(ツ)_/¯ )

[Changes][v0.10.0]


<a name="v0.9.1"></a>
# [v0.9.1](https://github.com/gotson/komga/releases/tag/v0.9.1) - 18 Dec 2019

This release is focused on performance enhancements.

## Changes

- Hibernate lazy loading was not working because of Kotlin closed by default classes
- add Hibernate caches: second level, collections, query
- enhance code path to reduce number of database queries
- fix a bug where Series thumbnail would be retrieved by the first book in the collection, instead of the first by number
- remove (unused) bi-directional OneToOne relationship between Book and BookMetadata to reduce database fetch

[Changes][v0.9.1]


<a name="v0.9.0"></a>
# [v0.9.0](https://github.com/gotson/komga/releases/tag/v0.9.0) - 12 Dec 2019

## Features

- Web Interface enhancements (closes #18)
  - browse Libraries, Series, and Books
  - dashboard with recently added Series and Books
  - search Series and Books
  - remove browser pop-up for basic auth, use a dedicated login screen instead
  - http cache for static assets

## Changes

- retrieve real ip in audit logs (for example if behind a reverse proxy)
- remove Humio metrics
- libraries are sorted by name in the API by default

## Deprecation

- endpoints of the form `/series/{seriesId}/books/{bookId}/**`, use `/books/{bookId}/**` instead

## Known issues

- UI is extremely slow when browsing libraries/series with many items (75+) on Chrome Android

[Changes][v0.9.0]


<a name="v0.8.1"></a>
# [v0.8.1](https://github.com/gotson/komga/releases/tag/v0.8.1) - 05 Nov 2019

## Changes

- add metrics exporter for InfluxDB (disabled by default)

[Changes][v0.8.1]


<a name="v0.8.0"></a>
# [v0.8.0](https://github.com/gotson/komga/releases/tag/v0.8.0) - 30 Oct 2019

## Changes

- change docker base image to adoptopenjdk 11
- retrieve file size of books when scanning. Add file size in REST API and OPDS.

## Bug fixes

- Swagger is not showing Kotlin nullable types as optional (fixes #15)
- compare file modification time at millisecond. With JDK 9+ on Windows, time precision of the underlying clock would go over 6 digits, which is the precision of the timestamps in database, which would lead to loss of precision on saved timestamps, and failing comparisons of modification times at every library scan
- return only books in ready state via OPDS, else it would throw an error 500 because metadata is not ready
- prevent circular loop in the error resolver, which would complain in logs

[Changes][v0.8.0]


<a name="v0.7.1"></a>
# [v0.7.1](https://github.com/gotson/komga/releases/tag/v0.7.1) - 25 Oct 2019

## Bug fixes

- could not delete a library if it was specifically shared with any user

## Changes

- add build and git info in `/actuator/info` endpoint
- add `humio` metrics exporter (disabled, need to be enabled by configuration)

[Changes][v0.7.1]


<a name="v0.7.0"></a>
# [v0.7.0](https://github.com/gotson/komga/releases/tag/v0.7.0) - 22 Oct 2019

## :warning: Breaking changes

- `admin` and `user` users are deprecated and replaced by the User Management feature

## Features

- User management:
  - Automatic creation of an admin account at startup if no user exist in database, outputting the login and a random password in the logs
  - Ability to add/remove user accounts
  - Ability to manage roles: Admin/User
  - Ability to manage access to shared libraries per user
  - Ability for a user to change his/her password

## Changes

- OPDS entry links to file now include the full filename. This helps some (badly implemented) OPDS clients to correctly see the files.
- logs are written to disk, and available via the `/actuator/logfile` endpoint

## Bug fixes

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
