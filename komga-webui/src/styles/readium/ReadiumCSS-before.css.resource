/* Readium CSS
   Config module

   A file allowing implementers to customize flags for reading modes,
   user settings, etc.

   Repo: https://github.com/readium/readium-css */

/* Custom medias
   Syntax: @custom-media --variable (prop: value) */

/* Responsive columns
   The minimum width for which responsive columns (2 -> 1 and vice versa,
   depending on the current font-size) must be enabled */

/* Mobile columns
   The minimum and maximum width for mobile devices.
   We’re forcing the landscape orientation by default,
   and must still investigate large tablets (iPad Pro, Surface Pro 3, etc.). */

/* Custom selectors
   Syntax: @custom-selector :--variable selector
   The selectors you will use for flags/switches
   You can alternatively use classes or custom data-* attributes */

/* User view = paged | scrolled */

/* Font-family override */

/* Advanced settings */

/* Reading Modes */

/* Filters (images) */

/* Accessibility normalization */

/* Accessibility font. You can add selectors, using “, ” as a separator, if you have multiple fonts */

/* Direction i.e. ltr and rtl */

/* Readium CSS
   Base module

   A minimal stylesheet for all ebooks

   Repo: https://github.com/readium/readium-css */

@namespace url("http://www.w3.org/1999/xhtml");

@namespace epub url("http://www.idpf.org/2007/ops");

@namespace m url("http://www.w3.org/1998/Math/MathML/");

@namespace svg url("http://www.w3.org/2000/svg");

/* Define viewport, HTML5-style */

@-ms-viewport {
  width: device-width;
}

@viewport {
  width: device-width;
  zoom: 1;
}

:root {
  /* Default font-stacks */
  --RS__oldStyleTf: "Iowan Old Style", "Sitka Text", Palatino, "Book Antiqua", serif;
  --RS__modernTf: Athelas, Constantia, Georgia, serif;
  --RS__sansTf: -apple-system, system-ui, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  --RS__humanistTf: Seravek, Calibri, Roboto, Arial, sans-serif;
  --RS__monospaceTf: "Andale Mono", Consolas, monospace;

  /* Config */
  --RS__baseFontFamily: var(--RS__oldStyleTf);

  /* For square-ish fonts (CJK, Indic, etc.), we must apply some compensation in dynamic leading. Default is 1 i.e. no compensation */
  --RS__lineHeightCompensation: 1;

  /* Dynamic leading based on typeface metrics + font-size setting */
  --RS__baseLineHeight: calc((1em + (2ex - 1ch) - ((1rem - 16px) * 0.1667)) * var(--RS__lineHeightCompensation));
}

/* Set default font for the html doc, so that it can be overridden by the authors’s stylesheet */

html {
  font-family: var(--RS__baseFontFamily);
  /* Fallback line-height */
  line-height: 1.6; /* Fits a little bit better for all languages than 1.5 */
  line-height: var(--RS__baseLineHeight);
  text-rendering: optimizeLegibility;
}

/* 1.5 being too loose with larger font-sizes, we reset headings to normal (which value is 1.125–1.375 for our font-stacks) */

h1, h2, h3 {
  line-height: normal;
}

:lang(ja),
:lang(zh),
:lang(ko) {
  word-wrap: break-word;
  -webkit-line-break: strict;
  -epub-line-break: strict;
  line-break: strict;
}

/* Set default font for Math */

math {
  font-family: "Latin Modern Math", "STIX Two Math", "XITS Math", "STIX Math", "Libertinus Math", "TeX Gyre Termes Math", "TeX Gyre Bonum Math", "TeX Gyre Schola", "DejaVu Math TeX Gyre", "TeX Gyre Pagella Math", "Asana Math", "Cambria Math", "Lucida Bright Math", "Minion Math", STIXGeneral, STIXSizeOneSym, Symbol, "Times New Roman", serif;
}

/* Language Overrides
   That will only work if either html or body have a (xml:)lang attribute, not for inline overrides */

:lang(am) {
  --RS__baseFontFamily: Kefa, Nyala, Roboto, Noto, "Noto Sans Ethiopic", serif;
  --RS__lineHeightCompensation: 1.167;
}

