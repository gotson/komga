<template>
  <div>
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
                        <v-list-item-subtitle>{{ $tc('dialog.add_to_collection.card_collection_subtitle', c.seriesIds.length) }}</v-list-item-subtitle>
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
        {{ $t('common.close') }}
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {SeriesDto} from "@/types/komga-series";

export default Vue.extend({
  name: 'CollectionAddToDialog',
  data: () => {
    return {
      confirmDelete: false,
      snackbar: false,
      snackText: '',
      modal: false,
      collections: [] as CollectionDto[],
      newCollection: '',
    }
  },
  props: {
    value: Boolean,
    series: {
      type: [Object as () => SeriesDto, Array as () => SeriesDto[]],
      required: true,
    },
  },
  watch: {
    async value(val) {
      this.modal = val
      if (val) {
        this.newCollection = ''
        this.collections = (await this.$komgaCollections.getCollections(undefined, {unpaged: true} as PageRequest)).content
      }
    },
    modal(val) {
      !val && this.dialogClose()
    },
  },
  async mounted() {

  },
  computed: {
    seriesIds(): string[] {
      if (Array.isArray(this.series)) return this.series.map(s => s.id)
      else return [this.series.id]
    },
    duplicate(): string {
      if (this.newCollection !== '' && this.collections.some(e => e.name === this.newCollection)) {
        return this.$t('dialog.add_to_collection.field_search_create_error').toString()
      } else return ''
    },
    collectionsFiltered(): CollectionDto[] {
      return this.collections.filter((x: CollectionDto) => x.name.toLowerCase().includes(this.newCollection.toLowerCase()))
    },
  },
  methods: {
    dialogClose() {
      this.$emit('input', false)
    },
    showSnack(message: string) {
      this.snackText = message
      this.snackbar = true
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
        this.showSnack(e.message)
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
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
