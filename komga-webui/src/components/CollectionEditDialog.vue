<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
    >
      <v-card>
        <v-card-title>Edit collection</v-card-title>

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
                <div class="body-2">By default, series in a collection will be ordered by name. You can enable manual
                  ordering to define your own order.
                </div>
                <v-checkbox
                  v-model="form.ordered"
                  label="Manual ordering"
                  hide-details
                />
              </v-col>
            </v-row>

          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer/>
          <v-btn text @click="dialogCancel">Cancel</v-btn>
          <v-btn text class="primary--text"
                 @click="dialogConfirm"
                 :disabled="getErrorsName !== ''"
          >Save changes
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
      >
        Close
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import { UserRoles } from '@/types/enum-users'
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
    value (val) {
      this.modal = val
    },
    modal (val) {
      !val && this.dialogCancel()
    },
    collection: {
      handler (val) {
        this.dialogReset(val)
      },
      immediate: true,
    },
  },
  computed: {
    libraries (): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    },
    getErrorsName (): string {
      if (this.form.name === '') return 'Name is required'
      if (this.form.name !== this.collection.name && this.collections.some(e => e.name === this.form.name)) {
        return 'A collection with this name already exists'
      }
      return ''
    },
  },
  methods: {
    async dialogReset (collection: CollectionDto) {
      this.form.name = collection.name
      this.form.ordered = collection.ordered
      this.collections = await this.$komgaCollections.getCollections()
    },
    dialogCancel () {
      this.$emit('input', false)
      this.dialogReset(this.collection)
    },
    dialogConfirm () {
      this.editCollection()
      this.$emit('input', false)
      this.dialogReset(this.collection)
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async editCollection () {
      try {
        const update = {
          name: this.form.name,
          ordered: this.form.ordered,
        } as CollectionUpdateDto

        await this.$komgaCollections.patchCollection(this.collection.id, update)
        this.$emit('updated', true)
      } catch (e) {
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
