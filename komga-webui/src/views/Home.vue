<template>
  <div class="fill-height">
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
            <v-icon>mdi-cog</v-icon>
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

      <v-divider/>

      <v-list>
        <v-list-item>
          <v-list-item-icon>
            <v-icon v-if="activeTheme === Theme.LIGHT">mdi-brightness-7</v-icon>
            <v-icon v-if="activeTheme === Theme.DARK">mdi-brightness-3</v-icon>
            <v-icon v-if="activeTheme === Theme.SYSTEM">mdi-brightness-auto</v-icon>
          </v-list-item-icon>
          <v-select
            v-model="activeTheme"
            :items="themes"
            label="Theme"
          ></v-select>
        </v-list-item>
      </v-list>

      <v-spacer/>

      <template v-slot:append>
        <div v-if="isAdmin && !$_.isEmpty(info)"
             class="pa-2 pb-6 text-caption"
        >
          <div>v{{ info.build.version }}-{{ info.git.branch }}</div>
        </div>
      </template>
    </v-navigation-drawer>

    <v-main class="fill-height">
      <dialogs/>
      <router-view/>
    </v-main>
  </div>
</template>

<script lang="ts">
import Dialogs from '@/components/Dialogs.vue'
import LibraryActionsMenu from '@/components/menus/LibraryActionsMenu.vue'
import SearchBox from '@/components/SearchBox.vue'
import { Theme } from '@/types/themes'
import Vue from 'vue'

const cookieTheme = 'theme'

export default Vue.extend({
  name: 'home',
  components: { LibraryActionsMenu, SearchBox, Dialogs },
  data: function () {
    return {
      drawerVisible: this.$vuetify.breakpoint.lgAndUp,
      info: {} as ActuatorInfo,
      activeTheme: Theme.LIGHT,
      Theme,
      themes: [
        { text: Theme.LIGHT.valueOf(), value: Theme.LIGHT },
        { text: Theme.DARK.valueOf(), value: Theme.DARK },
        { text: Theme.SYSTEM.valueOf(), value: Theme.SYSTEM },
      ],
    }
  },
  watch: {
    activeTheme (val) {
      this.changeTheme(val)
    },
  },
  async created () {
    if (this.isAdmin) {
      this.info = await this.$actuator.getInfo()
    }

    if (this.$cookies.isKey(cookieTheme)) {
      const theme = this.$cookies.get(cookieTheme)
      if (Object.values(Theme).includes(theme)) {
        this.activeTheme = theme as Theme
      }
    }

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', this.systemThemeChange)
  },
  async beforeDestroy () {
    window.matchMedia('(prefers-color-scheme: dark)').removeEventListener('change', this.systemThemeChange)
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
    systemThemeChange () {
      if (this.activeTheme === Theme.SYSTEM) {
        this.changeTheme(this.activeTheme)
      }
    },
    changeTheme (theme: Theme) {
      this.$cookies.set(cookieTheme, theme.valueOf())
      switch (theme) {
        case Theme.DARK:
          this.$vuetify.theme.dark = true
          break

        case Theme.SYSTEM:
          this.$vuetify.theme.dark = (window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches)
          break

        default:
          this.$vuetify.theme.dark = false
          break
      }
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
