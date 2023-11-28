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
   Pagination module

   A set of styles to paginate ePublications

   Repo: https://github.com/readium/readium-css */

/* Config */

/* Columns are responsive by default, even if column-width is set in pixels,
   which means two-page spread will switch to single page depending on current font-size.
   If you want more control, I’m afraid you’ll have to update colWidth/colGap dynamically,
   which is how a significant amount of RS do at the moment. */

/* Default for smartphone portrait (small screens) */

:root {
  /* Your columns’ width floor */
  --RS__colWidth: 45em; /* The width at which we’ll switch to 2 columns by default. PS: you can’t set it in rem */

  /* Ideal number of columns (depending on columns’ width floor) */
  --RS__colCount: 1;

  /* Gap between columns (in pixels so that it won’t resize with font-size) */
  --RS__colGap: 0;

  /* Optimal line-length (rem will take :root font-size into account, whatever the body’s font-size) */
  --RS__maxLineLength: 40rem;

  /* Default page horizontal margins (in pixels so that it won’t resize with font-size) */
  --RS__pageGutter: 20px; /* See if colGap and pageGutter can be the same var */
}

/* Reset page margins for Forward compatibility */

@page {
  margin: 0 !important;
}

/* :root selector has same specificity as a class i.e. 0010
   We might have to change that to html / context
   -> https://css-tricks.com/almanac/selectors/r/root/ */

:root {

  /* In case you use left position to scroll, can be removed if using transform: translateX() */
  position: relative;

  -webkit-column-width: var(--RS__colWidth);
  -moz-column-width: var(--RS__colWidth);
  column-width: var(--RS__colWidth);

  /* Init pagination */
  /* TODO: document columns’ logic cos it might feel weird at first */
  -webkit-column-count: var(--RS__colCount);
  -moz-column-count: var(--RS__colCount);
  column-count: var(--RS__colCount);

  -webkit-column-gap: var(--RS__colGap);
  -moz-column-gap: var(--RS__colGap);
  column-gap: var(--RS__colGap);

  /* Default is balance and we want columns to be filled entirely (100vh) */
  -moz-column-fill: auto;
  column-fill: auto;
  width: 100%;
  height: 100vh;
  max-width: 100%;
  max-height: 100vh;
  min-width: 100%;
  min-height: 100vh;
  padding: 0 !important;
  margin: 0 !important;

  /* Column size will depend on this if we want to make it responsive */
  font-size: 100% !important;

  -webkit-text-size-adjust: 100%;

  /* Switch to newer box model (not inherited by authors’ styles) */
  box-sizing: border-box;

  /* Fix bug for older Chrome */
  -webkit-perspective: 1;
  /* Prevents options pop-up when long tap in webkit */
  -webkit-touch-callout: none;
}

body {
  /*  overflow: hidden; bugfix: contents won’t paginate in Firefox and one sample in Safari */
  width: 100%;

  /* Limit line-length but we have to reset when 2 columns and control the viewport.
     By using max-width + margin auto, margins will shrink when font-size increases,
     which is what would be expected in terms of typography. */
  max-width: var(--RS__maxLineLength) !important;
  padding: 0 var(--RS__pageGutter) !important;
  margin: 0 auto !important;

  /* We need a minimum padding on body so that descandants/ascendants in italic/script are not cut-off.
     Drawback: we have to use border-box so that it doesn’t screw the box model,
     which means it impacts colWidth and max-width */
  box-sizing: border-box;
}

/* We’ll now redefine margins and columns depending on the minimum width available
   The goal is having the simplest model possible and avoid targeting devices */

/* Smartphone landscape */

@media screen and (min-width: 35em) {
  :root {
    --RS__pageGutter: 30px;
  }
}

/* Tablet portrait */

@media screen and (min-width: 45em) {
  :root {
    --RS__pageGutter: 40px;
  }
}

/* Desktop + tablet large */

/* We get the previous settings, we just change the margins */

@media screen and (min-width: 75em) {
  :root {
    --RS__pageGutter: 50px;
  }
}

/* At this point (80em or so), constraining line length must be done at the web view/iframe level, or by limiting the size of :root itself */

/* Responsive columns */

