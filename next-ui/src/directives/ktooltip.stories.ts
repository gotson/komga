import type { Meta, StoryObj } from '@storybook/vue3-vite'

import vktooltip from './ktooltip'
import { VCol, VContainer, VRow, VTooltip, VBtn } from 'vuetify/components'
import { Tooltip } from 'vuetify/directives'
import type { Directive } from 'vue'

const meta = {
  render: (args: object) => ({
    components: { VContainer, VRow, VCol, VTooltip, VBtn },
    directives: {
      ktooltip: vktooltip,
      tooltip: Tooltip as Directive,
    },
    setup() {
      const tooltip = {
        text: 'I open on click',
        scrim: true,
        openOnClick: true,
        openOnHover: false,
      }

      return { args, tooltip }
    },
    template: `
      <v-container>
        <v-row>
          <v-col cols="4"></v-col>
          <v-col cols="4">VTooltip</v-col>
          <v-col cols="4">KTooltip</v-col>
        </v-row>

        <v-row>
          <v-col cols="4">Activator text</v-col>
          <v-col cols="4">
            <VBtn v-ktooltip>Activator</VBtn>
          </v-col>
          <v-col cols="4">
                <VBtn v-ktooltip>Activator</VBtn>
          </v-col>
        </v-row>

        <v-row>
          <v-col cols="4">Simple text</v-col>
          <v-col cols="4">
            <VBtn v-ktooltip:top="'custom text'">Activator</VBtn>
          </v-col>
          <v-col cols="4">
                <VBtn v-ktooltip:bottom="'custom text'">Activator</VBtn>
          </v-col>
        </v-row>

        <v-row>
          <v-col cols="4">Object</v-col>
          <v-col cols="4">
            <VBtn v-ktooltip="tooltip">Click me</VBtn>
          </v-col>
          <v-col cols="4">
            <VBtn v-ktooltip="tooltip">Click me</VBtn>
          </v-col>
        </v-row>
      </v-container>
    `,
  }),
  parameters: {
    // More on how to position stories at: https://storybook.js.org/docs/configure/story-layout
    docs: {
      description: {
        component: 'Directive for KKooltip, use with v-ktooltip',
      },
    },
  },
  args: {},
} satisfies Meta

export default meta
type Story = StoryObj<typeof meta>

export const Default: Story = {}