:lang(ar) {
  --RS__baseFontFamily: "Geeza Pro", "Arabic Typesetting", Roboto, Noto, "Noto Naskh Arabic", "Times New Roman", serif;
}

:lang(bn) {
  --RS__baseFontFamily: "Kohinoor Bangla", "Bangla Sangam MN", Vrinda, Roboto, Noto, "Noto Sans Bengali", sans-serif;
  --RS__lineHeightCompensation: 1.067;
}

:lang(bo) {
  --RS__baseFontFamily: Kailasa, "Microsoft Himalaya", Roboto, Noto, "Noto Sans Tibetan", sans-serif;
}

:lang(chr) {
  --RS__baseFontFamily: "Plantagenet Cherokee", Roboto, Noto, "Noto Sans Cherokee";
  --RS__lineHeightCompensation: 1.167;
}

:lang(fa) {
  --RS__baseFontFamily: "Geeza Pro", "Arabic Typesetting", Roboto, Noto, "Noto Naskh Arabic", "Times New Roman", serif;
}

:lang(gu) {
  --RS__baseFontFamily: "Gujarati Sangam MN", "Nirmala UI", Shruti, Roboto, Noto, "Noto Sans Gujarati", sans-serif;
  --RS__lineHeightCompensation: 1.167;
}

:lang(he) {
  --RS__baseFontFamily: "New Peninim MT", "Arial Hebrew", Gisha, "Times New Roman", Roboto, Noto, "Noto Sans Hebrew" sans-serif;
  --RS__lineHeightCompensation: 1.1;
}

:lang(hi) {
  --RS__baseFontFamily: "Kohinoor Devanagari", "Devanagari Sangam MN", Kokila, "Nirmala UI", Roboto, Noto, "Noto Sans Devanagari", sans-serif;

  --RS__lineHeightCompensation: 1.1;
}

:lang(hy) {
  --RS__baseFontFamily: Mshtakan, Sylfaen, Roboto, Noto, "Noto Serif Armenian", serif;
}

:lang(iu) {
  --RS__baseFontFamily: "Euphemia UCAS", Euphemia, Roboto, Noto, "Noto Sans Canadian Aboriginal", sans-serif;
}

:lang(ja) {
  --RS__baseFontFamily: "游ゴシック体", YuGothic, "ヒラギノ丸ゴ", "Hiragino Sans", "Yu Gothic UI", "Meiryo UI", "MS Gothic", Roboto, Noto, "Noto Sans CJK JP", sans-serif;

  /* For CJK, the line-height is usually 15–20% more than for Latin */
  --RS__lineHeightCompensation: 1.167;

  /* Extra variables for Japanese font-stacks as we may want to reuse them for user settings + default */
  --RS__serif-ja: "ＭＳ Ｐ明朝", "MS PMincho", "Hiragino Mincho Pro", "ヒラギノ明朝 Pro W3", "游明朝", "YuMincho", "ＭＳ 明朝", "MS Mincho", "Hiragino Mincho ProN", serif;
  --RS__sans-serif-ja: "ＭＳ Ｐゴシック", "MS PGothic", "Hiragino Kaku Gothic Pro W3", "ヒラギノ角ゴ Pro W3", "Hiragino Sans GB", "ヒラギノ角ゴシック W3", "游ゴシック", "YuGothic", "ＭＳ ゴシック", "MS Gothic", "Hiragino Sans", sans-serif;
  --RS__serif-ja-v: "ＭＳ 明朝", "MS Mincho", "Hiragino Mincho Pro", "ヒラギノ明朝 Pro W3", "游明朝", "YuMincho", "ＭＳ Ｐ明朝", "MS PMincho", "Hiragino Mincho ProN", serif;
  --RS__sans-serif-ja-v: "ＭＳ ゴシック", "MS Gothic", "Hiragino Kaku Gothic Pro W3", "ヒラギノ角ゴ Pro W3", "Hiragino Sans GB", "ヒラギノ角ゴシック W3", "游ゴシック", "YuGothic", "ＭＳ Ｐゴシック", "MS PGothic", "Hiragino Sans", sans-serif;
}

