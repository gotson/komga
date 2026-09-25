<template>
  <v-btn-group
    v-if="first"
    ref="splitBtnRef"
    color=""
    :class="{ 'v-split-btn': isSplit }"
    rounded="pill"
    :size="small ? 'small' : undefined"
  >
    <v-btn
      variant="tonal"
      width="auto"
      :to="first.link"
    >
      <span
        class="text-truncate"
        style="max-width: 200px"
        >{{ first.text }}</span
      >

      <template
        v-if="first.imageUrl"
        #prepend
      >
        <v-avatar
          :image="first.imageUrl"
          class="ms-n2"
          :size="small ? 'x-small' : undefined"
        />
      </template>
    </v-btn>

    <v-btn
      v-if="isSplit"
      class="ps-2"
      variant="tonal"
      @click="menuOpen = !menuOpen"
    >
      <div class="d-flex align-center ga-0">
        <v-icon
          icon="i-mdi:plus"
          size="small"
        />
        {{ containers.length - 1 }}
      </div>
      <v-menu
        v-model="menuOpen"
        :target="splitBtnRef"
        location="bottom end"
        offset="4"
      >
        <v-list>
          <v-list-subheader class="text-title-small text-high-emphasis">{{
            $formatMessage({
              description:
                'Container chip, showing in what collection/readlist a series or book appears',
              defaultMessage: 'Appears in',
              id: 'Xxk+hl',
            })
          }}</v-list-subheader>
          <v-list-item
            v-for="(container, i) in containers"
            :key="i"
            :title="container.text"
            :subtitle="container.subTitle"
            :to="container.link"
          >
            <template #prepend>
              <v-avatar
                v-if="container.imageUrl"
                :image="container.imageUrl"
              />
              <v-avatar
                v-else
                icon="i-mdi:image-album"
              />
            </template>
          </v-list-item>
        </v-list>
      </v-menu>
    </v-btn>
  </v-btn-group>
</template>

<script setup lang="ts">
import type { RouteLocationObject } from '@/types/route'

export type Container = {
  text: string
  subTitle?: string
  imageUrl?: string
  link?: RouteLocationObject
}

const { containers } = defineProps<{
  containers: Container[]
  small?: boolean
}>()

const isSplit = computed(() => containers.length > 1)

const first = computed(() => containers[0])

const menuOpen = shallowRef(false)
const splitBtnRef = ref<ComponentPublicInstance | undefined>(undefined)
</script>

<style scoped lang="scss">
.v-split-btn {
  .v-btn:last-child {
    margin-inline: 4px 0;
    min-width: 0;
  }

  .v-btn {
    border-radius: 4px;
  }
}
</style>
