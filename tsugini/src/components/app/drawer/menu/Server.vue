<template>
  <q-expansion-item
    v-model="expanded"
    icon="mdi-cog"
    :label="
      $formatMessage({
        description: 'Drawer menu for Server',
        defaultMessage: 'Server',
        id: 'IpvWiZ',
      })
    "
  >
    <template #header>
      <q-item-section avatar>
        <q-icon name="mdi-cog">
          <Transition name="fab">
            <q-badge
              v-if="!expanded && unreadCount > 0"
              color="info"
              rounded
              floating
            />
          </Transition>
        </q-icon>
      </q-item-section>
      <q-item-section>
        {{
          $formatMessage({
            description: 'Drawer menu for Server',
            defaultMessage: 'Server',
            id: 'IpvWiZ',
          })
        }}
      </q-item-section>
    </template>

    <q-item
      to="/server/users"
      clickable
      :inset-level="1"
      active-class="drawer-menu-active"
    >
      <q-item-section>
        {{
          $formatMessage({
            description: 'Drawer menu for Server > Users',
            defaultMessage: 'Users',
            id: 'JGOfZq',
          })
        }}
      </q-item-section>
    </q-item>

    <q-item
      to="/server/announcements"
      clickable
      :inset-level="1"
      active-class="drawer-menu-active"
    >
      <q-item-section>
        <q-item-label
          >{{
            $formatMessage({
              description: 'Drawer menu for Server > Announcements',
              defaultMessage: 'Announcements',
              id: 'G7quju',
            })
          }}
        </q-item-label>
      </q-item-section>
      <q-item-section side>
        <Transition name="fab">
          <q-badge
            v-if="unreadCount > 0"
            color="info"
            rounded
            >{{ unreadCount }}</q-badge
          >
        </Transition>
      </q-item-section>
    </q-item>

    <q-item
      to="/server/updates"
      clickable
      :inset-level="1"
      active-class="drawer-menu-active"
    >
      <q-item-section>{{
        $formatMessage({
          description: 'Drawer menu for Server > Updates',
          defaultMessage: 'Updates',
          id: 'lDnmZD',
        })
      }}</q-item-section>
    </q-item>

    <!--    <q-item-->
    <!--      to="/server/settings"-->
    <!--      :title="-->
    <!--        $formatMessage({-->
    <!--          description: 'Drawer menu for Server > Settings',-->
    <!--          defaultMessage: 'Settings',-->
    <!--          id: 'HaWCi3',-->
    <!--        })-->
    <!--      "-->
    <!--    />-->
    <!--    <q-item-->
    <!--      to="/server/ui"-->
    <!--      :title="-->
    <!--        $formatMessage({-->
    <!--          description: 'Drawer menu for Server > User Interface',-->
    <!--          defaultMessage: 'User Interface',-->
    <!--          id: 'Yf4DJ2',-->
    <!--        })-->
    <!--      "-->
    <!--    />-->
    <!--    <q-item-->
    <!--      to="/server/metrics"-->
    <!--      :title="-->
    <!--        $formatMessage({-->
    <!--          description: 'Drawer menu for Server > Metrics',-->
    <!--          defaultMessage: 'Metrics',-->
    <!--          id: '2g7iOx',-->
    <!--        })-->
    <!--      "-->
    <!--    />-->

    <!--    <q-item-->
    <!--      to="/server/announcements"-->
    <!--      :title="-->
    <!--        $formatMessage({-->
    <!--          description: 'Drawer menu for Server > Announcements',-->
    <!--          defaultMessage: 'Announcements',-->
    <!--          id: 'G7quju',-->
    <!--        })-->
    <!--      "-->
    <!--    >-->
    <!--      <template #append>-->
    <!--        <v-badge-->
    <!--          :model-value="unreadCount > 0"-->
    <!--          :content="unreadCount"-->
    <!--          inline-->
    <!--          color="info"-->
    <!--        />-->
    <!--      </template>-->
    <!--    </q-item>-->

    <!--    <q-item-->
    <!--      to="/server/updates"-->
    <!--      :title="-->
    <!--        $formatMessage({-->
    <!--          description: 'Drawer menu for Server > Updates',-->
    <!--          defaultMessage: 'Updates',-->
    <!--          id: 'lDnmZD',-->
    <!--        })-->
    <!--      "-->
    <!--    />-->
  </q-expansion-item>
</template>

<script setup lang="ts">
import { useAnnouncements } from 'colada/queries/announcements'

const { unreadCount } = useAnnouncements()

const expanded = ref<boolean>(false)
</script>

<style scoped lang="scss">
@use 'styles/transitions/fab';
@use 'styles/drawer';
</style>
