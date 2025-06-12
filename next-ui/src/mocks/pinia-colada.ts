import { createPinia } from 'pinia'
import { PiniaColada } from '@pinia/colada'
import { mount } from '@vue/test-utils'

const DummyComponent = {
  template: '<p></p>',
}

export const createMockColada = () =>
  mount(DummyComponent, {
    global: {
      plugins: [createPinia(), [PiniaColada, {}]],
    },
  })
