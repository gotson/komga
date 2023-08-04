<template>
  <v-dialog v-model="modal"
            max-width="450"
            scrollable
  >
    <v-card>
      <v-card-title>{{ $t('dialog.add_to_readlist.dialog_title') }}</v-card-title>
      <v-btn icon absolute top right @click="dialogClose">
        <v-icon>mdi-close</v-icon>
      </v-btn>

      <v-divider/>

      <v-card-text style="height: 50%">
        <v-container fluid>

          <v-row align="center">
            <v-col>
              <v-text-field
                v-model="newReadList"
                autofocus
                :label="$t('dialog.add_to_readlist.field_search_create')"
                @keydown.enter="create"
                :error-messages="duplicate"
              />
            </v-col>
            <v-col cols="auto">
              <v-btn
                color="primary"
                @click="create"
                :disabled="newReadList.length === 0 || duplicate !== ''"
              >{{ $t('dialog.add_to_readlist.button_create') }}
              </v-btn>
            </v-col>
          </v-row>

          <v-divider/>

          <v-row v-if="readLists.length !== 0">
            <v-col>
              <v-list elevation="5" v-if="readListsFiltered.length !== 0">
                <div v-for="(c, index) in readListsFiltered"
                     :key="index"
                >
                  <v-list-item @click="addTo(c)"
                               two-line
                  >
                    <v-list-item-content>
                      <v-list-item-title>{{ c.name }}</v-list-item-title>
                      <v-list-item-subtitle>{{
                          $tc('dialog.add_to_readlist.card_readlist_subtitle', c.bookIds.length)
                        }}
                      </v-list-item-subtitle>
                    </v-list-item-content>
                  </v-list-item>
                  <v-divider v-if="index !== readListsFiltered.length-1"/>
                </div>
              </v-list>

              <v-alert
                v-else
                type="info"
                text
              >{{ $t('dialog.add_to_readlist.label_no_matching_readlist') }}
              </v-alert>
            </v-col>
          </v-row>

        </v-container>
      </v-card-text>

    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'
import {ERROR} from '@/types/events'
import {stripAccents} from '@/functions/string'
import {ReadListCreationDto, ReadListDto, ReadListUpdateDto} from '@/types/komga-readlists'

export default Vue.extend({
  name: 'ReadListAddToDialog',
  data: () => {
    return {
      confirmDelete: false,
      modal: false,
      readLists: [] as ReadListDto[],
      newReadList: '',
    }
  },
  props: {
    value: Boolean,
    bookIds: {
      type: [Array as () => string[]],
      required: true,
    },
  },
  watch: {
    async value(val) {
      this.modal = val
      if (val) {
        this.newReadList = ''
        this.readLists = this.$_.orderBy((await this.$komgaReadLists.getReadLists(undefined, {unpaged: true} as PageRequest)).content, ['lastModifiedDate'], ['desc'])
      }
    },
    modal(val) {
      !val && this.dialogClose()
    },
  },
  async mounted() {

  },
  computed: {
    duplicate(): string {
      if (this.newReadList !== '' && this.readLists.some(e => e.name.toLowerCase() === this.newReadList.toLowerCase())) {
        return this.$t('dialog.add_to_readlist.field_search_create_error').toString()
      } else return ''
    },
    readListsFiltered(): ReadListDto[] {
      return this.readLists.filter((x: ReadListDto) => stripAccents(x.name.toLowerCase()).includes(stripAccents(this.newReadList.toLowerCase())))
    },
  },
  methods: {
    dialogClose() {
      this.$emit('input', false)
    },
    async addTo(readList: ReadListDto) {
      const bookIds = this.$_.uniq(readList.bookIds.concat(this.bookIds))

      const toUpdate = {
        bookIds: bookIds,
      } as ReadListUpdateDto

      try {
        await this.$komgaReadLists.patchReadList(readList.id, toUpdate)
        this.dialogClose()
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
    async create() {
      const toCreate = {
        name: this.newReadList,
        bookIds: this.bookIds,
      } as ReadListCreationDto

      try {
        await this.$komgaReadLists.postReadList(toCreate)
        this.dialogClose()
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
  },
})
</script>

<style scoped>

</style>
