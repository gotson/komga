<template>
  <div>
    <v-app-bar
      app
      hide-on-scroll
    >
      <v-app-bar-nav-icon @click.stop="toggleDrawer"></v-app-bar-nav-icon>

      <v-tabs v-if="tabs.length > 0">
        <v-tab v-for="(t, index) in tabs" :key="index"
               :id="t.id"
               :to="{name: t.route}"
        >{{ t.name }}
        </v-tab>
      </v-tabs>
    </v-app-bar>

    <v-navigation-drawer app v-model="drawerVisible">
      <v-list-item>
        <v-list-item-avatar>
          <v-img src="../assets/logo.svg"></v-img>
        </v-list-item-avatar>

        <v-list-item-content>
          <v-list-item-title class="title">
            Komga
          </v-list-item-title>
        </v-list-item-content>
      </v-list-item>

      <v-divider></v-divider>

      <v-list>
        <v-list-item :to="{name: 'home'}" exact>
          <v-list-item-icon>
            <v-icon>mdi-home</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>Home</v-list-item-title>
          </v-list-item-content>
        </v-list-item>

        <v-list-item>
          <v-list-item-icon>
            <v-icon>mdi-book-multiple</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>Libraries</v-list-item-title>
          </v-list-item-content>
          <v-list-item-action v-if="isAdmin">
            <v-btn icon :to="{name: 'addlibrary'}" exact>
              <v-icon>mdi-plus</v-icon>
            </v-btn>
          </v-list-item-action>
        </v-list-item>

        <v-list-item v-for="(l, index) in libraries"
                     :key="index"
                     dense
                     :to="{name:'browse-library', params: {libraryId: l.id}}"
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
            <v-btn icon @click="promptDeleteLibrary(l)">
              <v-icon>mdi-delete</v-icon>
            </v-btn>
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

        <!--        <v-list-item @click="logout">-->
        <!--          <v-list-item-icon>-->
        <!--            <v-icon>mdi-power</v-icon>-->
        <!--          </v-list-item-icon>-->
        <!--          <v-list-item-content>-->
        <!--            <v-list-item-title>Log out</v-list-item-title>-->
        <!--          </v-list-item-content>-->
        <!--        </v-list-item>-->
      </v-list>
    </v-navigation-drawer>

    <v-content>
      <v-container fluid>
        <router-view/>
      </v-container>
    </v-content>

    <library-delete-dialog v-model="modalDeleteLibrary"
                           :library="libraryToDelete">
    </library-delete-dialog>

    <v-snackbar
      v-model="snackbar"
      bottom
      color="error"
    >
      {{ snackText }}
      <v-btn
        text
        @click="snackbar = false"
      >
        Close
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import LibraryDeleteDialog from '@/components/LibraryDeleteDialog.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'home',
  components: { LibraryDeleteDialog },
  data: () => ({
    drawerVisible: true,
    modalAddLibrary: false,
    modalDeleteLibrary: false,
    libraryToDelete: {} as LibraryDto,
    snackbar: false,
    snackText: ''
  }),
  computed: {
    libraries (): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    },
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
    tabs () {
      if (this.$store.state.route.name) {
        if (this.$store.state.route.name.startsWith('settings')) {
          return [
            { id: 'tab-users', route: 'settings-users', name: 'Users' }
          ]
        }
        return []
      }
      return []
    }
  },
  async mounted () {
    try {
      await this.$store.dispatch('getMe')
      await this.$store.dispatch('getLibraries')
    } catch (e) {
      this.showSnack(e.message)
    }
  },
  methods: {
    toggleDrawer () {
      this.drawerVisible = !this.drawerVisible
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    promptDeleteLibrary (library: LibraryDto) {
      this.libraryToDelete = library
      this.modalDeleteLibrary = true
    },
    logout () {
    }
  }
})
</script>
