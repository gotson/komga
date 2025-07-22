<template>
  <v-dialog
    ref="dialogRef"
    v-model="showDialog"
    :activator="activator"
    max-width="600px"
    @after-leave="reset()"
  >
    <template #default="{ isActive }">
      <v-form @submit.prevent="generateApiKey()">
        <v-card
          :title="
            $formatMessage({
              description: 'Generate API key dialog: title',
              defaultMessage: 'Generate new API key',
              id: 'ycrpqO',
            })
          "
          :loading="isLoading"
        >
          <v-card-text>
            <div
              v-if="!createdKey"
              class="d-flex flex-column ga-6"
            >
              <div>
                {{
                  $formatMessage({
                    description: 'Generate API key dialog: description',
                    defaultMessage:
                      'API Keys can be used to authenticate through the Kobo Sync protocol or the REST API.',
                    id: 'iin6d2',
                  })
                }}
              </div>

              <v-text-field
                v-model="comment"
                :label="
                  $formatMessage({
                    description: 'Generate API key dialog: input field label',
                    defaultMessage: 'Comment',
                    id: 'C9LkYh',
                  })
                "
                :hint="
                  $formatMessage({
                    description: 'Generate API key dialog: input field hint',
                    defaultMessage: 'What\'s this API key for?',
                    id: 'oWsqnh',
                  })
                "
                :rules="['required']"
                :error-messages="creationError"
                @update:modelValue="creationError = ''"
                :disabled="isLoading || !!createdKey"
                autofocus
              />
            </div>

            <v-fade-transition>
              <div
                v-if="!!createdKey"
                class="d-flex flex-column ga-6"
              >
                <div>
                  <v-alert type="info"
                    >{{
                      $formatMessage({
                        description: 'Generate API key dialog: message shown after key creation',
                        defaultMessage:
                          "Make sure to copy your API key now. You won't be able to see it again!",
                        id: 'X/Z8x+',
                      })
                    }}
                  </v-alert>
                </div>

                <div>
                  <v-text-field
                    readonly
                    :label="createdKey.comment"
                    v-model="createdKey.key"
                  >
                    <template
                      #append-inner
                      v-if="clipboardSupported"
                    >
                      <v-fab-transition>
                        <v-icon
                          v-if="copied"
                          icon="i-mdi:check"
                          color="success"
                        />
                        <v-icon
                          v-else
                          icon="i-mdi:content-copy"
                          @click="copy(createdKey.key)"
                        />
                      </v-fab-transition>
                    </template>
                  </v-text-field>
                </div>
              </div>
            </v-fade-transition>
          </v-card-text>

          <v-card-actions>
            <v-btn
              :text="
                $formatMessage({
                  description: 'Generate API key dialog: close button',
                  defaultMessage: 'Close',
                  id: 'HZqgan',
                })
              "
              @click="isActive.value = false"
            />

            <v-btn
              :text="
                $formatMessage({
                  description: 'Generate API key dialog: generate button',
                  defaultMessage: 'Generate',
                  id: 'VP+GhR',
                })
              "
              type="submit"
              :disabled="isLoading || !!createdKey"
            />
          </v-card-actions>
        </v-card>
      </v-form>
    </template>
  </v-dialog>
</template>

<script setup lang="ts">
import { useIntl } from 'vue-intl'
import { useCreateApiKey } from '@/colada/users'
import type { ErrorCause } from '@/api/komga-client'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useMessagesStore } from '@/stores/messages'
import type { components } from '@/generated/openapi/komga'
import { useClipboard } from '@vueuse/core'
import type { VDialog } from 'vuetify/components'

const intl = useIntl()
const messagesStore = useMessagesStore()
const { isSupported: clipboardSupported, copy, copied } = useClipboard({ copiedDuring: 3000 })

const showDialog = defineModel<boolean>('dialog', { required: false })
const activator = defineModel<Element | string>('activator', { required: false })

const comment = ref<string>('')
const creationError = ref<string>('')
const createdKey = ref<components['schemas']['ApiKeyDto'] | undefined>(undefined)

const { mutateAsync, isLoading } = useCreateApiKey()

function generateApiKey() {
  mutateAsync({ comment: comment.value })
    .then((key) => (createdKey.value = key))
    .catch((error) => {
      console.dir(error)
      if ((error?.cause as ErrorCause)?.message?.includes('ERR_1034')) {
        creationError.value = intl.formatMessage({
          description:
            'API Key generate dialog: error message displayed when an API key with the same comment already exists',
          defaultMessage: 'An API key with that comment already exists',
          id: 'zphiTI',
        })
      } else
        messagesStore.messages.push({
          text:
            (error?.cause as ErrorCause)?.message ||
            intl.formatMessage(commonMessages.networkError),
        })
    })
}

function reset() {
  createdKey.value = undefined
  comment.value = ''
  creationError.value = ''
}
</script>
