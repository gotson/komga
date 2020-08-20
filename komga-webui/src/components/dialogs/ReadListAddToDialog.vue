<template>
  <div>
    <v-dialog v-model="modal"
              max-width="450"
              scrollable
    >
      <v-card>
        <v-card-title>Add to read list</v-card-title>
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
                  label="Create new read list"
                  @keydown.enter="create"
                  :error-messages="duplicate"
                />
              </v-col>
              <v-col cols="auto">
                <v-btn
                  color="primary"
                  @click="create"
                  :disabled="newReadList.length === 0 || duplicate !== ''"
                >Create
                </v-btn>
              </v-col>
            </v-row>

            <v-divider/>

            <v-row v-if="readLists.length !== 0">
              <v-col>
                <v-list elevation="5">
                  <div v-for="(c, index) in readLists"
                       :key="index"
                  >
                    <v-list-item @click="addTo(c)"
                                 two-line
                    >
                      <v-list-item-content>
                        <v-list-item-title>{{ c.name }}</v-list-item-title>
                        <v-list-item-subtitle>{{ c.bookIds.length }} books</v-list-item-subtitle>
                      </v-list-item-content>
                    </v-list-item>
                    <v-divider v-if="index !== readLists.length-1"/>
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
  name: 'ReadListAddToDialog',
  data: () => {
    return {
      confirmDelete: false,
      snackbar: false,
      snackText: '',
      modal: false,
      readLists: [] as ReadListDto[],
      newReadList: '',
    }
  },
  props: {
    value: Boolean,
    books: {
      type: [Object as () => BookDto, Array as () => BookDto[]],
      required: true,
    },
  },
  watch: {
    async value (val) {
      this.modal = val
      if (val) {
        this.newReadList = ''
        this.readLists = (await this.$komgaReadLists.getReadLists(undefined, { unpaged: true } as PageRequest)).content
      }
    },
    modal (val) {
      !val && this.dialogClose()
    },
  },
  async mounted () {

  },
  computed: {
    bookIds (): string[] {
      if (Array.isArray(this.books)) return this.books.map(s => s.id)
      else return [this.books.id]
    },
    duplicate (): string {
      if (this.newReadList !== '' && this.readLists.some(e => e.name === this.newReadList)) {
        return 'A read list with this name already exists'
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
    async addTo (readList: ReadListDto) {
      const bookIds = this.$_.uniq(readList.bookIds.concat(this.bookIds))

      const toUpdate = {
        bookIds: bookIds,
      } as ReadListUpdateDto

      try {
        await this.$komgaReadLists.patchReadList(readList.id, toUpdate)
        this.$emit('added', readList)
        this.dialogClose()
      } catch (e) {
        this.showSnack(e.message)
      }
    },
    async create () {
      const toCreate = {
        name: this.newReadList,
        bookIds: this.bookIds,
      } as ReadListCreationDto

      try {
        const created = await this.$komgaReadLists.postReadList(toCreate)
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
