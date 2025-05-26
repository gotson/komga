<template>
  <v-dialog
    v-model="showDialog"
    :activator="activator"
    :max-width="maxWidth"
  >
    <v-form
      v-model="formValid"
      @submit.prevent="submitForm()"
    >
      <v-card
        :title="title"
        :subtitle="subtitle"
      >
        <template #text>
          <slot name="warning" />
          <slot name="text">
            Please type <span class="font-weight-bold">{{ validateText }}</span> to confirm.
          </slot>

          <v-text-field
            :rules="[rules.sameAs(validateText)]"
            hide-details
            class="mt-2"
          />
        </template>

        <template #actions>
          <v-spacer />
          <v-btn
            text="Cancel"
            @click="close()"
          />
          <v-btn
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
  </v-dialog>
</template>

<script setup lang="ts">
import {useRules} from 'vuetify/labs/rules'

const showDialog = defineModel<boolean>('dialog', {required: false})
const emit = defineEmits<{
  confirm: []
}>()

const formValid = ref<boolean>(false)

const rules = useRules()

function submitForm() {
  if(formValid.value) {
    emit('confirm')
    close()
  }
}

export interface Props {
  title?: string,
  subtitle?: string,
  okText?: string,
  validateText?: string,
  maxWidth?: string | number,
  activator?: Element | string,
}

const {
  title = undefined,
  subtitle = undefined,
  okText = 'Confirm',
  validateText = 'confirm',
  maxWidth = undefined,
  activator = undefined,
} = defineProps<Props>()

function close() {
  showDialog.value = false
}
</script>

