<template>
  <div>
    <v-list>
      <v-list-item class="contrast-1">
        <v-list-item-content>
          <v-list-item-title class="text-uppercase">{{ $t('common.reorder') }}</v-list-item-title>
        </v-list-item-content>
        <v-list-item-action class="ma-0">
          <v-btn icon @click.stop.capture.prevent="dismiss">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-list-item-action>
      </v-list-item>

      <v-list-item class="text--disabled">
        <v-list-item-icon>
          <v-icon>mdi-home</v-icon>
        </v-list-item-icon>
        <v-list-item-content>
          <v-list-item-title>{{ $t('navigation.home') }}</v-list-item-title>
        </v-list-item-content>
      </v-list-item>

      <draggable
        v-model="localItems"
        v-bind="dragOptions"
        handle=".handle"
      >
        <template v-for="(l, index) in localItems">
          <v-hover :key="index"
                   v-slot="{ hover }"
                   :disabled="!l.unpinned"
          >
            <v-list-item>
              <v-list-item-icon>
                <v-icon class="handle">mdi-drag-horizontal-variant</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title class="handle">{{ l.name }}</v-list-item-title>
              </v-list-item-content>
              <v-list-item-icon>
                <v-btn icon v-if="!l.unpinned" @click.stop.capture.prevent="unpin(l.id)" x-small>
                  <v-icon>mdi-pin</v-icon>
                </v-btn>
                <v-btn icon v-if="hover && l.unpinned" @click.stop.capture.prevent="pin(l.id)" x-small>
                  <v-icon>mdi-pin-off</v-icon>
                </v-btn>
              </v-list-item-icon>
            </v-list-item>
          </v-hover>
        </template>
      </draggable>
    </v-list>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import draggable from 'vuedraggable'
import {LibraryDto} from '@/types/komga-libraries'
import {ClientSettingLibraryUpdate} from '@/types/komga-clientsettings'

export default Vue.extend({
  name: 'ReorderLibraries',
  components: {draggable},
  data: () => {
    return {
      localItems: [] as LibraryDto[],
      unwatch: false,
    }
  },
  computed: {
    dragOptions(): any {
      return {
        animation: 200,
        ghostClass: 'ghost',
      }
    },
  },
  mounted() {
    this.localItems = this.$store.getters.getLibraries
  },
  watch: {
    localItems: {
      handler(val: LibraryDto[]) {
        const newSettings = val.map((it, index) => ({
          libraryId: it.id,
          patch: {
            order: index,
          },
        } as ClientSettingLibraryUpdate))

        this.$store.dispatch('updateLibrariesSettings', newSettings)
      },
      immediate: true,
    },
  },
  methods: {
    dismiss() {
      this.$emit('dismiss')
    },
    unpin(libraryId: string) {
      this.$store.dispatch('updateLibrarySetting', {
        libraryId: libraryId,
        patch: {
          unpinned: true,
        },
      } as ClientSettingLibraryUpdate)
      this.localItems.find(it => it.id == libraryId).unpinned = true
    },
    pin(libraryId: string) {
      this.$store.dispatch('updateLibrarySetting', {
        libraryId: libraryId,
        patch: {
          unpinned: false,
        },
      } as ClientSettingLibraryUpdate)
      this.localItems.find(it => it.id == libraryId).unpinned = false
    },
  },
})
</script>

<style scoped>
.handle {
  cursor: grab !important;
}

.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>
