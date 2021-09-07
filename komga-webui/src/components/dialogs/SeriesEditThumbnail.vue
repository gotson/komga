<template>
  <v-dialog v-model="modal"
            max-width="800"
            @keydown.esc="dialog = false"
  >
    <form novalidate>
      <v-card>
        <v-card-title>
          <v-icon class="mx-4">mdi-pencil</v-icon>
          {{ $t('dialog.edit_series_thumbnails.dialog_title', {series: this.$_.get(this.series, 'metadata.title')}).toString() }}
        </v-card-title>

        <v-tabs :vertical="$vuetify.breakpoint.smAndUp" v-model="tab">
          <v-tab class="justify-start">
            <v-icon left class="hidden-xs-only">mdi-upload</v-icon>
            {{ $t('dialog.edit_series_thumbnails.tab_upload') }}
          </v-tab>
          <v-tab class="justify-start" @click="getThumbnails">
            <v-icon left class="hidden-xs-only">mdi-collections</v-icon>
            {{ $t('dialog.edit_series_thumbnails.tab_gallery') }}
          </v-tab>

          <!--  Tab: Upload  -->
          <v-tab-item>
            <v-card flat>
              <v-container fluid>

                <!--  Upload  -->
                <v-row>
                  <v-col cols="12">
                    <v-file-input v-model="form.file"
                                  accept="image/*"
                                  show-size
                                  truncate-length="15"
                                  :label="$t('dialog.edit_series_thumbnails.field_upload_image')"
                    />
                  </v-col>
                </v-row>

                <v-row>
                  <v-col cols="12">
                    <v-btn
                      block
                      @click="uploadThumbnail"
                    >
                      Upload
                      <v-icon
                        right
                        dark
                      >
                        mdi-upload
                      </v-icon>
                    </v-btn>
                  </v-col>
                </v-row>

              </v-container>
            </v-card>
          </v-tab-item>

          <!--  Tab: Gallery  -->
          <v-tab-item>
            <v-card flat>
              <v-container fluid>

                <!-- List of thumbnail -->
                <v-card v-for="thumbnail in seriesThumbnails" :key="thumbnail.id">

                  <!-- Image -->
                  <v-img
                    :src="getThumbnailById(thumbnail.id)"
                  />

                  <!-- Actions -->
                  <v-card-actions>

                    <!-- Mark as Selected -->
                    <v-icon v-if="thumbnail.selected" class="mr-1">
                      mdi-check-circle
                    </v-icon>
                    <v-icon v-else class="mr-1" @click="markThumbnailAsSelected(thumbnail.id)">
                      mdi-check-circle-outline
                    </v-icon>

                    <!-- Delete -->
                    <v-icon class="mr-1" @click="deleteThumbnailById(thumbnail.id)">
                      mdi-delete
                    </v-icon>
                  </v-card-actions>
                </v-card>

              </v-container>
            </v-card>
          </v-tab-item>
        </v-tabs>

        <v-card-actions>
          <v-btn text @click="dialog = false">{{ $t('dialog.edit_series_thumbnails.button_close') }}</v-btn>
        </v-card-actions>
      </v-card>
    </form>
  </v-dialog>
</template>

<script lang="ts">
import {SeriesDto, SeriesThumbnailDto} from '@/types/komga-series'
import Vue from 'vue'
import {seriesThumbnailUrlByThumbnailId} from '@/functions/urls'

export default Vue.extend({
  name: 'SeriesEditThumbnail',
  data: () => ({
    modal: true,
    tab: 0,
    form: {
      file: (null as unknown as File),
    },
    seriesThumbnails: ([] as SeriesThumbnailDto[]),
  }),
  async created() {
    await this.getThumbnails
  },
  props: {
    series: {
      type: Object as () => SeriesDto,
      required: true,
    },
  },
  watch: {
    value(val) {
      this.modal = val
    },
    modal(val) {
      val
    },
  },
  methods: {
    async uploadThumbnail() {
      let formData = new FormData()
      formData.append('file', this.form.file)
      await this.$komgaSeries.uploadThumbnail(this.series.id, this.form.file)
    },
    getThumbnails: async function () {
      this.seriesThumbnails = await this.$komgaSeries.getThumbnails(this.series.id)
    },
    getThumbnailById: function (thumbnailId: string): string {
      return seriesThumbnailUrlByThumbnailId(this.series.id, thumbnailId)
    },
    markThumbnailAsSelected: async function (thumbnailId: string) {
      await this.$komgaSeries.markThumbnailAsSelected(this.series.id, thumbnailId)
    },
    deleteThumbnailById: async function (thumbnailId: string) {
      await this.$komgaSeries.deleteThumbnail(this.series.id, thumbnailId)
      const thumbnail = this.seriesThumbnails.find((thumbnail) => thumbnail.id === thumbnailId)
      if (thumbnail) {
        this.seriesThumbnails.unshift(thumbnail)
      }
    },
  },
})
</script>

<style scoped>

</style>
