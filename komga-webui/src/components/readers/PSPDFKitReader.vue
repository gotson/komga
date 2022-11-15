<template>
  <div>
    <div class="pspdfkit-container" style="width: 100%; height: 100vh;"></div>

    <!--  clickable zone: menu  -->
    <div @click="menuClick()" class="dot-btn" style="z-index: 1;" />
  </div>
</template>
  
<script lang="ts">
import Vue from 'vue'
import PSPDFKit from 'pspdfkit'
import urls from '@/functions/urls'

export default Vue.extend({
  name: 'PSPDFKitReader',
  data: () => {
    return {
      offsetTop: 0,
      totalHeight: 1000,
      currentPage: 0,
      seen: [] as boolean[],
    }
  },
  props: {
    page: {
      type: Number,
      required: true,
    },
    bookId: {
      type: String,
      required: true,
    },
  },
  watch: {
    page: {
      handler(val) {
      },
      immediate: true,
    },
  },
  created() {
  },
  destroyed() {
  },
  beforeUnmount() {
    PSPDFKit.unload('.pspdfkit-container')
  },
  mounted() {
    this.loadPSPDFKit().then((instance) => {
      this.$emit('loaded', instance)
    })
  },
  computed: {
  },
  methods: {
    onScroll(e: any) {
      this.offsetTop = e.target.scrollingElement.scrollTop
      this.totalHeight = e.target.scrollingElement.scrollHeight
    },
    menuClick() {
      this.$emit('menu')
    },
    async loadPSPDFKit() {
      PSPDFKit.unload('.pspdfkit-container')
      const jwt_token = await this.$komgaBooks.getBookJwt(this.bookId)
      var readerInst = PSPDFKit.load({
        container: '.pspdfkit-container',
        baseUrl: `${urls.originNoSlash}/api/v1/pdf/`,
        serverUrl: `${urls.originNoSlash}/api/v1/pdf/`,
        authPayload: {
          'jwt': jwt_token ,
        },
        documentId: this.bookId,
        instant: true,
        initialViewState: {
          currentPageIndex: this.page,
          zoom: PSPDFKit.ZoomMode.AUTO,
          pagesRotation: 0,
          layoutMode: PSPDFKit.LayoutMode.SINGLE,
          scrollMode: PSPDFKit.ScrollMode.CONTINUOUS,
          showSignatureValidationStatus: 'NEVER',
          spreadSpacing: 1,
          pageSpacing: 1,
          allowPrinting: false,
          allowExport: false,
          interactionMode: 'TEXT_HIGHLIGHTER',
          keepFirstSpreadAsSinglePage: false,
          readOnly: false,
          showAnnotations: true,
          showComments: false,
          showAnnotationNotes: true,
          showToolbar: true,
          enableAnnotationToolbar: false,
          sidebarMode: 'THUMBNAILS',
          sidebarPlacement: 'START',
          toolbarPlacement: 'BOTTOM',
          viewportPadding: {
            horizontal: 5,
            vertical: 5,
          },
          // zoom: IZoomMode | number;
          formDesignMode: false,
          previewRedactionMode: false,
          canScrollWhileDrawing: false,
          keepSelectedTool: true,
          // resolvedLayoutMode: ILayoutMode;
          sidebarWidth: 10,
        },
      })

        ; (await readerInst).addEventListener('viewState.currentPageIndex.change', (pageIndex: Number) => {
          console.log(`Current page index changed to ${pageIndex}`)
          this.$emit('update:page', pageIndex)
        })
      return readerInst
    },
  },
})
</script>
<style scoped>
.dot-btn {
  top: 10vh;
  right: 15px;
  position: fixed;
  width: 50px;
  height: 50px;
  background: rgb(53, 101, 244);
  -moz-border-radius: 50px;
  -webkit-border-radius: 50px;
  border-radius: 50px;
}
</style>
  