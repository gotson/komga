<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :max-width="maxWidth"
    :fullscreen="fullscreen"
  >
    <template #default="{ isActive }">
      <v-form
        v-model="formValid"
        @submit.prevent="submitForm(isActive)"
        :disabled="loading"
        class="fill-height"
      >
        <v-card
          :title="title"
          :subtitle="subtitle"
          :loading="loading"
        >
          <template #text>
            <slot name="warning" />
            <slot name="text">
              {{
                $formatMessage(
                  {
                    description: 'Confirmation dialog: default hint to retype validation text',
                    defaultMessage: 'Please type {validateText} to confirm.',
                    id: 'eVoe+D',
                  },
                  {
                    validateText: validateText,
                  },
                )
              }}
            </slot>

            <v-text-field
              :rules="[['sameAs', validateText]]"
              hide-details
              class="mt-2"
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
const showDialog = defineModel<boolean>('dialog', { required: false })
const emit = defineEmits<{
  confirm: []
}>()

const formValid = ref<boolean>(false)

function submitForm(isActive: Ref<boolean, boolean>) {
  if (formValid.value) {
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
