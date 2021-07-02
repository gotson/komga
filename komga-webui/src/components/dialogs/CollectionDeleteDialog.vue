<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
    >
      <v-card>
        <v-card-title v-if="single">{{ $t('dialog.delete_collection.dialog_title') }}</v-card-title>
        <v-card-title v-else>{{ $t('dialog.delete_collection.dialog_title_multiple') }}</v-card-title>

        <v-card-text>
          <v-container fluid>
            <v-row>
              <v-col
                v-if="single"
                v-html="$t('dialog.delete_collection.warning_html', { name: collections.name})"
              />
              <v-col
                v-else
                v-html="$t('dialog.delete_collection.warning_multiple_html', { count: collections.length})"
              />
            </v-row>

            <v-row>
              <v-col>
                <v-checkbox v-model="confirmDelete" color="red">
                  <template v-slot:label v-if="single">
                    {{ $t('dialog.delete_collection.confirm_delete', {name: collections.name}) }}
                  </template>
                  <template v-slot:label v-else>
                    {{ $t('dialog.delete_collection.confirm_delete_multiple', {count: collections.length}) }}
                  </template>
                </v-checkbox>
              </v-col>
            </v-row>
          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer/>
          <v-btn text @click="dialogCancel">{{ $t('dialog.delete_collection.button_cancel') }}</v-btn>
          <v-btn color="error"
                 @click="dialogConfirm"
                 :disabled="!confirmDelete"
          >{{ $t('dialog.delete_collection.button_confirm') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>

  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {ERROR} from "@/types/events";

export default Vue.extend({
  name: 'CollectionDeleteDialog',
  data: () => {
    return {
      confirmDelete: false,
      modal: false,
    }
  },
  props: {
    value: Boolean,
    collections: {
      type: [Object as () => CollectionDto, Array as () => CollectionDto[]],
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
  computed: {
    single(): boolean {
      return !Array.isArray(this.collections)
    },
  },
  methods: {
    dialogCancel() {
      this.$emit('input', false)
      this.confirmDelete = false
    },
    dialogConfirm() {
      this.deleteCollections()
      this.$emit('input', false)
    },
    async deleteCollections() {
      const toUpdate = (this.single ? [this.collections] : this.collections) as CollectionDto[]
      for (const b of toUpdate) {
        try {
          await this.$komgaCollections.deleteCollection(b.id)
        } catch (e) {
          this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
        }
      }
    },
  },
})
</script>

<style scoped>

</style>
