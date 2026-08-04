<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :max-width="maxWidth"
    :fullscreen="fullscreen"
    :transition="fullscreen ? 'dialog-bottom-transition' : undefined"
    :scrollable="scrollable"
    :aria-label="title"
  >
    <template #default="{ isActive }">
      <v-confirm-edit
        v-model="record"
        hide-actions
        @save="closeOnSave ? (isActive.value = false) : undefined"
      >
        <template #default="{ model: proxyModel, cancel, save, isPristine }">
          <v-form
            ref="form"
            :disabled="loading"
            class="fill-height"
            @submit.prevent="submitForm(save)"
          >
            <v-card
              :title="title"
              :subtitle="subtitle"
              :loading="loading"
            >
              <v-card-text :class="cardTextClass">
                <slot
                  name="text"
                  :proxy-model="proxyModel"
                  :cancel="cancel"
                  :save="save"
                  :is-pristine="isPristine"
                />
              </v-card-text>

              <template #actions>
                <v-spacer />
                <v-btn
                  :text="
                    $formatMessage({
                      description: 'ConfirmEdit dialog: Cancel button',
                      defaultMessage: 'Cancel',
                      id: 'G/T8/2',
                    })
                  "
                  @click="isActive.value = false"
                />
                <v-btn
                  :text="
                    okText ||
                    $formatMessage({
                      description: 'ConfirmEdit dialog: Save button',
                      defaultMessage: 'Save',
                      id: 'N9WFH4',
                    })
                  "
                  type="submit"
                  :loading="loading"
                />
              </template>
            </v-card>
          </v-form>
        </template>
      </v-confirm-edit>
    </template>
  </v-dialog>
</template>

<script setup lang="ts">
import type { DialogConfirmEditProps } from '@/types/dialog'

const showDialog = defineModel<boolean>('dialog', { required: false })
const record = defineModel<unknown>('record', { required: true })

const form = ref()

async function submitForm(callback: () => void) {
  const { valid } = await form.value.validate()
  if (valid) callback()
}

const {
  title = undefined,
  subtitle = undefined,
  okText = undefined,
  cardTextClass = undefined,
  maxWidth = undefined,
  activator = undefined,
  loading = false,
  closeOnSave = true,
  fullscreen = undefined,
  scrollable = undefined,
} = defineProps<DialogConfirmEditProps>()
</script>