@media screen and (min-width: 60em), screen and (min-device-width: 36em) and (max-device-width: 47em) and (orientation: landscape) {
  :root {
    /* The size at which we want 2 columns to switch to 1 (depending on font-size) */
    --RS__colWidth: 20em; /* 20 * 16 = 320px but 20 * 28 = 560px so it will switch to 1 col @ 175% font-size (user-setting) on an iPad */
    /* We constrain to 2 columns so that we can never get 3 or 4, etc. */
    --RS__colCount: 2;
    --RS__maxLineLength: 39.99rem; /* If we don’t use this, colNumber user setting won’t work in Safari… */
  }
}

/* Readium CSS
   Scroll module

   A set of styles to scroll ePublications
   This module overrides pagination

   Repo: https://github.com/readium/readium-css */

:root[style*="readium-scroll-on"] {

  /* Reset columns, auto + auto = columns can’t be created */
  -webkit-columns: auto auto !important;
  -moz-columns: auto auto !important;
  columns: auto auto !important;
  width: auto !important;
  height: auto !important;
  max-width: none !important;
  max-height: none !important;
  /* Reset html size so that the user can scroll */
  min-width: 0 !important;
  min-height: 0 !important;
}

/* Make sure line-length is limited in all configs */

:root[style*="readium-scroll-on"] body {
  --RS__maxLineLength: 40rem !important;

  margin-top: var(--RS__pageGutter) !important;
  margin-bottom: var(--RS__pageGutter) !important;
}

/* Scroll mode horizontal */

/* Vertical writing needs body height set */

/* Do we add a top/bottom margin for body in vertical scroll or not? */

/* Readium CSS
   Default highlights

   A stylesheet for user highlights

   Repo: https://github.com/readium/readium-css */

/* User Highlights */

.readiumCSS-yellow-highlight {
  background-color: rgba(255, 255, 0, 0.5);
}

.readiumCSS-green-highlight {
  background-color: rgba(0, 255, 0, 0.5);
}

.readiumCSS-orange-highlight {
  background-color: rgba(255, 165, 0, 0.5);
}

.readiumCSS-pink-highlight {
  background-color: rgba(255, 105, 180, 0.5);
}

/* Media overlays */

.readiumCSS-mo-active-default {
  color: black !important;
  background-color: yellow !important;
}

/* Readium CSS
   Night mode

   A preset theme for night mode

   Repo: https://github.com/readium/readium-css */

/* CONFIG */

/* [style*="--USER__appearance"] can be used to increase specificity but performance hit */

:root[style*="readium-night-on"] {
  --RS__backgroundColor: #000000;
  --RS__textColor: #FEFEFE;

  --RS__linkColor: #63caff;
  --RS__visitedColor: #0099E5;

  /* This can be customized but initial will re-use default value of the browser */
  --RS__selectionBackgroundColor: #b4d8fe;
  --RS__selectionTextColor: inherit;
}

/* we don’t need to redeclare bg-color and color for :root since we will inherit and update from day/default mode */

:root[style*="readium-night-on"] *:not(a) {
  color: inherit !important;
  background-color: transparent !important;
  border-color: currentColor !important;
}

:root[style*="readium-night-on"] svg text {
  fill: currentColor !important;
  stroke: none !important;
}

:root[style*="readium-night-on"] a:link,
:root[style*="readium-night-on"] a:link * {
  color: var(--RS__linkColor) !important;
}

:root[style*="readium-night-on"] a:visited,
:root[style*="readium-night-on"] a:visited * {
  color: var(--RS__visitedColor) !important;
}

:root[style*="readium-night-on"] img[class*="gaiji"],
:root[style*="readium-night-on"] *[epub\:type~="titlepage"] img:only-child {
  -webkit-filter: invert(100%);
  filter: invert(100%);
}

/* Darken all images on user’s demand */

:root[style*="readium-night-on"][style*="readium-darken-on"] img {
  -webkit-filter: brightness(80%);
  filter: brightness(80%);
}

/* Invert all images on user’s demand */

:root[style*="readium-night-on"][style*="readium-invert-on"] img {
  -webkit-filter: invert(100%);
  filter: invert(100%);
}

/* Darken and invert on user’s demand */

