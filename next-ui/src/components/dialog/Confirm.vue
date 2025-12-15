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
                v-if="mode === 'textinput'"
                :message-descriptor="
                  defineMessage({
                    description: 'Confirmation dialog: default hint to retype validation text',
                    defaultMessage: 'Please type <b>{validateText}</b> to confirm.',
                    id: 'XnidLu',
                  })
                "
                :values="{
                  validateText: validateTextEffective,
                }"
              >
                <template #b="Content">
                  <span class="font-weight-bold">
                    <component :is="Content" />
                  </span>
                </template>
              </FormattedMessage>
            </slot>

            <v-text-field
              v-if="mode === 'textinput'"
              :rules="[['sameAsIgnoreCase', validateTextEffective]]"
              hide-details
              class="mt-2"
              autofocus
            />
            <v-checkbox
              v-if="mode === 'checkbox'"
              :rules="['required']"
              hide-details
              :color="colorEffective"
              :label="
                checkboxLabel ||
                $formatMessage({
                  description: 'Confirmation dialog: default checkbox label',
                  defaultMessage: 'Click to confirm',
                  id: '3rNj7/',
                })
              "
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
              :disabled="mode !== 'click' && !formValid"
              :text="
                okText ||
                $formatMessage({
                  description: 'Confirmation dialog: default confirm button',
                  defaultMessage: 'Confirm',
                  id: 'ddthL2',
                })
              "
              type="submit"
              variant="elevated"
              rounded="xs"
              :color="colorEffective"
            />
          </template>
        </v-card>
      </v-form>
    </template>
  </v-dialog>
</template>

<script setup lang="ts">
import { defineMessage, useIntl } from 'vue-intl'
import type { DialogConfirmProps } from '@/types/dialog'

const intl = useIntl()

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

const validateTextEffective = computed(
  () =>
    validateText ||
    intl.formatMessage({
      description: 'Confirmation dialog: default validation text',
      defaultMessage: 'confirm',
      id: 'j7CGMQ',
    }),
)

const colorEffective = computed(() => color || 'error')

const {
  title = undefined,
  subtitle = undefined,
  okText = undefined,
  validateText = undefined,
  checkboxLabel = undefined,
  mode = 'click',
  color = undefined,
  maxWidth = undefined,
  activator = undefined,
  loading = false,
  closeOnSave = true,
  fullscreen = undefined,
} = defineProps<DialogConfirmProps>()
</script>
