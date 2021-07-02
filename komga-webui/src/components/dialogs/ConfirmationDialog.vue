<template>
  <v-dialog v-model="modal"
            max-width="450"
  >
    <v-card>
      <v-card-title>{{ title }}</v-card-title>

      <v-card-text>
        <v-container fluid>
          <v-row>
            <v-col>{{ body }}</v-col>
          </v-row>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer/>
        <v-btn text @click="dialogCancel">{{ buttonCancel || $t('common.cancel') }}</v-btn>
        <v-btn :color="buttonConfirmColor"
               @click="dialogConfirm"
        >{{ buttonConfirm }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'ConfirmationDialog',
  data: () => {
    return {
      modal: false,
    }
  },
  props: {
    value: Boolean,
    title: {
      type: String,
      required: true,
    },
    body: {
      type: String,
      required: true,
    },
    buttonCancel: {
      type: String,
      required: false,
    },
    buttonConfirm: {
      type: String,
      required: true,
    },
    buttonConfirmColor: {
      type: String,
      default: 'primary',
    },
  },
  watch: {
    value(val) {
      this.modal = val
    },
    modal(val) {
      !val && this.dialogCancel()
    },
  },
  methods: {
    dialogCancel() {
      this.$emit('input', false)
    },
    dialogConfirm() {
      this.$emit('confirm')
      this.$emit('input', false)
    },
  },
})
</script>

<style scoped>

</style>
