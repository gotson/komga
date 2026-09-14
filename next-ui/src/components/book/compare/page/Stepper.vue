<template>
  <div>
    <v-hover>
      <template #default="{ isHovering, props: hoverProps }">
        <div
          v-bind="hoverProps"
          class="d-inline-flex align-center stepper-hover-shell"
        >
          <v-sheet
            v-if="pill"
            :id="`${id}-pill`"
            elevation="4"
            class="d-inline-flex align-center ga-2 rounded-pill bg-surface-variant pa-1"
            style="backdrop-filter: blur(8px)"
          />

          <div
            v-else
            :id="`${id}-div`"
            class="d-inline-flex align-center ga-2"
          />

          <Teleport
            :to="pill ? `#${id}-pill` : `#${id}-div`"
            defer
          >
            <!-- Left controls -->
            <div
              v-show="hideWhenIdle ? isHovering : true"
              class="d-flex align-center ga-2"
            >
              <v-btn
                v-bind="controlProps"
                :icon="isRtl ? 'i-mdi:chevron-double-right' : 'i-mdi:chevron-double-left'"
                :disabled="isDisabledPrev"
                @click="onFirst()"
              />
              <v-btn
                v-bind="controlProps"
                :icon="isRtl ? 'i-mdi:chevron-right' : 'i-mdi:chevron-left'"
                :disabled="isDisabledPrev"
                @click="onPrevious()"
              />
            </div>

            <!-- Page Chip -->
            <v-chip
              v-if="!hideCount"
              v-bind="controlProps"
              rounded
              :text="`${model} / ${max}`"
            />

            <!-- Sync Icon (Only active when collapsible) -->
            <v-btn
              v-show="hideCount && hideWhenIdle && !isHovering"
              icon="i-mdi:sync"
              v-bind="controlProps"
              variant="text"
              class="show-on-leave-box"
            />

            <v-divider
              v-show="pill && hideCount && (hideWhenIdle ? isHovering : true)"
              vertical
              thickness="1"
              opacity=".5"
              inset
            />

            <!-- Right controls -->
            <div
              v-show="hideWhenIdle ? isHovering : true"
              class="d-flex align-center ga-2"
            >
              <v-btn
                v-bind="controlProps"
                :icon="isRtl ? 'i-mdi:chevron-left' : 'i-mdi:chevron-right'"
                :disabled="isDisabledNext"
                @click="onNext()"
              />
              <v-btn
                v-bind="controlProps"
                :icon="isRtl ? 'i-mdi:chevron-double-left' : 'i-mdi:chevron-double-right'"
                :disabled="isDisabledNext"
                @click="onLast()"
              />
            </div>
          </Teleport>
        </div>
      </template>
    </v-hover>
  </div>
</template>

<script setup lang="ts">
import { useRtl } from 'vuetify/framework'

const { isRtl } = useRtl()
const id = useId()

const model = defineModel<number>({ required: false })

const props = defineProps<{
  pill?: boolean
  max?: number
  hideCount?: boolean
  hideWhenIdle?: boolean
  controlProps?: object
  disablePrev?: boolean
  disableNext?: boolean
}>()

const emit = defineEmits<{
  'click:first': []
  'click:previous': []
  'click:next': []
  'click:last': []
}>()

const isDisabledPrev = computed(() => props.disablePrev || (!!model.value && model.value <= 1))
const isDisabledNext = computed(
  () => props.disableNext || (!!model.value && !!props.max && model.value >= props.max),
)

function onFirst() {
  emit('click:first')
  if (model.value) model.value = 1
}
function onPrevious() {
  emit('click:previous')
  if (model.value) model.value--
}
function onNext() {
  emit('click:next')
  if (model.value) model.value++
}
function onLast() {
  emit('click:last')
  if (model.value && props.max) model.value = props.max
}
</script>

<style scoped lang="scss">
.stepper-hover-shell {
  position: relative;
  /* Guarantees minimum size so zero-width states don't collapse mouse detection */
  min-height: 56px;
  min-width: 56px;
}

/* Invisible hit-area expansion prevents hover loss during width transitions */
.stepper-hover-shell::before {
  content: '';
  position: absolute;
  top: -12px;
  bottom: -12px;
  left: -12px;
  right: -12px;
  pointer-events: auto;
}
</style>