:root[style*="readium-night-on"][style*="readium-darken-on"][style*="readium-invert-on"] img {
  -webkit-filter: brightness(80%) invert(100%);
  filter: brightness(80%) invert(100%);
}

/* Readium CSS
   Sepia mode

   A preset theme for sepia mode

   Repo: https://github.com/readium/readium-css */

/* CONFIG */

:root[style*="readium-sepia-on"] {
  --RS__backgroundColor: #faf4e8;
  --RS__textColor: #121212;

  --RS__linkColor: #0000EE;
  --RS__visitedColor: #551A8B;

  /* This can be customized but initial will re-use default value of the browser */
  --RS__selectionBackgroundColor: #b4d8fe;
  --RS__selectionTextColor: inherit;

  --RS__maxLineLength: 40.01rem; /* Forcing a reflow in Blink/Webkit so that blend mode can work */
}

/* we don’t need to redeclare bg-color and color for :root since we will inherit and update from day/default mode */

:root[style*="readium-sepia-on"] body {
  /* Should be transparent but Chrome bug https://bugs.chromium.org/p/chromium/issues/detail?id=711955&q=mix-blend-mode&colspec=ID%20Pri%20M%20Stars%20ReleaseBlock%20Component%20Status%20Owner%20Summary%20OS%20Modified */

  color: inherit;
  background-color: var(--RS__backgroundColor);
}

:root[style*="readium-sepia-on"] a:link,
:root[style*="readium-sepia-on"] a:link * {
  color: var(--RS__linkColor);
}

:root[style*="readium-sepia-on"] a:visited,
:root[style*="readium-sepia-on"] a:visited * {
  color: var(--RS__visitedColor);
}

:root[style*="readium-sepia-on"] svg,
:root[style*="readium-sepia-on"] img {
  /* Make sure the proper bg-color is used for the blend mode */
  background-color: transparent !important;
  mix-blend-mode: multiply;
}

/* Readium CSS
   OS Accessibility Modes

   A stylesheet to deal with OS accessibility settings

   Repo: https://github.com/readium/readium-css */

/* Windows high contrast colors are mapped to CSS system color keywords
   See http://www.gwhitworth.com/blog/2017/04/how-to-use-ms-high-contrast */

