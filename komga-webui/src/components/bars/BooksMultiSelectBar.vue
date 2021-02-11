<template>
  <v-scroll-y-transition hide-on-leave>
    <toolbar-sticky v-if="value.length > 0" :elevation="5" color="base">
      <v-btn icon @click="unselectAll">
        <v-icon>mdi-close</v-icon>
      </v-btn>
      <v-toolbar-title>
        <span>{{ $tc('common.n_selected', value.length) }}</span>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="markRead">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-bookmark-check</v-icon>
          </template>
          <span>{{ $t('menu.mark_read') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="markUnread">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-bookmark-remove</v-icon>
          </template>
          <span>{{ $t('menu.mark_unread') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="addToReadList" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-book-plus-multiple</v-icon>
          </template>
          <span>{{ $t('menu.add_to_readlist') }}</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="edit" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-pencil</v-icon>
          </template>
          <span>{{ $t('menu.edit_metadata') }}</span>
        </v-tooltip>
      </v-btn>
    </toolbar-sticky>
  </v-scroll-y-transition>
</template>

<script lang="ts">
import Vue from 'vue'
import ToolbarSticky from './ToolbarSticky.vue'

export default Vue.extend({
  name: 'BooksMultiSelectBar',
  components: { ToolbarSticky },
  data: () => {
    return {}
  },
  props: {
    value: {
      type: Array,
      required: true,
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
    markRead () {
      this.$emit('mark-read')
    },
    markUnread () {
      this.$emit('mark-unread')
    },
    addToReadList () {
      this.$emit('add-to-readlist')
    },
    edit () {
      this.$emit('edit')
    },
  },
})
</script>
