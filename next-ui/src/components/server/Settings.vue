<template>
  <v-confirm-edit
    v-model="settingsUpdate"
    hide-actions
  >
    <template #default="{ model: proxyModel, cancel, save, isPristine }">
      <v-form
        v-model="formValid"
        :disabled="loading"
        @submit.prevent="submitForm(save)"
      >
        <v-row>
          <v-col>
            <div class="text-subtitle-2">
              {{
                $formatMessage({
                  description: 'Server settings: section header for posters',
                  defaultMessage: 'Posters',
                  id: 'a5MYiP',
                })
              }}
            </div>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <v-select
              v-model="proxyModel.value.thumbnailSize"
              :label="
                $formatMessage({
                  description: 'Server settings: selection of poster size',
                  defaultMessage: 'Generated poster size',
                  id: 'eDA9Gm',
                })
              "
              :items="thumbnailSizes"
            />
          </v-col>
        </v-row>

        <v-divider class="mb-4" />

        <v-row>
          <v-col>
            <div class="text-subtitle-2">
              {{
                $formatMessage({
                  description: 'Server settings: section header for scan behaviour',
                  defaultMessage: 'Overall scan behaviour',
                  id: 'dSlkbn',
                })
              }}
            </div>
            <v-checkbox
              v-model="proxyModel.value.deleteEmptyCollections"
              :label="
                $formatMessage({
                  description: 'Server settings: checkbox to delete empty collections after scan',
                  defaultMessage: 'Delete empty collections after scan',
                  id: 'pHdVzh',
                })
              "
              hide-details
            />
            <v-checkbox
              v-model="proxyModel.value.deleteEmptyReadLists"
              :label="
                $formatMessage({
                  description: 'Server settings: checkbox to delete empty readlists after scan',
                  defaultMessage: 'Delete empty read lists after scan',
                  id: 'kqV7EJ',
                })
              "
              hide-details
            />
          </v-col>
        </v-row>

        <v-divider class="mb-4" />

        <v-row>
          <v-col>
            <div class="text-subtitle-2">
              {{
                $formatMessage({
                  description: 'Server settings: section header for tasks',
                  defaultMessage: 'Tasks',
                  id: '8hC76W',
                })
              }}
            </div>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <v-number-input
              v-model="proxyModel.value.taskPoolSize"
              :label="
                $formatMessage({
                  description: 'Server settings: input field for task threads',
                  defaultMessage: 'Task threads',
                  id: 'rHwSrF',
                })
              "
              :min="1"
              :rules="['required']"
            ></v-number-input>
          </v-col>
        </v-row>

        <v-divider class="mb-4" />

        <v-row>
          <v-col>
            <div class="text-subtitle-2">
              {{
                $formatMessage({
                  description: 'Server settings: section header for remember me',
                  defaultMessage: 'Remember me',
                  id: 'EabQ38',
                })
              }}
            </div>
            <div class="text-caption">{{ messageRequiresRestart }}</div>
          </v-col>
        </v-row>
        <v-row>
          <v-col>
            <v-number-input
              v-model="proxyModel.value.rememberMeDurationDays"
              :label="
                $formatMessage({
                  description: 'Server settings: input field for remember me duration',
                  defaultMessage: 'Remember me duration (in days)',
                  id: 'iDU5FS',
                })
              "
              :min="1"
              :rules="['required']"
            ></v-number-input>
            <v-checkbox
              v-model="proxyModel.value.renewRememberMeKey"
              :label="
                $formatMessage({
                  description: 'Server settings: checkbox to regenerate the remember me key',
                  defaultMessage: 'Regenerate the \'remember me\' key',
                  id: 'UaD47n',
                })
              "
              hide-details
            />
          </v-col>
        </v-row>

        <v-divider class="mb-4" />

        <v-row>
          <v-col>
            <div class="text-subtitle-2">
              {{
                $formatMessage({
                  description: 'Server settings: section header for HTTP server',
                  defaultMessage: 'HTTP Server',
                  id: '6F1ebQ',
                })
              }}
            </div>
            <div class="text-caption">{{ messageRequiresRestart }}</div>
          </v-col>
        </v-row>

        <v-row>
          <v-col>
            <v-number-input
              v-model="proxyModel.value.serverPort"
              :label="
                $formatMessage({
                  description: 'Server settings: input field for server port',
                  defaultMessage: 'Server listening port',
                  id: '+r8FCS',
                })
              "
              :min="1"
              :max="65535"
              :placeholder="settings?.serverPort.configurationSource?.toString()"
              :persistent-placeholder="!!settings?.serverPort.configurationSource"
              clearable
              hide-details
            >
              <template
                v-if="!!settings?.serverPort.configurationSource"
                #append-inner
              >
                <v-icon
                  v-tooltip:bottom="messagePrecedence"
                  icon="i-mdi:information-outline"
                ></v-icon>
              </template>
            </v-number-input>
          </v-col>
        </v-row>

        <v-row>
          <v-col>
            <v-text-field
              v-model="proxyModel.value.serverContextPath"
              :label="
                $formatMessage({
                  description: 'Server settings: input field for server base URL',
                  defaultMessage: 'Server base URL',
                  id: 'eRJOa6',
                })
              "
              :placeholder="settings?.serverContextPath.configurationSource?.toString()"
              :persistent-placeholder="!!settings?.serverContextPath.configurationSource"
              clearable
              :rules="[
                [
                  'pattern',
                  /^\/[-a-zA-Z0-9_\/]*[a-zA-Z0-9]$/,
                  $formatMessage({
                    description:
                      'Server settings: error message when server context path is invalid',
                    defaultMessage:
                      'Must start with \'/\', not end with \'/-_\', and contain only \'/-_a-z0-9\'',
                    id: 'Lto2Lg',
                  }),
                ],
              ]"
              ><template
                v-if="!!settings?.serverContextPath.configurationSource"
                #append-inner
              >
                <v-icon
                  v-tooltip:bottom="messagePrecedence"
                  icon="i-mdi:information-outline"
                ></v-icon> </template
            ></v-text-field>
          </v-col>
        </v-row>

        <v-divider class="mb-4" />

        <v-row>
          <v-col>
            <div class="text-subtitle-2">
              {{
                $formatMessage({
                  description: 'Server settings: section header for Kobo Sync',
                  defaultMessage: 'Kobo Sync',
                  id: 'rRFQKU',
                })
              }}
            </div>
            <v-checkbox
              v-model="proxyModel.value.koboProxy"
              :label="
                $formatMessage({
                  description:
                    'Server settings: checkbox to enable Kobo Store proxying for Kobo Sync ',
                  defaultMessage: 'Proxy unhandled requests to Kobo Store',
                  id: 'iNBto3',
                })
              "
              hide-details
            />
            <v-number-input
              v-model="proxyModel.value.koboPort"
              :label="
                $formatMessage({
                  description: 'Server settings: input field for kobo sync port',
                  defaultMessage: 'Kobo Sync external port',
                  id: '4AKIbg',
                })
              "
              :min="1"
              :max="65535"
              :hint="
                $formatMessage({
                  description: 'Server settings: input field hint for kobo sync port',
                  defaultMessage: 'Set only in case of sync issues with covers and downloads',
                  id: 'TwB29u',
                })
              "
              persistent-hint
              clearable
            ></v-number-input>
          </v-col>
        </v-row>

        <v-row>
          <v-col cols="auto">
            <v-btn
              :text="
                $formatMessage({
                  description: 'Server settings: button to discard any changes made',
                  defaultMessage: 'Discard',
                  id: 'kh49ZJ',
                })
              "
              :disabled="isPristine"
              variant="text"
              @click="cancel"
            ></v-btn>
          </v-col>
          <v-col cols="auto">
            <v-btn
              :text="
                $formatMessage({
                  description: 'Server settings: button to save any changes made',
                  defaultMessage: 'Save changes',
                  id: 'FpwJlU',
                })
              "
              type="submit"
              :disabled="isPristine"
              variant="text"
              :loading="loading"
            ></v-btn>
          </v-col>
        </v-row>
      </v-form>
    </template>
  </v-confirm-edit>