@media screen and (-ms-high-contrast: active) {
  :root {
    color: windowText !important;
    background-color: window !important;
  }

  /* The following selectors are super funky but it makes sure everything is inherited, this is indeed critical for this mode */
  :root :not(#\#):not(#\#):not(#\#),
  :root :not(#\#):not(#\#):not(#\#) :not(#\#):not(#\#):not(#\#)
  :root :not(#\#):not(#\#):not(#\#) :not(#\#):not(#\#):not(#\#) :not(#\#):not(#\#):not(#\#) {
    color: inherit !important;
    background-color: inherit !important;
  }

  .readiumCSS-mo-active-default {
    color: highlightText !important;
    background-color: highlight !important;
  }

  /* For links, hyperlink keyword is automatically set */

  /* Should we also set user highlights? */
}

@media screen and (-ms-high-contrast: white-on-black) {
  :root[style*="readium-night-on"] img[class*="gaiji"],
  :root[style*="readium-night-on"] *[epub\:type~="titlepage"] img:only-child {
    -webkit-filter: none !important;
    filter: none !important;
  }
  :root[style*="readium-night-on"][style*="readium-invert-on"] img {
    -webkit-filter: none !important;
    filter: none !important;
  }
  :root[style*="readium-night-on"][style*="readium-darken-on"][style*="readium-invert-on"] img {
    -webkit-filter: brightness(80%);
    filter: brightness(80%);
  }
}

/* Will be true on recent versions of iOS and MacOS if inverted setting enabled by the user */

@media screen and (inverted-colors) {
  :root[style*="readium-night-on"] img[class*="gaiji"],
  :root[style*="readium-night-on"] *[epub\:type~="titlepage"] img:only-child {
    -webkit-filter: none !important;
    filter: none !important;
  }
  :root[style*="readium-night-on"][style*="readium-invert-on"] img {
    -webkit-filter: none !important;
    filter: none !important;
  }
  :root[style*="readium-night-on"][style*="readium-darken-on"][style*="readium-invert-on"] img {
    -webkit-filter: brightness(80%);
    filter: brightness(80%);
  }
}

@media screen and (monochrome) {
  /* Grayscale (Implemented in Safari, what about eInk?) */
  /* Must deal with anything color (contrast) so must be managed at the night/sepia/theme level :( */
}

@media screen and (prefers-reduced-motion) {
  /* If reduced motion is set on MacOS, in case we have animation/transition */
}

/* Readium CSS
   Columns number pref

   A submodule managing columns number for user settings
   Part of “Chrome Advanced” class – no flag required.

   Repo: https://github.com/readium/readium-css */

/* Number of columns = 1 | 2 */

/* We still need to see if we allow users to force number of columns for all configs, currently it behaves as an "auto" setting */

/* apply col setting except for mobile portrait */

@media screen and (min-width: 60em), screen and (min-device-width: 36em) and (max-device-width: 47em) and (orientation: landscape) {
  :root[style*="--USER__colCount: 1"],
  :root[style*="--USER__colCount:1"],
  :root[style*="--USER__colCount: 2"],
  :root[style*="--USER__colCount:2"] {
    -webkit-column-count: var(--USER__colCount);
    -moz-column-count: var(--USER__colCount);
    column-count: var(--USER__colCount);
  }

  /* If one column, make sure we limit line-length */
  :root[style*="--USER__colCount: 1"],
  :root[style*="--USER__colCount:1"] {
    --RS__maxLineLength: 40rem !important; /* This is the only way for the user setting to work in webkit… */
    --RS__colWidth: 100vw;
  }

  /* If smartphone landscape, and 2 columns, col width the same as iPad landscape + desktop */
  :root[style*="--USER__colCount: 2"],
  :root[style*="--USER__colCount:2"] {
    --RS__colWidth: auto; /* User explicitely tells he/she wants 2 columns, we reset floor value */
  }
}

/* Readium CSS
   Page margins pref

   A submodule managing page margins for user settings
   Part of “Chrome Advanced” class – no flag required.

   Repo: https://github.com/readium/readium-css */

/* Page Margins: the user margin is a factor of the page gutter e.g. 0.5, 0.75, 1, 1.25, 1.5, etc. */

:root[style*="--USER__pageMargins"] body {
  padding: 0 calc(var(--RS__pageGutter) * var(--USER__pageMargins)) !important;
}

/* Readium CSS
   Custom colors pref

   A submodule managing custom colors for user settings
   Part of “Chrome Advanced” class – no flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="--USER__backgroundColor"] {
  background-color: var(--USER__backgroundColor) !important;
}

:root[style*="--USER__backgroundColor"] * {
  background-color: transparent !important;
}

:root[style*="--USER__textColor"] {
  color: var(--USER__textColor) !important;
}

:root[style*="--USER__textColor"] *:not(h1):not(h2):not(h3):not(h4):not(h5):not(h6):not(pre) {
  color: inherit !important;
}

/* Readium CSS
   Text align pref

   A submodule managing text-align for user settings
   Part of “User Overrides Advanced” class – “advanced settings” flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="readium-advanced-on"][style*="--USER__textAlign"] {
  text-align: var(--USER__textAlign);
}

:root[style*="readium-advanced-on"][style*="--USER__textAlign"] body,
:root[style*="readium-advanced-on"][style*="--USER__textAlign"] *:not(blockquote):not(figcaption) p,
:root[style*="readium-advanced-on"][style*="--USER__textAlign"] li {
  text-align: inherit !important;
  -moz-text-align-last: auto !important;
  -epub-text-align-last: auto !important;
  text-align-last: auto !important;
}

/* In case something goes wrong at the programmatic level + rtl for body + rtl in ltr */

:root[style*="readium-advanced-on"][dir="rtl"][style*="--USER__textAlign: left"],
:root[style*="readium-advanced-on"][dir="rtl"][style*="--USER__textAlign:left"],
:root[style*="readium-advanced-on"][style*="--USER__textAlign: left"] *[dir="rtl"],
:root[style*="readium-advanced-on"][style*="--USER__textAlign:left"] *[dir="rtl"] {
  text-align: right;
}

/* Edge, if logical value is used, think of it as a polyfill. For LTR, it will fall back to the default, which is left */

:root[style*="readium-advanced-on"][dir="rtl"][style*="--USER__textAlign: start"],
:root[style*="readium-advanced-on"][dir="rtl"][style*="--USER__textAlign:start"] {
  text-align: right;
}

/* Readium CSS
   Hyphenation pref

   A submodule managing hyphens for user settings
   Part of “User Overrides Advanced” class – “advanced settings” flag required.

   Repo: https://github.com/readium/readium-css */

/* Managing hyphenation automatically for text-align values */

:root[style*="readium-advanced-on"][style*="--USER__textAlign: justify"] body,
:root[style*="readium-advanced-on"][style*="--USER__textAlign:justify"] body {
  -webkit-hyphens: auto;
  -moz-hyphens: auto;
  -ms-hyphens: auto;
  -epub-hyphens: auto;
  hyphens: auto;
}

:root[style*="readium-advanced-on"][style*="--USER__textAlign: left"] body,
:root[style*="readium-advanced-on"][style*="--USER__textAlign:left"] body,
:root[style*="readium-advanced-on"][style*="--USER__textAlign: right"] body,
:root[style*="readium-advanced-on"][style*="--USER__textAlign:right"] body {
  -webkit-hyphens: none;
  -moz-hyphens: none;
  -ms-hyphens: none;
  -epub-hyphens: none;
  hyphens: none;
}

/* Managing the user override */

:root[style*="readium-advanced-on"][style*="--USER__bodyHyphens"] {
  -webkit-hyphens: var(--USER__bodyHyphens) !important;
  -moz-hyphens: var(--USER__bodyHyphens) !important;
  -ms-hyphens: var(--USER__bodyHyphens) !important;
  -epub-hyphens: var(--USER__bodyHyphens) !important;
  hyphens: var(--USER__bodyHyphens) !important;
}

/* Sorry, we can’t use `:matches`, `:-moz-any` or `:-webkit-any` because MS doesn’t support it yet */

:root[style*="readium-advanced-on"][style*="--USER__bodyHyphens"] body,
:root[style*="readium-advanced-on"][style*="--USER__bodyHyphens"] p,
:root[style*="readium-advanced-on"][style*="--USER__bodyHyphens"] li,
:root[style*="readium-advanced-on"][style*="--USER__bodyHyphens"] div,
:root[style*="readium-advanced-on"][style*="--USER__bodyHyphens"] dd {
  -webkit-hyphens: inherit;
  -moz-hyphens: inherit;
  -ms-hyphens: inherit;
  -epub-hyphens: inherit;
  hyphens: inherit;
}

/* Readium CSS
   Font Family pref

   A submodule managing font-family for user settings
   Part of “User Overrides” class – “font override” flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="readium-font-on"][style*="--USER__fontFamily"] {
  font-family: var(--USER__fontFamily) !important;
}

:root[style*="readium-font-on"][style*="--USER__fontFamily"] body,
:root[style*="readium-font-on"][style*="--USER__fontFamily"] p,
:root[style*="readium-font-on"][style*="--USER__fontFamily"] li,
:root[style*="readium-font-on"][style*="--USER__fontFamily"] div,
:root[style*="readium-font-on"][style*="--USER__fontFamily"] dt,
:root[style*="readium-font-on"][style*="--USER__fontFamily"] dd {
  font-family: inherit !important;
}

:root[style*="readium-font-on"][style*="--USER__fontFamily"] i:not([lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] i:not([xml\:lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] em:not([lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] em:not([xml\:lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] cite:not([lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] cite:not([xml\:lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] b:not([lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] b:not([xml\:lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] strong:not([lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] strong:not([xml\:lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] span:not([lang]),
:root[style*="readium-font-on"][style*="--USER__fontFamily"] span:not([xml\:lang]) {
  font-family: inherit !important;
}

/* Readium CSS
   A11y font pref

   A submodule managing a11y text normalization for user settings
   Part of “User Overrides” class – “font override” flag required.

   Repo: https://github.com/readium/readium-css */

/* For AccessibleDfA, we need to normalize font-weight and font-style since only the normal style is available */

:root[style*="readium-font-on"][style*="AccessibleDfA"] {
  /* We won’t use the variable there since we need fallbacks for missing characters */
  font-family: AccessibleDfA, Verdana, Tahoma, "Trebuchet MS", sans-serif !important;
  --RS__lineHeightCompensation: 1.167;
}

:root[style*="readium-font-on"][style*="IA Writer Duospace"] {
  /* We won’t use the variable there since we need fallbacks for missing characters */
  font-family: "IA Writer Duospace", Menlo, "DejaVu Sans Mono", "Bitstream Vera Sans Mono", Courier, monospace !important;
  --RS__lineHeightCompensation: 1.167;
}

:root[style*="readium-font-on"][style*="readium-a11y-on"] {
  font-family: var(--USER__fontFamily) !important;
  --RS__lineHeightCompensation: 1.167;
}

/* Maybe users want a setting to normalize any font offered so there is a “a11y Normalize” flag for it */

:root[style*="readium-font-on"][style*="AccessibleDfA"],
:root[style*="readium-font-on"][style*="IA Writer Duospace"],
:root[style*="readium-font-on"][style*="readium-a11y-on"] {
  font-style: normal !important;
  font-weight: normal !important;
}

/* Targeting everything except code. Note that Open Dyslexic has a monospaced font for code */

:root[style*="readium-font-on"][style*="AccessibleDfA"] *:not(code):not(var):not(kbd):not(samp),
:root[style*="readium-font-on"][style*="IA Writer Duospace"] *:not(code):not(var):not(kbd):not(samp),
:root[style*="readium-font-on"][style*="readium-a11y-on"] *:not(code):not(var):not(kbd):not(samp) {
  font-family: inherit !important;
  font-style: inherit !important;
  font-weight: inherit !important;
}

/* Normalizing text-decoration, subs and sups */

:root[style*="readium-font-on"][style*="AccessibleDfA"] *,
:root[style*="readium-font-on"][style*="IA Writer Duospace"] *,
:root[style*="readium-font-on"][style*="readium-a11y-on"] * {
  text-decoration: none !important;
  font-variant-caps: normal !important;
  font-variant-numeric: normal !important;
  font-variant-position: normal !important;
}

:root[style*="readium-font-on"][style*="AccessibleDfA"] sup,
:root[style*="readium-font-on"][style*="IA Writer Duospace"] sup,
:root[style*="readium-font-on"][style*="readium-a11y-on"] sup,
:root[style*="readium-font-on"][style*="AccessibleDfA"] sub,
:root[style*="readium-font-on"][style*="IA Writer Duospace"] sub,
:root[style*="readium-font-on"][style*="readium-a11y-on"] sub {
  font-size: 1rem !important;
  vertical-align: baseline !important;
}

/* Readium CSS
   Font size pref

   A submodule managing font-size for user settings
   Part of “User Overrides” class – no flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="--USER__fontSize"] {
  font-size: var(--USER__fontSize) !important;
}

/* Readium CSS
   Line height pref

   A submodule managing line-height for user settings
   Part of “User Overrides Advanced” class – “advanced settings” flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="readium-advanced-on"][style*="--USER__lineHeight"] {
  line-height: calc((var(--USER__lineHeight) + (2ex - 1ch) - ((1rem - 16px) * 0.1667)) * var(--RS__lineHeightCompensation)) !important;
}

:root[style*="readium-advanced-on"][style*="--USER__lineHeight"] body,
:root[style*="readium-advanced-on"][style*="--USER__lineHeight"] p,
:root[style*="readium-advanced-on"][style*="--USER__lineHeight"] li,
:root[style*="readium-advanced-on"][style*="--USER__lineHeight"] div {
  line-height: inherit;
}

/* Readium CSS
   Para spacing pref

   A submodule managing paragraphs’ top and bottom margins for user settings
   Part of “User Overrides Advanced” class – “advanced settings” flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="readium-advanced-on"][style*="--USER__paraSpacing"] p {
  margin-top: var(--USER__paraSpacing) !important;
  margin-bottom: var(--USER__paraSpacing) !important;
}

/* Readium CSS
   Para indent pref

   A submodule managing paragraphs’ text-indent for user settings
   Part of “User Overrides Advanced” class – “advanced settings” flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="readium-advanced-on"][style*="--USER__paraIndent"] p {
  text-indent: var(--USER__paraIndent) !important;
}

/* If there are inline-block elements in paragraphs, text-indent will inherit so we must reset it */

:root[style*="readium-advanced-on"][style*="--USER__paraIndent"] p *,
:root[style*="readium-advanced-on"][style*="--USER__paraIndent"] p:first-letter {
  text-indent: 0 !important;
}

/* Readium CSS
   Word spacing pref

   A submodule managing word-spacing for user settings
   Part of “User Overrides Advanced” class – “advanced settings” flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] h1,
:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] h2,
:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] h3,
:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] h4,
:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] h5,
:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] h6,
:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] p,
:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] li,
:root[style*="readium-advanced-on"][style*="--USER__wordSpacing"] div {
  word-spacing: var(--USER__wordSpacing);
}

/* Readium CSS
   Letter spacing pref

   A submodule managing letter-spacing for user settings
   Part of “User Overrides Advanced” class – “advanced settings” flag required.

   Repo: https://github.com/readium/readium-css */

:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] h1,
:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] h2,
:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] h3,
:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] h4,
:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] h5,
:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] h6,
:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] p,
:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] li,
:root[style*="readium-advanced-on"][style*="--USER__letterSpacing"] div {
  letter-spacing: var(--USER__letterSpacing);
  font-variant: none;
}

