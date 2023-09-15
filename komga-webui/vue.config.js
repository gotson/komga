// vue.config.js
module.exports = {
  // with './' the dev server cannot load any arbitrary path
  // with '/' the prod build generates some url(/fonts…) calls in the css chunks, which doesn't work with a servlet context path
  publicPath: process.env.NODE_ENV === 'production' ? './' : '/',

  pluginOptions: {
    i18n: {
      locale: 'en',
      fallbackLocale: 'en',
      localeDir: 'locales',
      enableInSFC: false,
    },
  },
}
