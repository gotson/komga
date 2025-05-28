<template>
  <v-text-field
    v-model="newPassword"
    :rules="[rules.required()]"
    label="New password"
    autocomplete="off"
    autofocus
    :type="showPassword ? 'text' : 'password'"
    :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
    @click:append-inner="showPassword = !showPassword"
  />
  <v-text-field
    v-model="confirmPassword"
    class="mt-2"
    :rules="[rules.sameAs(newPassword, 'Passwords must be identical')]"
    label="Confirm password"
    autocomplete="off"
    :type="showPassword ? 'text' : 'password'"
    :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
    @click:append-inner="showPassword = !showPassword"
  />
</template>

<script setup lang="ts">
import { useRules } from 'vuetify/labs/rules'

const rules = useRules()

const newPassword = defineModel<string>({required: true})

const confirmPassword = ref<string>()
const showPassword = ref<boolean>(false)
</script>
