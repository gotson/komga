<template>
  <v-container fluid class="pa-6">
    <v-alert type="info" text class="body-2">
      <div v-html="$t('data_import.comicrack_preambule_html')"/>
    </v-alert>
    <v-form v-model="valid" ref="form">
      <v-row align="center">
        <v-col cols="12" sm="">
          <v-file-input
            v-model="files"
            :label="$t('data_import.field_files_label')"
            multiple
            prepend-icon="mdi-file-document-multiple"
            accept=".cbl"
            show-size
            :rules="rules"
          />
        </v-col>
        <v-col cols="auto">
          <v-btn
            color="primary"
            :disabled="!valid"
            @click="importFiles"
          >{{ $t('data_import.button_import') }}
          </v-btn>
        </v-col>
      </v-row>
    </v-form>

    <template v-if="results.length > 0">
      <div class="mb-4 body-2">{{ $t('data_import.results_preambule') }}</div>

      <v-expansion-panels>
        <v-expansion-panel
          v-for="result in results"
          :key="result.requestName"
        >
          <v-expansion-panel-header>
            <v-row no-gutters align="center">
              <v-col cols="1">
                <v-icon v-if="result.readList === null" color="error">mdi-alert</v-icon>
                <v-icon v-if="result.readList && result.unmatchedBooks.length === 0" color="success">mdi-check</v-icon>
                <v-icon v-if="result.readList && result.unmatchedBooks.length > 0" color="warning">mdi-alert-circle
                </v-icon>
              </v-col>
              <v-col cols="3">
                {{ result.requestName }}
              </v-col>
              <v-col
                cols="8"
                class="text--secondary"
              >
                <template v-if="result.readList">{{
                    $t('data_import.imported_as', {name: result.readList.name})
                  }}
                </template>
                <template v-else>{{ convertErrorCodes(result.errorCode) }}</template>
              </v-col>
            </v-row>
          </v-expansion-panel-header>
          <v-expansion-panel-content v-if="result.unmatchedBooks.length > 0">
            <v-list elevation="1" dense>
              <v-list-item
                v-for="(book, i) in result.unmatchedBooks"
                :key="i"
                three-line
              >
                <v-list-item-content>
                  <v-list-item-title>{{ convertErrorCodes(book.errorCode) }}</v-list-item-title>
                  <v-list-item-subtitle>{{
                      $t('data_import.book_series', {name: book.book.series})
                    }}
                  </v-list-item-subtitle>
                  <v-list-item-subtitle>{{
                      $t('data_import.book_number', {name: book.book.number})
                    }}
                  </v-list-item-subtitle>
                </v-list-item-content>
              </v-list-item>
            </v-list>
          </v-expansion-panel-content>
        </v-expansion-panel>
      </v-expansion-panels>
    </template>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {convertErrorCodes} from '@/functions/error-codes'

export default Vue.extend({
  name: 'ImportReadLists',
  data: () => ({
    convertErrorCodes,
    files: [],
    results: [] as ReadListRequestResultDto[],
    valid: true,
  }),
  computed: {
    rules(): any {
      return [
        (value: any) => !value || value.reduce((a: any, b: any) => a + (b['size'] || 0), 0) < 10_000_000 || this.$t('data_import.size_limit', {size: '10'}).toString(),
      ]
    },
  },
  methods: {
    async importFiles() {
      this.results.splice(0, this.results.length, ...(await this.$komgaReadLists.postReadListImport(this.files)));
      (this.$refs.form as any).reset()
    },

  },
})
</script>

<style scoped>

</style>
