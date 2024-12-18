<template>
  <v-container fluid class="pa-6">
    <v-row>
      <v-col>
        <div v-if="$store.getters.isLatestVersion() == 1">
          <v-alert type="success" text>
            {{ $t('updates.latest_installed')}}
          </v-alert>
        </div>
        <div v-if="$store.getters.isLatestVersion() == 0">
          <v-alert type="warning" text>
            {{ $t('updates.available')}}
          </v-alert>
        </div>
      </v-col>
    </v-row>

    <div v-for="(release, index) in $store.state.releases" :key="index">
      <v-row justify="space-between" align="center">
        <v-col cols="auto">
          <div>
            <a :href="release.url" target="_blank" class="text-h4 font-weight-medium link-underline me-2">{{
                release.version
              }}</a>
            <v-chip
              v-if="release.version == currentVersion"
              class="mx-2 mt-n3"
              small
              label
              color="info"
            >Currently installed</v-chip>
            <v-chip
              v-if="release.version == latest?.version"
              class="mx-2 mt-n3"
              small
              label
            >Latest</v-chip>
          </div>
          <div class="mt-2 subtitle-1">
            {{ new Intl.DateTimeFormat($i18n.locale, {dateStyle: 'long'}).format(release.releaseDate) }}
          </div>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <div v-html="marked(release.description)"></div>
        </v-col>
      </v-row>
      <v-divider class="my-8" v-if="index != $store.state.releases.length - 1"/>
    </div>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {marked} from 'marked'

export default Vue.extend({
  name: 'UpdatesView',
  data: () => ({
    marked,
  }),
  computed: {
    latest(): ReleaseDto | undefined {
      return this.$store.state.releases.find((x: ReleaseDto) => x.latest)
    },
    currentVersion(): string {
      return this.$store.state.actuatorInfo?.build?.version
    },
  },
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.$actuator.getInfo()
        .then(x => this.$store.commit('setActuatorInfo', x))
      this.$komgaReleases.getReleases()
        .then(x => this.$store.commit('setReleases', x))
    },
  },
})
</script>

<style scoped>

</style>
