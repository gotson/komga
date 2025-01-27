<template>
  <div>
    <v-dialog v-model="modal"
              max-width="600"
              scrollable
    >
      <v-card>
        <v-card-title>{{ $t('dialog.filename_chooser.title') }}</v-card-title>
        <v-btn icon absolute top right @click="dialogClose">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-divider/>

        <v-card-text style="height: 50%">
          <v-container fluid>

            <v-row>
              <v-col>
                <div class="text-subtitle-1">{{ $t('dialog.filename_chooser.label_source_filename') }}</div>
                <div style="cursor: pointer" @click="change(existing)">{{ existing }}</div>
              </v-col>
            </v-row>

            <v-row align="center">
              <v-col>
                <v-text-field
                  v-model="nameInternal"
                  autofocus
                  :label="$t('dialog.filename_chooser.field_destination_filename')"
                  @keydown.enter="choose"
                />
              </v-col>
              <v-col cols="auto">
                <v-btn @click="choose"
                       :disabled="!nameInternal"
                >{{ $t('dialog.filename_chooser.button_choose') }}</v-btn>
              </v-col>
            </v-row>

            <v-divider/>

            <v-simple-table
              v-if="books.length > 0"
              fixed-header
              :height="$vuetify.breakpoint.height / 2"
            >
              <thead>
              <tr>
                <th>{{ $t('dialog.filename_chooser.table.order') }}</th>
                <th>{{ $t('dialog.filename_chooser.table.existing_file') }}</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="(b, index) in books"
                  :key="index"
              >
                <td>{{ b.number }}</td>
                <td style="cursor: pointer" @click="change(b.name)">{{ b.name }}</td>
              </tr>
              </tbody>
            </v-simple-table>

          </v-container>
        </v-card-text>

      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'FileNameChooserDialog',
  data: () => {
    return {
      modal: false,
      nameInternal: '',
    }
  },
  props: {
    value: Boolean,
    name: {
      type: String,
      required: true,
    },
    existing: {
      type: String,
      required: false,
    },
    books: {
      type: Array,
      required: true,
    },
  },
  watch: {
    value(val) {
      this.modal = val
      if (val) {
        this.clear()
      }
    },
    existing: {
      handler(val) {
        this.nameInternal = val
      },
      immediate: true,
    },
    modal(val) {
      !val && this.dialogClose()
    },
  },
  methods: {
    clear() {
      this.nameInternal = this.name || this.existing || ''
    },
    change(val: string) {
      this.nameInternal = val
    },
    choose(){
      if(this.nameInternal) {
        this.$emit('update:name', this.nameInternal)
        this.dialogClose()
      }
    },
    dialogClose() {
      this.$emit('input', false)
    },
  },
})
</script>

<style scoped>

</style>