/* Readium CSS
   Font size normalize

   A stylesheet to normalize font-size

   Repo: https://github.com/readium/readium-css */

/* STYLES */

/* :root is used so that you can quickly add a class or attribute if you prefer e.g. `:root[data-rs-normalize]` */

/* We create a default so that you don’t need to explicitly set one in the DOM.
   Once the “Publisher’s styles” checkbox is unchecked, the normalize is applied automatically */

:root[style*="readium-advanced-on"] {
  --USER__typeScale: 1.2; /* This is the default type scale you’ll find in most publications */
}

:root[style*="readium-advanced-on"] p,
:root[style*="readium-advanced-on"] li,
:root[style*="readium-advanced-on"] div,
:root[style*="readium-advanced-on"] pre,
:root[style*="readium-advanced-on"] dd {
  font-size: 1rem !important;
}

:root[style*="readium-advanced-on"] h1 {
  /* Fallback if browser doesn’t support vars */
  font-size: 1.75rem !important;
  font-size: calc(((1rem * var(--USER__typeScale)) * var(--USER__typeScale)) * var(--USER__typeScale)) !important;
}

:root[style*="readium-advanced-on"] h2 {
  /* Fallback if browser doesn’t support vars */
  font-size: 1.5rem !important;
  font-size: calc((1rem * var(--USER__typeScale)) * var(--USER__typeScale)) !important;
}

