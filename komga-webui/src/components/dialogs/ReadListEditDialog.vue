<template>
  <v-dialog v-model="modal"
            :fullscreen="$vuetify.breakpoint.xsOnly"
            max-width="800"
            @keydown.esc="dialogCancel"
  >
    <form novalidate>
      <v-card>
        <v-card-title class="hidden-xs-only">
          <v-icon class="mx-4">mdi-pencil</v-icon>
          {{ $t('dialog.edit_readlist.dialog_title') }}
        </v-card-title>

        <v-tabs :vertical="$vuetify.breakpoint.smAndUp" v-model="tab">
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-format-align-center</v-icon>
            {{ $t('dialog.edit_readlist.tab_general') }}
          </v-tab>
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-image</v-icon>
            {{ $t('dialog.edit_readlist.tab_poster') }}
          </v-tab>

          <!--  Tab: General  -->
          <v-tab-item>
            <v-card flat>
              <v-container fluid>
                <!--  Name  -->
                <v-row>
                  <v-col>
                    <v-text-field v-model="form.name"
                                  :label="$t('dialog.edit_readlist.field_name')"
                                  :error-messages="getErrorsName"
                                  filled
                    />
                  </v-col>
                </v-row>

                <!--  Summary  -->
                <v-row>
                  <v-col>
                    <v-textarea v-model="form.summary"
                                :label="$t('dialog.edit_readlist.field_summary')"
                                filled
                    />
                  </v-col>
                </v-row>

                <v-row>
                  <v-col>
                    <div class="text-body-2">{{ $t('dialog.edit_readlist.label_ordering') }}</div>
                    <v-checkbox
                      v-model="form.ordered"
                      :label="$t('dialog.edit_readlist.field_manual_ordering')"
                      hide-details
                    />
                  </v-col>
                </v-row>

              </v-container>
            </v-card>
          </v-tab-item>

          <!--  Tab: Thumbnails  -->
          <v-tab-item>
            <v-card flat>
              <v-container fluid>
                <!-- Upload -->
                <v-row>
                  <v-col class="pa-1">
                    <drop-zone ref="thumbnailsUpload" @on-input-change="addThumbnail" class="pa-8"/>
                  </v-col>
                </v-row>

                <!-- Gallery -->
                <v-row>
                  <v-col
                    cols="6" sm="4" lg="3" class="pa-1"
                    v-for="(item, index) in [...poster.uploadQueue, ...poster.readListThumbnails]"
                    :key="index"
                  >
                    <thumbnail-card
                      :item="item"
                      :selected="isThumbnailSelected(item)"
                      :toBeDeleted="isThumbnailToBeDeleted(item)"
                      @on-select-thumbnail="selectThumbnail"
                      @on-delete-thumbnail="deleteThumbnail"
                    />
                  </v-col>
                </v-row>

              </v-container>
            </v-card>
          </v-tab-item>

        </v-tabs>

        <v-card-actions>
          <v-spacer/>
          <v-btn text @click="dialogCancel">{{ $t('dialog.edit_readlist.button_cancel') }}</v-btn>
          <v-btn color="primary"
                 @click="dialogConfirm"
                 :disabled="getErrorsName !== ''"
          >{{ $t('dialog.edit_readlist.button_confirm') }}
          </v-btn>
        </v-card-actions>
      </v-card>
    </form>
  </v-dialog>
</template>

<script lang="ts">
import {UserRoles} from '@/types/enum-users'
import Vue from 'vue'
import {ERROR, ErrorEvent} from '@/types/events'
import DropZone from '@/components/DropZone.vue'
import ThumbnailCard from '@/components/ThumbnailCard.vue'
import {ReadListDto, ReadListThumbnailDto, ReadListUpdateDto} from '@/types/komga-readlists'

