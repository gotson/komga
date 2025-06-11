import { createPinia } from 'pinia'
import { PiniaColada } from '@pinia/colada'
import { mount } from '@vue/test-utils'

const DummyComponent = {
  template: '<p></p>',
}

const pinia = createPinia()
export const mockPiniaColada = mount(DummyComponent, {
  global: {
    plugins: [pinia, [PiniaColada, {}]],
  },
})
