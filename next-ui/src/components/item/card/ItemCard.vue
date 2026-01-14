<template>
  <v-card
    v-on-long-press.prevent="onCardLongPress"
    :width="width"
  >
    <v-hover
      v-slot="{ props }"
      v-model="isHovering"
      :disabled="overlayDisabled"
    >
      <div
        :class="['position-relative', { 'cursor-pointer': isPreSelect || selected }]"
        v-bind="props"
        @click="isPreSelect || selected ? emit('selection', !selected) : {}"
      >
        <v-img
          :contain="!stretchPoster"
          :position="stretchPoster ? 'top' : undefined"
          :src="posterUrl"
          lazy-src="@/assets/cover.svg"
          aspect-ratio="0.7071"
          :class="{ normal: true, inset: selected }"
        >
          <template #placeholder>
            <div class="d-flex align-center justify-center fill-height">
              <v-progress-circular
                color="grey"
                indeterminate
              />
            </div>
          </template>

          <!--  This will just show lazy-src without the v-progress  -->
          <template #error></template>

          <div
            v-if="topRightIcon || topRight"
            class="top-0 right-0 position-absolute translucent text-white px-2 py-1 font-weight-bold text-caption"
            style="border-bottom-left-radius: 4px"
          >
            <v-icon
              v-if="topRightIcon"
              :icon="topRightIcon"
            />
            <template v-else>{{ topRight }}</template>
          </div>
        </v-img>

        <!--  The overlay is outside the image, so that we can scale transform the image only -->
        <v-overlay
          :model-value="overlayShown"
          :opacity="overlayTransparent ? 0 : 0.3"
          contained
          transition="fade-transition"
          content-class="fill-height w-100"
        >
          <!--  Top right number / icon  -->
          <v-icon-btn
            v-if="!hideSelection"
            :icon="
              selected || (isPreSelect && isHovering)
                ? 'i-mdi:checkbox-marked-circle'
                : 'i-mdi:checkbox-blank-circle-outline'
            "
            :variant="selected || preSelect ? 'text' : 'plain'"
            :color="selected ? 'primary' : 'white'"
            class="top-0 left-0 position-absolute"
            @click.stop="emit('selection', !selected)"
          />

          <!--  Center FAB  -->
          <v-hover
            v-if="isHovering && fabIcon && !hideFab"
            v-slot="{ isHovering: fabHover, props: fabProps }"
          >
            <v-fab
              :icon="fabIcon"
              location="center center"
              absolute
              :variant="fabHover ? 'flat' : 'outlined'"
              :color="fabHover ? 'primary' : 'white'"
              :class="{ plain: !fabHover }"
              v-bind="fabProps"
              @click.stop="emit('clickFab')"
            />
          </v-hover>

          <!--  Bottom left quick action icon  -->
          <v-icon-btn
            v-if="isHovering && quickActionIcon && !hideQuickAction"
            :icon="quickActionIcon"
            variant="plain"
            color="white"
            class="bottom-0 left-0 position-absolute"
            @click.stop="emit('clickQuickAction')"
            @mouseenter="(event: Event) => quickActionMouseEnter(event)"
          />

          <!--  Bottom right menu icon  -->
          <v-icon-btn
            v-if="isHovering && menuIcon && !hideMenu"
            :icon="menuIcon"
            variant="plain"
            color="white"
            class="bottom-0 right-0 position-absolute"
            @click.stop="emit('clickMenu')"
            @mouseenter="(event: Event) => menuMouseEnter(event)"
          />
        </v-overlay>
      </div>
    </v-hover>

    <v-card-title
      :class="['text-subtitle-2 px-2 pb-0 mb-2', { 'force-line-count text-wrap': title.lines }]"
      :style="[{ '--lines': title.lines }, { '--line-height': 1.6 }]"
      >{{ title.text }}</v-card-title
    >

    <template
      v-for="(line, i) in lines"
      :key="i"
    >
      <v-card-subtitle
        v-if="line.text || line.allowEmpty"
        :class="[
          'px-2 mb-1',
          { 'min-height': line.allowEmpty },
          line.classes,
          { 'force-line-count text-wrap': line.lines },
        ]"
        :style="[{ '--lines': line.lines }, { '--line-height': 1.4 }]"
        >{{ line.text }}
      </v-card-subtitle>
    </template>
  </v-card>
