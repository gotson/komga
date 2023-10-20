<template>
  <v-dialog v-model="modal"
            max-width="450"
  >
    <v-card>
      <v-card-title>{{ title }}</v-card-title>

      <v-card-text>
        <v-container fluid>
          <v-row>
            <v-col v-if="body && !bodyHtml">{{ body }}</v-col>
            <v-col v-if="bodyHtml" v-html="bodyHtml"/>
          </v-row>

          <v-row v-if="confirmText">
            <v-col>
              <v-checkbox v-model="confirmation" :color="buttonConfirmColor">
                <template v-slot:label>
                  {{ confirmText }}
                </template>
              </v-checkbox>
            </v-col>
          </v-row>
        </v-container>
      </v-card-text>

      <v-card-actions>
        <v-spacer/>
        <v-btn text @click="dialogCancel">{{ buttonCancel || $t('common.cancel') }}</v-btn>
        <v-btn v-if="buttonAlternate"
               text
               @click="dialogAlternate"
               :disabled="confirmText && !confirmation"
        >{{ buttonAlternate }}
        </v-btn>
        <v-btn :color="buttonConfirmColor"
               @click="dialogConfirm"
               :disabled="confirmText && !confirmation"
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
      confirmation: false,
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
      required: false,
    },
    bodyHtml: {
      type: String,
      required: false,
    },
    confirmText: {
      type: String,
      required: false,
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
    buttonAlternate: {
      type: String,
      required: false,
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
      this.confirmation = false
      this.$emit('input', false)
    },
    dialogConfirm() {
      this.$emit('confirm')
      this.$emit('input', false)
    },
    dialogAlternate() {
      this.$emit('alternate')
      this.$emit('input', false)
    },
  },
})
</script>

<style scoped>

</style>
