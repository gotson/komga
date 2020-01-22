<template>
  <v-container fluid class="pa-6">
    <v-row>
      <span class="headline">Users</span>
    </v-row>
    <v-row>
      <v-col cols="12" md="8" lg="6" xl="4">
        <div style="position: relative">
          <v-list
            elevation="3"
          >
            <div v-for="(u, index) in users" :key="index">
              <v-list-item
              >
                <v-tooltip bottom>
                  <template v-slot:activator="{ on }">
                    <v-list-item-icon v-on="on">
                      <v-icon v-if="u.roles.includes('ADMIN')" color="red">mdi-account-star</v-icon>
                      <v-icon v-else>mdi-account</v-icon>
                    </v-list-item-icon>
                  </template>
                  <span>{{ u.roles.includes('ADMIN') ? 'Administrator' : 'User' }}</span>
                </v-tooltip>

                <v-list-item-content>
                  <v-list-item-title>
                    {{ u.email }}
                  </v-list-item-title>
                </v-list-item-content>

                <v-list-item-action>
                  <v-tooltip bottom>
                    <template v-slot:activator="{ on }">
                      <v-btn icon @click="editUser(u)" :disabled="u.roles.includes('ADMIN')" v-on="on">
                        <v-icon>mdi-library-books</v-icon>
                      </v-btn>
                    </template>
                    <span>Edit shared libraries</span>
                  </v-tooltip>
                </v-list-item-action>

                <v-list-item-action>
                  <v-btn icon @click="promptDeleteUser(u)">
                    <v-icon>mdi-delete</v-icon>
                  </v-btn>
                </v-list-item-action>
              </v-list-item>

              <v-divider v-if="index !== users.length-1"/>
            </div>
          </v-list>

          <v-btn fab absolute bottom right color="primary"
                 class="mr-6"
                 small
                 :to="{name: 'settings-users-add'}">
            <v-icon>mdi-plus</v-icon>
          </v-btn>

          <user-shared-libraries-edit-dialog v-model="modalUserSharedLibraries"
                                             :user="userToEditSharedLibraries"
          />

          <user-delete-dialog v-model="modalDeleteUser"
                              :user="userToDelete">
          </user-delete-dialog>

          <router-view/>
        </div>
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts">
import UserDeleteDialog from '@/components/UserDeleteDialog.vue'
import UserSharedLibrariesEditDialog from '@/components/UserSharedLibrariesEditDialog.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'SettingsUsers',
  components: { UserSharedLibrariesEditDialog, UserDeleteDialog },
  data: () => ({
    modalDeleteUser: false,
    modalAddUser: false,
    userToDelete: {} as UserDto,
    modalUserSharedLibraries: false,
    userToEditSharedLibraries: {} as UserWithSharedLibrariesDto
  }),
  computed: {
    users (): UserWithSharedLibrariesDto[] {
      return this.$store.state.komgaUsers.users
    }
  },
  async mounted () {
    await this.$store.dispatch('getAllUsers')
  },
  methods: {
    promptDeleteUser (user: UserDto) {
      this.userToDelete = user
      this.modalDeleteUser = true
    },
    editUser (user: UserWithSharedLibrariesDto) {
      this.userToEditSharedLibraries = user
      this.modalUserSharedLibraries = true
    }
  }
})
</script>

<style scoped>

</style>