export default Vue.extend({
  name: 'ReadListEditDialog',
  components: {ThumbnailCard, DropZone},
  data: () => {
    return {
      UserRoles,
      modal: false,
      tab: 0,
      readLists: [] as ReadListDto[],
      form: {
        name: '',
        summary: '',
        ordered: true,
      },
      poster: {
        selectedThumbnail: '',
        uploadQueue: [] as File[],
        deleteQueue: [] as ReadListThumbnailDto[],
        readListThumbnails: [] as ReadListThumbnailDto[],
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
    async value(val) {
      this.modal = val
      if (val) {
        this.readLists = (await this.$komgaReadLists.getReadLists(undefined, {unpaged: true} as PageRequest)).content
        this.dialogReset(this.readList)
      }
    },
    modal(val) {
      !val && this.dialogCancel()
      val && this.getThumbnails(this.readList)
    },
    readList: {
      handler(val) {
        this.dialogReset(val)
      },
      immediate: true,
    },
  },
  computed: {
    getErrorsName(): string {
      if (this.form.name === '') return this.$t('common.required').toString()
      if (this.form.name?.toLowerCase() !== this.readList.name?.toLowerCase() && this.readLists.some(e => e.name.toLowerCase() === this.form.name.toLowerCase())) {
        return this.$t('dialog.add_to_readlist.field_search_create_error').toString()
      }
      return ''
    },
  },
  methods: {
    async dialogReset(readList: ReadListDto) {
      this.tab = 0
      this.form.name = readList.name
      this.form.summary = readList.summary
      this.form.ordered = readList.ordered

      this.poster.selectedThumbnail = ''
      this.poster.deleteQueue = []
      this.poster.uploadQueue = []
      this.poster.readListThumbnails = []
    },
    dialogCancel() {
      this.$emit('input', false)
    },
    dialogConfirm() {
      this.edit()
      this.$emit('input', false)
    },
    async edit() {
      try {
        if (this.poster.uploadQueue.length > 0) {
          let hadErrors = false
          for (const file of this.poster.uploadQueue.slice()) {
            try {
              await this.$komgaReadLists.uploadThumbnail(this.readList.id, file, file.name === this.poster.selectedThumbnail)
              this.deleteThumbnail(file)
            } catch (e) {
              this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
              hadErrors = true
            }
          }
          if (hadErrors) {
            await this.getThumbnails(this.readList)
            return false
          }
        }

        if (this.poster.selectedThumbnail !== '') {
          const id = this.poster.selectedThumbnail
          if (this.poster.readListThumbnails.find(value => value.id === id)) {
            await this.$komgaReadLists.markThumbnailAsSelected(this.readList.id, id)
          }
        }

        if (this.poster.deleteQueue.length > 0) {
          this.poster.deleteQueue.forEach(toDelete =>  this.$komgaReadLists.deleteThumbnail(toDelete.readListId, toDelete.id))
        }

        const update = {
          name: this.form.name,
          summary: this.form.summary,
          ordered: this.form.ordered,
        } as ReadListUpdateDto

        await this.$komgaReadLists.patchReadList(this.readList.id, update)
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
    addThumbnail(files: File[]) {
      let hasSelected = false
      for (const file of files) {
        if (!this.poster.uploadQueue.find(value => value.name === file.name)) {
          this.poster.uploadQueue.push(file)
          if (!hasSelected) {
            this.selectThumbnail(file)
            hasSelected = true
          }
        }
      }

      (this.$refs.thumbnailsUpload as any).reset()
    },
    async getThumbnails(readList: ReadListDto) {
      const thumbnails = await this.$komgaReadLists.getThumbnails(readList.id)

      this.selectThumbnail(thumbnails.find(x => x.selected))

      this.poster.readListThumbnails = thumbnails
    },
    isThumbnailSelected(item: File | ReadListThumbnailDto): boolean {
      return item instanceof File ? item.name === this.poster.selectedThumbnail : item.id === this.poster.selectedThumbnail
    },
    selectThumbnail(item: File | ReadListThumbnailDto | undefined) {
      if (!item) {
        return
      } else if (item instanceof File) {
        this.poster.selectedThumbnail = item.name
      } else {
        const index = this.poster.deleteQueue.indexOf(item, 0)
        if (index > -1) this.poster.deleteQueue.splice(index, 1)

        this.poster.selectedThumbnail = item.id
      }
    },
    isThumbnailToBeDeleted(item: File | ReadListThumbnailDto) {
      if (item instanceof File) {
        return false
      } else {
        return this.poster.deleteQueue.includes(item)
      }
    },
    deleteThumbnail(item: File | ReadListThumbnailDto) {
      if (item instanceof File) {
        const index = this.poster.uploadQueue.indexOf(item, 0)
        if (index > -1) {
          this.poster.uploadQueue.splice(index, 1)
        }
        if (item.name === this.poster.selectedThumbnail) {
          this.poster.selectedThumbnail = ''
        }
      } else {
        // if thumbnail was marked for deletion, unmark it
        if (this.isThumbnailToBeDeleted(item)) {
          const index = this.poster.deleteQueue.indexOf(item, 0)
          if (index > -1) {
            this.poster.deleteQueue.splice(index, 1)
          }
        } else {
          this.poster.deleteQueue.push(item)
          if (item.id === this.poster.selectedThumbnail) this.poster.selectedThumbnail = ''
        }
      }
    },
  },
})
</script>

<style scoped>

</style>
