<template>
  <div>
    <v-dialog v-model="modal"
              scrollable
              fullscreen
    >
      <v-card>
        <v-card-title v-if="single">{{ $t('dialog.transient_book_viewer.title') }}</v-card-title>
        <v-card-title v-else>{{ $t('dialog.transient_book_viewer.title_comparison') }}</v-card-title>
        <v-btn icon absolute top right @click="dialogClose">
          <v-icon>mdi-close</v-icon>
        </v-btn>

        <v-divider/>

        <v-card-text style="height: 50%">
          <v-container fluid class="">

            <v-row justify="space-around" v-if="!single">
              <v-col cols="auto" class="pa-1">{{ $t('dialog.transient_book_viewer.label_candidate') }}</v-col>
              <v-col cols="auto" class="pa-1">{{ $t('dialog.transient_book_viewer.label_existing') }}</v-col>
            </v-row>

            <v-row justify="space-around">
              <v-col cols="auto" class="pa-1">
                <v-btn icon :disabled="!canPrevLeft" @click="firstPageLeft">
                  <rtl-icon icon="mdi-chevron-double-left" rtl="mdi-chevron-double-right"/>
                </v-btn>
                <v-btn icon :disabled="!canPrevLeft" @click="previousPageLeft">
                  <rtl-icon icon="mdi-chevron-left" rtl="mdi-chevron-right"/>
                </v-btn>
                {{ $t('dialog.transient_book_viewer.page_of_pages', {page: leftPageNumber, pages: leftPages.length}) }}
                <v-btn icon :disabled="!canNextLeft" @click="nextPageLeft">
                  <rtl-icon icon="mdi-chevron-right" rtl="mdi-chevron-left"/>
                </v-btn>
                <v-btn icon :disabled="!canNextLeft" @click="lastPageLeft">
                  <rtl-icon icon="mdi-chevron-double-right" rtl="mdi-chevron-double-left"/>
                </v-btn>
              </v-col>


              <v-col cols="auto" class="pa-1" v-if="!single">
                <v-btn icon :disabled="!canPrevAny" @click="firstPageBoth">
                  <rtl-icon icon="mdi-chevron-double-left" rtl="mdi-chevron-double-right"/>
                </v-btn>
                <v-btn icon :disabled="!canPrevAny" @click="previousPageBoth">
                  <rtl-icon icon="mdi-chevron-left" rtl="mdi-chevron-right"/>
                </v-btn>
                <v-btn icon :disabled="!canNextAny" @click="nextPageBoth">
                  <rtl-icon icon="mdi-chevron-right" rtl="mdi-chevron-left"/>
                </v-btn>
                <v-btn icon :disabled="!canNextAny" @click="lastPageBoth">
                  <rtl-icon icon="mdi-chevron-double-right" rtl="mdi-chevron-double-left"/>
                </v-btn>
              </v-col>

              <v-col cols="auto" class="pa-1" v-if="!single">
                <v-btn icon :disabled="!canPrevRight" @click="firstPageRight">
                  <rtl-icon icon="mdi-chevron-double-left" rtl="mdi-chevron-double-right"/>
                </v-btn>
                <v-btn icon :disabled="!canPrevRight" @click="previousPageRight">
                  <rtl-icon icon="mdi-chevron-left" rtl="mdi-chevron-right"/>
                </v-btn>
                {{
                  $t('dialog.transient_book_viewer.page_of_pages', {page: rightPageNumber, pages: rightPages.length})
                }}
                <v-btn icon :disabled="!canNextRight" @click="nextPageRight">
                  <rtl-icon icon="mdi-chevron-right" rtl="mdi-chevron-left"/>
                </v-btn>
                <v-btn icon :disabled="!canNextRight" @click="lastPageRight">
                  <rtl-icon icon="mdi-chevron-double-right" rtl="mdi-chevron-double-left"/>
                </v-btn>
              </v-col>
            </v-row>

            <v-row justify="center">
              <v-col cols="6" class="pa-0">
                <v-img
                  :src="$_.get(pageLeft, 'url', '')"
                  contain
                  :max-height="$vuetify.breakpoint.height * .75"
                >
                </v-img>
              </v-col>

              <v-col cols="6" class="pa-0" v-if="!single">
                <v-img
                  :src="$_.get(pageRight, 'url', '')"
                  :max-height="$vuetify.breakpoint.height * .75"
                  contain
                >
                </v-img>
              </v-col>
            </v-row>

            <v-row justify="space-around">
              <v-col cols="auto" class="pa-1">
                w: {{ $_.get(pageLeft, 'width', '') }} h: {{ $_.get(pageLeft, 'height', '') }}
                {{ $_.get(pageLeft, 'mediaType', '') }}
              </v-col>
              <v-col cols="auto" class="pa-1" v-if="!single">
                w: {{ $_.get(pageRight, 'width', '') }} h: {{ $_.get(pageRight, 'height', '') }}
                {{ $_.get(pageRight, 'mediaType', '') }}
              </v-col>
            </v-row>

          </v-container>
        </v-card-text>

      </v-card>
    </v-dialog>
  </div>
