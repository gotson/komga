<template>
  <div>
    <v-dialog v-model="modal"
              :fullscreen="$vuetify.breakpoint.xsOnly"
              max-width="800"
    >
      <v-card>
        <v-toolbar class="hidden-sm-and-up">
          <v-btn icon @click="dialogCancel">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title>Edit {{ series.name }}</v-toolbar-title>
          <v-spacer/>
          <v-toolbar-items>
            <v-btn text color="primary" @click="dialogConfirm">Save changes</v-btn>
          </v-toolbar-items>
        </v-toolbar>

        <v-card-title class="hidden-xs-only">
          <v-icon class="mr-4">mdi-pencil</v-icon>
          Edit {{ series.name }}
        </v-card-title>

        <v-tabs :vertical="$vuetify.breakpoint.smAndUp">
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-format-align-center</v-icon>
            General
          </v-tab>

          <!--  General  -->
          <v-tab-item>
            <v-card flat>
              <form novalidate>
                <v-container fluid>
                  <v-row>
                    <v-col cols="auto">
                      <v-select
                        :items="seriesStatus"
                        v-model="form.status"
                        label="Status"
                      />
                    </v-col>
                  </v-row>
                </v-container>
              </form>
            </v-card>
          </v-tab-item>

        </v-tabs>

        <v-card-actions class="hidden-xs-only">
          <v-spacer/>
          <v-btn text @click="dialogCancel">Cancel</v-btn>
          <v-btn text class="primary--text" @click="dialogConfirm">Save changes</v-btn>
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
import { capitalize } from '@/functions/text'
import { SeriesStatus } from '@/types/common'
import Vue from 'vue'

export default Vue.extend({
  name: 'EditSeriesDialog',
  data: () => {
    return {
      modal: false,
      snackbar: false,
      snackText: '',
      seriesStatus: Object.keys(SeriesStatus).map(x => capitalize(x)),
      form: {
        status: ''
      }
    }
  },
  props: {
    value: Boolean,
    series: {
      type: Object,
      required: true
    }
  },
  watch: {
    value (val) {
      this.modal = val
    },
    modal (val) {
      !val && this.dialogCancel()
    },
    series (val) {
      this.dialogReset(val)
    }
  },
  computed: {
    libraries (): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    }
  },
  methods: {
    dialogReset (series: SeriesDto) {
      this.form.status = capitalize(series.metadata.status)
    },
    dialogCancel () {
      this.$emit('input', false)
      this.dialogReset(this.series)
    },
    dialogConfirm () {
      this.editSeries()
      this.$emit('input', false)
      this.dialogReset(this.series)
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async editSeries () {
      try {
        const metadata = {
          status: this.form.status.toUpperCase()
        } as SeriesMetadataUpdateDto

        await this.$komgaSeries.updateMetadata(this.series.id, metadata)
      } catch (e) {
        this.showSnack(e.message)
      }
    }
  }
})
</script>

<style scoped>

</style>
