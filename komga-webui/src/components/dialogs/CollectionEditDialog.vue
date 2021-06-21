<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
    >
      <v-card>
        <v-card-title>{{ $t('dialog.edit_collection.dialog_title') }}</v-card-title>

        <v-card-text>
          <v-container fluid>
            <v-row>
              <v-col>
                <v-text-field v-model="form.name"
                              label="Name"
                              :error-messages="getErrorsName"
                />
              </v-col>
            </v-row>

            <v-row>
              <v-col>
                <div class="text-body-2">{{ $t('dialog.edit_collection.label_ordering') }}</div>
                <v-checkbox
                  v-model="form.ordered"
                  :label="$t('dialog.edit_collection.field_manual_ordering')"
                  hide-details
                />
              </v-col>
            </v-row>

          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer/>
          <v-btn text @click="dialogCancel">{{ $t('dialog.edit_collection.button_cancel') }}</v-btn>
          <v-btn text class="primary--text"
                 @click="dialogConfirm"
                 :disabled="getErrorsName !== ''"
          >{{ $t('dialog.edit_collection.button_confirm') }}
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
import {UserRoles} from '@/types/enum-users'
import Vue from 'vue'

export default Vue.extend({
  name: 'CollectionEditDialog',
  data: () => {
    return {
      UserRoles,
      snackbar: false,
      snackText: '',
      modal: false,
      collections: [] as CollectionDto[],
      form: {
        name: '',
        ordered: false,
      },
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
    async value(val) {
      this.modal = val
      if (val) {
        this.collections = (await this.$komgaCollections.getCollections(undefined, {unpaged: true} as PageRequest)).content
        this.dialogReset(this.collection)
      }
    },
    modal(val) {
      !val && this.dialogCancel()
    },
    collection: {
      handler(val) {
        this.dialogReset(val)
      },
      immediate: true,
    },
  },
  computed: {
    libraries(): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    },
    getErrorsName(): string {
      if (this.form.name === '') return this.$t('common.required').toString()
      if (this.form.name !== this.collection.name && this.collections.some(e => e.name === this.form.name)) {
        return this.$t('dialog.add_to_collection.field_search_create_error').toString()
      }
      return ''
    },
  },
  methods: {
    async dialogReset(collection: CollectionDto) {
      this.form.name = collection.name
      this.form.ordered = collection.ordered
    },
    dialogCancel() {
      this.$emit('input', false)
    },
    dialogConfirm() {
      this.editCollection()
      this.$emit('input', false)
    },
    showSnack(message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async editCollection() {
      try {
        const update = {
          name: this.form.name,
          ordered: this.form.ordered,
        } as CollectionUpdateDto

        await this.$komgaCollections.patchCollection(this.collection.id, update)
      } catch (e) {
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