</template>

<script lang="ts">
import Vue, {PropType} from 'vue'
import {PageDtoWithUrl} from '@/types/komga-books'
import RtlIcon from '@/components/RtlIcon.vue'


export default Vue.extend({
  name: 'TransientBookViewerDialog',
  components: {RtlIcon},
  data: () => {
    return {
      modal: false,
      leftPageNumber: 1,
      rightPageNumber: 1,
    }
  },
  props: {
    value: Boolean,
    leftPages: {
      type: Array as PropType<PageDtoWithUrl[]>,
      default: () => [],
    },
    rightPages: {
      type: Array as PropType<PageDtoWithUrl[]>,
      default: () => [],
    },
  },
  watch: {
    async value(val) {
      this.modal = val
      if (val) this.firstPageBoth()
    },
    modal(val) {
      !val && this.dialogClose()
    },
  },
  computed: {
    pageLeft(): PageDtoWithUrl {
      return this.leftPages[this.leftPageNumber - 1]
    },
    pageRight(): PageDtoWithUrl {
      return this.rightPages[this.rightPageNumber - 1]
    },
    canPrevLeft(): boolean {
      return this.leftPageNumber > 1
    },
    canPrevRight(): boolean {
      return this.rightPageNumber > 1
    },
    canPrevAny(): boolean {
      return this.canPrevLeft || this.canPrevRight
    },
    canNextLeft(): boolean {
      return this.leftPageNumber < this.leftPages.length
    },
    canNextRight(): boolean {
      return this.rightPageNumber < this.rightPages.length
    },
    canNextAny(): boolean {
      return this.canNextLeft || this.canNextRight
    },
    single(): boolean {
      return this.rightPages.length === 0
    },
  },
  methods: {
    firstPageLeft() {
      this.leftPageNumber = 1
    },
    firstPageRight() {
      this.rightPageNumber = 1
    },
    firstPageBoth() {
      this.firstPageLeft()
      this.firstPageRight()
    },
    previousPageLeft() {
      if (this.canPrevLeft) this.leftPageNumber--
    },
    previousPageRight() {
      if (this.canPrevRight) this.rightPageNumber--
    },
    nextPageLeft() {
      if (this.canNextLeft) this.leftPageNumber++
    },
    nextPageRight() {
      if (this.canNextRight) this.rightPageNumber++
    },
    previousPageBoth() {
      this.previousPageLeft()
      this.previousPageRight()
    },
    nextPageBoth() {
      this.nextPageLeft()
      this.nextPageRight()
    },
    lastPageLeft() {
      this.leftPageNumber = this.leftPages.length
    },
    lastPageRight() {
      this.rightPageNumber = this.rightPages.length
    },
    lastPageBoth() {
      this.lastPageLeft()
      this.lastPageRight()
    },
    dialogClose() {
      this.$emit('input', false)
    },
  },
})
</script>

<style scoped>

</style>
