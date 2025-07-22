<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :max-width="maxWidth"
    :fullscreen="fullscreen"
    :transition="fullscreen ? 'dialog-bottom-transition' : undefined"
    :scrollable="scrollable"
  >
    <template #default="{ isActive }">
      <v-confirm-edit
        v-model="record"
        hide-actions
        @save="closeOnSave ? (isActive.value = false) : undefined"
      >
        <template #default="{ model: proxyModel, cancel, save, isPristine }">
          <v-form
            v-model="formValid"
            @submit.prevent="submitForm(save)"
            :disabled="loading"
            class="fill-height"
          >
            <v-card
              :title="title"
              :subtitle="subtitle"
              :loading="loading"
            >
              <template #text>
                <slot
                  name="text"
                  :proxy-model="proxyModel"
                  :cancel="cancel"
                  :save="save"
                  :is-pristine="isPristine"
                />
              </template>

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
const showDialog = defineModel<boolean>('dialog', { required: false })
const record = defineModel<unknown>('record', { required: true })

const formValid = ref<boolean>(false)

function submitForm(callback: () => void) {
  if (formValid.value) callback()
}

export interface DialogConfirmEditProps {
  /**
   * Dialog title
   * @type string
   */
  title?: string
  subtitle?: string
  maxWidth?: string | number
  activator?: Element | string
  loading?: boolean
  closeOnSave?: boolean
  fullscreen?: boolean
  scrollable?: boolean
}

const {
  title = undefined,
  subtitle = undefined,
  maxWidth = undefined,
  activator = undefined,
  loading = false,
  closeOnSave = true,
  fullscreen = undefined,
  scrollable = undefined,
} = defineProps<DialogConfirmEditProps>()
</script>
