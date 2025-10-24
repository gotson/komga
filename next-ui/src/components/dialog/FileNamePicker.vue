<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :fullscreen="fullscreen"
    scrollable
    :transition="fullscreen ? 'dialog-bottom-transition' : undefined"
    max-width="600px"
    :aria-label="dialogTitle"
    @after-leave="reset()"
  >
    <template #default="{ isActive }">
      <v-card :title="dialogTitle">
        <template #append>
          <v-icon
            icon="i-mdi:close"
            @click="isActive.value = false"
          />
        </template>

        <v-card-text>
          <v-container fluid>
            <v-row>
              <v-col>
                <div class="text-subtitle-2">
                  {{
                    $formatMessage({
                      description: 'File name picker dialog: source file name field label',
                      defaultMessage: 'Source file name',
                      id: 'lSlhp0',
                    })
                  }}
                </div>
                <div>
                  {{ existingName }}
                </div>
              </v-col>
            </v-row>

            <v-row align="center">
              <v-col>
                <v-text-field
                  v-model="newName"
                  autofocus
                  :label="
                    $formatMessage({
                      description: 'File name picker dialog: destination file name field label',
                      defaultMessage: 'Destination file name',
                      id: 'bFoCHJ',
                    })
                  "
                  variant="underlined"
                  append-inner-icon="i-mdi:restore"
                  hide-details
                  @click:append-inner="newName = existingName"
                  @keydown.enter="choose"
                />
              </v-col>

              <v-col cols="auto">
                <v-btn
                  :disabled="!newName"
                  :text="
                    $formatMessage({
                      description: 'File name picker dialog: confirmation button',
                      defaultMessage: 'Choose',
                      id: 'd2J/J/',
                    })
                  "
                  @click="choose"
                />
              </v-col>
            </v-row>
          </v-container>

          <v-divider class="my-2" />

          <v-data-table
            v-if="seriesBooks"
            :items="seriesBooks"
            :headers="bookTableHeaders"
            fixed-header
            fixed-footer
            style="height: 80%"
          >
            <template #top>
              <v-toolbar
                flat
                :title="
                  $formatMessage({
                    description: 'File name picker dialog: series books table header',
                    defaultMessage: 'Series books',
                    id: 'Vpmsx0',
                  })
                "
              ></v-toolbar>
            </template>

            <template #[`item.name`]="{ value }">
              <span
                class="cursor-pointer"
                @click="newName = value"
                >{{ value }}</span
              >
            </template>
          </v-data-table>

          <v-alert
            v-else
            type="info"
            variant="tonal"
            :text="
              $formatMessage({
                description:
                  'File name picker dialog: info text shown when there are no existing series books to show',
                defaultMessage: 'Select a series to see its books',
                id: 'FSJoDl',
              })
            "
          />
        </v-card-text>
      </v-card>
    </template>
  </v-dialog>
</template>

<script setup lang="ts">
import type { components } from '@/generated/openapi/komga'
import { useIntl } from 'vue-intl'

const intl = useIntl()

const showDialog = defineModel<boolean>('dialog', { required: false })

const {
  fullscreen = undefined,
  activator = undefined,
  existingName = '',
  seriesBooks,
} = defineProps<{
  fullscreen?: boolean
  activator?: Element | string
  existingName?: string
  seriesBooks?: components['schemas']['BookDto'][]
}>()

const emit = defineEmits<{
  selectedName: [name: string]
}>()

const newName = ref<string>(existingName)

// sync the ref if the prop changes
watch(
  () => existingName,
  (it) => (newName.value = it),
)

function choose() {
  emit('selectedName', newName.value)
  showDialog.value = false
}

function reset() {
  newName.value = existingName
}

const dialogTitle = intl.formatMessage({
  description: 'Filename picker dialog: title',
  defaultMessage: 'Destination filename',
  id: '3Pm2PO',
})

const bookTableHeaders = [
  {
    title: intl.formatMessage({
      description: 'File name picker dialog: series books table header: order',
      defaultMessage: 'Order',
      id: 'rhtmLf',
    }),
    key: 'number',
  },
  {
    title: intl.formatMessage({
      description: 'File name picker dialog: series books table header: existing file name',
      defaultMessage: 'Existing file',
      id: 'IEUgyy',
    }),
    key: 'name',
  },
]
</script>
<script setup lang="ts"></script>
