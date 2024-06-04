/* Readium CSS
   Default module

   A stylesheet for unstyled ebooks based on HTML5 Suggested Rendering
   Note: works in combination with Base module

   Repo: https://github.com/readium/readium-css */

/* CONFIG */

:root {
  --RS__compFontFamily: var(--RS__baseFontFamily);
  --RS__codeFontFamily: var(--RS__monospaceTf);

  --RS__typeScale: 1.125; /* 1.067 | 1.125 | 1.2 | 1.25 | 1.333 | 1.414 | 1.5 | 1.618 */
  --RS__baseFontSize: 100%;

  --RS__flowSpacing: 1.5rem;
  --RS__paraSpacing: 0;
  --RS__paraIndent: 1em;

  --RS__linkColor: #0000EE;
  --RS__visitedColor: #551A8B;

  --RS__primaryColor: ;
  --RS__secondaryColor: ;
}

/* STYLES */

/* Typo */
mark {
    background: yellow;

}
mark.current {
    background: orange;
}

body {
  font-size: var(--RS__baseFontSize);
}

h1, h2, h3, h4, h5, h6 {
  font-family: var(--RS__compFontFamily);
}

/* Flow content */

blockquote, figure, p, pre,
aside, footer, form, hr {
  margin-top: var(--RS__flowSpacing);
  margin-bottom: var(--RS__flowSpacing);
}

p {
  margin-top: var(--RS__paraSpacing);
  margin-bottom: var(--RS__paraSpacing);
  text-indent: var(--RS__paraIndent);
}

h1 + p, h2 + p, h3 + p, h4 + p, h5 + p, h6 + p,
hr + p {
  text-indent: 0;
}

pre {
  font-family: var(--RS__codeFontFamily);
}

/* Phrasing content */

code, kbd, samp, tt {
  font-family: var(--RS__codeFontFamily);
}

sub, sup {
  position: relative;
  font-size: 67.5%;
  line-height: 1;
}

sub {
  bottom: -0.2ex;
}

sup {
  bottom: 0;
}

:link {
  color: var(--RS__linkColor);
}

:visited {
  color: var(--RS__visitedColor);
}

/* Headings */

h1 {
  margin-top: calc(var(--RS__flowSpacing) * 2);
  margin-bottom: calc(var(--RS__flowSpacing) * 2);
  /* The following is base font size * typescale power of 3 */
  font-size: calc(((1em * var(--RS__typeScale)) * var(--RS__typeScale)) * var(--RS__typeScale));
}

h2 {
  margin-top: calc(var(--RS__flowSpacing) * 2);
  margin-bottom: var(--RS__flowSpacing);
  /* The following is base font size * typescale power of 2 */
  font-size: calc((1em * var(--RS__typeScale)) * var(--RS__typeScale));
}

h3 {
  margin-top: var(--RS__flowSpacing);
  margin-bottom: var(--RS__flowSpacing);
  font-size: calc(1em * var(--RS__typeScale));
}

h4 {
  margin-top: var(--RS__flowSpacing);
  margin-bottom: var(--RS__flowSpacing);
  font-size: 1em;
}

h5 {
  margin-top: var(--RS__flowSpacing);
  margin-bottom: var(--RS__flowSpacing);
  font-size: 1em;
  font-variant: small-caps;
}

h6 {
  margin-top: var(--RS__flowSpacing);
  margin-bottom: 0;
  font-size: 1em;
  text-transform: lowercase;
  font-variant: small-caps;
}

/* Lists */

dl, ol, ul {
  margin-top: var(--RS__flowSpacing);
  margin-bottom: var(--RS__flowSpacing);
}

/* Table */

table {
  margin: var(--RS__flowSpacing) 0;
  border: 1px solid currentColor;
  border-collapse: collapse;
  empty-cells: show;
}

thead, tbody, tfoot, table > tr {
  vertical-align: top;
}

th {
  text-align: left;
}

th, td {
  padding: 4px;
  border: 1px solid currentColor;
}
