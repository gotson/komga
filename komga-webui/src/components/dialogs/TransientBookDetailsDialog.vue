<template>
  <div v-if="leftBook">
    <v-dialog v-model="modal"
              scrollable
    >
      <v-card>
        <v-card-title v-if="!rightBook">{{ $t('dialog.transient_book_details.title') }}</v-card-title>
        <v-card-title v-else>{{ $t('dialog.transient_book_details.title_comparison') }}</v-card-title>
        <v-btn icon absolute top right @click="dialogClose">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-divider/>

        <v-card-text style="height: 50%">
          <v-container fluid>

            <v-simple-table class="body-2">
              <thead v-if="rightBook">
              <tr>
                <th></th>
                <th :class="rightBook ? 'diff' : ''">{{ $t('dialog.transient_book_details.label_candidate') }}</th>
                <th>{{ $t('dialog.transient_book_details.label_existing') }}</th>
              </tr>
              </thead>
              <tbody>
              <tr>
                <td class="font-weight-medium">{{ $t('dialog.transient_book_details.label_name') }}</td>
                <td :class="rightBook ? 'diff' : ''">{{ leftBook.name }}</td>
                <td v-if="rightBook">{{ rightBook.metadata.title }}</td>
              </tr>

              <tr>
                <td class="font-weight-medium">{{ $t('dialog.transient_book_details.label_size') }}</td>
                <td :class="rightBook ? 'diff' : ''">{{ leftBook.size }}</td>
                <td v-if="rightBook">{{ rightBook.size }}</td>
              </tr>

              <tr>
                <td class="font-weight-medium">{{ $t('dialog.transient_book_details.label_format') }}</td>
                <td :class="rightBook ? 'diff' : ''">{{ getBookFormatFromMediaType(leftBook.mediaType).type }}</td>
                <td v-if="rightBook">{{ getBookFormatFromMediaType(rightBook.media.mediaType).type }}</td>
              </tr>

              <tr>
                <td class="font-weight-medium">{{ $t('dialog.transient_book_details.label_pages') }}</td>
                <td :class="rightBook ? 'diff' : ''">{{ leftBook.pages.length }}</td>
                <td v-if="rightBook">{{ rightBook.media.pagesCount }}</td>
              </tr>

              <tr>
                <td :colspan="rightBook ? 3 : 2" class="pa-0">
                  <pages-table :left-pages="leftBook.pages" :right-pages="rightPages"></pages-table>
                </td>
              </tr>

              </tbody>
            </v-simple-table>

          </v-container>
        </v-card-text>

      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {TransientBookDto} from '@/types/komga-transientbooks'
import {BookDto, PageDto} from '@/types/komga-books'
import {getBookFormatFromMedia} from '@/functions/book-format'
import PagesTable from '@/components/PagesTable.vue'

export default Vue.extend({
  name: 'TransientBookDetailsDialog',
  components: {PagesTable},
  data: () => {
    return {
      modal: false,
      getBookFormatFromMediaType: getBookFormatFromMedia,
    }
  },
  props: {
    value: Boolean,
    leftBook: {
      type: Object as PropType<TransientBookDto>,
      required: false,
    },
    rightBook: {
      type: Object as PropType<BookDto>,
      required: false,
    },
    rightPages: {
      type: Array as PropType<PageDto[]>,
      required: false,
    },
  },
  watch: {
    async value(val) {
      this.modal = val
    },
    modal(val) {
      !val && this.dialogClose()
    },
  },
  methods: {
    dialogClose() {
      this.$emit('input', false)
    },
  },
})
</script>

<style scoped>

</style>
