<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :max-width="maxWidth"
  >
    <v-confirm-edit
      v-model="record"
      hide-actions
      @save="close()"
    >
      <template #default="{ model: proxyModel, cancel, save, isPristine }">
        <v-form
          v-model="formValid"
          @submit.prevent="submitForm(save)"
        >
          <v-card
            :title="title"
            :subtitle="subtitle"
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
                @click="close()"
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
              />
            </template>
          </v-card>
        </v-form>
      </template>
    </v-confirm-edit>
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
}

const {
  title = undefined,
  subtitle = undefined,
  maxWidth = undefined,
  activator = undefined,
} = defineProps<DialogConfirmEditProps>()

function close() {
  showDialog.value = false
}
</script>