:lang(km) {
  --RS__baseFontFamily: "Khmer Sangam MN", "Leelawadee UI", "Khmer UI", Roboto, Noto, "Noto Sans Khmer", sans-serif;
  --RS__lineHeightCompensation: 1.067;
}

:lang(kn) {
  --RS__baseFontFamily: "Kannada Sangam MN", "Nirmala UI", Tunga, Roboto, Noto, "Noto Sans Kannada", sans-serif;
  --RS__lineHeightCompensation: 1.1;
}

:lang(ko) {
  --RS__baseFontFamily: "Nanum Gothic", "Apple SD Gothic Neo", "Malgun Gothic", Roboto, Noto, "Noto Sans CJK KR", sans-serif;

  /* For CJK, the line-height is usually 15–20% more than for Latin */
  --RS__lineHeightCompensation: 1.167;
}

:lang(lo) {
  --RS__baseFontFamily: "Lao Sangam MN", "Leelawadee UI", "Lao UI", Roboto, Noto, "Noto Sans Lao", sans-serif;
}

:lang(ml) {
  --RS__baseFontFamily: "Malayalam Sangam MN", "Nirmala UI", Kartika, Roboto, Noto, "Noto Sans Malayalam", sans-serif;
  --RS__lineHeightCompensation: 1.067;
}

:lang(or) {
  --RS__baseFontFamily: "Oriya Sangam MN", "Nirmala UI", Kalinga, Roboto, Noto, "Noto Sans Oriya", sans-serif;
  --RS__lineHeightCompensation: 1.167;
}

:lang(pa) {
  --RS__baseFontFamily: "Gurmukhi MN", "Nirmala UI", Kartika, Roboto, Noto, "Noto Sans Gurmukhi", sans-serif;
  --RS__lineHeightCompensation: 1.1;
}

:lang(si) {
  --RS__baseFontFamily: "Sinhala Sangam MN", "Nirmala UI", "Iskoola Pota", Roboto, Noto, "Noto Sans Sinhala", sans-serif;
  --RS__lineHeightCompensation: 1.167;
}

:lang(ta) {
  --RS__baseFontFamily: "Tamil Sangam MN", "Nirmala UI", Latha, Roboto, Noto, "Noto Sans Tamil", sans-serif;
  --RS__lineHeightCompensation: 1.067;
}

:lang(te) {
  --RS__baseFontFamily: "Kohinoor Telugu", "Telugu Sangam MN", "Nirmala UI", Gautami, Roboto, Noto, "Noto Sans Telugu", sans-serif;
}

:lang(th) {
  --RS__baseFontFamily: "Thonburi", "Leelawadee UI", "Cordia New", Roboto, Noto, "Noto Sans Thai", sans-serif;
  --RS__lineHeightCompensation: 1.067;
}

/* The following will also work for zh-Hans */

:lang(zh) {
  --RS__baseFontFamily: "方体", "PingFang SC", "黑体", "Heiti SC", "Microsoft JhengHei UI", "Microsoft JhengHei", Roboto, Noto, "Noto Sans CJK SC", sans-serif;

  /* For CJK, the line-height is usually 15–20% more than for Latin */
  --RS__lineHeightCompensation: 1.167;
}

:lang(zh-Hant),
:lang(zh-TW) {
  --RS__baseFontFamily: "方體", "PingFang TC", "黑體", "Heiti TC", "Microsoft JhengHei UI", "Microsoft JhengHei", Roboto, Noto, "Noto Sans CJK TC", sans-serif;

  /* For CJK, the line-height is usually 15–20% more than for Latin */
  --RS__lineHeightCompensation: 1.167;
}

:lang(zh-HK) {
  --RS__baseFontFamily: "方體", "PingFang HK", "方體", "PingFang TC", "黑體", "Heiti TC", "Microsoft JhengHei UI", "Microsoft JhengHei", Roboto, Noto, "Noto Sans CJK TC", sans-serif;

  /* For CJK, the line-height is usually 15–20% more than for Latin */
  --RS__lineHeightCompensation: 1.167;
}

/* Readium CSS
   Day/Default mode

   A preset theme for day mode, which is the default

   Repo: https://github.com/readium/readium-css */

/* CONFIG */

