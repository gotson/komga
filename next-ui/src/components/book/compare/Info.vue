<template>
  <v-table density="compact">
    <colgroup>
      <col style="width: 1%" />
      <col />
    </colgroup>
    <tbody>
      <tr
        v-for="row in rows"
        :key="row.header"
      >
        <th class="text-no-wrap">
          {{ row.header }}
        </th>
        <td
          style="overflow-wrap: anywhere"
          :class="{ 'text-success': row.display === 'more', 'text-error': row.display === 'less' }"
        >
          <div class="d-flex align-center ga-1">
            <v-icon
              v-if="row.display"
              :icon="row.display === 'more' ? 'i-mdi:arrow-up-bold' : 'i-mdi:arrow-down-bold'"
              size="x-small"
            />
            {{ row.value }}
          </div>
        </td>
      </tr>
    </tbody>
  </v-table>
</template>

<script setup lang="ts">
import type { BookDetails } from '@/types/BookDetails'
import { useIntl } from 'vue-intl'
import { getFileSize } from '@/functions/filesize'

const { book, fileSizeCompared, pagesNumberCompared } = defineProps<{
  book: BookDetails
  fileSizeCompared?: 'more' | 'less'
  pagesNumberCompared?: 'more' | 'less'
}>()

const intl = useIntl()

const rows = computed(
  () =>
    [
      {
        header: intl.formatMessage({
          description: 'Book info: filename',
          defaultMessage: 'Filename',
          id: 'xAZMxz',
        }),
        value: book.filename,
      },
      {
        header: intl.formatMessage({
          description: 'Book info: file size',
          defaultMessage: 'File size',
          id: 'uTuk/9',
        }),
        value: `${getFileSize(book.fileSize)}`,
        display: fileSizeCompared,
      },
      {
        header: intl.formatMessage({
          description: 'Book info: media type',
          defaultMessage: 'Media type',
          id: 'jeXb2z',
        }),
        value: book.mediaType,
      },
      {
        header: intl.formatMessage({
          description: 'Book info: number of pages',
          defaultMessage: 'Number of pages',
          id: 'Jj4uO/',
        }),
        value: `${book.pages.length}`,
        display: pagesNumberCompared,
      },
    ] satisfies { header: string; value: string; display?: 'more' | 'less' }[],
)
</script>
