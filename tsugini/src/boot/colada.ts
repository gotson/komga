import { defineBoot } from '#q-app/wrappers'
import { PiniaColada } from '@pinia/colada'
import { PiniaColadaAutoRefetch } from '@pinia/colada-plugin-auto-refetch'

// "async" is optional;
// more info on params: https://v2.quasar.dev/quasar-cli-vite/boot-files
export default defineBoot(({ app }) => {
  app.use(PiniaColada, {
    plugins: [PiniaColadaAutoRefetch()],
  })
})