:root {
  --RS__backgroundColor: #FFFFFF;
  --RS__textColor: #121212;

  /* This can be customized but initial will re-use default value of the browser */
  --RS__selectionBackgroundColor: #b4d8fe;
  --RS__selectionTextColor: inherit;
}

:root {
  color: var(--RS__textColor) !important;
  background-color: var(--RS__backgroundColor) !important;
}

/* Note: Though `::selection` was present in drafts of CSS Selectors Level 3, it was removed during the Candidate Recommendation phase because its behavior was under-specified (especially with nested elements) and interoperability wasn’t achieved. Source: https://developer.mozilla.org/en-US/docs/Web/CSS/::selection */

::-moz-selection {
  color: var(--RS__selectionTextColor);
  background-color: var(--RS__selectionBackgroundColor);
}

::selection {
  color: var(--RS__selectionTextColor);
  background-color: var(--RS__selectionBackgroundColor);
}

/* Readium CSS
   Fonts module

   A stylesheet for embedded fonts

   Repo: https://github.com/readium/readium-css */

/* /!\ Mind the path (relative to the folders in which you have stylesheets and the fonts) */

/*@font-face {*/
/*  font-family: AccessibleDfA;*/
/*  font-style: normal;*/
/*  font-weight: normal;*/
/*  src: local("AccessibleDfA"),*/
/*    url("fonts/AccessibleDfA.otf") format("opentype");*/
/*}*/

/*@font-face {*/
/*  font-family: "IA Writer Duospace";*/
/*  font-style: normal;*/
/*  font-weight: normal;*/
/*  src: local("iAWriterDuospace-Regular"),*/
/*    url("fonts/iAWriterDuospace-Regular.ttf") format("truetype");*/
/*}*/

/* If you have different weights/styles,
   use `font-weight` and `font-style`,
   not prefixes in the font-family name,
   or else it will be a nightmare to manage in user settings. */

/* Readium CSS
   HTML5 SR Patch stylesheet

   A set of style to adjust HTML5 Suggested Rendering to paginated content

   Repo: https://github.com/readium/readium-css */

/* Fragmentation */

body {
  widows: 2;
  orphans: 2;
}

figcaption, th, td {
  widows: 1;
  orphans: 1;
}

h2, h3, h4, h5, h6, dt,
hr, caption {
  -webkit-column-break-after: avoid;
  page-break-after: avoid;
  break-after: avoid;
}

h1, h2, h3, h4, h5, h6, dt,
figure, tr {
  -webkit-column-break-inside: avoid;
  page-break-inside: avoid;
  break-inside: avoid;
}

/* Hyphenation */

body {
  -webkit-hyphenate-character: "\002D";
  -moz-hyphenate-character: "\002D";
  -ms-hyphenate-character: "\002D";
  hyphenate-character: "\002D";
  -webkit-hyphenate-limit-lines: 3;
  -ms-hyphenate-limit-lines: 3;
  hyphenate-limit-lines: 3;
}

h1, h2, h3, h4, h5, h6, dt,
figcaption, pre, caption, address,
center, code, var {
  -ms-hyphens: none;
  -moz-hyphens: none;
  -webkit-hyphens: none;
  -epub-hyphens: none;
  hyphens: none;
}

/* OTF */

body {
  font-variant-numeric: oldstyle-nums proportional-nums;
}

:lang(ja) body,
:lang(zh) body,
:lang(ko) body {
  font-variant-numeric: lining-nums proportional-nums;
}

h1, h2, h3, h4, h5, h6, dt {
  font-variant-numeric: lining-nums proportional-nums;
}

table {
  font-variant-numeric: lining-nums tabular-nums;
}

code, var {
  font-variant-ligatures: none;
  font-variant-numeric: lining-nums tabular-nums slashed-zero;
}

rt {
  font-variant-east-asian: ruby;
}

:lang(ar) {
  font-variant-ligatures: common-ligatures;
}

:lang(ko) {
  font-kerning: normal;
}

/* Night mode */

hr {
  color: inherit;
  border-color: currentColor;
}

table, th, td {
  border-color: currentColor;
}

/* Horizontal Spacing */

figure, blockquote {
  margin: 1em 5%;
}

