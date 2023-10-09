<template>
  <v-menu offset-y>
    <template v-slot:activator="{on}">
      <v-btn icon v-on="on">
        <v-icon>mdi-book-outline</v-icon>
      </v-btn>
    </template>
    <v-list>
      <v-list-item-group v-model="selection">

        <v-list-item :to="{name: 'browse-libraries', params: {libraryId: libraryId}}">
          <v-list-item-title>{{ $t(`enums.item_types.SERIES`) }}</v-list-item-title>
        </v-list-item>

        <v-list-item :to="{name: 'browse-libraries-by-book', params: {libraryId: libraryId}}">
          <v-list-item-title>{{ $t(`enums.item_types.BOOK`) }}</v-list-item-title>
        </v-list-item>

      </v-list-item-group>
    </v-list>
  </v-menu>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'LibraryItemTypeSelect',
  data: () => {
    return {
      selection: 0,
    }
  },
  props: {
    libraryId: {
      type: String,
      required: true,
    },
  },
})
</script>

<style scoped>

</style>