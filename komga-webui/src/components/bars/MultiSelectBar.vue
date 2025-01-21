<template>
  <v-scroll-y-transition hide-on-leave>
    <toolbar-sticky v-if="value.length > 0" :elevation="5" color="base">
      <v-btn icon @click="unselectAll">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-close</v-icon>
          </template>
          <span>{{ $t('menu.deselect_all') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="selectAll" v-if="showSelectAll">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-select-all</v-icon>
          </template>
          <span>{{ $t('menu.select_all') }}</span>
        </v-tooltip>
      </v-btn>

      <v-toolbar-title class="mx-2">
        <span>{{ $tc('common.n_selected', value.length) }}</span>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="markRead" v-if="kind === 'books' || kind === 'series'">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-bookmark-check</v-icon>
          </template>
          <span>{{ $t('menu.mark_read') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="markUnread" v-if="kind === 'books' || kind === 'series'">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-bookmark-remove</v-icon>
          </template>
          <span>{{ $t('menu.mark_unread') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="addToCollection" v-if="isAdmin && (kind === 'series' || (kind === 'books' && oneshots))">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-playlist-plus</v-icon>
          </template>
          <span>{{ $t('menu.add_to_collection') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="addToReadList" v-if="isAdmin && (kind === 'books' || kind === 'series')">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-book-plus-multiple</v-icon>
          </template>
          <span>{{ $t('menu.add_to_readlist') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="bulkEdit" v-if="isAdmin && kind === 'books'">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-table-edit</v-icon>
          </template>
          <span>{{ $t('menu.bulk_edit_metadata') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="edit" v-if="isAdmin && (kind === 'books' || kind === 'series')">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-pencil</v-icon>
          </template>
          <span>{{ $t('menu.edit_metadata') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="doDelete" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-delete</v-icon>
          </template>
          <span>{{ $t('menu.delete') }}</span>
        </v-tooltip>
      </v-btn>
    </toolbar-sticky>
  </v-scroll-y-transition>
</template>

<script lang="ts">
import Vue from 'vue'
import ToolbarSticky from './ToolbarSticky.vue'

export default Vue.extend({
  name: 'MultiSelectBar',
  components: { ToolbarSticky },
  data: () => {
    return {}
  },
  props: {
    value: {
      type: Array,
      required: true,
    },
    /**
     * The kind of items this toolbar acts on.
     * @values books, series, collections, readlists
     */
    kind: {
      type: String,
      required: true,
    },
    oneshots: {
      type: Boolean,
      default: false,
    },
    showSelectAll: {
      type: Boolean,
      default: false,
    },
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    unselectAll () {
      this.$emit('unselect-all')
    },
    selectAll () {
      this.$emit('select-all')
    },
    markRead () {
      this.$emit('mark-read')
    },
    markUnread () {
      this.$emit('mark-unread')
    },
    addToCollection () {
      this.$emit('add-to-collection')
    },
    addToReadList () {
      this.$emit('add-to-readlist')
    },
    edit () {
      this.$emit('edit')
    },
    bulkEdit () {
      this.$emit('bulk-edit')
    },
    doDelete () {
      this.$emit('delete')
    },
  },
})
</script>
