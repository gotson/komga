<template>
  <div v-if="readList">
    <toolbar-sticky v-if="!editElements && selectedBooks.length === 0">

      <read-list-actions-menu v-if="readList"
                              :read-list="readList"
      />

      <v-toolbar-title v-if="readList">
        <span>{{ readList.name }}</span>
        <v-chip label class="mx-4">
          <span style="font-size: 1.1rem">{{ readList.bookIds.length }}</span>
        </v-chip>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="startEditElements" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-playlist-edit</v-icon>
          </template>
          <span>{{ $t('browse_readlist.edit_elements') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="editReadList" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-pencil</v-icon>
          </template>
          <span>{{ $t('browse_readlist.edit_readlist') }}</span>
        </v-tooltip>
      </v-btn>

    </toolbar-sticky>

    <multi-select-bar
      v-model="selectedBooks"
      kind="books"
      show-select-all
      @unselect-all="selectedBooks = []"
      @select-all="selectedBooks = books"
      @mark-read="markSelectedRead"
      @mark-unread="markSelectedUnread"
      @add-to-readlist="addToReadList"
      @edit="editMultipleBooks"
    />

    <!--  Edit elements sticky bar  -->
    <v-scroll-y-transition hide-on-leave>
      <toolbar-sticky v-if="editElements" :elevation="5" color="base">
        <v-btn icon @click="cancelEditElements">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-btn icon color="primary" @click="doEditElements" :disabled="books.length === 0">
          <v-icon>mdi-check</v-icon>
        </v-btn>

      </toolbar-sticky>
    </v-scroll-y-transition>

    <v-container fluid>

      <item-browser
        :items.sync="books"
        :selected.sync="selectedBooks"
        :edit-function="editSingleBook"
        :draggable="editElements"
        :deletable="editElements"
      />

    </v-container>

  </div>
</template>

<script lang="ts">
import ItemBrowser from '@/components/ItemBrowser.vue'
import ToolbarSticky from '@/components/bars/ToolbarSticky.vue'
import {
  BOOK_CHANGED,
  BOOK_DELETED,
  READLIST_CHANGED,
  READLIST_DELETED,
  READPROGRESS_CHANGED,
  READPROGRESS_DELETED,
} from '@/types/events'
import Vue from 'vue'
import ReadListActionsMenu from '@/components/menus/ReadListActionsMenu.vue'
import MultiSelectBar from '@/components/bars/MultiSelectBar.vue'
import {BookDto, ReadProgressUpdateDto} from '@/types/komga-books'
import {ContextOrigin} from '@/types/context'
import {BookSseDto, ReadListSseDto, ReadProgressSseDto} from "@/types/komga-sse";
import {throttle} from "lodash";

export default Vue.extend({
  name: 'BrowseReadList',
  components: {
    ToolbarSticky,
    ItemBrowser,
    ReadListActionsMenu,
    MultiSelectBar,
  },
  data: () => {
    return {
      readList: undefined as ReadListDto | undefined,
      books: [] as BookDto[],
      booksCopy: [] as BookDto[],
      selectedBooks: [] as BookDto[],
      editElements: false,
    }
  },
  props: {
    readListId: {
      type: String,
      required: true,
    },
  },
  created () {
    this.$eventHub.$on(READLIST_CHANGED, this.readListChanged)
    this.$eventHub.$on(READLIST_DELETED, this.readListDeleted)
    this.$eventHub.$on(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$on(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$on(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$on(READPROGRESS_DELETED, this.readProgressChanged)
  },
  beforeDestroy () {
    this.$eventHub.$off(READLIST_CHANGED, this.readListChanged)
    this.$eventHub.$off(READLIST_DELETED, this.readListDeleted)
    this.$eventHub.$off(BOOK_CHANGED, this.bookChanged)
    this.$eventHub.$off(BOOK_DELETED, this.bookChanged)
    this.$eventHub.$off(READPROGRESS_CHANGED, this.readProgressChanged)
    this.$eventHub.$off(READPROGRESS_DELETED, this.readProgressChanged)
  },
  mounted () {
    this.loadReadList(this.readListId)
  },
  beforeRouteUpdate (to, from, next) {
    if (to.params.readListId !== from.params.readListId) {
      // reset
      this.books = []
      this.editElements = false

      this.loadReadList(to.params.readListId)
    }

    next()
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    readListChanged (event: ReadListSseDto) {
      if (event.readListId === this.readListId) {
        this.loadReadList(this.readListId)
      }
    },
    readListDeleted (event: ReadListSseDto) {
      if (event.readListId === this.readListId) {
        this.$router.push({name: 'browse-readlists', params: {libraryId: 'all'}})
      }
    },
    async loadReadList (readListId: string) {
      this.$komgaReadLists.getOneReadList(readListId)
      .then(v => this.readList = v)
      await this.loadBooks(readListId)
    },
    async loadBooks (readListId: string) {
      this.books = (await this.$komgaReadLists.getBooks(readListId, { unpaged: true } as PageRequest)).content
      this.books.forEach((x: BookDto) => x.context = { origin: ContextOrigin.READLIST, id: readListId })
      this.booksCopy = [...this.books]
      this.selectedBooks = []
    },
    reloadBooks: throttle(function (this: any) {
      this.loadBooks(this.readListId)
    }, 1000),
    editSingleBook (book: BookDto) {
      this.$store.dispatch('dialogUpdateBooks', book)
    },
    editMultipleBooks () {
      this.$store.dispatch('dialogUpdateBooks', this.selectedBooks)
    },
    async markSelectedRead () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.updateReadProgress(b.id, { completed: true } as ReadProgressUpdateDto),
      ))
    },
    async markSelectedUnread () {
      await Promise.all(this.selectedBooks.map(b =>
        this.$komgaBooks.deleteReadProgress(b.id),
      ))
    },
    addToReadList () {
      this.$store.dispatch('dialogAddBooksToReadList', this.selectedBooks)
    },
    startEditElements () {
      this.editElements = true
    },
    cancelEditElements () {
      this.editElements = false
      this.books = [...this.booksCopy]
    },
    doEditElements () {
      this.editElements = false
      const update = {
        bookIds: this.books.map(x => x.id),
      } as ReadListUpdateDto
      this.$komgaReadLists.patchReadList(this.readListId, update)
    },
    editReadList () {
      this.$store.dispatch('dialogEditReadList', this.readList)
    },
    bookChanged (event: BookSseDto) {
      if (this.books.some(b => b.id === event.bookId)) this.reloadBooks()
    },
    readProgressChanged(event: ReadProgressSseDto){
      if (this.books.some(b => b.id === event.bookId)) this.reloadBooks()
    },
  },
})
</script>
