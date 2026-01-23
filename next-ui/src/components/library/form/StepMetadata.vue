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
              defaultMessage: 'Import book metadata',
              id: '3b7v1+',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.importComicInfoSeries"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - comicinfo series metadata',
              defaultMessage: 'Import series metadata',
              id: 'MoOECu',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.importComicInfoSeriesAppendVolume"
          :disabled="!model.importComicInfoSeries"
          :label="
            $formatMessage({
              description:
                'Form add/edit library: Metadata - comicinfo append volume to series title',
              defaultMessage: 'Append volume to series title',
              id: 'AjBiEw',
            })
          "
          hide-details
          class="ms-8"
        />

        <v-checkbox
          v-model="model.importComicInfoCollection"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - comicinfo collections',
              defaultMessage: 'Create or add to collections',
              id: '3MUQYL',
            })
          "
          hide-details
        >
          <template #append>
            <v-icon
              v-tooltip:bottom="
                $formatMessage({
                  description:
                    'Form add/edit library: Metadata - Comicinfo.xml collections - information tooltip',
                  defaultMessage: `The 'SeriesGroup' element will be used`,
                  id: 'VcueWO',
                })
              "
              icon="i-mdi:information-outline"
            ></v-icon>
          </template>
        </v-checkbox>

        <v-checkbox
          v-model="model.importComicInfoReadList"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - comicinfo read lists',
              defaultMessage: 'Create or add to read lists',
              id: '/Acs82',
            })
          "
          hide-details
        >
          <template #append>
            <v-icon
              v-tooltip:bottom="
                $formatMessage({
                  description:
                    'Form add/edit library: Metadata - Comicinfo.xml collections - information tooltip',
                  defaultMessage: `The 'AlternateSeries' or 'StoryArc' elements will be used`,
                  id: 'nyxfPy',
                })
              "
              icon="i-mdi:information-outline"
            ></v-icon>
          </template>
        </v-checkbox>
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
              defaultMessage: 'Import book metadata',
              id: '81BMAQ',
            })
          "
          hide-details
        />

        <v-checkbox
          v-model="model.importEpubSeries"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - epub series metadata',
              defaultMessage: 'Import series metadata',
              id: '4CX0cY',
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
              defaultMessage: 'Mylar (series.json)',
              id: 'FVq9w5',
            })
          }}
        </div>

        <v-checkbox
          v-model="model.importMylarSeries"
          :label="
            $formatMessage({
              description: 'Form add/edit library: Metadata - mylar series metadata',
              defaultMessage: 'Import series metadata',
              id: '9TJRkm',
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
              defaultMessage: 'Use local artwork',
              id: 'GnHrCd',
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
              defaultMessage: 'Barcode',
              id: 'jyrqhz',
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
              defaultMessage: 'Import ISBN from barcode',
              id: 'B2wcN0',
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
