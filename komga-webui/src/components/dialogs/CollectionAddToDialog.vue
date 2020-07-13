<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
              scrollable
    >
      <v-card>
        <v-card-title>Add to collection</v-card-title>
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
                  label="Create new collection"
                  @keydown.enter="create"
                  :error-messages="duplicate"
                />
              </v-col>
              <v-col cols="auto">
                <v-btn
                  color="primary"
                  @click="create"
                  :disabled="newCollection.length === 0 || duplicate !== ''"
                >Create
                </v-btn>
              </v-col>
            </v-row>

            <v-divider/>

            <v-row v-if="collections.length !== 0">
              <v-col>
                <v-list elevation="5">
                  <div v-for="(c, index) in collections"
                       :key="index"
                  >
                    <v-list-item @click="addTo(c)"
                                 two-line
                    >
                      <v-list-item-content>
                        <v-list-item-title>{{ c.name }}</v-list-item-title>
                        <v-list-item-subtitle>{{ c.seriesIds.length }} series</v-list-item-subtitle>
                      </v-list-item-content>
                    </v-list-item>
                    <v-divider v-if="index !== collections.length-1"/>
                  </div>
                </v-list>
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
        Close
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'CollectionAddToDialog',
  data: () => {
    return {
      confirmDelete: false,
      snackbar: false,
      snackText: '',
      modal: false,
      collections: [] as CollectionDto[],
      selectedCollection: null,
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
    async value (val) {
      this.modal = val
      if (val) {
        this.newCollection = ''
        this.collections = (await this.$komgaCollections.getCollections(undefined, { unpaged: true } as PageRequest)).content
      }
    },
    modal (val) {
      !val && this.dialogClose()
    },
  },
  async mounted () {

  },
  computed: {
    seriesIds (): string[] {
      if (Array.isArray(this.series)) return this.series.map(s => s.id)
      else return [this.series.id]
    },
    duplicate (): string {
      if (this.newCollection !== '' && this.collections.some(e => e.name === this.newCollection)) {
        return 'A collection with this name already exists'
      } else return ''
    },
  },
  methods: {
    dialogClose () {
      this.$emit('input', false)
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async addTo (collection: CollectionDto) {
      const seriesIds = this.$_.uniq(collection.seriesIds.concat(this.seriesIds))

      const toUpdate = {
        seriesIds: seriesIds,
      } as CollectionUpdateDto

      try {
        await this.$komgaCollections.patchCollection(collection.id, toUpdate)
        this.$emit('added', collection)
        this.dialogClose()
      } catch (e) {
        this.showSnack(e.message)
      }
    },
    async create () {
      const toCreate = {
        name: this.newCollection,
        ordered: false,
        seriesIds: this.seriesIds,
      } as CollectionCreationDto

      try {
        const created = await this.$komgaCollections.postCollection(toCreate)
        this.$emit('created', created)
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
