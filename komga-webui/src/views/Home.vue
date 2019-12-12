<template>
  <div>
    <v-app-bar
      app
    >
      <v-app-bar-nav-icon @click.stop="toggleDrawer"/>

      <search-box class="flex-fill"/>

    </v-app-bar>

    <v-navigation-drawer app v-model="drawerVisible">
      <v-list-item>
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

        <v-list-item :to="{name:'browse-libraries', params: {libraryId: 0}}">
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
            <v-btn icon @click.prevent="promptDeleteLibrary(l)">
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

        <v-list-item @click="logout">
          <v-list-item-icon>
            <v-icon>mdi-power</v-icon>
          </v-list-item-icon>
          <v-list-item-content>
            <v-list-item-title>Log out</v-list-item-title>
          </v-list-item-content>
        </v-list-item>
      </v-list>
    </v-navigation-drawer>

    <v-content>
      <router-view/>
    </v-content>

    <library-delete-dialog v-model="modalDeleteLibrary"
                           :library="libraryToDelete">
    </library-delete-dialog>
  </div>
</template>

<script lang="ts">
import LibraryDeleteDialog from '@/components/LibraryDeleteDialog.vue'
import SearchBox from '@/components/SearchBox.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'home',
  components: { LibraryDeleteDialog, SearchBox },
  data: function () {
    return {
      drawerVisible: this.$vuetify.breakpoint.lgAndUp,
      modalAddLibrary: false,
      modalDeleteLibrary: false,
      libraryToDelete: {} as LibraryDto
    }
  },
  computed: {
    libraries (): LibraryDto[] {
      return this.$store.state.komgaLibraries.libraries
    },
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    }
  },
  methods: {
    toggleDrawer () {
      this.drawerVisible = !this.drawerVisible
    },
    promptDeleteLibrary (library: LibraryDto) {
      this.libraryToDelete = library
      this.modalDeleteLibrary = true
    },
    logout () {
      this.$store.dispatch('logout')
      this.$router.push({ name: 'login' })
    }
  }
})
</script>
