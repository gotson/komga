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
          {{ $t('dialog.edit_collection.dialog_title') }}
        </v-card-title>

        <v-tabs :vertical="$vuetify.breakpoint.smAndUp" v-model="tab">
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-format-align-center</v-icon>
            {{ $t('dialog.edit_collection.tab_general') }}
          </v-tab>
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-image</v-icon>
            {{ $t('dialog.edit_collection.tab_poster') }}
          </v-tab>

          <!--  Tab: General  -->
          <v-tab-item>
            <v-card flat>
              <v-container fluid>
                <v-row>
                  <v-col>
                    <v-text-field v-model="form.name"
                                  label="Name"
                                  :error-messages="getErrorsName"
                    />
                  </v-col>
                </v-row>

                <v-row>
                  <v-col>
                    <div class="text-body-2">{{ $t('dialog.edit_collection.label_ordering') }}</div>
                    <v-checkbox
                      v-model="form.ordered"
                      :label="$t('dialog.edit_collection.field_manual_ordering')"
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
                    v-for="(item, index) in [...poster.uploadQueue, ...poster.collectionThumbnails]"
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
          <v-btn text @click="dialogCancel">{{ $t('dialog.edit_collection.button_cancel') }}</v-btn>
          <v-btn color="primary"
                 @click="dialogConfirm"
                 :disabled="getErrorsName !== ''"
          >{{ $t('dialog.edit_collection.button_confirm') }}
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
import ThumbnailCard from '@/components/ThumbnailCard.vue'
import DropZone from '@/components/DropZone.vue'

export default Vue.extend({
  name: 'CollectionEditDialog',
  components: {ThumbnailCard, DropZone},
  data: () => {
    return {
      UserRoles,
      modal: false,
      tab: 0,
      collections: [] as CollectionDto[],
      form: {
        name: '',
        ordered: false,
      },
      poster: {
        selectedThumbnail: '',
        uploadQueue: [] as File[],
        deleteQueue: [] as CollectionThumbnailDto[],
        collectionThumbnails: [] as CollectionThumbnailDto[],
      },
    }
  },
  props: {
    value: Boolean,
    collection: {
      type: Object as () => CollectionDto,
      required: true,
    },
  },
  watch: {
    async value(val) {
      this.modal = val
      if (val) {
        this.collections = (await this.$komgaCollections.getCollections(undefined, {unpaged: true} as PageRequest)).content
        this.dialogReset(this.collection)
      }
    },
    modal(val) {
      !val && this.dialogCancel()
      val && this.getThumbnails(this.collection)
    },
    collection: {
      handler(val) {
        this.dialogReset(val)
      },
      immediate: true,
    },
  },
  computed: {
    getErrorsName(): string {
      if (this.form.name === '') return this.$t('common.required').toString()
      if (this.form.name?.toLowerCase() !== this.collection.name?.toLowerCase() && this.collections.some(e => e.name.toLowerCase() === this.form.name.toLowerCase())) {
        return this.$t('dialog.add_to_collection.field_search_create_error').toString()
      }
      return ''
    },
  },
  methods: {
    async dialogReset(collection: CollectionDto) {
      this.tab = 0
      this.form.name = collection.name
      this.form.ordered = collection.ordered

      this.poster.selectedThumbnail = ''
      this.poster.deleteQueue = []
      this.poster.uploadQueue = []
      this.poster.collectionThumbnails = []
    },
    dialogCancel() {
      this.$emit('input', false)
    },
    dialogConfirm() {
      this.editCollection()
      this.$emit('input', false)
    },
    async editCollection() {
      try {
        if (this.poster.uploadQueue.length > 0) {
          let hadErrors = false
          for (const file of this.poster.uploadQueue.slice()) {
            try {
              await this.$komgaCollections.uploadThumbnail(this.collection.id, file, file.name === this.poster.selectedThumbnail)
              this.deleteThumbnail(file)
            } catch (e) {
              this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
              hadErrors = true
            }
          }
          if (hadErrors) {
            await this.getThumbnails(this.collection)
            return false
          }
        }

        if (this.poster.selectedThumbnail !== '') {
          const id = this.poster.selectedThumbnail
          if (this.poster.collectionThumbnails.find(value => value.id === id)) {
            await this.$komgaCollections.markThumbnailAsSelected(this.collection.id, id)
          }
        }

        if (this.poster.deleteQueue.length > 0) {
          this.poster.deleteQueue.forEach(toDelete => this.$komgaCollections.deleteThumbnail(toDelete.collectionId, toDelete.id))
        }

        const update = {
          name: this.form.name,
          ordered: this.form.ordered,
        } as CollectionUpdateDto

        await this.$komgaCollections.patchCollection(this.collection.id, update)
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
    async getThumbnails(readList: CollectionDto) {
      const thumbnails = await this.$komgaCollections.getThumbnails(readList.id)

      this.selectThumbnail(thumbnails.find(x => x.selected))

      this.poster.collectionThumbnails = thumbnails
    },
    isThumbnailSelected(item: File | CollectionThumbnailDto): boolean {
      return item instanceof File ? item.name === this.poster.selectedThumbnail : item.id === this.poster.selectedThumbnail
    },
    selectThumbnail(item: File | CollectionThumbnailDto | undefined) {
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
    isThumbnailToBeDeleted(item: File | CollectionThumbnailDto) {
      if (item instanceof File) {
        return false
      } else {
        return this.poster.deleteQueue.includes(item)
      }
    },
    deleteThumbnail(item: File | CollectionThumbnailDto) {
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
