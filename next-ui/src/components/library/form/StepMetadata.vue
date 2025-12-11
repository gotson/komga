<template>
  <v-container class="px-0">
    <v-row>
      <v-col cols="auto">
        <div class="text-subtitle-2 d-flex ga-2 align-center">
          <div>
            {{
              $formatMessage({
                description: 'Form add/edit library: Metadata - section header for comicinfo',
                defaultMessage: 'ComicInfo.xml (CBR/CBZ)',
                id: 'fVqdik',
              })
            }}
          </div>
        </div>

        <v-checkbox
          v-model="model.importComicInfoBook"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - comicinfo book metadata',
              defaultMessage: 'Book metadata',
              id: 'YHQNM8',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.importComicInfoSeries"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - comicinfo series metadata',
              defaultMessage: 'Series metadata',
              id: '6zF2Um',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.importComicInfoSeriesAppendVolume"
          :label="
            $formatMessage({
              description:
                'Form add/edit library: Metadata - comicinfo append volume to series title',
              defaultMessage: 'Append volume to series title',
              id: 'AjBiEw',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.importComicInfoCollection"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - comcinfo collections',
              defaultMessage: 'Collections',
              id: 'Ahipg5',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.importComicInfoReadList"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - comcinfo read lists',
              defaultMessage: 'Read lists',
              id: 'AV3Iae',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>

    <v-divider class="mb-4" />

    <v-row>
      <v-col>
        <div class="text-subtitle-2">
          {{
            $formatMessage({
              description: 'Form add/edit library: Metadata - section header for epub',
              defaultMessage: 'EPUB',
              id: '4PDVX2',
            })
          }}
        </div>
        <v-checkbox
          v-model="model.importEpubBook"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - epub book metadata',
              defaultMessage: 'Book metadata',
              id: '7bZ3Eg',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.importEpubSeries"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - epub series metadata',
              defaultMessage: 'Series metadata',
              id: '8PXwjO',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>

    <v-divider class="mb-4" />

    <v-row>
      <v-col>
        <div class="text-subtitle-2">
          {{
            $formatMessage({
              description: 'Form add/edit library: Metadata - section header for mylar series.json',
              defaultMessage: 'series.json (Mylar)',
              id: 'xofdkF',
            })
          }}
        </div>

        <v-checkbox
          v-model="model.importMylarSeries"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - mylar series metadata',
              defaultMessage: 'Series metadata',
              id: 'JvSGEY',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>

    <v-divider class="mb-4" />

    <v-row>
      <v-col>
        <div class="text-subtitle-2">
          {{
            $formatMessage({
              description:
                'Form add/edit library: Metadata - section header for local media assets',
              defaultMessage: 'Local media assets',
              id: 'COn5A6',
            })
          }}
        </div>

        <v-checkbox
          v-model="model.importLocalArtwork"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - local artwork',
              defaultMessage: 'Local artwork',
              id: 'iBj0mW',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>

    <v-divider class="mb-4" />

    <v-row>
      <v-col>
        <div class="text-subtitle-2 d-flex ga-2 align-center">
          {{
            $formatMessage({
              description: 'Form add/edit library: Metadata - section header for ISBN barcode',
              defaultMessage: 'ISBN within barcode',
              id: 'eHS96Q',
            })
          }}
          <v-icon
            v-tooltip:bottom="$formatMessage(commonMessages.resourceIntensive)"
            size="small"
            color="warning"
            icon="i-mdi:alert-circle-outline"
          />
        </div>

        <v-checkbox
          v-model="model.importBarcodeIsbn"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - isbn barcode',
              defaultMessage: 'ISBN',
              id: 'cKUU8S',
            })
          "
          hide-details
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { commonMessages } from '@/utils/i18n/common-messages'
import type { components } from '@/generated/openapi/komga'

type LibraryCreationMetadata = Pick<
  components['schemas']['LibraryCreationDto'],
  | 'importComicInfoBook'
  | 'importComicInfoSeries'
  | 'importComicInfoSeriesAppendVolume'
  | 'importComicInfoCollection'
  | 'importComicInfoReadList'
  | 'importEpubBook'
  | 'importEpubSeries'
  | 'importMylarSeries'
  | 'importLocalArtwork'
  | 'importBarcodeIsbn'
>

const model = defineModel<LibraryCreationMetadata>({ required: true })
</script>
