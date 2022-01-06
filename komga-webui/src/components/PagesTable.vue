<template>
  <v-simple-table v-if="leftPages.length > 0">
    <thead>
      <tr>
        <th>{{ $t('dialog.transient_book_details.pages_table.index') }}</th>
        <th :class="rightPages.length > 0 ? 'diff' : ''">{{ $t('dialog.transient_book_details.pages_table.filename') }}</th>
        <th :class="rightPages.length > 0 ? 'diff' : ''">{{ $t('dialog.transient_book_details.pages_table.media_type') }}</th>
        <th :class="rightPages.length > 0 ? 'diff' : ''">{{ $t('dialog.transient_book_details.pages_table.width') }}</th>
        <th :class="rightPages.length > 0 ? 'diff' : ''">{{ $t('dialog.transient_book_details.pages_table.height') }}</th>
        <th :class="rightPages.length > 0 ? 'diff' : ''">{{ $t('dialog.transient_book_details.pages_table.size') }}</th>
        <template v-if="rightPages.length > 0">
          <th>{{ $t('dialog.transient_book_details.pages_table.filename') }}</th>
          <th>{{ $t('dialog.transient_book_details.pages_table.media_type') }}</th>
          <th>{{ $t('dialog.transient_book_details.pages_table.width') }}</th>
          <th>{{ $t('dialog.transient_book_details.pages_table.height') }}</th>
          <th>{{ $t('dialog.transient_book_details.pages_table.size') }}</th>
        </template>
      </tr>
    </thead>
    <tbody>
      <tr
        v-for="(n, i) in numberRows"
        :key="i"
      >
        <td>{{ n }}</td>
        <td :class="rightPages.length > 0 ? 'diff' : ''">{{ $_.get(leftPages[n-1], 'fileName', '') }}</td>
        <td :class="rightPages.length > 0 ? 'diff' : ''">{{ $_.get(leftPages[n-1], 'mediaType', '') }}</td>
        <td :class="rightPages.length > 0 ? 'diff' : ''">{{ $_.get(leftPages[n-1], 'width', '') }}</td>
        <td :class="rightPages.length > 0 ? 'diff' : ''">{{ $_.get(leftPages[n-1], 'height', '') }}</td>
        <td :class="rightPages.length > 0 ? 'diff' : ''">{{ $_.get(leftPages[n-1], 'size', '') }}</td>
        <template v-if="rightPages.length > 0">
          <td>{{ $_.get(rightPages[n-1], 'fileName', '') }}</td>
          <td>{{ $_.get(rightPages[n-1], 'mediaType', '') }}</td>
          <td>{{ $_.get(rightPages[n-1], 'width', '') }}</td>
          <td>{{ $_.get(rightPages[n-1], 'height', '') }}</td>
          <td>{{ $_.get(rightPages[n-1], 'size', '') }}</td>
        </template>
      </tr>
    </tbody>
  </v-simple-table>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {PageDto} from '@/types/komga-books'

export default Vue.extend({
  name: 'PagesTable',
  props: {
    leftPages: {
      type: Array as PropType<PageDto[]>,
      default: () => [],
    },
    rightPages: {
      type: Array as PropType<PageDto[]>,
      default: () => [],
    },
  },
  computed: {
    numberRows(): number {
      if(this.leftPages) {
        if(this.rightPages) return Math.max(this.leftPages.length, this.rightPages.length)
        return this.leftPages.length
      }
      return 0
    },
  },
})
</script>

<style scoped>

</style>
