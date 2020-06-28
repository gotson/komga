<template>
  <v-scroll-y-transition hide-on-leave>
    <toolbar-sticky v-if="value.length > 0" :elevation="5" color="white">
      <v-btn icon @click="unselectAll">
        <v-icon>mdi-close</v-icon>
      </v-btn>
      <v-toolbar-title>
        <span>{{ value.length }} selected</span>
      </v-toolbar-title>

      <v-spacer/>

      <v-btn icon @click="markRead">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-bookmark-check</v-icon>
          </template>
          <span>Mark as Read</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="markUnread">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-bookmark-remove</v-icon>
          </template>
          <span>Mark as Unread</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="addToCollection" v-if="isAdmin">
        <v-tooltip bottom>
          <template v-slot:activator="{ on }">
            <v-icon v-on="on">mdi-playlist-plus</v-icon>
          </template>
          <span>Add to collection</span>
        </v-tooltip>
      </v-btn>

      <v-btn icon @click="edit" v-if="isAdmin">
        <v-icon>mdi-pencil</v-icon>
      </v-btn>
    </toolbar-sticky>
  </v-scroll-y-transition>
</template>

<script lang="ts">
import Vue from 'vue'
import ToolbarSticky from './ToolbarSticky.vue'

export default Vue.extend({
  name: 'SeriesMultiSelectBar',
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
    addToCollection () {
      this.$emit('add-to-collection')
    },
    edit () {
      this.$emit('edit')
    },
  },
})
</script>
