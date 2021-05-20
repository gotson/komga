## [0.95.5](https://github.com/gotson/komga/compare/v0.95.4...v0.95.5) (2021-05-20)


### Bug Fixes

* allow cors configuration ([f435b9d](https://github.com/gotson/komga/commit/f435b9dc784b91077e632b68b1f3124373f1ea1f)), closes [#540](https://github.com/gotson/komga/issues/540)

## [0.95.4](https://github.com/gotson/komga/compare/v0.95.3...v0.95.4) (2021-05-17)


### Bug Fixes

* book conversion would fail if page dimensions were missing ([e8e46a0](https://github.com/gotson/komga/commit/e8e46a0834f1ace429183973266fb25d573d0694))

## [0.95.3](https://github.com/gotson/komga/compare/v0.95.2...v0.95.3) (2021-05-17)


### Bug Fixes

* fix rar books with potential issues ([8befde0](https://github.com/gotson/komga/commit/8befde0ea8e8aba007fd9c0374a133e0f3a753ef))

## [0.95.2](https://github.com/gotson/komga/compare/v0.95.1...v0.95.2) (2021-05-14)


### Bug Fixes

* **api:** better handling of tachiyomi tracking ([a7ab0da](https://github.com/gotson/komga/commit/a7ab0da0253bb42b23db2d5b711dbe58eadca437))

## [0.95.1](https://github.com/gotson/komga/compare/v0.95.0...v0.95.1) (2021-05-11)


### Bug Fixes

* **webui:** import details dialog not updated correctly ([33cb083](https://github.com/gotson/komga/commit/33cb0836ba5b7c9dec53a6941b9dc6d4a25bd34b))

# [0.95.0](https://github.com/gotson/komga/compare/v0.94.0...v0.95.0) (2021-05-10)


### Features

* **api:** mark read progress for read lists with Tachiyomi format ([0177ee3](https://github.com/gotson/komga/commit/0177ee3e08ad08eb061bed668cb312e70c0b0858))
* **api:** mark read progress for series with Tachiyomi format ([82af4b3](https://github.com/gotson/komga/commit/82af4b3bbfb2a7dcb91b46032dc7a8e22e5d2e90))

# [0.94.0](https://github.com/gotson/komga/compare/v0.93.0...v0.94.0) (2021-05-09)


### Features

* added translation using Weblate (Vietnamese) ([648f0d4](https://github.com/gotson/komga/commit/648f0d40ff7d0ee6af26d294513914af2edb8915))

# [0.93.0](https://github.com/gotson/komga/compare/v0.92.1...v0.93.0) (2021-05-06)


### Features

* added translation using Weblate (Hungarian) ([3abc8bd](https://github.com/gotson/komga/commit/3abc8bde92db420128fe509680a73707f59d3dd7))

## [0.92.1](https://github.com/gotson/komga/compare/v0.92.0...v0.92.1) (2021-05-06)


### Bug Fixes

* priority tasks could be executed out of order ([55b2883](https://github.com/gotson/komga/commit/55b288388f26e223b09c3ea14d05f893b3a4cba6))

# [0.92.0](https://github.com/gotson/komga/compare/v0.91.3...v0.92.0) (2021-05-05)


### Features

* **webui:** library dialog options for extension repair ([f6ad0f0](https://github.com/gotson/komga/commit/f6ad0f0012b7d67a4cdbb95bad11cc5dcc8e2e9f))
* repair file extensions ([39cd31c](https://github.com/gotson/komga/commit/39cd31cbb6f8b50e36620c36f394d6d06822a3a2))
* **webui:** show in progress books as unread ([db95544](https://github.com/gotson/komga/commit/db955447f95e5946062a5aa91c5dbd356d8e0f82)), closes [#526](https://github.com/gotson/komga/issues/526)

## [0.91.3](https://github.com/gotson/komga/compare/v0.91.2...v0.91.3) (2021-05-05)


### Bug Fixes

* rar extractor could mis-analyze some images ([bd15aec](https://github.com/gotson/komga/commit/bd15aecccc507675aa16c5943fb91009bddf56dc))

## [0.91.2](https://github.com/gotson/komga/compare/v0.91.1...v0.91.2) (2021-05-05)


### Bug Fixes

* book conversion could fail because of different path separators ([e577e4a](https://github.com/gotson/komga/commit/e577e4a78f0586262fecfcb673af3201422435eb))

## [0.91.1](https://github.com/gotson/komga/compare/v0.91.0...v0.91.1) (2021-05-04)


### Bug Fixes

* **opds:** acquisition link is not a valid url ([00ade7f](https://github.com/gotson/komga/commit/00ade7fb362bc8ba30364d5353c857290838ccf3)), closes [#525](https://github.com/gotson/komga/issues/525)

# [0.91.0](https://github.com/gotson/komga/compare/v0.90.3...v0.91.0) (2021-05-04)


### Features

* **webui:** automatic book conversion in library settings ([1c3e853](https://github.com/gotson/komga/commit/1c3e853202b9fd5a4d3d7e7686304b20aa8abe8b))
* automatic book conversion to cbz ([dc2663e](https://github.com/gotson/komga/commit/dc2663ecb78b70c06a8ba1e364986191b802e638))

## [0.90.3](https://github.com/gotson/komga/compare/v0.90.2...v0.90.3) (2021-05-04)


### Bug Fixes

* **webreader:** browser back button would not honor page in query param ([dc4bd44](https://github.com/gotson/komga/commit/dc4bd440d9be77385815f98c4824384c152f2088))

## [0.90.2](https://github.com/gotson/komga/compare/v0.90.1...v0.90.2) (2021-05-04)


### Bug Fixes

* priority tasks ([6ee968b](https://github.com/gotson/komga/commit/6ee968be924b497bda482a13e876407a84d789b8))

## [0.90.1](https://github.com/gotson/komga/compare/v0.90.0...v0.90.1) (2021-05-03)


### Bug Fixes

* better stream handling for rar and zip files ([ebc8df2](https://github.com/gotson/komga/commit/ebc8df2053e270f420c6752619854d125041ef4e))

# [0.90.0](https://github.com/gotson/komga/compare/v0.89.3...v0.90.0) (2021-04-30)


### Features

* added translation using Weblate (Dutch) ([e15f174](https://github.com/gotson/komga/commit/e15f174ff124c14d753779d6f53e7a92e64b3b94))

## [0.89.3](https://github.com/gotson/komga/compare/v0.89.2...v0.89.3) (2021-04-29)


### Bug Fixes

* **webreader:** progress not marked correctly ([f3c541c](https://github.com/gotson/komga/commit/f3c541cd1452496469431439f85ec160564224d6)), closes [#518](https://github.com/gotson/komga/issues/518)

## [0.89.2](https://github.com/gotson/komga/compare/v0.89.1...v0.89.2) (2021-04-28)


### Bug Fixes

* add configuration to unload native webp library ([64a805e](https://github.com/gotson/komga/commit/64a805e0194357622ef7e18efc74d97a301ccdb3))
* native webp library excludes support for armv7l ([731e07a](https://github.com/gotson/komga/commit/731e07a070a6c2046e0db30bbc90bef0270d4687)), closes [#488](https://github.com/gotson/komga/issues/488)

## [0.89.1](https://github.com/gotson/komga/compare/v0.89.0...v0.89.1) (2021-04-28)


### Bug Fixes

* **webreader:** incorrect double pages display ([ece9813](https://github.com/gotson/komga/commit/ece981345fe78afe4e1ef82103aa2e9fbfc54244))
* **webui:** all libraries should show browse view by default ([3e1bf89](https://github.com/gotson/komga/commit/3e1bf89bdba60e60188960e462de4d335e23b946))
* **webui:** dashboard would not reload properly ([18e3e21](https://github.com/gotson/komga/commit/18e3e21f06d9e1a810eae98aaf2f03a97d2b91a4))

# [0.89.0](https://github.com/gotson/komga/compare/v0.88.1...v0.89.0) (2021-04-27)


### Bug Fixes

* **webui:** home link would not show as active ([4c6b3f5](https://github.com/gotson/komga/commit/4c6b3f5e5e4cc99bb952263d9a6b155063da5704))
* **webui:** wrong comparison could show error in javascript console ([b345c30](https://github.com/gotson/komga/commit/b345c30d1232fb8529c6e822b5a2e99565d12b4e))


### Features

* **webui:** display library navigation within toolbar ([a6252ff](https://github.com/gotson/komga/commit/a6252ff2e8e1664bab2492216667fc46546d7f64)), closes [#234](https://github.com/gotson/komga/issues/234)
* **webui:** remember view within library ([b1931aa](https://github.com/gotson/komga/commit/b1931aa892a71d6718c12ae00a917ef1e7d8fcb4)), closes [#367](https://github.com/gotson/komga/issues/367)
* Added a 'Recommended' tab in the library views for a library specific dashboard like the home page ([b26559d](https://github.com/gotson/komga/commit/b26559dc47a76220e20599caa807fe9a6b008d44))

## [0.88.1](https://github.com/gotson/komga/compare/v0.88.0...v0.88.1) (2021-04-26)


### Bug Fixes

* replace java webp library ([f658f9a](https://github.com/gotson/komga/commit/f658f9abe04896cbce6590bae0783836f4e59f74))

# [0.88.0](https://github.com/gotson/komga/compare/v0.87.5...v0.88.0) (2021-04-26)


### Features

* **webui:** ability to read incognito ([dc0cc13](https://github.com/gotson/komga/commit/dc0cc1380793bf870e11444debf1872a6cefc95c))

## [0.87.5](https://github.com/gotson/komga/compare/v0.87.4...v0.87.5) (2021-04-26)


### Bug Fixes

* **webui:** double pages could show duplicate pages ([76ba55a](https://github.com/gotson/komga/commit/76ba55a1242cfc90bceaddef8520719ac1165854))
* **webui:** mark read progress instantly ([97cc3e0](https://github.com/gotson/komga/commit/97cc3e043d15255bc53641bdf0b5cd53cb5c14c4)), closes [#475](https://github.com/gotson/komga/issues/475)
* **webui:** scroll continuous reader to top on book change ([d27828d](https://github.com/gotson/komga/commit/d27828de13fdbe348055fe4c23d129a29ad974e8)), closes [#475](https://github.com/gotson/komga/issues/475)

## [0.87.4](https://github.com/gotson/komga/compare/v0.87.3...v0.87.4) (2021-04-21)


### Bug Fixes

* **api:** filter referential data by access rights ([09fa5e9](https://github.com/gotson/komga/commit/09fa5e95a5a59f2136a7992468ca77a257a267a5)), closes [#492](https://github.com/gotson/komga/issues/492)

## [0.87.3](https://github.com/gotson/komga/compare/v0.87.2...v0.87.3) (2021-04-21)


### Bug Fixes

* error importing reading list with non-numerical number ([3ef0240](https://github.com/gotson/komga/commit/3ef02409bac07ee64c0136e34aac6afcf7c1aba4)), closes [#499](https://github.com/gotson/komga/issues/499)
* **webui:** stored filters are not validated ([fbb4171](https://github.com/gotson/komga/commit/fbb4171d9bd3727d3826e650fe97822ec146bd4d)), closes [#504](https://github.com/gotson/komga/issues/504)

## [0.87.2](https://github.com/gotson/komga/compare/v0.87.1...v0.87.2) (2021-04-20)


### Bug Fixes

* translated using Weblate (Esperanto) ([19ff72f](https://github.com/gotson/komga/commit/19ff72f9e34fce599b3f9691dcaa78b8a21909ef))
* **importer:** keep metadata when upgrading book ([a3b8866](https://github.com/gotson/komga/commit/a3b88667d0364edf68f5a906053af0977c176135))
* prevent transient scanning of directories that are part of existing libraries ([8a92b84](https://github.com/gotson/komga/commit/8a92b84fd06c7b6b8ae3ab6975f7802708598de3))
* **importer:** prevent import of files that are in an existing library ([b0170c7](https://github.com/gotson/komga/commit/b0170c7c8840089811108e275e76cf4bb0350d8b))

## [0.87.1](https://github.com/gotson/komga/compare/v0.87.0...v0.87.1) (2021-04-20)


### Bug Fixes

* **importer:** hardlink fails inside docker ([24cf181](https://github.com/gotson/komga/commit/24cf1819b02dd2625d7a6327a17ef2081cf52e64))

# [0.87.0](https://github.com/gotson/komga/compare/v0.86.0...v0.87.0) (2021-04-19)


### Bug Fixes

* translated using Weblate (Esperanto) ([#506](https://github.com/gotson/komga/issues/506)) ([248f474](https://github.com/gotson/komga/commit/248f47458cb82d777e0f5823ddbe044ca76092c7))


### Features

* **api:** import books ([d41dcef](https://github.com/gotson/komga/commit/d41dcefd3efd4f9844d5b3b1d336a246c320a1ec))
* **api:** support for transient books ([02b0893](https://github.com/gotson/komga/commit/02b08932babd27b5b309b3038279885ac65d0821))
* **webui:** import books ([13b304d](https://github.com/gotson/komga/commit/13b304dd147f3102345c2edb85d41f87ccae1871))
* added translation using Weblate (Esperanto, Polish) ([f3cc6f6](https://github.com/gotson/komga/commit/f3cc6f6e916862741cd7ff3aafa98a4c587653c6))

# [0.86.0](https://github.com/gotson/komga/compare/v0.85.1...v0.86.0) (2021-04-07)


### Bug Fixes

* **webui:** series year incorrectly formatted ([d166207](https://github.com/gotson/komga/commit/d16620791243201f2e2eb0910201f73e2c2975f7))


### Features

* added translation using Weblate (Finnish) ([81142ab](https://github.com/gotson/komga/commit/81142ab570ea9ce1cfd964e7c3205d0c1a9ead7a))

## [0.85.1](https://github.com/gotson/komga/compare/v0.85.0...v0.85.1) (2021-03-31)


### Bug Fixes

* **webreader:** settings are not persisted ([d202dc2](https://github.com/gotson/komga/commit/d202dc239a1d7f8b440ec09fa509ebe0686bc695)), closes [#489](https://github.com/gotson/komga/issues/489)

# [0.85.0](https://github.com/gotson/komga/compare/v0.84.1...v0.85.0) (2021-03-29)


### Bug Fixes

* **webui:** page size chooser would not always reflect the actual value ([5ee4230](https://github.com/gotson/komga/commit/5ee423030f2775754a9e556fcef776b9517eb902))


### Features

* **webui:** change/restore theme even on login page ([7f7c6c3](https://github.com/gotson/komga/commit/7f7c6c3e6f7c75b90aa15666f3af5b657e5764bd))

## [0.84.1](https://github.com/gotson/komga/compare/v0.84.0...v0.84.1) (2021-03-24)


### Bug Fixes

* download current page not working on mobile ([f98d791](https://github.com/gotson/komga/commit/f98d7915c599580a39c4aa77e9b491a16af79e3c))

# [0.84.0](https://github.com/gotson/komga/compare/v0.83.0...v0.84.0) (2021-03-24)


### Features

* download current page from webreader ([93cec4e](https://github.com/gotson/komga/commit/93cec4e4e593a0bdbe3b3c324dda28d843dbdf49)), closes [#469](https://github.com/gotson/komga/issues/469)

# [0.83.0](https://github.com/gotson/komga/compare/v0.82.1...v0.83.0) (2021-03-22)


### Bug Fixes

* refresh series metadata and aggregation after book deletion ([9e44437](https://github.com/gotson/komga/commit/9e4443765b77f503177871cfc68d3252dd0e4206))
* skip ISBN barcode provider if not enabled in library ([9cb63b5](https://github.com/gotson/komga/commit/9cb63b57985afb192555ab89feb1aa8163207000))


### Features

* import ISBN from epub metadata, split authors on comma ([52fceec](https://github.com/gotson/komga/commit/52fceecaa9c15fb9d75844b6f56ed948f0e4b947))


### Performance Improvements

* only refresh relevant metadata providers on series sort ([3dccb9a](https://github.com/gotson/komga/commit/3dccb9a6a7e6b51cbdbd85fc12fc4b001998864a))

## [0.82.1](https://github.com/gotson/komga/compare/v0.82.0...v0.82.1) (2021-03-22)


### Bug Fixes

* **webui:** display status instead of pages on cards for books not ready ([dcf065f](https://github.com/gotson/komga/commit/dcf065f0054088063d0c2e2773043e9df063a9c8))
* translated using Weblate ([a01b764](https://github.com/gotson/komga/commit/a01b764b07f49022e74ccc65b4ab9cfe92d1b46d))

# [0.82.0](https://github.com/gotson/komga/compare/v0.81.1...v0.82.0) (2021-03-19)


### Features

* **webui:** import ComicRack lists as read lists ([8b0dac3](https://github.com/gotson/komga/commit/8b0dac312589dbd5fd9a6c972d61802147022b2f))
* added translation using Weblate (Italian)  ([e5343d7](https://github.com/gotson/komga/commit/e5343d7ab40b95eaed861a0cb65282911093a0aa))
* import ComicRack lists as read lists ([c1e4357](https://github.com/gotson/komga/commit/c1e435762c732d38a4e074f4927fa17445d89709)), closes [#464](https://github.com/gotson/komga/issues/464)

## [0.81.1](https://github.com/gotson/komga/compare/v0.81.0...v0.81.1) (2021-03-16)


### Bug Fixes

* prevent crash during h2 cleanup if parent folder is null ([bd20ff6](https://github.com/gotson/komga/commit/bd20ff67cc9b1973323d603b384783ae533eaed7))
* translated using Weblate (French) ([a5f7dc5](https://github.com/gotson/komga/commit/a5f7dc5dafe59ba43a1fc5442a58a0adaee98f18))
* translated using Weblate (Japanese) ([7c95fff](https://github.com/gotson/komga/commit/7c95fffccf6d2e1dc161121aa9e1880e699534e2))
* translated using Weblate (Swedish) ([1b72135](https://github.com/gotson/komga/commit/1b72135beff2e82c1df885d9d1e5580444056397))

# [0.81.0](https://github.com/gotson/komga/compare/v0.80.0...v0.81.0) (2021-03-15)


### Features

* remove H2 dependencies ([50248e7](https://github.com/gotson/komga/commit/50248e72332c493b070d1a200eec994661fd6b7d)), closes [#455](https://github.com/gotson/komga/issues/455)
* sort series by books count ([b51e491](https://github.com/gotson/komga/commit/b51e4917e1636c83a121952b1b69bd3ab881a279)), closes [#459](https://github.com/gotson/komga/issues/459)

# [0.80.0](https://github.com/gotson/komga/compare/v0.79.1...v0.80.0) (2021-03-11)


### Features

* localize server side errors ([cbe47ea](https://github.com/gotson/komga/commit/cbe47ea5931427193bc853e80b8faae40135ec1a))


### Performance Improvements

* prevent failing tasks ([07cec50](https://github.com/gotson/komga/commit/07cec50417da93d299f59734d6e6139e8e037589))

## [0.79.1](https://github.com/gotson/komga/compare/v0.79.0...v0.79.1) (2021-03-10)


### Performance Improvements

* reduce disk usage during filesystem scan ([e154583](https://github.com/gotson/komga/commit/e154583d30c49f6e88473b7a6594e39365498d34))

# [0.79.0](https://github.com/gotson/komga/compare/v0.78.0...v0.79.0) (2021-03-10)


### Bug Fixes

* **api:** some metadata fields would not unset if set to null ([1996071](https://github.com/gotson/komga/commit/1996071794dddc87c67d99a676f901c725870043))
* **webui:** only show writers and pencillers on series screen ([6101e83](https://github.com/gotson/komga/commit/6101e83292b2e238508559db28fe249bf9fb0fff))


### Features

* **webui:** group checkbox for advanced library options ([6fa0324](https://github.com/gotson/komga/commit/6fa0324666050c80f5247a3313d20ea349a08754))
* **webui:** manage isbn barcode import for libraries ([ee1a0a2](https://github.com/gotson/komga/commit/ee1a0a2aa0909f79dfe99da484ba519e6daaac7d))
* **webui:** show and edit ISBN for book ([65c16f1](https://github.com/gotson/komga/commit/65c16f109f2a1172128b91e0d7f015206a523c48))
* read ISBN from barcode ([6431b1f](https://github.com/gotson/komga/commit/6431b1f0008fc43c1997dec966d93c5b9e1531d0)), closes [#380](https://github.com/gotson/komga/issues/380) [#381](https://github.com/gotson/komga/issues/381)

# [0.78.0](https://github.com/gotson/komga/compare/v0.77.3...v0.78.0) (2021-03-09)


### Bug Fixes

* **webui:** adjust read and download button icon size ([6cdc777](https://github.com/gotson/komga/commit/6cdc777be9d62116859e956057c8a3396f2d0277))


### Features

* add Japanese translation ([8e5748d](https://github.com/gotson/komga/commit/8e5748d97309b5821d38d04c0285f3240a087e7b))

## [0.77.3](https://github.com/gotson/komga/compare/v0.77.2...v0.77.3) (2021-03-05)


### Bug Fixes

* translated using Weblate (Swedish) ([04c1dde](https://github.com/gotson/komga/commit/04c1dde4eefcc4abaccce1eefb7d6129954d8433))

## [0.77.2](https://github.com/gotson/komga/compare/v0.77.1...v0.77.2) (2021-03-03)


### Bug Fixes

* **webui:** keyboard navigation in searchbox results ([604ccf1](https://github.com/gotson/komga/commit/604ccf11928e8f80b4a9470e92272770bb08ed96)), closes [#250](https://github.com/gotson/komga/issues/250)
* **webui:** rearrange browse series and books views ([a5c7b17](https://github.com/gotson/komga/commit/a5c7b17829b73cb11e6e365c0949ccb624e6e0c3))

## [0.77.1](https://github.com/gotson/komga/compare/v0.77.0...v0.77.1) (2021-03-02)


### Bug Fixes

* **webui:** show only author roles present ([847b704](https://github.com/gotson/komga/commit/847b7044a0730683bf57b5907799fa7dffed0f13))

# [0.77.0](https://github.com/gotson/komga/compare/v0.76.0...v0.77.0) (2021-03-02)


### Features

* **webui:** show read status on Books and Series when searching  ([234997c](https://github.com/gotson/komga/commit/234997c27d86d93c23bc26e989954e6da384865d)), closes [#433](https://github.com/gotson/komga/issues/433)

# [0.76.0](https://github.com/gotson/komga/compare/v0.75.2...v0.76.0) (2021-03-02)


### Features

* **webui:** make authors chips clickable ([9fed50e](https://github.com/gotson/komga/commit/9fed50e4056a13570f90a1bc289117b27b253eda)), closes [#431](https://github.com/gotson/komga/issues/431)

## [0.75.2](https://github.com/gotson/komga/compare/v0.75.1...v0.75.2) (2021-03-01)


### Bug Fixes

* better email validation ([97871f7](https://github.com/gotson/komga/commit/97871f7fbc3b615b02ffbaf1f81359cce03a57c1)), closes [#434](https://github.com/gotson/komga/issues/434)

## [0.75.1](https://github.com/gotson/komga/compare/v0.75.0...v0.75.1) (2021-02-26)


### Bug Fixes

* **webui:** validation on user login screen ([392b3b8](https://github.com/gotson/komga/commit/392b3b87fe9ff4778086a8f6b34e144e0582c772)), closes [#429](https://github.com/gotson/komga/issues/429)

# [0.75.0](https://github.com/gotson/komga/compare/v0.74.0...v0.75.0) (2021-02-26)


### Bug Fixes

* **webui:** filter panel would not display values properly ([0c5a744](https://github.com/gotson/komga/commit/0c5a7447f8b7d76e5ad5af3a482dae7e64289261))
* translated using Weblate (Chinese (Simplified)) ([#430](https://github.com/gotson/komga/issues/430)) ([b68e3e5](https://github.com/gotson/komga/commit/b68e3e54b86ee258f2b4cae7ab0d6a9641f041d1))


### Features

* **api:** filter series and books by authors ([bd64381](https://github.com/gotson/komga/commit/bd64381a8e869b26c84168722a100b1e9572c505)), closes [#339](https://github.com/gotson/komga/issues/339)
* **api:** revamp search authors ([f549067](https://github.com/gotson/komga/commit/f549067a8ae3e0e5a44eac97aa8002712b0d20b1))
* **webui:** filter series and books by authors ([c2c2f58](https://github.com/gotson/komga/commit/c2c2f58f1a4fc24a46bc9377768d75fa5f73c6ac)), closes [#339](https://github.com/gotson/komga/issues/339)

# [0.74.0](https://github.com/gotson/komga/compare/v0.73.2...v0.74.0) (2021-02-25)


### Features

* **webui:** make publisher, status, language, ageRating chips clickable ([8bf805e](https://github.com/gotson/komga/commit/8bf805e1e414a0f9464d121b0de53515f11697f1))

## [0.73.2](https://github.com/gotson/komga/compare/v0.73.1...v0.73.2) (2021-02-24)


### Bug Fixes

* translated using Weblate (Chinese (Simplified)) ([#425](https://github.com/gotson/komga/issues/425)) ([e270034](https://github.com/gotson/komga/commit/e270034c8023b46b89d4dd830ef7781ed4a736e0))

## [0.73.1](https://github.com/gotson/komga/compare/v0.73.0...v0.73.1) (2021-02-24)


### Bug Fixes

* **webui:** properly restore query params on page reload ([01f9317](https://github.com/gotson/komga/commit/01f9317b89e211bbc69512d4965a1fee09fcc9e6))

# [0.73.0](https://github.com/gotson/komga/compare/v0.72.0...v0.73.0) (2021-02-24)


### Bug Fixes

* **webui:** incorrect display of html in library delete dialog ([fd6fee1](https://github.com/gotson/komga/commit/fd6fee19d886c397d6c79d0a6013a4bdae2ed097))
* **webui:** remove hash prefix for book numbers ([114f55f](https://github.com/gotson/komga/commit/114f55fe864d70a6fae40a5a0406f9d9111b504a))
* **webui:** text overlapping in some locales ([0ac9f24](https://github.com/gotson/komga/commit/0ac9f24245133214fba6f2e64616eaf81de6344e))


### Features

* **webui:** sort books by file name ([55f0647](https://github.com/gotson/komga/commit/55f06476f205f3c5ce60bba18cb5ce68db4251e4)), closes [#420](https://github.com/gotson/komga/issues/420)
* sort series by folder name ([6ba5cd4](https://github.com/gotson/komga/commit/6ba5cd483218644bdc09e9cdfe573f22c1389963))
* **webui:** Duplicate pagination links at bottom of content lists ([22cdd28](https://github.com/gotson/komga/commit/22cdd28f93bffe814877f68487ebd6eceb8b3fa8))
* add simplified chinese translation ([b76907f](https://github.com/gotson/komga/commit/b76907fe4be29dcb1fa20903ef9ea380df3cd284))

# [0.72.0](https://github.com/gotson/komga/compare/v0.71.7...v0.72.0) (2021-02-22)


### Features

* **api:** enable actuator shutdown endpoint ([8f5ba00](https://github.com/gotson/komga/commit/8f5ba00f1ab64a406bf8aefb752f5b5678f44b4c))
* **webui:** new server management screen with shutdown button ([fd81e17](https://github.com/gotson/komga/commit/fd81e17dedada691d92871be2d77794f29a3c250)), closes [#416](https://github.com/gotson/komga/issues/416)

## [0.71.7](https://github.com/gotson/komga/compare/v0.71.6...v0.71.7) (2021-02-22)


### Bug Fixes

* translated using Weblate (French) ([f5fdf5c](https://github.com/gotson/komga/commit/f5fdf5c8a925ecfc993b23d50364a88481250f4f))
* translated using Weblate (Norwegian Bokmål) ([33fd19b](https://github.com/gotson/komga/commit/33fd19befb10464f49af28fc2d0fc94140a929ff))

## [0.71.6](https://github.com/gotson/komga/compare/v0.71.5...v0.71.6) (2021-02-22)


### Bug Fixes

* **webui:** missing i18n for vuetify datatable ([4fc4e32](https://github.com/gotson/komga/commit/4fc4e32d00d390f438d2564a518433d635c5e440))

## [0.71.5](https://github.com/gotson/komga/compare/v0.71.4...v0.71.5) (2021-02-22)


### Bug Fixes

* **webui:** add i18n for Vuetify datatable ([add6160](https://github.com/gotson/komga/commit/add6160eacffc0dd5f242ecca08e9fedc6e0587f))
* **webui:** add i18n media analysis media status ([6d5b50e](https://github.com/gotson/komga/commit/6d5b50e3c5992f7814600e781bf17f28ecf31dc3))
* **webui:** adjust summary text font size ([cd8805c](https://github.com/gotson/komga/commit/cd8805c434c2608f5ff19cde9d0c9e7f503d2b72))
* **webui:** change grid layout for browse book ([c8543a9](https://github.com/gotson/komga/commit/c8543a95e1993bc4f3d75f419276b55652d77f13))
* **webui:** missing i18n for Settings Users screen ([8a3b16d](https://github.com/gotson/komga/commit/8a3b16db8f6fd163949b96ea36c74d563debc2b2))
* **webui:** missing i18n on collection delete dialog ([f57b949](https://github.com/gotson/komga/commit/f57b949de26917414f6637455671560663ce89f7))
* **webui:** remove text capitalization for some i18n strings ([4c0b24b](https://github.com/gotson/komga/commit/4c0b24b9ac91854c31824fe0d28f7b923556c43b))

## [0.71.4](https://github.com/gotson/komga/compare/v0.71.3...v0.71.4) (2021-02-20)


### Bug Fixes

* **webui:** translations update from Weblate ([#415](https://github.com/gotson/komga/issues/415)) ([71a280a](https://github.com/gotson/komga/commit/71a280a1b0c6c0ee201f1b85302cac1829c69d9e))

## [0.71.3](https://github.com/gotson/komga/compare/v0.71.2...v0.71.3) (2021-02-19)


### Bug Fixes

* **webui:** filter panel would not show values properly ([a63daaf](https://github.com/gotson/komga/commit/a63daafcce8975df46b9d65be83e9d1aac4fb374))
* **webui:** incorrect rtl icon ([98a8f61](https://github.com/gotson/komga/commit/98a8f61a488856e038451a866184b0b1e3402f12))

## [0.71.2](https://github.com/gotson/komga/compare/v0.71.1...v0.71.2) (2021-02-19)


### Bug Fixes

* **webui:** right to left compatibility ([9420010](https://github.com/gotson/komga/commit/9420010caeba3053fd22e0579c2224c42dd08326))

## [0.71.1](https://github.com/gotson/komga/compare/v0.71.0...v0.71.1) (2021-02-19)


### Bug Fixes

* handle query parameters with square brackets ([245dea9](https://github.com/gotson/komga/commit/245dea906c52245ada916c7282cc5e579b4b8a87))

# [0.71.0](https://github.com/gotson/komga/compare/v0.70.0...v0.71.0) (2021-02-17)


### Features

* series download ([e44bc7b](https://github.com/gotson/komga/commit/e44bc7b4915e8d8d55ad3c89c6148bdaba33278f))

# [0.70.0](https://github.com/gotson/komga/compare/v0.69.2...v0.70.0) (2021-02-16)


### Features

* **webui:** support for translations ([efe6476](https://github.com/gotson/komga/commit/efe6476a9057ce610c61278a9e402c250ee61365)), closes [#187](https://github.com/gotson/komga/issues/187)


### Reverts

* update sqlite library to handle armv6 ([e39a2ac](https://github.com/gotson/komga/commit/e39a2ac6bac77f29d29828e2e58f0d97033cc15d))

## [0.69.2](https://github.com/gotson/komga/compare/v0.69.1...v0.69.2) (2021-01-29)


### Bug Fixes

* update sqlite library to handle armv6 ([33a10b3](https://github.com/gotson/komga/commit/33a10b3f7b8b2f40ecb05a905d1a6157fac2b075)), closes [#398](https://github.com/gotson/komga/issues/398)

## [0.69.1](https://github.com/gotson/komga/compare/v0.69.0...v0.69.1) (2021-01-25)


### Bug Fixes

* **api:** sort series release years descending ([3cea176](https://github.com/gotson/komga/commit/3cea17612e82606503a5e8900cd8fc190a0eb707)), closes [#395](https://github.com/gotson/komga/issues/395)

# [0.69.0](https://github.com/gotson/komga/compare/v0.68.2...v0.69.0) (2021-01-25)


### Features

* **api:** filter series by release year ([9133f3a](https://github.com/gotson/komga/commit/9133f3a441a6a3e3dab80cbbc701049df060beda))
* **api:** get all release years for series ([c68951b](https://github.com/gotson/komga/commit/c68951be849a85b2852afabdf49e060974be6f96))
* **webui:** filter series by release year ([33ecb72](https://github.com/gotson/komga/commit/33ecb72f57cecdd63a7f5a2d37de95b3b965eeaf)), closes [#374](https://github.com/gotson/komga/issues/374)

## [0.68.2](https://github.com/gotson/komga/compare/v0.68.1...v0.68.2) (2021-01-24)


### Bug Fixes

* **scanner:** prevent library deletion when inaccessible ([2a262cc](https://github.com/gotson/komga/commit/2a262ccad1c276bf8bc7f489305dac9cf1c295c6)), closes [#392](https://github.com/gotson/komga/issues/392)

## [0.68.1](https://github.com/gotson/komga/compare/v0.68.0...v0.68.1) (2021-01-11)


### Bug Fixes

* **webui:** truncate summary and authors when too long ([9071ad5](https://github.com/gotson/komga/commit/9071ad59ef9f52482c73849732ca2967617a6e68))

# [0.68.0](https://github.com/gotson/komga/compare/v0.67.1...v0.68.0) (2021-01-11)


### Features

* **webui:** display book aggregated data on series screen ([e642174](https://github.com/gotson/komga/commit/e642174c8450fa9e6f6707e59f1c3830e283a47f)), closes [#181](https://github.com/gotson/komga/issues/181)
* aggregate book information at series level ([eb029d9](https://github.com/gotson/komga/commit/eb029d9bb5506775fac03a5fd2eca93d32678f57))

## [0.67.1](https://github.com/gotson/komga/compare/v0.67.0...v0.67.1) (2021-01-09)


### Bug Fixes

* scanner should ignore hidden files ([2d72b47](https://github.com/gotson/komga/commit/2d72b47cdfc03b90554e01259dadd8a5b37ab85b))
* scanner would not ignore hidden directories properly ([1cbcdaa](https://github.com/gotson/komga/commit/1cbcdaa0a193bc3440e9b25fc9b7f1db83808885))

# [0.67.0](https://github.com/gotson/komga/compare/v0.66.1...v0.67.0) (2021-01-06)


### Features

* **webui:** filter collections/readlists in Add To dialog ([1b422a2](https://github.com/gotson/komga/commit/1b422a2086113dce613121c46a79272d13e6b114)), closes [#378](https://github.com/gotson/komga/issues/378)

## [0.66.1](https://github.com/gotson/komga/compare/v0.66.0...v0.66.1) (2021-01-06)


### Bug Fixes

* **webui:** context navigation messed up ([ba61660](https://github.com/gotson/komga/commit/ba6166031bf02eabd246187ec5af09e28ee38362))

# [0.66.0](https://github.com/gotson/komga/compare/v0.65.0...v0.66.0) (2021-01-06)


### Bug Fixes

* **api:** endpoint for books in readlist is not filtered properly ([cdca78b](https://github.com/gotson/komga/commit/cdca78b38f1071e592d458cf9bc29c057d44197d))


### Features

* **api:** find previous/next book in readlist ([bcfb203](https://github.com/gotson/komga/commit/bcfb203f74e16fcc9df53e69720680a7cb328eb0))
* **webreader:** navigate between books of a readlist ([3a3d85c](https://github.com/gotson/komga/commit/3a3d85c3fc83f1a0c74adc091a843870b59bcc12)), closes [#310](https://github.com/gotson/komga/issues/310)
* **webui:** navigate between books of a readlist ([88d4342](https://github.com/gotson/komga/commit/88d4342ef5a17bb65f966b77620cd8dfbd7e42b9))

# [0.65.0](https://github.com/gotson/komga/compare/v0.64.8...v0.65.0) (2021-01-05)


### Features

* **api:** filter /series endpoint by library_id ([1603a96](https://github.com/gotson/komga/commit/1603a96de6a0bb4fdcadad01220498f63a8fd729)), closes [#360](https://github.com/gotson/komga/issues/360)

## [0.64.8](https://github.com/gotson/komga/compare/v0.64.7...v0.64.8) (2020-12-31)


### Bug Fixes

* fix sqlite library detection for aarch64 ([10df531](https://github.com/gotson/komga/commit/10df5316b3381183e32de10c0db83c72534da881))

## [0.64.7](https://github.com/gotson/komga/compare/v0.64.6...v0.64.7) (2020-12-18)


### Bug Fixes

* **opds:** duplicate series if in multiple collections ([c7e6209](https://github.com/gotson/komga/commit/c7e62097656b435bd3e728e95038a7986f226525)), closes [#363](https://github.com/gotson/komga/issues/363)

## [0.64.6](https://github.com/gotson/komga/compare/v0.64.5...v0.64.6) (2020-12-03)


### Bug Fixes

* use bionic base image ([85fe674](https://github.com/gotson/komga/commit/85fe674e6a3f4977450f089b83c91f22e1f6f390)), closes [#349](https://github.com/gotson/komga/issues/349)

## [0.64.5](https://github.com/gotson/komga/compare/v0.64.4...v0.64.5) (2020-11-23)


### Bug Fixes

* enhanced logging for sqlite native lib loading ([f1a894d](https://github.com/gotson/komga/commit/f1a894db9eb9c6720353039a9dc79c021856cabd))

## [0.64.4](https://github.com/gotson/komga/compare/v0.64.3...v0.64.4) (2020-11-02)


### Bug Fixes

* **api:** support accept header in getBookPage ([#352](https://github.com/gotson/komga/issues/352)) ([e123d38](https://github.com/gotson/komga/commit/e123d386928c07c69915fcaa903c1a9b5373b8c6)), closes [#350](https://github.com/gotson/komga/issues/350)

## [0.64.3](https://github.com/gotson/komga/compare/v0.64.2...v0.64.3) (2020-10-29)


### Bug Fixes

* don't append volume to series title if equals to 1 ([1cb9ae6](https://github.com/gotson/komga/commit/1cb9ae6aad85900db1a1dc12e14a7a88d4a430a9)), closes [#347](https://github.com/gotson/komga/issues/347)

## [0.64.2](https://github.com/gotson/komga/compare/v0.64.1...v0.64.2) (2020-10-25)


### Bug Fixes

* native webp library ([15caea3](https://github.com/gotson/komga/commit/15caea3b99b76162aa636bcd691032a4510f32ff)), closes [#279](https://github.com/gotson/komga/issues/279)

## [0.64.1](https://github.com/gotson/komga/compare/v0.64.0...v0.64.1) (2020-10-24)


### Bug Fixes

* use own distrib of sqlite with freebsd support ([3d2793a](https://github.com/gotson/komga/commit/3d2793a768cb220265246fabc16ec3474e0af935)), closes [#319](https://github.com/gotson/komga/issues/319)

# [0.64.0](https://github.com/gotson/komga/compare/v0.63.3...v0.64.0) (2020-10-22)


### Features

* append volume to series name ([2567796](https://github.com/gotson/komga/commit/25677967db255efe39834ce31fe0f048481a3067)), closes [#343](https://github.com/gotson/komga/issues/343) [#132](https://github.com/gotson/komga/issues/132)

## [0.63.3](https://github.com/gotson/komga/compare/v0.63.2...v0.63.3) (2020-10-20)


### Bug Fixes

* scanner crashes if folders are not readable ([f43a386](https://github.com/gotson/komga/commit/f43a3864f869d254e75b18c00c4a1585ed69fd25)), closes [#342](https://github.com/gotson/komga/issues/342)

## [0.63.2](https://github.com/gotson/komga/compare/v0.63.1...v0.63.2) (2020-10-05)


### Bug Fixes

* **api:** incorrect parameter type ([342f04c](https://github.com/gotson/komga/commit/342f04c117ee0ad2d26782f7ee65ec14247f741e))

## [0.63.1](https://github.com/gotson/komga/compare/v0.63.0...v0.63.1) (2020-09-29)


### Bug Fixes

* **opds:** url encode publisher query param ([8347fdc](https://github.com/gotson/komga/commit/8347fdc44d5610159599ba9946ae28d26779e65b)), closes [#335](https://github.com/gotson/komga/issues/335)

# [0.63.0](https://github.com/gotson/komga/compare/v0.62.6...v0.63.0) (2020-09-28)


### Features

* **opds:** browse series by publishers ([f3fcab4](https://github.com/gotson/komga/commit/f3fcab41e49cd3fbe861ec8110fa9f2ee0972ec1)), closes [#332](https://github.com/gotson/komga/issues/332)
* **opds:** recently added books ([dd7bc9c](https://github.com/gotson/komga/commit/dd7bc9cbf63c51a7b9655d26d7c69c005f13417c)), closes [#327](https://github.com/gotson/komga/issues/327)
* order books by release date ([e3bb8bc](https://github.com/gotson/komga/commit/e3bb8bc7aee48154a4798620baa3b5f409f6d2a5)), closes [#318](https://github.com/gotson/komga/issues/318)

## [0.62.6](https://github.com/gotson/komga/compare/v0.62.5...v0.62.6) (2020-09-12)


### Bug Fixes

* **webreader:** images smaller than viewport would not fill width ([81d9a00](https://github.com/gotson/komga/commit/81d9a001850191b6ce2f6891caf296cc015a8dc6)), closes [#311](https://github.com/gotson/komga/issues/311)

## [0.62.5](https://github.com/gotson/komga/compare/v0.62.4...v0.62.5) (2020-09-06)


### Bug Fixes

* trim and lowercase tags and genres in metadata ([656f23a](https://github.com/gotson/komga/commit/656f23a64abd5cd22de6d670e64352df005a628e)), closes [#302](https://github.com/gotson/komga/issues/302)
* **opds:** cannot download file with semicolon in filename ([e1e251e](https://github.com/gotson/komga/commit/e1e251ec2f384892d7705ab590c318cf0e3232bb)), closes [#309](https://github.com/gotson/komga/issues/309)

## [0.62.4](https://github.com/gotson/komga/compare/v0.62.3...v0.62.4) (2020-09-05)


### Bug Fixes

* **api:** openAPI spec fields marked as required ([8f228db](https://github.com/gotson/komga/commit/8f228dbb1868c17133c61b85375d027dbce532f6)), closes [#308](https://github.com/gotson/komga/issues/308)

## [0.62.3](https://github.com/gotson/komga/compare/v0.62.2...v0.62.3) (2020-09-01)


### Bug Fixes

* **webui:** clear selection after action performed ([e0cff70](https://github.com/gotson/komga/commit/e0cff70ff513a35cdeb509a168e0663219f89090)), closes [#303](https://github.com/gotson/komga/issues/303)
* **webui:** edit series dialog could incorrectly display MIXED placeholder ([5d3f061](https://github.com/gotson/komga/commit/5d3f061a810dd9deefec710ebeec45c0ed1f5b18))
* **webui:** filter values could be duplicated ([e91954f](https://github.com/gotson/komga/commit/e91954f6bd0072d5316e716e9ff687225684d549))
* **webui:** theme is not restored properly ([432dc91](https://github.com/gotson/komga/commit/432dc9156293bfaabbdde01472229cf2951ff504)), closes [#304](https://github.com/gotson/komga/issues/304)

## [0.62.2](https://github.com/gotson/komga/compare/v0.62.1...v0.62.2) (2020-08-31)


### Bug Fixes

* **opds:** latest series sort inverted ([bddc008](https://github.com/gotson/komga/commit/bddc00855202d4c3694f7069837178b029ebf026)), closes [#301](https://github.com/gotson/komga/issues/301)
* **webui:** compute scrollability on mount, fix right chevron when using display scaling ([8928959](https://github.com/gotson/komga/commit/8928959fdb8adf9c555379c67a13a657fc291e05))

## [0.62.1](https://github.com/gotson/komga/compare/v0.62.0...v0.62.1) (2020-08-28)


### Bug Fixes

* series genre is not imported from metadata ([58fac36](https://github.com/gotson/komga/commit/58fac365ad046fa652ea825ba7b0c57b24b8d141))

# [0.62.0](https://github.com/gotson/komga/compare/v0.61.0...v0.62.0) (2020-08-28)


### Bug Fixes

* **webui:** correct label in library edit dialog ([c532e50](https://github.com/gotson/komga/commit/c532e50489aa5c873cc3c64da9918cd48f1d9ff7))
* **webui:** restore library filter for language and age rating ([dc064f2](https://github.com/gotson/komga/commit/dc064f20ecd671e35b399b79263b180214ce6498))


### Features

* **api:** filter collection's series ([cfa06a9](https://github.com/gotson/komga/commit/cfa06a9d2bd890e37bf46ba70c8494e7a27a1a15))
* **api:** get all age ratings ([be80d86](https://github.com/gotson/komga/commit/be80d86d6cba564293053f883c0ceda186652fe0))
* **api:** get referential data by collection ([d7fd296](https://github.com/gotson/komga/commit/d7fd2964928530e221be800d4cafe4334dbbf368))
* **api:** search series by age rating ([f51d575](https://github.com/gotson/komga/commit/f51d575bda49a89ff565009aec4a0b866095e2d9))
* **webui:** filter collection content ([fc905ef](https://github.com/gotson/komga/commit/fc905ef9b5bda44e1da67299e9e87865a04652ab)), closes [#270](https://github.com/gotson/komga/issues/270)
* **webui:** filter series by age rating ([01eef83](https://github.com/gotson/komga/commit/01eef838a20ee01b179bf3ad5dcd03b94f0e7a8b))

# [0.61.0](https://github.com/gotson/komga/compare/v0.60.0...v0.61.0) (2020-08-27)


### Bug Fixes

* **webui:** contextual filters ([f515819](https://github.com/gotson/komga/commit/f5158197ded703d57c3c0fb7eb6c31dae1629d03)), closes [#290](https://github.com/gotson/komga/issues/290)
* **webui:** disable filter panels with no values ([57cc6c4](https://github.com/gotson/komga/commit/57cc6c48d3249f80756bb2a8c4a79b590c514a42))


### Features

* **api:** filter referential data by library or series ([982983e](https://github.com/gotson/komga/commit/982983e3db0ca0e4fa143ef510f4a61ff2bd0d57)), closes [#290](https://github.com/gotson/komga/issues/290)
* **webreader:** add double page no cover layout ([5fe015e](https://github.com/gotson/komga/commit/5fe015ede0329ed85b39b78f11d55150214f65bf)), closes [#103](https://github.com/gotson/komga/issues/103)

# [0.60.0](https://github.com/gotson/komga/compare/v0.59.2...v0.60.0) (2020-08-27)


### Bug Fixes

* ignore blank metadata fields ([c8c5df2](https://github.com/gotson/komga/commit/c8c5df27013719f533554ed8d2fe2d51b5ed9972)), closes [#288](https://github.com/gotson/komga/issues/288)
* series genre is not imported from metadata ([d0815dd](https://github.com/gotson/komga/commit/d0815dd80ec246ad1c3c5dca70f49ed165fddf1f))


### Features

* handle multiple story arcs in ComicInfoProvider ([f4451bf](https://github.com/gotson/komga/commit/f4451bfd413f78598fe5760345b9c2a4864b3054)), closes [#282](https://github.com/gotson/komga/issues/282)

## [0.59.2](https://github.com/gotson/komga/compare/v0.59.1...v0.59.2) (2020-08-27)


### Bug Fixes

* potential incorrect read progress for series ([7b90244](https://github.com/gotson/komga/commit/7b90244bdda9a06bc1a40f9aa5f6a719b54f5169))


### Performance Improvements

* add sql indexes ([41f3e8d](https://github.com/gotson/komga/commit/41f3e8dc638178846f37da21f17d2a314692b33e))
* lazy sql joins ([6eb7669](https://github.com/gotson/komga/commit/6eb76698583255ce408ec9c660ef697bb4f05f5a))

## [0.59.1](https://github.com/gotson/komga/compare/v0.59.0...v0.59.1) (2020-08-25)


### Bug Fixes

* incorrect read progress for books ([107d7db](https://github.com/gotson/komga/commit/107d7db3946012c4d634300e75e3118cc3d90cf2)), closes [#286](https://github.com/gotson/komga/issues/286)

# [0.59.0](https://github.com/gotson/komga/compare/v0.58.1...v0.59.0) (2020-08-25)


### Bug Fixes

* **api:** do not return empty language ([2ab3c9c](https://github.com/gotson/komga/commit/2ab3c9ca957b1ec578d2fec8dd0d3afbb94416dd))
* **webui:** field should not display if empty ([968e297](https://github.com/gotson/komga/commit/968e297af0e0ee4ffaf7e76e52071f19699df223))


### Features

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

## [0.58.1](https://github.com/gotson/komga/compare/v0.58.0...v0.58.1) (2020-08-24)


### Bug Fixes

* database migration failure ([2ca6077](https://github.com/gotson/komga/commit/2ca607708e21513fdf00f43b05b840dba0e07560))

# [0.58.0](https://github.com/gotson/komga/compare/v0.57.0...v0.58.0) (2020-08-24)


### Bug Fixes

* **webui:** update option title in library dialog ([29d9726](https://github.com/gotson/komga/commit/29d97266137a4f38497e75f1a1f24fbf9ae96f46))


### Features

* **webui:** handle new metadata fields ([5567adc](https://github.com/gotson/komga/commit/5567adc9460a20373e3a63f48995d1bbaca5bf50)), closes [#276](https://github.com/gotson/komga/issues/276)
* add/rearrange metadata fields ([9e406e3](https://github.com/gotson/komga/commit/9e406e33167284f3803beb45243bf11992ec7e83)), closes [#276](https://github.com/gotson/komga/issues/276)

# [0.57.0](https://github.com/gotson/komga/compare/v0.56.1...v0.57.0) (2020-08-20)


### Features

* **webui:** read lists ([27edf17](https://github.com/gotson/komga/commit/27edf17424ff7a53ccfb272b877cf452c95f1158)), closes [#106](https://github.com/gotson/komga/issues/106)
* read lists ([f0c864f](https://github.com/gotson/komga/commit/f0c864f4eba8c70bdac8f92171bfb03edabcea53)), closes [#106](https://github.com/gotson/komga/issues/106)

## [0.56.1](https://github.com/gotson/komga/compare/v0.56.0...v0.56.1) (2020-08-16)


### Bug Fixes

* database migration could fail ([1c9f3d0](https://github.com/gotson/komga/commit/1c9f3d0e804631a8a3437473b181f389a7d73814))

# [0.56.0](https://github.com/gotson/komga/compare/v0.55.1...v0.56.0) (2020-08-14)


### Bug Fixes

* **webui:** change grid size for sm screens ([18280be](https://github.com/gotson/komga/commit/18280beb95b44bb9dfeec9f81cfe04b97c225fdd))


### Features

* **api:** unpaged queries for books ([8f8054e](https://github.com/gotson/komga/commit/8f8054e1d859928cc249cb5631283da498be908d))
* **webui:** library option to import local artwork ([cba0a2e](https://github.com/gotson/komga/commit/cba0a2e701e4d508d2a1b68da7ae7e6c5673846c))
* **webui:** navigate to series books from book details screen ([48e92c7](https://github.com/gotson/komga/commit/48e92c7580f93e01d9e90a43870d3c1ed04c66cf)), closes [#272](https://github.com/gotson/komga/issues/272)
* library option to import local artwork ([eefb802](https://github.com/gotson/komga/commit/eefb80213a89903a01ca0114d319feb1e83bb041))
* **webui:** scanner option per library ([fca0180](https://github.com/gotson/komga/commit/fca018033dd094e0ddf6b0ca3a447b65d492aaa4))
* scanner option per library ([4da6ff9](https://github.com/gotson/komga/commit/4da6ff9fd44d2283c31adc5a3185e0ad642255f3))
* **api:** http eTag caching for all API calls ([fe22cb5](https://github.com/gotson/komga/commit/fe22cb5ce6157a23d9b01cd6fb2139555d5a2da5))

## [0.55.1](https://github.com/gotson/komga/compare/v0.55.0...v0.55.1) (2020-08-14)


### Bug Fixes

* incorrect book number after adding book in series ([3b1eb36](https://github.com/gotson/komga/commit/3b1eb368ca022268be7beed7aa8c9d8e1d89657d)), closes [#269](https://github.com/gotson/komga/issues/269)

# [0.55.0](https://github.com/gotson/komga/compare/v0.54.0...v0.55.0) (2020-08-13)


### Features

* **webreader:** side padding, original scaling for Webtoon ([6499788](https://github.com/gotson/komga/commit/6499788543e9487c90f2b38b9b2eb354aaf802c2)), closes [#266](https://github.com/gotson/komga/issues/266) [#264](https://github.com/gotson/komga/issues/264)

# [0.54.0](https://github.com/gotson/komga/compare/v0.53.0...v0.54.0) (2020-08-12)


### Features

* custom thumbnails for series ([f5f423f](https://github.com/gotson/komga/commit/f5f423f05f3d3bf824bc10c9d1dcc712fbaa98fe)), closes [#63](https://github.com/gotson/komga/issues/63)

# [0.53.0](https://github.com/gotson/komga/compare/v0.52.3...v0.53.0) (2020-08-12)


### Features

* sidecar thumbnails for books ([d01b29f](https://github.com/gotson/komga/commit/d01b29f28098102804c0bddd29c6e0867c560c01))

## [0.52.3](https://github.com/gotson/komga/compare/v0.52.2...v0.52.3) (2020-08-07)


### Bug Fixes

* **api:** large file download ([d100db2](https://github.com/gotson/komga/commit/d100db22eb6505a182b6778d4d52de13ad8653eb))

## [0.52.2](https://github.com/gotson/komga/compare/v0.52.1...v0.52.2) (2020-08-07)


### Bug Fixes

* some PDF pages are blurry ([9ad1cfe](https://github.com/gotson/komga/commit/9ad1cfe80975639a349a99a45e874efd3aba7e1b)), closes [#260](https://github.com/gotson/komga/issues/260)
* **webreader:** incorrect display on safari and firefox ([22b0b7e](https://github.com/gotson/komga/commit/22b0b7e7480d45728c92eb0f418b8143fb523de7)), closes [#262](https://github.com/gotson/komga/issues/262)
* **webui:** missing settings icon in sidebar ([b9e4bea](https://github.com/gotson/komga/commit/b9e4bea71b3a1168312961e104109669feef8078))
* **webui:** thumbnails explorer navigation misaligned ([2ffed28](https://github.com/gotson/komga/commit/2ffed282d705bb74336e191bb28708b1a0337da4))

## [0.52.1](https://github.com/gotson/komga/compare/v0.52.0...v0.52.1) (2020-08-06)


### Bug Fixes

* **webreader:** webtoon reader not loading images ([1a030b4](https://github.com/gotson/komga/commit/1a030b4db558b02e996246670f6ce86094f00495)), closes [#259](https://github.com/gotson/komga/issues/259)

# [0.52.0](https://github.com/gotson/komga/compare/v0.51.1...v0.52.0) (2020-08-06)


### Bug Fixes

* **webui:** missing settings icon in webreader ([9693829](https://github.com/gotson/komga/commit/969382988dbd40448a68fdf1b2586c527f2209f6))


### Features

* **webreader:** display reading direction on book opening ([ebf2aac](https://github.com/gotson/komga/commit/ebf2aac0ac44097da00a628e25ceb446b2fba9e4))
* **webreader:** webtoon reader, fit to screen ([44c814a](https://github.com/gotson/komga/commit/44c814a5baeea9e6b715a63c31285b33cd2e6872)), closes [#81](https://github.com/gotson/komga/issues/81) [#145](https://github.com/gotson/komga/issues/145)

## [0.51.1](https://github.com/gotson/komga/compare/v0.51.0...v0.51.1) (2020-08-03)


### Bug Fixes

* **metadata:** recognize ComicInfo Day tag and "MA15+" rating ([3ad438d](https://github.com/gotson/komga/commit/3ad438d53fadb4766d9167fbc45937455af95ddb))

# [0.51.0](https://github.com/gotson/komga/compare/v0.50.1...v0.51.0) (2020-07-31)


### Bug Fixes

* **webreader:** cycle image fit would not persist setting ([e5272d2](https://github.com/gotson/komga/commit/e5272d224383d9afdc183dfc626937ba87fc27cf))


### Features

* **webreader:** better display of landscape images ([09984a4](https://github.com/gotson/komga/commit/09984a4284c6ee9a50c7457ea9271fc36c58b770)), closes [#123](https://github.com/gotson/komga/issues/123)
* get images dimension during media analysis ([f9d55ec](https://github.com/gotson/komga/commit/f9d55ecfd0743a3c0e07f5f63725d25e6183af0c)), closes [#123](https://github.com/gotson/komga/issues/123)

## [0.50.1](https://github.com/gotson/komga/compare/v0.50.0...v0.50.1) (2020-07-30)


### Bug Fixes

* **docker:** docker image doesn't start ([8bce80b](https://github.com/gotson/komga/commit/8bce80b408143416335a8afb356b8ea404b22369))

# [0.50.0](https://github.com/gotson/komga/compare/v0.49.0...v0.50.0) (2020-07-30)


### Features

* **webui:** add system theme option ([8f22f01](https://github.com/gotson/komga/commit/8f22f01b3bdbfc8951bb291ef9c78f13dc61cbc0))

# [0.49.0](https://github.com/gotson/komga/compare/v0.48.1...v0.49.0) (2020-07-25)


### Features

* **webreader:** add shortcut information menu ([1885f32](https://github.com/gotson/komga/commit/1885f32416635611e62795bdf22420006bb577ad))
* **webui:** dark theme ([efe2021](https://github.com/gotson/komga/commit/efe2021bdcb28cbeac93cee8add6dc80a5708c60)), closes [#179](https://github.com/gotson/komga/issues/179)

## [0.48.1](https://github.com/gotson/komga/compare/v0.48.0...v0.48.1) (2020-07-21)


### Bug Fixes

* fix database migration errors ([f84ba17](https://github.com/gotson/komga/commit/f84ba17079d1ba4bf320d923b971468d19f0aa1d)), closes [#239](https://github.com/gotson/komga/issues/239) [#238](https://github.com/gotson/komga/issues/238) [#237](https://github.com/gotson/komga/issues/237) [#240](https://github.com/gotson/komga/issues/240)

# [0.48.0](https://github.com/gotson/komga/compare/v0.47.0...v0.48.0) (2020-07-16)


### Features

* change database from H2 to SQLite ([20b2b39](https://github.com/gotson/komga/commit/20b2b39d364a04a4b73b8f8bea2f86ae4c6e0304)), closes [#218](https://github.com/gotson/komga/issues/218)

# [0.47.0](https://github.com/gotson/komga/compare/v0.46.0...v0.47.0) (2020-07-14)


### Features

* **webui:** change internals for shortcuts. add new shortcuts for settings ([b402817](https://github.com/gotson/komga/commit/b402817edbface6d622d92c98347f6fe1914bf88)), closes [#172](https://github.com/gotson/komga/issues/172)

# [0.46.0](https://github.com/gotson/komga/compare/v0.45.2...v0.46.0) (2020-07-13)


### Features

* **webui:** add simple next/previous book buttons to reader toolbar ([e428115](https://github.com/gotson/komga/commit/e4281156f5398d478257ee699d7016c1aa5ee81c)), closes [#233](https://github.com/gotson/komga/issues/233)

## [0.45.2](https://github.com/gotson/komga/compare/v0.45.1...v0.45.2) (2020-07-05)


### Bug Fixes

* **webui:** browse collection should show all series ([8629ea9](https://github.com/gotson/komga/commit/8629ea9936ecfcf118be82d7cbf9ad5c2db8035f)), closes [#226](https://github.com/gotson/komga/issues/226)

## [0.45.1](https://github.com/gotson/komga/compare/v0.45.0...v0.45.1) (2020-07-05)


### Bug Fixes

* **webui:** read button link underline on hover ([863258d](https://github.com/gotson/komga/commit/863258d8be037c983c75cd70224970f70d8212a5))

# [0.45.0](https://github.com/gotson/komga/compare/v0.44.0...v0.45.0) (2020-07-05)


### Features

* **api:** claim status ([47dd2f6](https://github.com/gotson/komga/commit/47dd2f66e0049e04cfda0f8d62ff9b7c2f207873)), closes [#207](https://github.com/gotson/komga/issues/207)
* **webui:** claim server from login screen ([d4810bd](https://github.com/gotson/komga/commit/d4810bdc809d87cd94027566b0cbee10d6673a33)), closes [#207](https://github.com/gotson/komga/issues/207)

# [0.44.0](https://github.com/gotson/komga/compare/v0.43.4...v0.44.0) (2020-07-03)


### Bug Fixes

* **webui:** make card title as link ([d6e4b80](https://github.com/gotson/komga/commit/d6e4b807dbcaa9c5ce91ed72ba88d8d8f3434f7e)), closes [#224](https://github.com/gotson/komga/issues/224)
* **webui:** reset edit book dialog to first tab on reopen ([5760a06](https://github.com/gotson/komga/commit/5760a06b7a3496d9ec910697122abd51e2c5f9bd))


### Features

* generate collections from ComicInfo SeriesGroup ([277cdcd](https://github.com/gotson/komga/commit/277cdcd4e39c2ad1da84522de78d693a30c89788)), closes [#210](https://github.com/gotson/komga/issues/210)
* **api:** metadata import settings per library ([6824212](https://github.com/gotson/komga/commit/6824212514d8586bdb47ac2c38d6e121be8db489)), closes [#199](https://github.com/gotson/komga/issues/199)
* **webui:** metadata import settings per library ([521cc42](https://github.com/gotson/komga/commit/521cc42858415abde9a4eb378e5276578bdbed59)), closes [#199](https://github.com/gotson/komga/issues/199)

## [0.43.4](https://github.com/gotson/komga/compare/v0.43.3...v0.43.4) (2020-07-01)


### Bug Fixes

* properly expand home dir in config ([5d86d3e](https://github.com/gotson/komga/commit/5d86d3ea0b3558d8bca2e7994049c1daed4d3fd6)), closes [#195](https://github.com/gotson/komga/issues/195) [#203](https://github.com/gotson/komga/issues/203)

## [0.43.3](https://github.com/gotson/komga/compare/v0.43.2...v0.43.3) (2020-06-30)


### Bug Fixes

* **api:** add missing sort fields for Books ([797535d](https://github.com/gotson/komga/commit/797535d71ff59f37ab49afc5383ed270ab45ac9b))

## [0.43.2](https://github.com/gotson/komga/compare/v0.43.1...v0.43.2) (2020-06-30)


### Bug Fixes

* **api:** exception on unpaged empty content ([2cc27f2](https://github.com/gotson/komga/commit/2cc27f244ccf886b4c5d538d9cfdf61ec92f5478))

## [0.43.1](https://github.com/gotson/komga/compare/v0.43.0...v0.43.1) (2020-06-29)


### Bug Fixes

* **webui:** clear selection when reloading search results ([7d808c5](https://github.com/gotson/komga/commit/7d808c5dee753bcef8987ddeb66bb7086c4cb708))

# [0.43.0](https://github.com/gotson/komga/compare/v0.42.0...v0.43.0) (2020-06-29)


### Bug Fixes

* **webui:** lazy load collections on browse series ([d89533d](https://github.com/gotson/komga/commit/d89533ded69f58a96a2c1062067858b589857b00))


### Features

* **webui:** sort/filter settings are persisted per library ([bf737de](https://github.com/gotson/komga/commit/bf737de91065e56fe1fe11b9ef27cfa913f0ab77)), closes [#190](https://github.com/gotson/komga/issues/190)

# [0.42.0](https://github.com/gotson/komga/compare/v0.41.1...v0.42.0) (2020-06-29)


### Bug Fixes

* **webui:** incorrect count of collections in browse collections screen ([2254929](https://github.com/gotson/komga/commit/22549291d8798f62da4c387e75cec1ad23817c5a))
* **webui:** lazy http call for CollectionEditDialog ([4716a1b](https://github.com/gotson/komga/commit/4716a1b22b873549e07e13f4c76b0caa0c597078))


### Features

* **webui:** collection edition on card in browse collections ([e45389d](https://github.com/gotson/komga/commit/e45389d77b8277fb4850be3f9349a1aa89ac97ca))
* **webui:** collection edition on card in search results ([68fe4fd](https://github.com/gotson/komga/commit/68fe4fd23d4836b40be834201f84d70fb5ec2e7a))
* **webui:** enable edit on books and series cards in search screen ([32dad62](https://github.com/gotson/komga/commit/32dad627773341fb4c070146a3eda4898409fc4a))
* **webui:** multi-select in dashboard page ([8e33be7](https://github.com/gotson/komga/commit/8e33be78e22fa0b622d92606ec976d5756770445))
* **webui:** multi-select in search results page ([c5417ac](https://github.com/gotson/komga/commit/c5417ac8da1dde26237e69561cc718a5882c2baf)), closes [#213](https://github.com/gotson/komga/issues/213)

## [0.41.1](https://github.com/gotson/komga/compare/v0.41.0...v0.41.1) (2020-06-27)


### Bug Fixes

* **api:** pagination for collection's series ([6dd0704](https://github.com/gotson/komga/commit/6dd070436be7458eb3b3019d6286b8dd32d1b27d))
* **opds:** prepend position for series in ordered collection ([7e5a141](https://github.com/gotson/komga/commit/7e5a1412ca1a0278f2f5169fa55d5e25fbb75480))

# [0.41.0](https://github.com/gotson/komga/compare/v0.40.1...v0.41.0) (2020-06-26)


### Bug Fixes

* **api:** collection without element would return incorrect dto ([0891981](https://github.com/gotson/komga/commit/08919814d361aa11083ed7a5c3169d449e744d10))
* **api:** incorrect number of books per series ([bf2d0ba](https://github.com/gotson/komga/commit/bf2d0ba1fad8cf193cc60d2680ad90226c49e93f))
* **webui:** adjust padding for grids ([02e9168](https://github.com/gotson/komga/commit/02e916898e90db49b64bb59e90d152c92553b69c))
* **webui:** display collection after adding in series browsing screen ([8ce5a39](https://github.com/gotson/komga/commit/8ce5a391e53b34c7c46e0c568e7438603c01302c))
* **webui:** library navigation not highlighted correctly ([e28c070](https://github.com/gotson/komga/commit/e28c070e364a3fbae8836ad6b246ebe364c6271c))


### Features

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

## [0.40.1](https://github.com/gotson/komga/compare/v0.40.0...v0.40.1) (2020-06-19)


### Bug Fixes

* **webui:** drag series by handle when editing collection ([e72f4ab](https://github.com/gotson/komga/commit/e72f4ab57e26405d9117da1c02c4efa6d4a6fa23)), closes [#214](https://github.com/gotson/komga/issues/214)
* **webui:** wider display for collection expansion panel ([96c95ea](https://github.com/gotson/komga/commit/96c95ea0745d3bed9b0f618cb2aebdf8bde34a7f))

# [0.40.0](https://github.com/gotson/komga/compare/v0.39.0...v0.40.0) (2020-06-19)


### Bug Fixes

* **api:** sort libraries case insensitive ([1650aec](https://github.com/gotson/komga/commit/1650aec75bd3f6b8cd1e58d2110761f9b59f5e42))


### Features

* **api:** collections management ([c2f9403](https://github.com/gotson/komga/commit/c2f940336ac455a7f7623d4367033b6b77cc23d9)), closes [#30](https://github.com/gotson/komga/issues/30)
* **webui:** collections management ([2f8255a](https://github.com/gotson/komga/commit/2f8255a05fa0f477af764e72f95f6c28fe4ebfe3)), closes [#30](https://github.com/gotson/komga/issues/30)

# [0.39.0](https://github.com/gotson/komga/compare/v0.38.0...v0.39.0) (2020-06-10)


### Features

* **api:** restrict page streaming and file download with roles ([6291dab](https://github.com/gotson/komga/commit/6291dab86472be0f7d564c43914d20baec566c85)), closes [#146](https://github.com/gotson/komga/issues/146)
* **webui:** restrict page streaming and file download per user ([381b196](https://github.com/gotson/komga/commit/381b1960330c97681c84b136d4a670c9dd295c46)), closes [#146](https://github.com/gotson/komga/issues/146)

# [0.38.0](https://github.com/gotson/komga/compare/v0.37.0...v0.38.0) (2020-06-09)


### Bug Fixes

* **webui:** simplify unread filter ([bb60f10](https://github.com/gotson/komga/commit/bb60f10d49b41ac2288ad7fe6765cb2d783b41c1))


### Features

* **webui:** search results page ([89039a4](https://github.com/gotson/komga/commit/89039a4170b674adb0453e16e12ea54577abf473)), closes [#29](https://github.com/gotson/komga/issues/29)

# [0.37.0](https://github.com/gotson/komga/compare/v0.36.0...v0.37.0) (2020-06-08)


### Bug Fixes

* **scanner:** add TRACE logs for file update times ([5433567](https://github.com/gotson/komga/commit/54335674eed6fccd626329248b532b791a133f60)), closes [#159](https://github.com/gotson/komga/issues/159)
* order of pages is not loaded correctly from database ([d2288dd](https://github.com/gotson/komga/commit/d2288dda72b053821be5226e64dd460f5a97f2d0)), closes [#189](https://github.com/gotson/komga/issues/189)


### Features

* automatic database backup ([bbb9f7c](https://github.com/gotson/komga/commit/bbb9f7ce06a546768f662309bb635713aff70cb9)), closes [#138](https://github.com/gotson/komga/issues/138)

# [0.36.0](https://github.com/gotson/komga/compare/v0.35.2...v0.36.0) (2020-06-08)


### Bug Fixes

* strip accented characters for title sort when creating series ([6f1e36e](https://github.com/gotson/komga/commit/6f1e36e7dc3087445a55c0b5a360a932c2a3778f)), closes [#188](https://github.com/gotson/komga/issues/188)
* **webui:** prevent cropping on book thumbnails ([e038857](https://github.com/gotson/komga/commit/e038857887f180ed4fa147064cad84febf647567)), closes [#191](https://github.com/gotson/komga/issues/191)


### Features

* **api:** on deck books ([1b6a030](https://github.com/gotson/komga/commit/1b6a030ab568a657e9d7b3c89292ebd8e05d6611)), closes [#131](https://github.com/gotson/komga/issues/131)
* **webui:** add On Deck section on dashboard ([37c935e](https://github.com/gotson/komga/commit/37c935ec9a0d5ada86eb0ee8f9b201422a532e6d)), closes [#131](https://github.com/gotson/komga/issues/131)

## [0.35.2](https://github.com/gotson/komga/compare/v0.35.1...v0.35.2) (2020-06-08)


### Bug Fixes

* **analysis:** page number was not persisted ([99f800c](https://github.com/gotson/komga/commit/99f800ce8f40833588d3e937abba2e838a205218)), closes [#189](https://github.com/gotson/komga/issues/189)

## [0.35.1](https://github.com/gotson/komga/compare/v0.35.0...v0.35.1) (2020-06-05)


### Bug Fixes

* **api:** books could disappear for users if read by others ([3d1f0e0](https://github.com/gotson/komga/commit/3d1f0e0d058ba2d740e31c9185ff8c9e60e03131))

# [0.35.0](https://github.com/gotson/komga/compare/v0.34.1...v0.35.0) (2020-06-05)


### Bug Fixes

* **api:** add count of in progress books in SeriesDto ([0d2713a](https://github.com/gotson/komga/commit/0d2713a090257cf427cc71700fd2a9d42978001c)), closes [#25](https://github.com/gotson/komga/issues/25)


### Features

* **api:** add read_status parameter to series books ([fc5c502](https://github.com/gotson/komga/commit/fc5c50240e5311d476d235adff638b91e8826605)), closes [#25](https://github.com/gotson/komga/issues/25)
* **api:** read progress as search criteria for Series ([885c891](https://github.com/gotson/komga/commit/885c89126700d6a5a2a153ffee346a1324d04e41)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** add read status filter when browsing Series ([cc39ce8](https://github.com/gotson/komga/commit/cc39ce8b44f847501ac3600cd70b9503193dee67)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** read progress filter for Series when browsing Libraries ([0c046a7](https://github.com/gotson/komga/commit/0c046a767db2bde801ea31ebfbbb2df7d6fac3c6)), closes [#25](https://github.com/gotson/komga/issues/25)

## [0.34.1](https://github.com/gotson/komga/compare/v0.34.0...v0.34.1) (2020-06-04)


### Bug Fixes

* **webui:** filter status not reset properly when changing library ([fea5431](https://github.com/gotson/komga/commit/fea54313d3f7e635d131d0bd339c59123cfc8422))

# [0.34.0](https://github.com/gotson/komga/compare/v0.33.1...v0.34.0) (2020-06-04)


### Bug Fixes

* **webui:** adjust series unread count when marking books ([31e21fe](https://github.com/gotson/komga/commit/31e21fed45603c0e5ad6706e25aac5a863e42675))
* **webui:** series grid not expanding to full width ([2f7d2a4](https://github.com/gotson/komga/commit/2f7d2a447f91a8af71a1a18d3d114035964c9ee4))


### Features

* **analysis:** handle read progress during book analysis ([1fc893e](https://github.com/gotson/komga/commit/1fc893ecb31138c3529ffb80a1c8fc05ea62bb07))
* **api:** add read/unread books count in SeriesDto ([3ca50d7](https://github.com/gotson/komga/commit/3ca50d7b34c3fad99a162c09d417bd6dc42e9d54)), closes [#25](https://github.com/gotson/komga/issues/25)
* **api:** search books by read status ([7f3c492](https://github.com/gotson/komga/commit/7f3c49280b48b38e9308025e4dd95b6f1cc921c0)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** display unread count on series card ([4962f17](https://github.com/gotson/komga/commit/4962f170284c8d43961798a77b7c27a103977821)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** keep reading section in dashboard ([7984cef](https://github.com/gotson/komga/commit/7984cef06664f84d1cb1f1336b34338f90c1c1a0)), closes [#131](https://github.com/gotson/komga/issues/131)
* **webui:** read button on item card ([a59f263](https://github.com/gotson/komga/commit/a59f26365f660fbf0ca6ffa70df029bf14f07ba9)), closes [#133](https://github.com/gotson/komga/issues/133)

## [0.33.1](https://github.com/gotson/komga/compare/v0.33.0...v0.33.1) (2020-06-03)


### Bug Fixes

* **api:** incorrect number of books returned ([544c873](https://github.com/gotson/komga/commit/544c8731416276cc97125851e503008be3ea8769)), closes [#177](https://github.com/gotson/komga/issues/177)
* **webui:** unread tick not showing properly on books ([0d0b998](https://github.com/gotson/komga/commit/0d0b9985c01764e707893fa66f1288f08276477b))

# [0.33.0](https://github.com/gotson/komga/compare/v0.32.0...v0.33.0) (2020-06-03)


### Bug Fixes

* **webui:** action menu not showing for non-admin ([e60666c](https://github.com/gotson/komga/commit/e60666caa39e6f18446ebea2b7d910687da60ac1))
* **webui:** replace mark as read/unread buttons with icons ([709ee1e](https://github.com/gotson/komga/commit/709ee1e29b7f98e733b5b2070fdabef3f6d4d171))
* **webui:** use pagination for browsing screens ([5867db7](https://github.com/gotson/komga/commit/5867db77f5dc20181671fb5bf409941cf42591f7)), closes [#91](https://github.com/gotson/komga/issues/91)


### Features

* **api:** mark all books in series as read or unread ([75b7216](https://github.com/gotson/komga/commit/75b72164fe1b3784c5b40ba6aa923fe071f72b56)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** mark series as read/unread ([4d7e243](https://github.com/gotson/komga/commit/4d7e243d3aa8729139d3311a8e670e101b3a1bbb)), closes [#25](https://github.com/gotson/komga/issues/25)

# [0.32.0](https://github.com/gotson/komga/compare/v0.31.0...v0.32.0) (2020-06-02)


### Features

* **api:** manage book read progress per user ([17c80cd](https://github.com/gotson/komga/commit/17c80cd1a1407de2de8a8341374f92dd2fe72cc7)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webreader:** mark progress while reading ([10895a3](https://github.com/gotson/komga/commit/10895a37f503503ad23a24632ad28eaeaf8db1de)), closes [#25](https://github.com/gotson/komga/issues/25)
* **webui:** mark books as read or unread ([24c994f](https://github.com/gotson/komga/commit/24c994f840df95920e5a8d501e58a916eaea04cc)), closes [#25](https://github.com/gotson/komga/issues/25)

# [0.31.0](https://github.com/gotson/komga/compare/v0.30.0...v0.31.0) (2020-06-01)


### Features

* migrate DAO from Hibernate to jOOQ ([75e1079](https://github.com/gotson/komga/commit/75e1079992fdecfe51b0175ed0c19351cd7b3672))

# [0.30.0](https://github.com/gotson/komga/compare/v0.29.1...v0.30.0) (2020-05-28)


### Features

* switch to JRE for docker image ([3206495](https://github.com/gotson/komga/commit/320649594f72563e41f0052fef26493f518fff55))

## [0.29.1](https://github.com/gotson/komga/compare/v0.29.0...v0.29.1) (2020-05-12)


### Bug Fixes

* **opds:** use servlet context path to build links ([d82eefe](https://github.com/gotson/komga/commit/d82eefe19a95950df72d1070fdf4c5194b377392)), closes [#156](https://github.com/gotson/komga/issues/156)

# [0.29.0](https://github.com/gotson/komga/compare/v0.28.6...v0.29.0) (2020-05-08)


### Features

* **webreader:** add setting to disable swipe navigation ([32170d4](https://github.com/gotson/komga/commit/32170d4d690fc047444e48ef3c71c640f3c45024))
* **webreader:** do not always show reading direction pop ([dc7dc75](https://github.com/gotson/komga/commit/dc7dc75e317c904727045f9c9b818eb8dbd4d422)), closes [#151](https://github.com/gotson/komga/issues/151)

## [0.28.6](https://github.com/gotson/komga/compare/v0.28.5...v0.28.6) (2020-05-08)


### Bug Fixes

* ignore alpha channel on image conversion ([9556ae5](https://github.com/gotson/komga/commit/9556ae51f262470b1fbb5fd9af2d8caeb3c1fa64)), closes [#153](https://github.com/gotson/komga/issues/153)

## [0.28.5](https://github.com/gotson/komga/compare/v0.28.4...v0.28.5) (2020-05-08)


### Bug Fixes

* **opds:** do not always convert images ([6430c92](https://github.com/gotson/komga/commit/6430c92bcb11ef598e4c26eb61ad9fe6733a1d0d)), closes [#153](https://github.com/gotson/komga/issues/153)

## [0.28.4](https://github.com/gotson/komga/compare/v0.28.3...v0.28.4) (2020-05-06)


### Bug Fixes

* **tasks:** background tasks would stop running if disk is over 90% full ([dc33fb1](https://github.com/gotson/komga/commit/dc33fb1d10a72d0fc3bd6715f7fcd0c1dfead90b))

## [0.28.3](https://github.com/gotson/komga/compare/v0.28.2...v0.28.3) (2020-05-05)


### Bug Fixes

* **epub:** epub not loading correctly on windows (again) ([8535f46](https://github.com/gotson/komga/commit/8535f46848287e03e5e36493481d25e9dfcc8a04))

## [0.28.2](https://github.com/gotson/komga/compare/v0.28.1...v0.28.2) (2020-05-04)


### Bug Fixes

* **epub:** epub were not loading properly on Windows ([eca453c](https://github.com/gotson/komga/commit/eca453c5344ce7c0144f30b6c1c08aa9d2c79038)), closes [#149](https://github.com/gotson/komga/issues/149)

## [0.28.1](https://github.com/gotson/komga/compare/v0.28.0...v0.28.1) (2020-05-04)


### Bug Fixes

* **webui:** refactor Cards to a single dynamic component ([#148](https://github.com/gotson/komga/issues/148)) ([74a9f7e](https://github.com/gotson/komga/commit/74a9f7e628de95cd78902d539f858737bb5c1d61))

# [0.28.0](https://github.com/gotson/komga/compare/v0.27.7...v0.28.0) (2020-05-03)


### Bug Fixes

* **rar:** unsupported rar archives are marked as such ([6c0ebbe](https://github.com/gotson/komga/commit/6c0ebbeee126a77299d4198bb5ab1f03391e774f)), closes [#147](https://github.com/gotson/komga/issues/147)


### Features

* add hawt.io support ([dfa2405](https://github.com/gotson/komga/commit/dfa24057bbad382a2da54471b91db523f18545ba))
* **docker:** persist artemis data in /config ([08e7f5d](https://github.com/gotson/komga/commit/08e7f5dbf26995fcf2b39818561c0259c473fd30))
* **scan:** add configuration to disable startup scan ([37590dd](https://github.com/gotson/komga/commit/37590dd9129174c6be5f9e2e1509f79555bed207))
* **tasks:** background tasks persistency ([1a31c89](https://github.com/gotson/komga/commit/1a31c8971269380d9fabef60dce881aee957c230))
* **tasks:** replace background tasks management ([60ce87a](https://github.com/gotson/komga/commit/60ce87a25dd62a2d357ce3b62a03f03bbce43f1c))

## [0.27.7](https://github.com/gotson/komga/compare/v0.27.6...v0.27.7) (2020-04-20)


### Bug Fixes

* **swagger:** fix incorrect Pageable schema ([14bee56](https://github.com/gotson/komga/commit/14bee566b3b32659080e5b8f19e9b52f2ba8c4d1))

## [0.27.6](https://github.com/gotson/komga/compare/v0.27.5...v0.27.6) (2020-04-18)


### Bug Fixes

* **swagger:** better pageable support ([6401dc9](https://github.com/gotson/komga/commit/6401dc9b9b051ed49b1341891383e6015fd77fc9))

## [0.27.5](https://github.com/gotson/komga/compare/v0.27.4...v0.27.5) (2020-04-18)


### Bug Fixes

* set default forward headers strategy to framework ([b24fbe3](https://github.com/gotson/komga/commit/b24fbe3a1e206e35ea0da1223483d625eacaa323))

## [0.27.4](https://github.com/gotson/komga/compare/v0.27.3...v0.27.4) (2020-04-16)


### Bug Fixes

* **analyzer:** better rar5 detection ([b07e9b9](https://github.com/gotson/komga/commit/b07e9b9728f387c15fb22f970a42c5e484f22b7a))
* **swagger:** correct response type for thumbnails and file ([07f634e](https://github.com/gotson/komga/commit/07f634e658b8ce74486dc5654518f65832602e99))

## [0.27.3](https://github.com/gotson/komga/compare/v0.27.2...v0.27.3) (2020-04-16)


### Bug Fixes

* **analyzer:** regression in RAR handling ([b8462f3](https://github.com/gotson/komga/commit/b8462f3568a8d8428b88bb45f55a7e7fdf22497e))

## [0.27.2](https://github.com/gotson/komga/compare/v0.27.1...v0.27.2) (2020-04-15)


### Bug Fixes

* **webreader:** stretched images on Safari with fit to Width ([74f9305](https://github.com/gotson/komga/commit/74f9305e166482003f4ef3fc12f2a003d5ec468c))

## [0.27.1](https://github.com/gotson/komga/compare/v0.27.0...v0.27.1) (2020-04-15)


### Bug Fixes

* **swagger:** fix duplicated mapping key error ([face321](https://github.com/gotson/komga/commit/face321b0841b93b24b8e51bcce49f391a9aaeb8))

# [0.27.0](https://github.com/gotson/komga/compare/v0.26.3...v0.27.0) (2020-04-13)


### Bug Fixes

* **api:** libraries are not sorted for restricted users ([1d58322](https://github.com/gotson/komga/commit/1d583229eeb4eb279c545b433e6160612df7a0b7)), closes [#140](https://github.com/gotson/komga/issues/140)


### Features

* **swagger:** update to OpenAPI 3 ([c9de7c8](https://github.com/gotson/komga/commit/c9de7c8074ec15a3a69db808c31bd3415c17ba15))

## [0.26.3](https://github.com/gotson/komga/compare/v0.26.2...v0.26.3) (2020-04-12)


### Bug Fixes

* **epub:** add support for opf:role for authors ([30bf7f4](https://github.com/gotson/komga/commit/30bf7f469f0d9895646ade1b9b050975fc7228c5)), closes [#139](https://github.com/gotson/komga/issues/139)
* **epub:** match artist and illustrator roles to penciller ([b7fe4bd](https://github.com/gotson/komga/commit/b7fe4bde749bb6fff2c8e81d9881cf70acb04d76))

## [0.26.2](https://github.com/gotson/komga/compare/v0.26.1...v0.26.2) (2020-04-11)


### Bug Fixes

* **swagger:** paging and sorting parameters showing incorrectly ([1e9407c](https://github.com/gotson/komga/commit/1e9407cfe25a6104150efe2b7892a9973878ef0a))

## [0.26.1](https://github.com/gotson/komga/compare/v0.26.0...v0.26.1) (2020-04-10)


### Bug Fixes

* **swagger:** parameters were not showing for claim method ([e5a0841](https://github.com/gotson/komga/commit/e5a084144dab96209ad55172cd4fe59fea08efe5))

# [0.26.0](https://github.com/gotson/komga/compare/v0.25.1...v0.26.0) (2020-04-10)


### Bug Fixes

* **comicinfo:** avoid reading the file twice ([1dab826](https://github.com/gotson/komga/commit/1dab826622d6d4bc1121862bbc0acbdff7ff743b))
* **epub:** some EPUB files would not be parsed correctly ([f0ff785](https://github.com/gotson/komga/commit/f0ff785d66f15bc93d27bb1ee6314e7940bbaa1c))
* **swagger:** ignore non-functional parameters ([d1c8b23](https://github.com/gotson/komga/commit/d1c8b23f447e5b3bd196159692d43794d28cc44b))


### Features

* **metadata:** retrieve EPUB metadata ([a4f5015](https://github.com/gotson/komga/commit/a4f50154351818e42506cc31f414b51e06c14e23))
* **scan:** force rescan of existing series to pickup EPUB files ([854f090](https://github.com/gotson/komga/commit/854f090415f47ff10704c714e69026a70b3d7bce))
* support for EPUB format (comics only) ([0a06a6f](https://github.com/gotson/komga/commit/0a06a6f7995cf540d6cdabc8f8c058eef82e81e1)), closes [#119](https://github.com/gotson/komga/issues/119)

## [0.25.1](https://github.com/gotson/komga/compare/v0.25.0...v0.25.1) (2020-04-06)


### Bug Fixes

* **api:** thumbnails not updating properly ([a5bd908](https://github.com/gotson/komga/commit/a5bd9087df1e43be9bef5dea1039e1720f0f9541))
* **opds:** prepend issue number for book titles for Chunky ([42cad8b](https://github.com/gotson/komga/commit/42cad8b4d572ee587b8b908662863f83f15f6349))

# [0.25.0](https://github.com/gotson/komga/compare/v0.24.10...v0.25.0) (2020-04-03)


### Bug Fixes

* **api:** book file download uses streaming ([74be1f0](https://github.com/gotson/komga/commit/74be1f0face9b910cf9fa25c2379b3c8eaefaab6))


### Features

* **webui:** action menu to refresh metadata ([6ad59c4](https://github.com/gotson/komga/commit/6ad59c4efb28517d9aabc1c67e240d0939493cf9))
* retrieve metadata from ComicInfo.xml ([af01d25](https://github.com/gotson/komga/commit/af01d25ede29feb5159e8a800b789f200e6e7e5c))

## [0.24.10](https://github.com/gotson/komga/compare/v0.24.9...v0.24.10) (2020-03-25)


### Bug Fixes

* **opds:** add summary and authors to book entries ([2414792](https://github.com/gotson/komga/commit/241479292ae1adc50ab45b03e39ef27d25f7847a))
* **webui:** properly display newline in book summary ([15d95ac](https://github.com/gotson/komga/commit/15d95ac442bba89e2fb98bf064995526d9dd6725))

## [0.24.9](https://github.com/gotson/komga/compare/v0.24.8...v0.24.9) (2020-03-24)


### Bug Fixes

* **webui:** redirect to initial page after login ([5f61597](https://github.com/gotson/komga/commit/5f6159712515d0df128443dbec5cc01b9dadff07)), closes [#122](https://github.com/gotson/komga/issues/122)

## [0.24.8](https://github.com/gotson/komga/compare/v0.24.7...v0.24.8) (2020-03-23)


### Bug Fixes

* **api:** check if authors field is set ([9218e6b](https://github.com/gotson/komga/commit/9218e6bb265b32fbd28d61ef797e7a1d93cb9a1c)), closes [#120](https://github.com/gotson/komga/issues/120)

## [0.24.7](https://github.com/gotson/komga/compare/v0.24.6...v0.24.7) (2020-03-23)


### Bug Fixes

* **webui:** fix metadata dialogs on xs screens ([112837a](https://github.com/gotson/komga/commit/112837a163a2a123b421cf8cebef15d431f67367))

## [0.24.6](https://github.com/gotson/komga/compare/v0.24.5...v0.24.6) (2020-03-21)


### Bug Fixes

* **api:** use etag on book thumbnails ([871a92a](https://github.com/gotson/komga/commit/871a92a7835333ea11014ac06ead5b434a44c23d)), closes [#117](https://github.com/gotson/komga/issues/117)

## [0.24.5](https://github.com/gotson/komga/compare/v0.24.4...v0.24.5) (2020-03-20)


### Bug Fixes

* **api:** find book siblings by metadata.numberSort ([1902e72](https://github.com/gotson/komga/commit/1902e72f86c1fcaca8492d828a8fc19dbc0fac9c))
* **api:** series thumbnail is of first book by metadata.numberSort ([0721f31](https://github.com/gotson/komga/commit/0721f31e2faa0123b1c581afe28c042947bd8e0b))
* **opds:** books are ordered by metadata.numberSort ([60edbe1](https://github.com/gotson/komga/commit/60edbe1090f99575bc209a3c439f41158398b7a6))
* **opds:** display book metadata title instead of name ([d06da57](https://github.com/gotson/komga/commit/d06da572b64caca6fbed00606c6aae73fa8c40f8))
* **opds:** search series by metadata.title ([f4466b4](https://github.com/gotson/komga/commit/f4466b45bfc374724d48008c642cad295e444b4d))

## [0.24.4](https://github.com/gotson/komga/compare/v0.24.3...v0.24.4) (2020-03-20)


### Bug Fixes

* **edit series dialog:** better form handling ([c225829](https://github.com/gotson/komga/commit/c2258294ced569310d21b5c8e03e455408b18d22))

## [0.24.3](https://github.com/gotson/komga/compare/v0.24.2...v0.24.3) (2020-03-20)


### Bug Fixes

* **webui:** change page title on book change ([2d0e21b](https://github.com/gotson/komga/commit/2d0e21b0b8d36a81e17417db01545cc3e51f61b2))

## [0.24.2](https://github.com/gotson/komga/compare/v0.24.1...v0.24.2) (2020-03-19)


### Bug Fixes

* **webui:** use metadata title for display name ([64c3356](https://github.com/gotson/komga/commit/64c33565c5648154a7ede69f7f5f8de69b6697c3))

## [0.24.1](https://github.com/gotson/komga/compare/v0.24.0...v0.24.1) (2020-03-19)


### Bug Fixes

* **api:** search books by metadata title ([487b18d](https://github.com/gotson/komga/commit/487b18d15b04e86eacf55bfda5b286456d36e563))
* **api:** search series by metadata title ([51dd917](https://github.com/gotson/komga/commit/51dd91724976a14a029d69c04f2483b7072778b6))
* **book card:** use book metadata ([ea1bdc6](https://github.com/gotson/komga/commit/ea1bdc646af70be647dc9b1709817dcfd657aa15))
* **browse series:** hide toolbar on selection ([e6d014f](https://github.com/gotson/komga/commit/e6d014f1ac29c6e02537eaf0c4be636785f69ab5))
* **searchbox:** display book metadata title instead of name ([26d37a0](https://github.com/gotson/komga/commit/26d37a0fc91c476e246ba09a7ae9f8c71f0b82b5))

# [0.24.0](https://github.com/gotson/komga/compare/v0.23.0...v0.24.0) (2020-03-18)


### Bug Fixes

* **add library:** special characters handling ([15afa93](https://github.com/gotson/komga/commit/15afa9343155aecee4132cb0ceedbfb5bf22b229))


### Features

* **book reader:** set reading direction from metadata ([30e766b](https://github.com/gotson/komga/commit/30e766be16dff121577ebb24d11881f73450c5cd))
* **book reader:** vertical reading mode ([ca03111](https://github.com/gotson/komga/commit/ca03111b0b4246239bad84b79d7d179975d722aa))

# [0.23.0](https://github.com/gotson/komga/compare/v0.22.2...v0.23.0) (2020-03-18)


### Features

* **book reader:** background color settings ([2c87e7b](https://github.com/gotson/komga/commit/2c87e7bba6f33c52313d5b28690cf179eaac8c85)), closes [#113](https://github.com/gotson/komga/issues/113)
* **book reader:** pressing ESC will close the toolbars ([791f5df](https://github.com/gotson/komga/commit/791f5dff0878a72a5d34f8e201090e656492784c))

## [0.22.2](https://github.com/gotson/komga/compare/v0.22.1...v0.22.2) (2020-03-17)


### Bug Fixes

* **webui:** change page size on media analysis screen ([afc2cd4](https://github.com/gotson/komga/commit/afc2cd4e7002aeea440477203af0ac9d67cbdbb3))

## [0.22.1](https://github.com/gotson/komga/compare/v0.22.0...v0.22.1) (2020-03-17)


### Bug Fixes

* use JDBC update statements for database migration ([f68e035](https://github.com/gotson/komga/commit/f68e0352fdfd6d4683d4015c4b21bad6bcd070dc))

# [0.22.0](https://github.com/gotson/komga/compare/v0.21.0...v0.22.0) (2020-03-17)


### Features

* support for book metadata ([6a53e8f](https://github.com/gotson/komga/commit/6a53e8fd6b53533020b9e06a14028f9776e641af)), closes [#48](https://github.com/gotson/komga/issues/48) [#43](https://github.com/gotson/komga/issues/43)

# [0.21.0](https://github.com/gotson/komga/compare/v0.20.0...v0.21.0) (2020-03-10)


### Bug Fixes

* **scanner:** follow symlinks when scanning libraries ([1044262](https://github.com/gotson/komga/commit/1044262a1c2c7be47002a1128b2374051fa4f7fc)), closes [#96](https://github.com/gotson/komga/issues/96)


### Features

* docker multi-arch images ([d54c67b](https://github.com/gotson/komga/commit/d54c67b3df0d0c4d753e0057d3349720e819eca5))

# [0.20.0](https://github.com/gotson/komga/compare/v0.19.0...v0.20.0) (2020-03-05)


### Features

* add default location for database ([ce50403](https://github.com/gotson/komga/commit/ce50403a86cb14de5edba34a0ac9f34b1b586af5))

# [0.19.0](https://github.com/gotson/komga/compare/v0.18.0...v0.19.0) (2020-03-05)


### Features

* demo profile ([24b2125](https://github.com/gotson/komga/commit/24b21250be81789ffd2897cb95c56959bf1ee0af))

# [0.18.0](https://github.com/gotson/komga/compare/v0.17.0...v0.18.0) (2020-03-03)


### Features

* add claim profile ([b7eeb4c](https://github.com/gotson/komga/commit/b7eeb4c6cbac9207578e6d303b361dad70a1a646)), closes [#104](https://github.com/gotson/komga/issues/104)

# [0.17.0](https://github.com/gotson/komga/compare/v0.16.5...v0.17.0) (2020-03-03)


### Bug Fixes

* **webreader:** defaults to LTR and fix touch ([2eae83f](https://github.com/gotson/komga/commit/2eae83f561ef2fc0009770d1fbc21ff2fff56b78))


### Features

* **webreader:** add 's' keyboard shortcut to show settings ([99b14cb](https://github.com/gotson/komga/commit/99b14cb80c65c42a1955b53df012cb1e213ffc48))
* **webreader:** escape closes dialogs ([9e44571](https://github.com/gotson/komga/commit/9e44571af53673c5acbee2fced22f047bad3316e))
* **webui:** redesign reader to follow material design ([7f0ab5f](https://github.com/gotson/komga/commit/7f0ab5fde3119abff1b0ba88b6e958a0c5645369)), closes [#74](https://github.com/gotson/komga/issues/74)

## [0.16.5](https://github.com/gotson/komga/compare/v0.16.4...v0.16.5) (2020-03-02)


### Bug Fixes

* prevent user self-deletion ([3d9b78d](https://github.com/gotson/komga/commit/3d9b78d364c07b4c7f9bd1923c36943bbe5147f6)), closes [#100](https://github.com/gotson/komga/issues/100)

## [0.16.4](https://github.com/gotson/komga/compare/v0.16.3...v0.16.4) (2020-02-29)


### Bug Fixes

* swagger-ui and h2-console work again ([626f047](https://github.com/gotson/komga/commit/626f04769630a8b02f04e796ffb299c12168686d)), closes [#99](https://github.com/gotson/komga/issues/99)

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
