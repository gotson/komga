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
        <v-card
          :title="title"
          :subtitle="subtitle"
        >
          <template #text>
            <v-form v-model="formValid">
              <slot
                name="text"
                :proxy-model="proxyModel"
                :cancel="cancel"
                :save="save"
                :is-pristine="isPristine"
              />
            </v-form>
          </template>

          <template #actions>
            <v-spacer />
            <v-btn
              text="Cancel"
              @click="close()"
            />
            <v-btn
              :disabled="!formValid"
              text="Save"
              @click="save"
            />
          </template>
        </v-card>
      </template>
    </v-confirm-edit>
  </v-dialog>
</template>

<script setup lang="ts">
const showDialog = defineModel<boolean>('dialog', {required: false})
const record = defineModel<unknown>('record', {required: true})

const formValid = ref<boolean>(false)

export interface Props {
  title?: string,
  subtitle?: string,
  maxWidth?: string | number,
  activator?: Element | string,
}

const {
  title = undefined,
  subtitle = undefined,
  maxWidth = undefined,
  activator = undefined,
} = defineProps<Props>()

function close() {
  showDialog.value = false
}
</script>

