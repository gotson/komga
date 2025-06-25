import { createPinia } from 'pinia'
import { PiniaColada } from '@pinia/colada'
import { mount } from '@vue/test-utils'

export const createMockColada = (setupFn: () => unknown) => {
  return mount(
    {
      template: '<p></p>',
      setup() {
        setupFn()
        return {}
      },
    },
    {
      global: {
        plugins: [createPinia(), [PiniaColada, {}]],
      },
    },
  )
}
