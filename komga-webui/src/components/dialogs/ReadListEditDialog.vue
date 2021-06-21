<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
    >
      <v-card>
        <v-card-title>{{ $t('dialog.edit_readlist.dialog_title') }}</v-card-title>

        <v-card-text>
          <v-container fluid>
            <v-row>
              <v-col>
                <v-text-field v-model="form.name"
                              :label="$t('dialog.edit_readlist.field_name')"
                              :error-messages="getErrorsName"
                />
              </v-col>
            </v-row>

          </v-container>
        </v-card-text>

        <v-card-actions>
          <v-spacer/>
          <v-btn text @click="dialogCancel">{{ $t('dialog.edit_readlist.button_cancel') }}</v-btn>
          <v-btn text class="primary--text"
                 @click="dialogConfirm"
                 :disabled="getErrorsName !== ''"
          >{{ $t('dialog.edit_readlist.button_confirm') }}</v-btn>
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
      >{{ $t('common.close') }}</v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import {UserRoles} from '@/types/enum-users'
import Vue from 'vue'

export default Vue.extend({
  name: 'ReadListEditDialog',
  data: () => {
    return {
      UserRoles,
      snackbar: false,
      snackText: '',
      modal: false,
      readLists: [] as ReadListDto[],
      form: {
        name: '',
      },
    }
  },
  props: {
    value: Boolean,
    readList: {
      type: Object as () => ReadListDto,
      required: true,
    },
  },
  watch: {
    async value (val) {
      this.modal = val
      if (val) {
        this.readLists = (await this.$komgaReadLists.getReadLists(undefined, { unpaged: true } as PageRequest)).content
        this.dialogReset(this.readList)
      }
    },
    modal (val) {
      !val && this.dialogCancel()
    },
    readList: {
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
      if (this.form.name === '') return this.$t('common.required').toString()
      if (this.form.name !== this.readList.name && this.readLists.some(e => e.name === this.form.name)) {
        return this.$t('dialog.add_to_readlist.field_search_create_error').toString()
      }
      return ''
    },
  },
  methods: {
    async dialogReset (readList: ReadListDto) {
      this.form.name = readList.name
    },
    dialogCancel () {
      this.$emit('input', false)
    },
    dialogConfirm () {
      this.edit()
      this.$emit('input', false)
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async edit () {
      try {
        const update = {
          name: this.form.name,
        } as ReadListUpdateDto

        await this.$komgaReadLists.patchReadList(this.readList.id, update)
      } catch (e) {
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
