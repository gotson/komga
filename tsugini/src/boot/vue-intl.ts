import { defineBoot } from '#q-app/wrappers'
import { createIntl } from 'vue-intl'

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli-vite/boot-files
export default defineBoot(({ app }) => {
  app.use(
    createIntl({
      locale: 'en',
      defaultLocale: 'en',
      messages: {},
    }),
  )
})
