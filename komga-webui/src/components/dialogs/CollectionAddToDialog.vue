<template>
  <v-dialog v-model="modal"
            max-width="450"
            scrollable
  >
    <v-card>
      <v-card-title>{{ $t('dialog.add_to_collection.dialog_title') }}</v-card-title>
      <v-btn icon absolute top right @click="dialogClose">
        <v-icon>mdi-close</v-icon>
      </v-btn>

      <v-divider/>

      <v-card-text style="height: 50%">
        <v-container fluid>

          <v-row align="center">
            <v-col>
              <v-text-field
                v-model="newCollection"
                autofocus
                :label="$t('dialog.add_to_collection.field_search_create')"
                @keydown.enter="create"
                :error-messages="duplicate"
              />
            </v-col>
            <v-col cols="auto">
              <v-btn
                color="primary"
                @click="create"
                :disabled="newCollection.length === 0 || duplicate !== ''"
              >{{ $t('dialog.add_to_collection.button_create') }}
              </v-btn>
            </v-col>
          </v-row>

          <v-divider/>

          <v-row v-if="collections.length !== 0">
            <v-col>
              <v-list elevation="5" v-if="collectionsFiltered.length !== 0">
                <div v-for="(c, index) in collectionsFiltered"
                     :key="index"
                >
                  <v-list-item @click="addTo(c)"
                               two-line
                  >
                    <v-list-item-content>
                      <v-list-item-title>{{ c.name }}</v-list-item-title>
                      <v-list-item-subtitle>
                        {{ $tc('dialog.add_to_collection.card_collection_subtitle', c.seriesIds.length) }}
                      </v-list-item-subtitle>
                    </v-list-item-content>
                  </v-list-item>
                  <v-divider v-if="index !== collectionsFiltered.length-1"/>
                </div>
              </v-list>

              <v-alert
                v-else
                type="info"
                text
              >{{ $t('dialog.add_to_collection.label_no_matching_collection') }}
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

export default Vue.extend({
  name: 'CollectionAddToDialog',
  data: () => {
    return {
      confirmDelete: false,
      modal: false,
      collections: [] as CollectionDto[],
      newCollection: '',
    }
  },
  props: {
    value: Boolean,
    seriesIds: {
      type: [Array as () => string[]],
      required: true,
    },
  },
  watch: {
    async value(val) {
      this.modal = val
      if (val) {
        this.newCollection = ''
        this.collections = this.$_.orderBy((await this.$komgaCollections.getCollections(undefined, {unpaged: true} as PageRequest)).content, ['lastModifiedDate'], ['desc'])
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
      if (this.newCollection !== '' && this.collections.some(e => e.name.toLowerCase() === this.newCollection.toLowerCase())) {
        return this.$t('dialog.add_to_collection.field_search_create_error').toString()
      } else return ''
    },
    collectionsFiltered(): CollectionDto[] {
      return this.collections.filter((x: CollectionDto) => stripAccents(x.name.toLowerCase()).includes(stripAccents(this.newCollection.toLowerCase())))
    },
  },
  methods: {
    dialogClose() {
      this.$emit('input', false)
    },
    async addTo(collection: CollectionDto) {
      const seriesIds = this.$_.uniq(collection.seriesIds.concat(this.seriesIds))

      const toUpdate = {
        seriesIds: seriesIds,
      } as CollectionUpdateDto

      try {
        await this.$komgaCollections.patchCollection(collection.id, toUpdate)
        this.dialogClose()
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
    async create() {
      const toCreate = {
        name: this.newCollection,
        ordered: false,
        seriesIds: this.seriesIds,
      } as CollectionCreationDto

      try {
        const created = await this.$komgaCollections.postCollection(toCreate)
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
