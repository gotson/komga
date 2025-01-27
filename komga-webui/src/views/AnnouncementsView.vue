<template>
  <v-container fluid class="pa-6">
    <div v-for="(item, index) in $store.state.announcements.items" :key="item.id">
      <v-row justify="space-between" align="center">
        <v-col cols="auto">
          <div class="ml-n2">
            <v-badge
              dot
              inline
              left
              :value="item._komga.read ? 0 : 1"
              color="info"
            >
              <a :href="item.url" target="_blank" class="text-h3 font-weight-medium link-underline">{{ item.title }}</a>
              <v-icon
                x-small
                color="grey"
                class="ps-1"
              >
                mdi-open-in-new
              </v-icon>
            </v-badge>
          </div>
          <div class="mt-2 subtitle-1">
            {{ new Intl.DateTimeFormat($i18n.locale, {dateStyle: 'long'}).format(item.date_modified) }}
          </div>
        </v-col>
        <v-col cols="auto">
          <v-tooltip :left="!$vuetify.rtl" :right="$vuetify.rtl">
            <template v-slot:activator="{ on }">
              <v-btn icon elevation="5" color="success" v-on="on" :disabled="item._komga.read"
                     @click="markRead(item.id)">
                <v-icon>mdi-check</v-icon>
              </v-btn>
            </template>
            {{ $t('announcements.mark_read') }}
          </v-tooltip>
        </v-col>
      </v-row>

      <v-row>
        <v-col cols="12">
          <div v-html="item.content_html"></div>
        </v-col>
      </v-row>
      <v-divider class="my-8" v-if="index != $store.state.announcements.items.length - 1"/>
    </div>

    <v-tooltip :left="!$vuetify.rtl" :right="$vuetify.rtl">
      <template v-slot:activator="{ on }">
        <v-fab-transition>
          <v-btn v-if="$store.getters.getUnreadAnnouncementsCount()"
                 color="success"
                 fab
                 bottom
                 right
                 fixed
                 elevation="10"
                 v-on="on"
                 @click="markAllRead"
          >
            <v-icon>mdi-check-all</v-icon>
          </v-btn>
        </v-fab-transition>
      </template>
      {{ $t('announcements.mark_all_read') }}
    </v-tooltip>
  </v-container>
</template>

<script lang="ts">
import Vue from 'vue'
import {ItemDto} from '@/types/json-feed'

export default Vue.extend({
  name: 'AnnouncementsView',
  mounted() {
    this.loadData()
  },
  methods: {
    async loadData() {
      this.$komgaAnnouncements.getAnnouncements()
        .then(x => this.$store.commit('setAnnouncements', x))
    },
    markRead(announcementId: string) {
      this.$komgaAnnouncements.markAnnouncementsRead([announcementId])
        .then(() => this.loadData())
    },
    markAllRead() {
      this.$komgaAnnouncements.markAnnouncementsRead(this.$store.state.announcements.items.map((x: ItemDto) => x.id))
        .then(() => this.loadData())
    },
  },
})
</script>

<style scoped>

</style>