</template>

<script setup lang="ts">
import type { ItemCardEmits, ItemCardLine, ItemCardProps, ItemCardTitle } from '@/types/ItemCard'
import { vOnLongPress } from '@vueuse/components'
import { usePrimaryInput } from '@/composables/device'

const { isTouchPrimary } = usePrimaryInput()

function onCardLongPress() {
  if (isTouchPrimary.value) emit('cardLongPress')
}

const {
  stretchPoster = false,
  width = 150,
  disableSelection = false,
  selected = false,
  preSelect = false,
  fabIcon,
  quickActionIcon,
  quickActionMouseEnter = () => {},
  menuIcon,
  menuMouseEnter = () => {},
} = defineProps<
  ItemCardProps & {
    /**
     * Poster URL.
     */
    posterUrl?: string
    /**
     * Whether to stretch the poster or not. If `false`, the image will have the `contain` property.
     *
     * Defaults to `true`.
     */
    stretchPoster?: boolean
    /**
     * Card title. Displayed under the poster.
     */
    title: ItemCardTitle
    /**
     * Lines of text to display under the title.
     */
    lines?: ItemCardLine[]
    /**
     * Number displayed in the top-right corner.
     */
    topRight?: number
    /**
     * Icon displayed in the top-right corner. Takes precedence over `topRight`.
     */
    topRightIcon?: string
    /**
     * Icon displayed in the middle.
     */
    fabIcon?: string
    /**
     * Icon displayed in the bottom-left corner.
     */
    quickActionIcon?: string
    /**
     * Callback function called when the mouse enters the quick action button.
     * @param event
     */
    quickActionMouseEnter?: (event: Event) => void
    /**
     * Icon displayed in the bottom-right corner.
     */
    menuIcon?: string
    /**
     * Callback function called when the mouse enters the menu button.
     * @param event
     */
    menuMouseEnter?: (event: Event) => void
  }
>()

const emit = defineEmits<
  ItemCardEmits & {
    clickFab: []
    clickQuickAction: []
    clickMenu: []
    cardLongPress: []
  }
>()

const isHovering = ref(false)

const overlayDisabled = computed(() => {
  return disableSelection && !fabIcon && !quickActionIcon && !menuIcon
})
const isPreSelect = computed(() => preSelect && !selected && !disableSelection)
const overlayTransparent = computed(
  () => isTouchPrimary.value || ((selected || isPreSelect.value) && !isHovering.value),
)
const overlayShown = computed(() => isHovering.value || isPreSelect.value || selected)
const hideSelection = computed(
  () => disableSelection || (isTouchPrimary.value && !isPreSelect.value && !selected),
)
const hideFab = computed(() => selected || isPreSelect.value || isTouchPrimary.value)
const hideQuickAction = computed(() => selected || isPreSelect.value || isTouchPrimary.value)
const hideMenu = computed(() => selected || isPreSelect.value || isTouchPrimary.value)
</script>

<style lang="scss">
.min-height {
  min-height: 1.5rem;
}

.translucent {
  background-color: rgba(0, 0, 0, 0.7);
}

// Required so that opacity change in VOverlay has transition
.v-overlay__scrim {
  transition: opacity 0.3s ease;
}

// Required as initial state so that transition happens on deselect
.normal {
  transform: scale(1);
  transition: transform 0.135s cubic-bezier(0, 0, 0.2, 1);
}
.inset {
  transform: scale(0.78);
  border-radius: 8px;
}

.plain {
  opacity: 0.62;
}
</style>
