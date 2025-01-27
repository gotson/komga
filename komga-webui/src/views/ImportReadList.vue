<template>
  <v-container fluid class="pa-6">
    <v-alert type="info" text class="body-2" dismissible>
      <div v-html="$t('data_import.comicrack_preambule_html')"/>
    </v-alert>
    <v-form v-model="validMatch" ref="formMatch">
      <v-row align="center">
        <v-col cols="12" sm="">
          <v-file-input
            v-model="file"
            :label="$t('data_import.field_file_label')"
            prepend-icon="mdi-file-document-multiple"
            accept=".cbl"
            show-size
            :rules="importRules"
          />
        </v-col>
        <v-col cols="auto">
          <v-btn
            color="primary"
            :disabled="!validMatch || matching"
            :loading="matching"
            @click="matchFile"
          >{{ $t('data_import.button_match') }}
          </v-btn>
        </v-col>
      </v-row>
    </v-form>

    <template v-if="result">
      <v-divider/>

      <v-chip-group v-model="filter" color="primary" mandatory multiple>
        <v-chip filter value="ok">{{ $t('common.ok') }}</v-chip>
        <v-chip filter value="error">{{ $t('common.error') }}</v-chip>
        <v-chip filter value="duplicate">{{ $t('common.duplicate') }}</v-chip>
      </v-chip-group>

      <v-simple-table>
        <thead class="font-weight-medium">
        <tr>
          <td>#</td>
          <td>{{ $t('data_import.requested_series') }}</td>
          <td>{{ $t('data_import.requested_number') }}</td>
          <td>{{ $tc('common.series', 1) }}</td>
          <td>{{ $t('common.book') }}</td>
          <td>
            <v-icon>mdi-alert-circle-outline</v-icon>
          </td>
        </tr>
        </thead>
        <tbody
          v-for="(match, i) in result.requests"
          :key="i"
        >
        <read-list-match-row :request="match.request"
                             :series.sync="series[i]"
                             :book.sync="form.books[i]"
                             :duplicate="isDuplicateBook(form.books[i])"
                             :error="isErrorBook(series[i], form.books[i])"
                             :hidden="shouldHideRow(i)"
        >
          <td>{{ i + 1 }}</td>
        </read-list-match-row>
        </tbody>
      </v-simple-table>

      <form novalidate>
        <v-row align="center" justify="end">
          <v-col cols="3">
            <v-text-field
              :label="$t('dialog.edit_readlist.field_name')"
              v-model="form.name"
              clearable
              :error-messages="nameErrors"
            />
          </v-col>

          <v-col cols="auto">
            <v-checkbox
              :label="$t('dialog.edit_readlist.field_manual_ordering')"
              v-model="form.ordered"
            />
          </v-col>

          <v-col cols="5">
            <v-textarea v-model="form.summary"
                        :label="$t('dialog.edit_readlist.field_summary')"
                        rows="1"
            />
          </v-col>

          <v-spacer/>

          <v-col cols="auto">
            <v-btn :color="creationFinished ? 'success': 'primary'"
                   @click="create(false)"
                   :disabled="$v.$invalid"
            >
              <v-icon left v-if="creationFinished">mdi-check</v-icon>
              {{ $t('common.create') }}
            </v-btn>
          </v-col>
        </v-row>
      </form>
      <confirmation-dialog :title="$t('data_import.dialog_confirmation.title')"
                           :button-confirm="$t('data_import.dialog_confirmation.create')"
                           :body-html="missingDialogText"
                           v-model="modalConfirmation"
                           @confirm="create(true)"
      />
    </template>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {convertErrorCodes} from '@/functions/error-codes'
import {
  ReadListCreationDto,
  ReadListDto,
  ReadListRequestBookMatchBookDto,
  ReadListRequestBookMatchSeriesDto,
  ReadListRequestMatchDto,
} from '@/types/komga-readlists'
import ReadListMatchRow from '@/components/ReadListMatchRow.vue'
import {ERROR, NOTIFICATION, NotificationEvent} from '@/types/events'
import {helpers, required} from 'vuelidate/lib/validators'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'

function validName(this: any, value: string) {
  return !helpers.req(value) || !this.readLists.some((e: ReadListDto) => e.name.toLowerCase() === value.toLowerCase())
}

