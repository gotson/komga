<template>
  <v-list>
    <v-list-subheader>
      <div class="d-flex ga-2 align-center mb-1">
        <span>{{ $formatMessage(commonMessages.filterPanelHeader) }}</span>
        <FilterChipCount
          :count="count"
          @clear="emit('clear')"
        />
      </div>
    </v-list-subheader>

    <FilterByReadStatus
      v-if="filters?.read"
      v-model="filters.read.filter.v"
      class="py-0"
    />

    <v-list class="py-0">
      <FilterByOneShot
        v-if="filters?.oneshot"
        v-model="filters.oneshot.filter"
      />
      <FilterByComplete
        v-if="filters?.complete"
        v-model="filters.complete.filter"
      />
      <FilterByUnavailable
        v-if="filters?.unavailable"
        v-model="filters.unavailable.filter"
      />
    </v-list>

    <v-expansion-panels
      v-model="filterExpansionPanels"
      variant="accordion"
      class="no-padding"
      flat
      tile
    >
      <FilterExpansionPanel
        v-if="filters?.library"
        :title="$formatMessage(commonMessages.filterPanelLibrary)"
        :count="filters.library.count"
        @clear="filters.library.clear()"
      >
        <FilterByLibrary v-model="filters.library.filter.v" />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.seriesStatus"
        :title="$formatMessage(commonMessages.filterPanelSeriesStatus)"
        :count="filters.seriesStatus.count"
        @clear="filters.seriesStatus.clear()"
      >
        <FilterBySeriesStatus v-model="filters.seriesStatus.filter.v" />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.genre"
        :title="$formatMessage(commonMessages.filterPanelGenre)"
        :count="filters.genre.count"
        @clear="filters.genre.clear()"
      >
        <FilterByGenre
          v-model="filters.genre.filter.v"
          v-model:mode="filters.genre.filter.m"
        />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.profile"
        :title="$formatMessage(commonMessages.filterPanelMediaProfile)"
        :count="filters.profile.count"
        @clear="filters.profile.clear()"
      >
        <FilterByMediaProfile v-model="filters.profile.filter.v" />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.mediaStatus"
        :title="$formatMessage(commonMessages.filterPanelMediaStatus)"
        :count="filters.mediaStatus.count"
        @clear="filters.mediaStatus.clear()"
      >
        <FilterByMediaStatus v-model="filters.mediaStatus.filter.v" />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.tag"
        :title="$formatMessage(commonMessages.filterPanelTag)"
        :count="filters.tag.count"
        @clear="filters.tag.clear()"
      >
        <FilterByTag
          v-model="filters.tag.filter.v"
          v-model:mode="filters.tag.filter.m"
          :include="tagsInclude"
        />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.publisher"
        :title="$formatMessage(commonMessages.filterPanelPublisher)"
        :count="filters.publisher.count"
        @clear="filters.publisher.clear()"
      >
        <FilterByPublisher
          v-model="filters.publisher.filter.v"
          v-model:mode="filters.publisher.filter.m"
        />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.year"
        :title="$formatMessage(commonMessages.filterPanelReleaseYear)"
        :count="filters.year.count"
        @clear="filters.year.clear()"
      >
        <FilterByReleaseYear v-model="filters.year.filter" />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.age"
        :title="$formatMessage(commonMessages.filterPanelAgeRating)"
        :count="filters.age.count"
        @clear="filters.age.clear()"
      >
        <FilterByAgeRating v-model="filters.age.filter" />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.language"
        :title="$formatMessage(commonMessages.filterPanelLanguage)"
        :count="filters.language.count"
        @clear="filters.language.clear()"
      >
        <FilterByLanguage
          v-model="filters.language.filter.v"
          v-model:mode="filters.language.filter.m"
        />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.sharingLabel"
        :title="$formatMessage(commonMessages.filterPanelSharingLabel)"
        :count="filters.sharingLabel.count"
        @clear="filters.sharingLabel.clear()"
      >
        <FilterBySharingLabel
          v-model="filters.sharingLabel.filter.v"
          v-model:mode="filters.sharingLabel.filter.m"
        />
      </FilterExpansionPanel>

      <FilterExpansionPanel
        v-if="filters?.poster"
        :title="$formatMessage(commonMessages.filterPanelPoster)"
        :count="filters.poster.count"
        @clear="filters.poster.clear()"
      >
        <FilterByPoster v-model="filters.poster.filter.v" />
      </FilterExpansionPanel>
    </v-expansion-panels>

    <v-divider
      ><span class="text-body-medium text-medium-emphasis">{{
        $formatMessage(commonMessages.filterPanelContributors)
      }}</span></v-divider
    >

    <v-expansion-panels
      v-if="filters?.contributors"
      v-model="filterExpansionPanels"
      variant="accordion"
      class="no-padding"
      flat
      tile
    >
      <FilterExpansionPanel
        v-for="(filterContributor, role) in filters.contributors.filter"
        :key="role"
        :title="
          contributorsRolesMessages?.[role]
            ? $formatMessage(contributorsRolesMessages?.[role])
            : role
        "
        :count="countFilter(filterContributor)"
        @clear="filters.contributors.clearOne(role)"
      >
        <FilterByContributor
          v-model="filterContributor.v"
          v-model:mode="filterContributor.m"
          :role="role"
        />
      </FilterExpansionPanel>
    </v-expansion-panels>

    <v-divider
      ><span class="text-body-medium text-medium-emphasis">{{
        $formatMessage(commonMessages.filterPanelNotableEntities)
      }}</span></v-divider
    >

    <v-expansion-panels
      v-if="filters?.notableEntities"
      v-model="filterExpansionPanels"
      variant="accordion"
      class="no-padding"
      flat
      tile
    >
      <FilterExpansionPanel
        v-for="(filterNotableEntity, kind) in filters.notableEntities.filter"
        :key="kind"
        :title="
          notableEntitiesKindsMessages?.[kind]
            ? $formatMessage(notableEntitiesKindsMessages?.[kind])
            : kind
        "
        :count="countFilter(filterNotableEntity)"
        @clear="filters.notableEntities.clearOne(kind)"
      >
        <FilterByNotableEntity
          v-model="filterNotableEntity.v"
          v-model:mode="filterNotableEntity.m"
          :kind="kind"
        />
      </FilterExpansionPanel>
    </v-expansion-panels>
  </v-list>
</template>

<script setup lang="ts">
import { commonMessages } from '@/utils/i18n/common-messages'
import { contributorsRolesMessages } from '@/types/referential/contributor'
import { countFilter } from '@/functions/filter'
import { notableEntitiesKindsMessages } from '@/types/referential/notableEntity'
import type { SupportedFiltersOutput } from '@/composables/filter'
import type { UnwrapRef } from 'vue'

const filters = defineModel<Partial<UnwrapRef<SupportedFiltersOutput>>>()

defineProps<{
  count: number
  tagsInclude?: 'SERIES' | 'BOOK' | 'BOTH'
}>()

const emit = defineEmits<{
  clear: []
}>()

// shared model for all the expansion-panels, so only 1 is opened at the same time
const filterExpansionPanels = ref()
</script>

<style lang="scss">
.no-padding .v-expansion-panel-text__wrapper {
  padding: 0 4px;
}
</style>
