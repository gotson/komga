<template>
  <v-text-field
    v-model="newPassword"
    :rules="[rules.required()]"
    label="New password"
    autocomplete="off"
    :type="showPassword ? 'text' : 'password'"
    :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
    @click:append="showPassword = !showPassword"
  />
  <v-text-field
    v-model="confirmPassword"
    :rules="[rules.sameAs(newPassword)]"
    label="Confirm password"
    autocomplete="off"
    :type="showPassword ? 'text' : 'password'"
    :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
    @click:append="showPassword = !showPassword"
  />
</template>

<script setup lang="ts">
import { useRules } from 'vuetify/labs/rules'

const rules = useRules()

const newPassword = defineModel<string>({required: true})

const confirmPassword = ref<string>()
const showPassword = ref<boolean>(false)
</script>
