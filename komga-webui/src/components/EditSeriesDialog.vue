<template>
  <div>
    <v-dialog v-model="modal"
              :fullscreen="$vuetify.breakpoint.xsOnly"
              max-width="800"
              @keydown.esc="dialogCancel"
    >
      <v-card>
        <v-toolbar class="hidden-sm-and-up">
          <v-btn icon @click="dialogCancel">
            <v-icon>mdi-close</v-icon>
          </v-btn>
          <v-toolbar-title>{{ dialogTitle }}</v-toolbar-title>
          <v-spacer/>
          <v-toolbar-items>
            <v-btn text color="primary" @click="dialogConfirm">Save changes</v-btn>
          </v-toolbar-items>
        </v-toolbar>

        <v-card-title class="hidden-xs-only">
          <v-icon class="mr-4">mdi-pencil</v-icon>
          {{ dialogTitle }}
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

                  <!--  Title  -->
                  <v-row v-if="!multiple">
                    <v-col cols="12">
                      <v-text-field v-model="form.title"
                                    label="Title"
                                    @change="form.titleLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.titleLock ? 'secondary' : ''"
                                  @click="form.titleLock = !form.titleLock"
                          >
                            {{ form.titleLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>
                  </v-row>

                  <!--  Sort Title  -->
                  <v-row v-if="!multiple">
                    <v-col cols="12">
                      <v-text-field v-model="form.titleSort"
                                    label="Sort Title"
                                    @change="form.titleSortLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.titleSortLock ? 'secondary' : ''"
                                  @click="form.titleSortLock = !form.titleSortLock"
                          >
                            {{ form.titleSortLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-text-field>
                    </v-col>
                  </v-row>

                  <!--  Status  -->
                  <v-row>
                    <v-col cols="auto">
                      <v-select :items="seriesStatus"
                                v-model="form.status"
                                label="Status"
                                @change="form.statusLock = true"
                      >
                        <template v-slot:prepend>
                          <v-icon :color="form.statusLock ? 'secondary' : ''"
                                  @click="form.statusLock = !form.statusLock"
                          >
                            {{ form.statusLock ? 'mdi-lock' : 'mdi-lock-open' }}
                          </v-icon>
                        </template>
                      </v-select>
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
      form: {
        status: '',
        statusLock: false,
        title: '',
        titleLock: false,
        titleSort: '',
        titleSortLock: false
      }
    }
  },
  props: {
    value: Boolean,
    series: {
      type: [Object as () => SeriesDto, Array as () => SeriesDto[]],
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
    multiple (): boolean {
      return Array.isArray(this.series)
    },
    seriesStatus (): string[] {
      return Object.keys(SeriesStatus).map(x => capitalize(x))
    },
    dialogTitle (): string {
      return this.multiple
        ? `Edit ${(this.series as SeriesDto[]).length} series`
        : `Edit ${this.$_.get(this.series, 'metadata.title')}`
    }
  },
  methods: {
    dialogReset (series: SeriesDto | SeriesDto[]) {
      if (Array.isArray(series) && series.length === 0) return
      if (Array.isArray(series) && series.length > 0) {
        const status = this.$_.uniq(series.map(x => x.metadata.status))
        this.form.status = status.length > 1 ? '' : capitalize(status[0])
        const statusLock = this.$_.uniq(series.map(x => x.metadata.statusLock))
        this.form.statusLock = statusLock.length > 1 ? false : statusLock[0]
      } else {
        const s = series as SeriesDto
        this.form.status = capitalize(s.metadata.status)
        this.form.statusLock = s.metadata.statusLock
        this.form.title = s.metadata.title
        this.form.titleLock = s.metadata.titleLock
        this.form.titleSort = s.metadata.titleSort
        this.form.titleSortLock = s.metadata.titleSortLock
      }
    },
    dialogCancel () {
      this.$emit('input', false)
      this.dialogReset(this.series)
    },
    async dialogConfirm () {
      await this.editSeries()
      this.$emit('input', false)
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async editSeries () {
      const updated = [] as SeriesDto[]
      const toUpdate = (this.multiple ? this.series : [this.series]) as SeriesDto[]
      for (const s of toUpdate) {
        try {
          if (this.form.status === '') {
            return
          }
          const metadata = {
            status: this.form.status.toUpperCase(),
            statusLock: this.form.statusLock
          } as SeriesMetadataUpdateDto

          if (!this.multiple) {
            this.$_.merge(metadata, {
              title: this.form.title,
              titleLock: this.form.titleLock,
              titleSort: this.form.titleSort,
              titleSortLock: this.form.titleSortLock
            })
          }

          const updatedSeries = await this.$komgaSeries.updateMetadata(s.id, metadata)
          updated.push(updatedSeries)
        } catch (e) {
          this.showSnack(e.message)
          updated.push(s)
        }
      }
      this.$emit('update:series', this.multiple ? updated : updated[0])
    }
  }
})
</script>

<style scoped>

</style>
