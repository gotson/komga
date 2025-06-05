<template>
  <div class="column items-center">
    <q-avatar
      v-if="icon"
      :color="avatarColor"
      :size="iconSize"
      class="q-mb-sm"
    >
      <q-icon
        :name="icon"
        :color="iconColor"
      />
    </q-avatar>
    <div class="text-h4">{{ title }}</div>
    <div class="text-subtitle1">{{ subTitle }}</div>
  </div>
</template>

<script setup lang="ts">
interface Props {
  title: string
  subTitle?: string
  icon?: string
  iconColor?: string
  iconSize?: string
  avatarColor?: string
}

const { title, subTitle, icon, iconSize, iconColor, avatarColor } = defineProps<Props>()
</script>