/*

:lang(ja) figure, :lang(ja) blockquote,
:lang(zh-Hant) figure, :lang(zh-Hant) blockquote,
:lang(zh-TW) figure, :lang(zh-TW) blockquote,
:lang(mn) figure, :lang(mn) blockquote {
  margin: 5% 1em;
}

*/

ul, ol {
  padding-left: 5%;
}

/*

:lang(ja) ul, :lang(ja) ol,
:lang(zh-Hant) ul, :lang(zh-Hant) ol,
:lang(zh-TW) ul, :lang(zh-TW) ol,
:lang(mn) ul, :lang(mn) ol   {
  padding-top: 5%;
}

*/

dd {
  margin-left: 5%;
}

/*

:lang(ja) dd,
:lang(zh-Hant) dd,
:lang(zh-TW) dd,
:lang(mn) dd {
  margin-top: 5%;
}

*/

pre {
  white-space: pre-wrap;
  -ms-tab-size: 2;
  -moz-tab-size: 2;
  -webkit-tab-size: 2;
  tab-size: 2;
}

/* Normalization */

abbr[title], acronym[title] {
  text-decoration: dotted underline;
}

nobr wbr {
  white-space: normal;
}

/* Make ruby text and parentheses non-selectable (TBC) */

ruby > rt, ruby > rp {
  -webkit-user-select: none;
  -moz-user-select: none;
  -ms-user-select: none;
  user-select: none;
}

/* Internationalization */

*:lang(ja),
*:lang(zh),
*:lang(ko),
:lang(ja) cite, :lang(ja) dfn, :lang(ja) em, :lang(ja) i,
:lang(zh) cite, :lang(zh) dfn, :lang(zh) em, :lang(zh) i,
:lang(ko) cite, :lang(ko) dfn, :lang(ko) em, :lang(ko) i {
  font-style: normal;
}

:lang(ja) a,
:lang(zh) a,
:lang(ko) a {
  text-decoration: none;
}

/* Readium CSS
   Safeguards module

   A set of styles to prevent common issues in pagination

   Repo: https://github.com/readium/readium-css */

/* Config */

/* We’ll be using an "RS__" prefix so that we can prevent collisions with authors’ CSS */

:root {
  /* max-width for media, you can override that via JS if not compiled to static */
  --RS__maxMediaWidth: 100%;

  /* max-height for media, you can override that via JS if not compiled to static
     Please consider figures might have a figcaption, which is why 95vh in the first place */
  --RS__maxMediaHeight: 95vh;

  /* value for medias’ box-sizing */
  --RS__boxSizingMedia: border-box;

  /* value for table’s box-sizing */
  --RS__boxSizingTable: border-box;
}

/* Sanitize line-heights in webkit e.g. raised cap without a declared line-height
   See effect by checking this demo in Safari: https://codepen.io/JayPanoz/pen/gRmzrE
   Note: glyphs has to be reset to inline for CJK */

html {
  -webkit-line-box-contain: block glyphs replaced;
}

:lang(ja) {
  -webkit-line-box-contain: block inline replaced;
}

/* Wrap long strings if larger than line-length */

a, h1, h2, h3, h4, h5, h6 {
  word-wrap: break-word;
}

div {
  max-width: var(--RS__maxMediaWidth);
}

/* Size medias */

/* You can override CSS variables by re-defining it for all elements or a specific one */

img, svg, audio, video {

  /* Object-fit allows us to keep the correct aspect-ratio */
  object-fit: contain;

  width: auto;
  height: auto;

  /* Some files don’t have max-width */
  max-width: var(--RS__maxMediaWidth);

  /* We’re setting a max-height, especially for covers */
  max-height: var(--RS__maxMediaHeight) !important;
  /* We probably don’t need to use modern box-sizing as auto behaves like it */
  box-sizing: var(--RS__boxSizingMedia);

  /* For page-break, we must use those 3
     We can’t use a variable there, webkit seems to no support them for those properties */
  -webkit-column-break-inside: avoid;
  page-break-inside: avoid;
  break-inside: avoid;
}

/* Try preventing border being cut-off, webkit + blink have content-box by default */

table {
  max-width: var(--RS__maxMediaWidth);
  box-sizing: var(--RS__boxSizingTable);
}