</template>

<script setup lang="ts">
import { ThumbnailSize, thumbnailSizeMessages } from '@/types/ThumbnailSize'
import { useIntl } from 'vue-intl'
import type { components } from '@/generated/openapi/komga'

const intl = useIntl()

const { settings, loading = false } = defineProps<{
  settings?: components['schemas']['SettingsDto']
  loading?: boolean
}>()

const emit = defineEmits<{
  updateSettings: [settings: components['schemas']['SettingsUpdateDto']]
}>()

const settingsUpdate = ref<components['schemas']['SettingsUpdateDto']>({
  thumbnailSize: ThumbnailSize.DEFAULT,
  deleteEmptyCollections: false,
  deleteEmptyReadLists: false,
  taskPoolSize: 1,
  rememberMeDurationDays: 365,
  renewRememberMeKey: false,
  serverPort: 25600,
  serverContextPath: '',
  koboProxy: false,
  koboPort: undefined,
})

watch(
  () => settings,
  () => {
    if (settings)
      settingsUpdate.value = {
        thumbnailSize: settings.thumbnailSize,
        deleteEmptyCollections: settings.deleteEmptyCollections,
        deleteEmptyReadLists: settings.deleteEmptyReadLists,
        taskPoolSize: settings.taskPoolSize,
        rememberMeDurationDays: settings.rememberMeDurationDays,
        renewRememberMeKey: false,
        serverPort: settings.serverPort.databaseSource,
        serverContextPath: settings.serverContextPath.databaseSource,
        koboProxy: settings.koboProxy,
        koboPort: settings.koboPort,
      }
  },
  { immediate: true },
)

const formValid = ref<boolean>(false)

function submitForm(callback: () => void) {
  if (formValid.value) {
    callback()
    emit('updateSettings', settingsUpdate.value)
  }
}

const thumbnailSizes = Object.values(ThumbnailSize).map((x) => ({
  title: intl.formatMessage(thumbnailSizeMessages[x]),
  value: x,
}))

const messageRequiresRestart = intl.formatMessage({
  description: 'Server settings: input field hint for settings that require a restart',
  defaultMessage: 'Requires restart to take effect',
  id: '6Kd/YV',
})
const messagePrecedence = intl.formatMessage({
  description: 'Server settings: tooltip shown for configuration items that can be overriden',
  defaultMessage: 'Takes precedence over the configuration file',
  id: 'l7+H/k',
})
</script>
