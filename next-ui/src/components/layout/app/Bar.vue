<template>
  <v-app-bar elevation="1">
    <template #prepend>
      <v-hover v-slot="{ isHovering, props }">
        <v-app-bar-nav-icon
          v-bind="props"
          @click="appStore.drawer = !appStore.drawer"
        >
          <template #default>
            <v-tooltip
              activator="parent"
              location="end"
              :text="tooltipMessage"
            />

            <v-badge
              :model-value="unreadCount > 0 && !appStore.drawer"
              dot
              floating
              color="info"
            >
              <v-icon
                :icon="
                  isHovering
                    ? appStore.drawer
                      ? 'i-tabler:layout-sidebar-left-collapse'
                      : 'i-tabler:layout-sidebar-left-expand'
                    : 'i-tabler:layout-sidebar'
                "
              />
            </v-badge>
          </template>
        </v-app-bar-nav-icon>
      </v-hover>
    </template>

    <v-app-bar-title>
      <RouterLink to="/">
        <v-avatar start>
          <v-img src="@/assets/logo.svg" />
        </v-avatar>
      </RouterLink>
      Komga
    </v-app-bar-title>

    <template #append>
      <LocaleSelector />
      <ThemeSelector />
    </template>
  </v-app-bar>
</template>

<script setup lang="ts">
import { useAppStore } from '@/stores/app'
import { useAnnouncements } from '@/colada/announcements'
import { useIntl } from 'vue-intl'

const intl = useIntl()

const appStore = useAppStore()
const { unreadCount } = useAnnouncements()

const expandMessage = intl.formatMessage({
  description: 'Sidebar button: tooltip expand',
  defaultMessage: 'Expand sidebar',
  id: 'RTNaxB',
})
const collapseMessage = intl.formatMessage({
  description: 'Sidebar button: tooltip collapse',
  defaultMessage: 'Collapse sidebar',
  id: '0JF9f5',
})
const tooltipMessage = computed(() => (appStore.drawer ? collapseMessage : expandMessage))
</script>

<script lang="ts"></script>

<style scoped></style>
