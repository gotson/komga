import type { Meta, StoryObj } from '@storybook/vue3-vite'

import Tooltip from './Tooltip.vue'
import { VCol, VContainer, VRow, VTooltip, VBtn } from 'vuetify/components'

const meta = {
  component: Tooltip,
  render: (args: object) => ({
    components: { Tooltip, VContainer, VRow, VCol, VTooltip, VBtn },
    setup() {
      return { args }
    },
    template: `
      <v-container>
        <v-row>
          <v-col cols="6">VTooltip</v-col>
          <v-col cols="6">KTooltip</v-col>
        </v-row>
        <v-row>
          <v-col cols="6">
            <VTooltip text="custom text">
              <template #activator="{props}">
                <VBtn v-bind="props">Activator</VBtn>
              </template>
            </VTooltip>
          </v-col>
          <v-col cols="6">
            <Tooltip text="custom text">
              <template #activator="{props}">
                <VBtn v-bind="props">Activator</VBtn>
              </template>
            </Tooltip>
          </v-col>
        </v-row>
      </v-container>
    `,
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component:
          'An extension of v-tooltip that works better on mobile by supporting long press.',
      },
    },
  },
  args: {},
} satisfies Meta<typeof Tooltip>

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