:root[style*="readium-advanced-on"] h3 {
  /* Fallback if browser doesn’t support vars */
  font-size: 1.25rem !important;
  font-size: calc(1rem * var(--USER__typeScale)) !important;
}

:root[style*="readium-advanced-on"] h4,
:root[style*="readium-advanced-on"] h5,
:root[style*="readium-advanced-on"] h6 {
  font-size: 1rem !important;
}

:root[style*="readium-advanced-on"] small {
  font-size: smaller !important;
}

:root[style*="readium-advanced-on"] sub,
:root[style*="readium-advanced-on"] sup {
  font-size: 67.5% !important;
}

/* The following styles kick in if you define the typeScale variable in the DOM.
   No need to repeat declarations which don’t make use of the variable */

:root[style*="readium-advanced-on"][style*="--USER__typeScale"] h1 {
  font-size: calc(((1rem * var(--USER__typeScale)) * var(--USER__typeScale)) * var(--USER__typeScale)) !important;
}

:root[style*="readium-advanced-on"][style*="--USER__typeScale"] h2 {
  font-size: calc((1rem * var(--USER__typeScale)) * var(--USER__typeScale)) !important;
}

:root[style*="readium-advanced-on"][style*="--USER__typeScale"] h3 {
  font-size: calc(1rem * var(--USER__typeScale)) !important;
}
