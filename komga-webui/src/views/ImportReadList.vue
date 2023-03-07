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
            :disabled="!validMatch"
            @click="matchFile"
          >{{ $t('data_import.button_match') }}
          </v-btn>
        </v-col>
      </v-row>
    </v-form>

    <template v-if="result">
      <v-divider/>

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
          v-for="(match, i) in result.matches"
          :key="i"
        >
        <read-list-match-row :match="match" :book-id.sync="form.bookIds[i]"
                             :duplicate="isDuplicateBook(form.bookIds[i])">
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
                   @click="create"
                   :disabled="$v.$invalid"
            >
              <v-icon left v-if="creationFinished">mdi-check</v-icon>
              {{ $t('common.create') }}
            </v-btn>
          </v-col>
        </v-row>
      </form>
    </template>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {convertErrorCodes} from '@/functions/error-codes'
import {ReadListCreationDto, ReadListDto, ReadListRequestMatchDto} from '@/types/komga-readlists'
import ReadListMatchRow from '@/components/ReadListMatchRow.vue'
import {ERROR, NOTIFICATION, NotificationEvent} from '@/types/events'
import {helpers, required} from 'vuelidate/lib/validators'

function validBookIds(this: any, value: string[]) {
  return value.filter(Boolean).length === this.result.matches.length && value.filter(Boolean).length === [...new Set(value)].length
}

function validName(this: any, value: string) {
  return !helpers.req(value) || !this.readLists.some((e: ReadListDto) => e.name.toLowerCase() === value.toLowerCase())
}

export default Vue.extend({
  name: 'ImportReadLists',
  components: {ReadListMatchRow},
  data: () => ({
    convertErrorCodes,
    file: undefined,
    result: undefined as unknown as ReadListRequestMatchDto,
    validMatch: true,
    validCreate: true,
    form: {
      name: '',
      ordered: true,
      summary: '',
      bookIds: [] as string[],
    },
    readLists: [] as ReadListDto[],
    creationFinished: false,
  }),
  validations: {
    form: {
      name: {required, validName},
      ordered: {},
      summary: {},
      bookIds: {validBookIds},
    },
  },
  computed: {
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
    isDuplicateBook(bookId: string): boolean {
      return this.form.bookIds.filter((b) => b === bookId).length > 1
    },
    async matchFile() {
      try {
        this.result = await this.$komgaReadLists.postReadListMatch(this.file)
        this.form.name = this.result.readListMatch.name
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: convertErrorCodes(e.message)} as ErrorEvent)
      }
      this.form.summary = ''
      this.form.ordered = true
      this.form.bookIds = []
      this.creationFinished = false
    },
    validateForm(): ReadListCreationDto | undefined {
      if (this.$v.$invalid) return undefined
      return {
        name: this.form.name,
        bookIds: this.form.bookIds,
        ordered: this.form.ordered,
        summary: this.form.summary,
      }
    },
    async create() {
      if (!this.creationFinished) {
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
