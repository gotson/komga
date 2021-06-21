<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
    >
      <v-card>
        <v-card-title>{{ $t('dialog.delete_collection.dialog_title') }}</v-card-title>

        <v-card-text>
          <v-container fluid>
            <v-row>
              <v-col v-html="$t('dialog.delete_collection.warning_html', { name: collection.name})"></v-col>
            </v-row>

            <v-row>
              <v-col>
                <v-checkbox v-model="confirmDelete" color="red">
                  <template v-slot:label>
                    {{ $t('dialog.delete_collection.confirm_delete', {name: collection.name}) }}
                  </template>
                </v-checkbox>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer/>
          <v-btn text @click="dialogCancel">{{ $t('dialog.delete_collection.button_cancel') }}</v-btn>
          <v-btn text color="error"
                 @click="dialogConfirm"
                 :disabled="!confirmDelete"
          >{{ $t('dialog.delete_collection.button_confirm') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

    <v-snackbar
      v-model="snackbar"
      bottom
      color="error"
    >
      {{ snackText }}
      <v-btn
        text
        @click="snackbar = false"
      >{{ $t('common.close') }}
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'CollectionDeleteDialog',
  data: () => {
    return {
      confirmDelete: false,
      snackbar: false,
      snackText: '',
      modal: false,
    }
  },
  props: {
    value: Boolean,
    collection: {
      type: Object as () => CollectionDto,
      required: true,
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
      this.confirmDelete = false
    },
    dialogConfirm() {
      this.deleteCollection()
      this.$emit('input', false)
    },
    showSnack(message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async deleteCollection() {
      try {
        await this.$komgaCollections.deleteCollection(this.collection.id)
      } catch (e) {
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
