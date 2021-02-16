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
            <v-list-item-title class="text-capitalize">{{ $t('navigation.home') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-list-item :to="{name:'browse-libraries', params: {libraryId: 'all'}}">
          <v-list-item-icon>
            <v-icon>mdi-book-multiple</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title class="text-capitalize">{{ $t('navigation.libraries') }}</v-list-item-title>
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
            <v-list-item-title class="text-capitalize">{{ $t('server_settings.server_settings') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-list-item :to="{name: 'account'}">
          <v-list-item-action>
            <v-icon>mdi-account</v-icon>
          </v-list-item-action>
          <v-list-item-content>
            <v-list-item-title class="text-capitalize">{{ $t('account_settings.account_settings') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-list-item @click="logout">
          <v-list-item-icon>
            <v-icon>mdi-power</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title class="text-capitalize">{{ $t('navigation.logout') }}</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>

      <v-divider/>

      <v-list>
        <v-list-item>
          <v-list-item-icon>
            <v-icon v-if="theme === Theme.LIGHT">mdi-brightness-7</v-icon>
            <v-icon v-if="theme === Theme.DARK">mdi-brightness-3</v-icon>
            <v-icon v-if="theme === Theme.SYSTEM">mdi-brightness-auto</v-icon>
          </v-list-item-icon>
          <v-select
            v-model="theme"
            :items="themes"
            :label="$t('home.theme')"
          ></v-select>
        </v-list-item>
      </v-list>

      <v-list>
        <v-list-item>
          <v-list-item-icon>
            <v-icon>mdi-translate</v-icon>
          </v-list-item-icon>
          <v-select v-model="locale"
                    :items="locales"
                    :label="$t('home.translation')"
          >
          </v-select>
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
import {Theme} from '@/types/themes'
import Vue from 'vue'

const cookieTheme = 'theme'
const cookieLocale = 'locale'

export default Vue.extend({
  name: 'home',
  components: {LibraryActionsMenu, SearchBox, Dialogs},
  data: function () {
    return {
      drawerVisible: this.$vuetify.breakpoint.lgAndUp,
      info: {} as ActuatorInfo,
      settings: {
        theme: Theme.LIGHT,
      },
      Theme,
      locales: this.$i18n.availableLocales.map((x: any) => ({text: this.$i18n.t('common.locale_name', x), value: x})),
    }
  },
  async created() {
    if (this.isAdmin) {
      this.info = await this.$actuator.getInfo()
    }

    if (this.$cookies.isKey(cookieTheme)) {
      const theme = this.$cookies.get(cookieTheme)
      if (Object.values(Theme).includes(theme)) {
        this.theme = theme as Theme
      }
    }

    window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', this.systemThemeChange)
  },
  async beforeDestroy() {
    window.matchMedia('(prefers-color-scheme: dark)').removeEventListener('change', this.systemThemeChange)
  },
  computed: {
    libraries(): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    },
    isAdmin(): boolean {
      return this.$store.getters.meAdmin
    },
    themes(): object[] {
      return [
        {text: this.$i18n.t(Theme.LIGHT), value: Theme.LIGHT},
        {text: this.$i18n.t(Theme.DARK), value: Theme.DARK},
        {text: this.$i18n.t(Theme.SYSTEM), value: Theme.SYSTEM},
      ]
    },

    theme: {
      get: function (): Theme {
        return this.settings.theme
      },
      set: function (theme: Theme): void {
        if (Object.values(Theme).includes(theme)) {
          this.settings.theme = theme
          this.changeTheme(theme)
          this.$cookies.set(cookieTheme, theme, Infinity)
        }
      },
    },
    locale: {
      get: function (): string {
        return this.$i18n.locale
      },
      set: function (locale: string): void {
        if (this.$i18n.availableLocales.includes(locale)) {
          this.$i18n.locale = locale
          this.$cookies.set(cookieLocale, locale, Infinity)
        }
      },
    },
  },
  methods: {
    toggleDrawer() {
      this.drawerVisible = !this.drawerVisible
    },
    systemThemeChange() {
      if (this.theme === Theme.SYSTEM) {
        this.changeTheme(this.theme)
      }
    },
    changeTheme(theme: Theme) {
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
    logout() {
      this.$store.dispatch('logout')
      this.$router.push({name: 'login'})
    },
    addLibrary() {
      this.$store.dispatch('dialogAddLibrary')
    },
  },
})
</script>
