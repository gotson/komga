<template>
  <v-menu v-if="currentUser">
    <template #activator="{ props: activatorProps }">
      <v-avatar
        v-ktooltip:bottom="
          $formatMessage({
            description: 'Application bar: user icon tooltip',
            defaultMessage: 'Open user navigation menu',
            id: 'y5GoQ3',
          })
        "
        color="primary"
        v-bind="activatorProps"
      >
        <span class="text-headline-small text-uppercase">{{ currentUser.email.charAt(0) }}</span>
      </v-avatar>
    </template>

    <v-list>
      <UserMenuItemAccount />

      <v-divider class="my-2 mx-2" />

      <UserMenuItemNavigation />

      <v-divider class="my-2 mx-2" />

      <UserMenuItemSettings />

      <v-divider class="my-2 mx-2" />

      <UserMenuItemTheme />

      <v-divider class="my-2 mx-2" />

      <UserMenuItemLocale />

      <v-divider class="my-2 mx-2" />

      <UserMenuItemLogout />
    </v-list>
  </v-menu>
</template>

<script setup lang="ts">
import { useCurrentUser } from '@/colada/users'

const { data: currentUser } = useCurrentUser()
</script>
