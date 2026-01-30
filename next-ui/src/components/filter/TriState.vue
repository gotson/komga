<template>
  <v-list-item
    :title="label"
    @click="cycle()"
    ><template #prepend
      ><v-icon
        :icon="icon"
        :color="!!model ? color : undefined" /></template
  ></v-list-item>
</template>

<script setup lang="ts">
const model = defineModel<string>()

const icon = computed(() => states.value.find((it) => it.value === model.value)?.icon)

const {
  label,
  triState = true,
  color,
} = defineProps<{
  label: string
  triState?: boolean
  color?: string
}>()

const states = computed(() => [
  {
    icon: 'i-mdi:checkbox-marked',
    value: 'include',
  },
  ...(triState
    ? [
        {
          icon: 'i-mdi:close-box',
          value: 'exclude',
        },
      ]
    : []),
  {
    icon: 'i-mdi:checkbox-blank-outline',
    value: undefined,
  },
])

if (!states.value.some((it) => it.value === model.value)) model.value = undefined

function cycle() {
  const index = states.value.findIndex((x) => x.value === model.value)
  const newIndex = (index + 1) % states.value.length
  model.value = states.value[newIndex]!.value
}
</script>

<script lang="ts"></script>

<style scoped></style>
