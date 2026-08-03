import type { Meta, StoryObj } from '@storybook/vue3-vite'

import ImportReadList from './readlist.vue'
import DialogConfirmEditInstance from '@/components/dialog/ConfirmEditInstance.vue'
import { delay, http } from 'msw'
import SnackQueue from '@/components/SnackQueue.vue'
import { emptyCbl, garbledCbl } from '@/mocks/api/handlers/readlists'
import { fireEvent, expect, waitFor } from 'storybook/test'

import { response400 } from '@/mocks/api/utils'

const meta = {
  component: ImportReadList,
  render: (args: object) => ({
    components: { ImportReadList, DialogConfirmEditInstance, SnackQueue },
    setup() {
      return { args }
    },
    template: '<ImportReadList /><DialogConfirmEditInstance/><SnackQueue/>',
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: '',
      },
    },
  },
  args: {},
} satisfies Meta<typeof ImportReadList>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {
  args: {},
  play: async ({ canvasElement, canvas }) => {
    const mockFile = new File([''], 'test-cbl.cbl', { type: 'text/plain' })
    const fileInput = canvasElement.querySelector('input[type="file"]')
    if (!fileInput) {
      throw new Error('Could not find an input element with type="file"')
    }
    await fireEvent.change(fileInput, {
      target: { files: [mockFile] },
    })
    await waitFor(() => expect(canvas.getByText(/create/i)).toBeVisible())
  },
}

export const Loading: Story = {
  beforeEach({ msw }) {
    msw.use(http.all('*/api/*', async () => await delay(5_000)))
  },
  play: async ({ canvasElement }) => {
    const mockFile = new File([''], 'test-cbl.cbl', { type: 'text/plain' })
    const fileInput = canvasElement.querySelector('input[type="file"]')
    if (!fileInput) {
      throw new Error('Could not find an input element with type="file"')
    }
    await fireEvent.change(fileInput, {
      target: { files: [mockFile] },
    })
  },
}

export const ErrorInvalidFileType: Story = {
  play: async ({ canvasElement, canvas }) => {
    const mockFile = new File([''], 'test-cbl.txt', { type: 'text/plain' })
    const fileInput = canvasElement.querySelector('input[type="file"]')
    if (!fileInput) {
      throw new Error('Could not find an input element with type="file"')
    }
    await fireEvent.change(fileInput, {
      target: { files: [mockFile] },
    })
    await waitFor(() => expect(canvas.getByText(/type not supported/i)).toBeVisible())
  },
}

export const ErrorNoBooks: Story = {
  beforeEach({ msw }) {
    msw.use(http.post('*/api/v1/readlists/match/comicrack', () => response400(emptyCbl)))
  },
  play: async ({ canvasElement, canvas }) => {
    const mockFile = new File([''], 'test-cbl.cbl', { type: 'text/plain' })
    const fileInput = canvasElement.querySelector('input[type="file"]')
    if (!fileInput) {
      throw new Error('Could not find an input element with type="file"')
    }
    await fireEvent.change(fileInput, {
      target: { files: [mockFile] },
    })
    await waitFor(() => expect(canvas.getByText(/does not contain/i)).toBeVisible())
  },
}

export const ErrorInvalidFile: Story = {
  beforeEach({ msw }) {
    msw.use(http.post('*/api/v1/readlists/match/comicrack', () => response400(garbledCbl)))
  },
  play: async ({ canvasElement, canvas }) => {
    const mockFile = new File([''], 'test-cbl.cbl', { type: 'text/plain' })
    const fileInput = canvasElement.querySelector('input[type="file"]')
    if (!fileInput) {
      throw new Error('Could not find an input element with type="file"')
    }
    await fireEvent.change(fileInput, {
      target: { files: [mockFile] },
    })
    await waitFor(() => expect(canvas.getByText(/error while deserializing/i)).toBeVisible())
  },
}
