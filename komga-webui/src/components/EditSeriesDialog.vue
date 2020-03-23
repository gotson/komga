<template>
  <div>
    <v-dialog v-model="modal"
              :fullscreen="$vuetify.breakpoint.xsOnly"
              max-width="800"
              @keydown.esc="dialogCancel"
    >
      <form novalidate>
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
                <v-container fluid>

                  <!--  Title  -->
                  <v-row v-if="single">
                    <v-col cols="12">
                      <v-text-field v-model="form.title"
                                    label="Title"
                                    filled
                                    dense
                                    :error-messages="requiredErrors('title')"
                                    @input="$v.form.title.$touch()"
                                    @blur="$v.form.title.$touch()"
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
                  <v-row v-if="single">
                    <v-col cols="12">
                      <v-text-field v-model="form.titleSort"
                                    label="Sort Title"
                                    filled
                                    dense
                                    :error-messages="requiredErrors('titleSort')"
                                    @input="$v.form.titleSort.$touch()"
                                    @blur="$v.form.titleSort.$touch()"
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
                                filled
                                :placeholder="mixed.status ? 'MIXED' : ''"
                                @input="$v.form.status.$touch()"
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
              </v-card>
            </v-tab-item>

          </v-tabs>

          <v-card-actions class="hidden-xs-only">
            <v-spacer/>
            <v-btn text @click="dialogCancel">Cancel</v-btn>
            <v-btn text class="primary--text" @click="dialogConfirm">Save changes</v-btn>
          </v-card-actions>
        </v-card>
      </form>
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
import Vue from 'vue'
import { SeriesStatus } from '@/types/enum-series'
import { requiredIf } from 'vuelidate/lib/validators'

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
      },
      mixed: {
        status: false
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
  validations: {
    form: {
      title: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        })
      },
      titleSort: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        })
      },
      status: {
        required: requiredIf(function (this: any, model: any) {
          return this.single
        })
      }
    }
  },
  computed: {
    single (): boolean {
      return !Array.isArray(this.series)
    },
    seriesStatus (): any[] {
      return Object.keys(SeriesStatus).map(x => ({
        text: this.$_.capitalize(x),
        value: x
      }))
    },
    dialogTitle (): string {
      return this.single
        ? `Edit ${this.$_.get(this.series, 'metadata.title')}`
        : `Edit ${(this.series as SeriesDto[]).length} series`
    }
  },
  methods: {
    requiredErrors (fieldName: string): string[] {
      const errors = [] as string[]
      const formField = this.$v.form!![fieldName] as any
      if (!formField.$dirty) return errors
      !formField.required && errors.push('Required')
      return errors
    },
    dialogReset (series: SeriesDto | SeriesDto[]) {
      this.$v.$reset()
      if (Array.isArray(series) && series.length === 0) return
      if (Array.isArray(series) && series.length > 0) {
        const status = this.$_.uniq(series.map(x => x.metadata.status))
        this.form.status = status.length > 1 ? '' : status[0]
        this.mixed.status = status.length > 1

        const statusLock = this.$_.uniq(series.map(x => x.metadata.statusLock))
        this.form.statusLock = statusLock.length > 1 ? false : statusLock[0]
      } else {
        this.$_.merge(this.form, (series as SeriesDto).metadata)
      }
    },
    dialogCancel () {
      this.$emit('input', false)
      this.dialogReset(this.series)
    },
    async dialogConfirm () {
      if (await this.editSeries()) {
        this.$emit('input', false)
      }
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    validateForm (): any {
      if (!this.$v.$invalid) {
        const metadata = {
          statusLock: this.form.statusLock
        }

        if (this.$v.form?.status?.$dirty) {
          this.$_.merge(metadata, { status: this.form.status })
        }

        if (this.single) {
          this.$_.merge(metadata, {
            titleLock: this.form.titleLock,
            titleSortLock: this.form.titleSortLock
          })

          if (this.$v.form?.title?.$dirty) {
            this.$_.merge(metadata, { title: this.form.title })
          }

          if (this.$v.form?.titleSort?.$dirty) {
            this.$_.merge(metadata, { titleSort: this.form.titleSort })
          }
        }

        return metadata
      }
      return null
    },
    async editSeries (): Promise<boolean> {
      const metadata = this.validateForm()
      if (metadata) {
        const updated = [] as SeriesDto[]
        const toUpdate = (this.single ? [this.series] : this.series) as SeriesDto[]
        for (const s of toUpdate) {
          try {
            const updatedSeries = await this.$komgaSeries.updateMetadata(s.id, metadata)
            updated.push(updatedSeries)
          } catch (e) {
            this.showSnack(e.message)
            updated.push(s)
          }
        }
        this.$emit('update:series', this.single ? updated[0] : updated)
        return true
      } else return false
    }
  }
})
</script>

<style lang="sass" scoped>
@import '../styles/tabbed-dialog.sass'
</style>
