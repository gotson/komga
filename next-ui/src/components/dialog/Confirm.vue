<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :max-width="maxWidth"
    :fullscreen="fullscreen"
    :transition="fullscreen ? 'dialog-bottom-transition' : undefined"
    :aria-label="title"
  >
    <template #default="{ isActive }">
      <v-form
        ref="form"
        v-model="formValid"
        :disabled="loading"
        class="fill-height"
        @submit.prevent="submitForm(isActive)"
      >
        <v-card
          :title="title"
          :subtitle="subtitle"
          :loading="loading"
        >
          <template #text>
            <slot name="warning" />
            <slot name="text">
              <FormattedMessage
                :message-descriptor="
                  defineMessage({
                    description: 'Confirmation dialog: default hint to retype validation text',
                    defaultMessage: 'Please type <b>{validateText}</b> to confirm.',
                    id: 'XnidLu',
                  })
                "
                :values="{ validateText: validateText }"
              >
                <template #b="Content">
                  <span class="font-weight-bold">
                    <component :is="Content" />
                  </span>
                </template>
              </FormattedMessage>
            </slot>

            <v-text-field
              :rules="[['sameAsIgnoreCase', validateText]]"
              hide-details
              class="mt-2"
              autofocus
            />
          </template>

          <template #actions>
            <v-spacer />
            <v-btn
              :text="
                $formatMessage({
                  description: 'Confirmation dialog: Cancel button',
                  defaultMessage: 'Cancel',
                  id: 'pENCUD',
                })
              "
              @click="isActive.value = false"
            />
            <v-btn
              :loading="loading"
              :disabled="!formValid"
              :text="okText"
              type="submit"
              variant="elevated"
              rounded="xs"
              color="error"
            />
          </template>
        </v-card>
      </v-form>
    </template>
  </v-dialog>
</template>

<script setup lang="ts">
import { defineMessage } from 'vue-intl'

const showDialog = defineModel<boolean>('dialog', { required: false })
const emit = defineEmits<{
  confirm: []
}>()

const form = ref()
const formValid = ref<boolean>(false)

async function submitForm(isActive: Ref<boolean, boolean>) {
  const { valid } = await form.value.validate()
  if (valid) {
    emit('confirm')
    if (closeOnSave) isActive.value = false
  }
}

export interface DialogConfirmProps {
  title?: string
  subtitle?: string
  okText?: string
  validateText?: string
  maxWidth?: string | number
  activator?: Element | string
  loading?: boolean
  closeOnSave?: boolean
  fullscreen?: boolean
}

const {
  title = undefined,
  subtitle = undefined,
  okText = 'Confirm',
  validateText = 'confirm',
  maxWidth = undefined,
  activator = undefined,
  loading = false,
  closeOnSave = true,
  fullscreen = undefined,
} = defineProps<DialogConfirmProps>()
</script>