export default Vue.extend({
  name: 'ImportReadLists',
  components: {ConfirmationDialog, ReadListMatchRow},
  data: () => ({
    file: undefined,
    result: undefined as unknown as ReadListRequestMatchDto | undefined,
    filter: ['ok', 'error', 'duplicate'],
    validMatch: true,
    validCreate: true,
    series: [] as (ReadListRequestBookMatchSeriesDto | undefined)[],
    form: {
      name: '',
      ordered: true,
      summary: '',
      books: [] as (ReadListRequestBookMatchBookDto | undefined)[],
    },
    readLists: [] as ReadListDto[],
    creationFinished: false,
    matching: false,
    modalConfirmation: false,
  }),
  validations: {
    form: {
      name: {required, validName},
      ordered: {},
      summary: {},
      books: {},
    },
  },
  computed: {
    totalCount(): number {
      return this.result!!.requests.length
    },
    matchedCount(): number {
      return this.form.books.filter(Boolean).length
    },
    unmatchedCount(): number {
      return this.totalCount - this.matchedCount
    },
    duplicatesCount(): number {
      return this.form.books.filter(this.isDuplicateBook).length
    },
    missingDialogText(): string {
      let s = ''
      if (this.unmatchedCount > 0)
        s += this.$t('data_import.dialog_confirmation.body', {
          unmatched: this.unmatchedCount,
          total: this.totalCount,
        }).toString()
      if (this.duplicatesCount > 0) {
        if (s !== '') s += '<br/><br/>'
        s += this.$t('data_import.dialog_confirmation.body2', {
          duplicates: this.duplicatesCount,
          total: this.totalCount,
        }).toString()
      }
      return s
    },
    importRules(): any {
      return [
        (value: any) => {
          if (value == null) return false
          return value['size'] < 10_000_000 || this.$t('data_import.size_limit', {size: '10'}).toString()
        },
      ]
    },
    nameErrors(): string[] {
      const errors = [] as string[]
      !this.$v?.form?.name?.required && errors.push(this.$t('common.required').toString())
      !this.$v?.form?.name?.validName && errors.push(this.$t('dialog.add_to_readlist.field_search_create_error').toString())
      return errors
    },
  },
  async mounted() {
    this.readLists = (await this.$komgaReadLists.getReadLists(undefined, {unpaged: true} as PageRequest)).content
  },
  methods: {
    isDuplicateBook(book?: ReadListRequestBookMatchBookDto): boolean {
      if (book == undefined) return false
      return this.form.books.filter((b) => b?.bookId === book?.bookId).length > 1
    },
    isErrorBook(series?: ReadListRequestBookMatchSeriesDto, book?: ReadListRequestBookMatchBookDto): string {
      if (!series) return this.$t('book_import.row.error_choose_series').toString()
      if (!book) return this.$t('readlist_import.row.error_choose_book').toString()
      return ''
    },
    shouldHideRow(i: number): boolean {
      const error = this.isErrorBook(this.series[i], this.form.books[i])
      const duplicate = this.isDuplicateBook(this.form.books[i])
      if (error && !this.filter.includes('error')) return true
      if (!error && duplicate && !this.filter.includes('duplicate')) return true
      if (!error && !duplicate && !this.filter.includes('ok')) return true
      return false
    },
    async matchFile() {
      this.matching = true
      this.result = undefined
      this.filter = ['ok', 'error', 'duplicate']
      this.form.summary = ''
      this.form.ordered = true
      this.form.books = []
      this.series = []
      this.creationFinished = false
      try {
        this.result = await this.$komgaReadLists.postReadListMatch(this.file)
        this.form.name = this.result.readListMatch.name
        this.result.requests.forEach((request, index) => {
          const match = request.matches.find(Boolean)
          this.$set(this.series, index, match?.series)
          this.$set(this.form.books, index, match?.books.find(Boolean))
        })
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: convertErrorCodes(e.message)} as ErrorEvent)
      }
      this.matching = false
    },
    validateForm(): ReadListCreationDto | undefined {
      if (this.$v.$invalid) return undefined
      return {
        name: this.form.name,
        bookIds: [...new Set(this.form.books.map(b => b?.bookId).filter(Boolean))] as string[],
        ordered: this.form.ordered,
        summary: this.form.summary,
      }
    },
    async create(bypassMissing: boolean) {
      if (!this.creationFinished) {
        if (!bypassMissing && (this.duplicatesCount > 0 || this.unmatchedCount > 0)) {
          this.modalConfirmation = true
          return
        }
        const toCreate = this.validateForm()
        if (!toCreate) return

        try {
          const created = await this.$komgaReadLists.postReadList(toCreate)
          this.$eventHub.$emit(NOTIFICATION, {
            message: this.$t('data_import.readlist_created', {name: created.name}).toString(),
            goTo: {
              text: this.$t('common.go_to_readlist'),
              click: () => this.$router.push({name: 'browse-readlist', params: {readListId: created.id}}),
            },
          } as NotificationEvent)
          this.creationFinished = true
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
