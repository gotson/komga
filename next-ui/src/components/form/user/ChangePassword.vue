<template>
  <v-text-field
    v-model="newPassword"
    :rules="['required']"
    :label="
      $formatMessage({
        description: 'User password change dialog: New Password field label',
        defaultMessage: 'New password',
        id: 'WhasCZ',
      })
    "
    autocomplete="off"
    autofocus
    :type="showPassword ? 'text' : 'password'"
    :append-inner-icon="showPassword ? mdiEye : mdiEyeOff"
    @click:append-inner="showPassword = !showPassword"
  />
  <v-text-field
    v-model="confirmPassword"
    class="mt-2"
    :rules="[
      [
        'sameAs',
        newPassword,
        $formatMessage({
          description: 'User password change dialog: Error message if passwords differ',
          defaultMessage: 'Passwords must be identical',
          id: 'LaxrEO',
        }),
      ],
    ]"
    :label="
      $formatMessage({
        description: 'User password change dialog: Confirm Password field label',
        defaultMessage: 'Confirm password',
        id: 'nJiYF7',
      })
    "
    autocomplete="off"
    :type="showPassword ? 'text' : 'password'"
    :append-inner-icon="showPassword ? mdiEye : mdiEyeOff"
    @click:append-inner="showPassword = !showPassword"
  />
</template>

<script setup lang="ts">
import mdiEye from '~icons/mdi/eye'
import mdiEyeOff from '~icons/mdi/eye-off'

const newPassword = defineModel<string>({ required: true })

const confirmPassword = ref<string>()
const showPassword = ref<boolean>(false)
</script>
