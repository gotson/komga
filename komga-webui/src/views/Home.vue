<template>
  <div>
    <v-app-bar
      app
    >
      <v-app-bar-nav-icon @click.stop="toggleDrawer"/>

      <search-box class="flex-fill"/>

    </v-app-bar>

    <v-navigation-drawer app v-model="drawerVisible">
      <v-list-item @click="$router.push({name: 'home'})" inactive class="pb-2">
        <v-list-item-avatar>
          <v-img src="../assets/logo.svg"/>
        </v-list-item-avatar>

        <v-list-item-content>
          <v-list-item-title class="title">
            Komga
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>

      <v-divider/>

      <v-list>
        <v-list-item :to="{name: 'home'}" exact>
          <v-list-item-icon>
            <v-icon>mdi-home</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>Home</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-list-item :to="{name:'browse-libraries', params: {libraryId: 'all'}}">
          <v-list-item-icon>
            <v-icon>mdi-book-multiple</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>Libraries</v-list-item-title>
          </v-list-item-content>
          <v-list-item-action v-if="isAdmin">
            <v-btn icon @click.stop.capture.prevent="addLibrary">
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>

        <v-list-item v-for="(l, index) in libraries"
                     :key="index"
                     dense
                     :to="{name:'browse-libraries', params: {libraryId: l.id}}"
        >
          <v-list-item-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-tooltip bottom :disabled="!isAdmin">
              <template v-slot:activator="{ on }">
                <v-list-item-title v-on="on">{{ l.name }}
                </v-list-item-title>
              </template>
              <span>{{ l.root }}</span>
            </v-tooltip>
          </v-list-item-content>
          <v-list-item-action v-if="isAdmin">
            <library-actions-menu :library="l"/>
          </v-list-item-action>
        </v-list-item>

        <v-list-item :to="{name: 'settings'}" v-if="isAdmin">
          <v-list-item-action>
            <v-icon>mdi-settings</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Server settings</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-list-item :to="{name: 'account'}">
          <v-list-item-action>
            <v-icon>mdi-account</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title>Account settings</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-list-item @click="logout">
          <v-list-item-icon>
            <v-icon>mdi-power</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>Log out</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>

      <v-spacer/>

      <template v-slot:append>
        <div v-if="isAdmin && !$_.isEmpty(info)"
             class="pa-2 pb-6 caption"
        >
          <div>v{{ info.build.version }}-{{ info.git.branch }}</div>
        </div>
      </template>
    </v-navigation-drawer>

    <v-content>
      <dialogs/>
      <router-view/>
    </v-content>
  </div>
</template>

<script lang="ts">
import Dialogs from '@/components/Dialogs.vue'
import LibraryActionsMenu from '@/components/menus/LibraryActionsMenu.vue'
import SearchBox from '@/components/SearchBox.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'home',
  components: { LibraryActionsMenu, SearchBox, Dialogs },
  data: function () {
    return {
      drawerVisible: this.$vuetify.breakpoint.lgAndUp,
      info: {} as ActuatorInfo,
    }
  },
  async created () {
    if (this.isAdmin) {
      this.info = await this.$actuator.getInfo()
    }
  },
  computed: {
    libraries (): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    },
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    toggleDrawer () {
      this.drawerVisible = !this.drawerVisible
    },
    logout () {
      this.$store.dispatch('logout')
      this.$router.push({ name: 'login' })
    },
    addLibrary () {
      this.$store.dispatch('dialogAddLibrary')
    },
  },
})
</script>
