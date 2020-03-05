<template>
  <card-thumbnail
    :thumbnail="thumbnail"
    :width="width"
    :selection="selection"
    :edit="edit"
    :preSelect="preSelect"
    :select="select"
    :selected="selected"
    @editItem="editItem"
    @selectItem="selectItem"
    @goTo="goTo"
  >
    <template v-slot:topright>
      <span class="white--text pt-2 pb-1 px-2 subtitle-2" style="background: darkcyan none repeat scroll 0% 0%;">
         {{ book.number }}
       </span>
    </template>
    <template v-slot:topleft>
      <span
        class="white--text pa-1 px-2 subtitle-2"
        :style="{background: format.color, position: 'absolute'}"
      >
        {{ format.type }}
      </span>
    </template>
    <template v-slot:bottom>
      <span v-if="book.media.status !== 'READY'"
            class="white--text pa-1 px-2 subtitle-2 text-center"
            :style="{background: statusColor, width: '100%', float: 'right'}"
      >
        {{ book.media.status }}
      </span>
    </template>
    <template #subtitle>
      {{ title }}
    </template>
    <template #description>
<!--      <span v-if="series.booksCount === 1">{{ series.booksCount }} book</span>-->
<!--      <span v-else>{{ series.booksCount }} books</span>-->
    </template>
  </card-thumbnail>
</template>

<script lang="ts">
import { getBookFormatFromMediaType } from '@/functions/book-format'
import { bookThumbnailUrl } from '@/functions/urls'
import Vue, { PropType } from 'vue'
import CardThumbnail from '@/components/CardThumbnail.vue'

export default Vue.extend({
  name: 'CardBook',
  components: { CardThumbnail },
  computed: {
    thumbnail (): string {
      return bookThumbnailUrl(this.book.id)
    },
    title (): string {
      return `${this.book.name}`
    },
    format (): BookFormat {
      return getBookFormatFromMediaType(this.book.media.mediaType)
    },
    statusColor (): string {
      switch (this.book.media.status) {
        case 'ERROR':
          return 'red'
        case 'UNKOWN':
          return 'grey'
        case 'UNSUPPORTED':
          return 'orange'
        default:
          return 'black'
      }
    }
  },
  methods: {
    editItem () {
      if (this.edit) {
        this.edit(this.book)
      }
    },
    selectItem () {
      if (this.select) {
        this.select()
      }
    },
    goTo () {
      this.$router.push({ name: 'browse-book', params: { bookId: this.book.id.toString() } })
    }
  },
  props: {
    book: {
      type: Object as PropType<BookDto>,
      required: true
    },
    width: {
      type: [String, Number],
      required: false,
      default: 150
    },
    selection: {
      type: Boolean,
      default: false
    },
    selected: {
      type: Boolean,
      default: false
    },
    preSelect: {
      type: Boolean,
      default: false
    },
    select: {
      type: Function,
      required: false
    },
    edit: {
      type: Function,
      required: false
    }
  }
})
</script>
<!--<template>-->
<!--  <v-card :width="width"-->
<!--          :to="{name:'browse-book', params: {bookId: book.id}}"-->
<!--  >-->
<!--    <v-img-->
<!--      :src="thumbnailUrl"-->
<!--      lazy-src="../assets/cover.svg"-->
<!--      aspect-ratio="0.7071"-->
<!--    >-->

<!--    </v-img>-->

<!--    <v-card-subtitle class="pa-2 pb-1 text&#45;&#45;primary"-->
<!--                     v-line-clamp="2"-->
<!--                     style="word-break: normal !important; height: 4em"-->
<!--                     :title="book.name"-->
<!--    >-->
<!--      #{{ book.number }} - {{ book.name }}-->
<!--    </v-card-subtitle>-->

<!--    <v-card-text class="px-2"-->
<!--    >-->
<!--      <div>{{ book.size }}</div>-->
<!--      <div v-if="book.media.pagesCount === 1">{{ book.media.pagesCount }} page</div>-->
<!--      <div v-else>{{ book.media.pagesCount }} pages</div>-->
<!--    </v-card-text>-->

<!--  </v-card>-->
<!--</template>-->

<!--<script lang="ts">-->
<!--import { bookThumbnailUrl } from '@/functions/urls'-->
<!--import Vue, { PropType } from 'vue'-->

<!--export default Vue.extend({-->
<!--  name: 'CardBook',-->
<!--  props: {-->
<!--    book: {-->
<!--      type: Object as PropType<BookDto>,-->
<!--      required: true-->
<!--    },-->
<!--    width: {-->
<!--      type: [String, Number],-->
<!--      required: false,-->
<!--      default: 150-->
<!--    }-->
<!--  },-->
<!--  computed: {-->
<!--    thumbnailUrl (): string {-->
<!--      return bookThumbnailUrl(this.book.id)-->
<!--    },-->

<!--  }-->
<!--})-->
<!--</script>-->

<!--<style scoped>-->
<!--</style>-->
