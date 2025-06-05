<template>
  <q-dialog
    ref="dialogRef"
    @hide="onDialogHide"
  >
    <q-card class="q-dialog-plugin q-pa-xs">
      <q-card-section v-if="title || subtitle">
        <div class="text-h6">{{ props.title }}</div>
        <div class="text-subtitle1 text-weight-light">{{ props.subtitle }}</div>
      </q-card-section>

      <q-form
        greedy
        @submit="onDialogOK(newPassword)"
      >
        <q-card-section>
          <q-input
            v-model="newPassword"
            :rules="[required()]"
            lazy-rules
            :label="
              $formatMessage({
                description: 'User password change dialog: New Password field label',
                defaultMessage: 'New password',
                id: 'WhasCZ',
              })
            "
            autocomplete="off"
            autofocus
            outlined
            :type="showPassword ? 'text' : 'password'"
          >
            <template #append>
              <q-btn
                flat
                round
                :icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                @click="showPassword = !showPassword"
                :ripple="false"
              />
            </template>
          </q-input>

          <q-input
            v-model="confirmPassword"
            class="q-mt-sm"
            :rules="[
              sameAs(
                newPassword,
                $formatMessage({
                  description: 'User password change dialog: Error message if passwords differ',
                  defaultMessage: 'Passwords must be identical',
                  id: 'LaxrEO',
                }),
              ),
            ]"
            lazy-rules
            :label="
              $formatMessage({
                description: 'User password change dialog: Confirm Password field label',
                defaultMessage: 'Confirm password',
                id: 'nJiYF7',
              })
            "
            autocomplete="off"
            outlined
            :type="showPassword ? 'text' : 'password'"
          >
            <template #append>
              <q-btn
                flat
                round
                :icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                @click="showPassword = !showPassword"
                :ripple="false"
              />
            </template>
          </q-input>
        </q-card-section>

        <q-card-actions align="right">
          <q-btn
            label="Cancel"
            flat
            rounded
            color="primary"
            @click="onDialogCancel"
          />
          <q-btn
            label="Save"
            flat
            rounded
            color="primary"
            type="submit"
          />
        </q-card-actions>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { useDialogPluginComponent } from 'quasar'
import { required, sameAs } from 'utils/rules'

const props = defineProps<{
  title?: string
  subtitle?: string
}>()

defineEmits([
  // REQUIRED; need to specify some events that your
  // component will emit through useDialogPluginComponent()
  ...useDialogPluginComponent.emits,
])

const { dialogRef, onDialogHide, onDialogOK, onDialogCancel } = useDialogPluginComponent()
// dialogRef      - Vue ref to be applied to QDialog
// onDialogHide   - Function to be used as handler for @hide on QDialog
// onDialogOK     - Function to call to settle dialog with "ok" outcome
//                    example: onDialogOK() - no payload
//                    example: onDialogOK({ /*...*/ }) - with payload
// onDialogCancel - Function to call to settle dialog with "cancel" outcome

const newPassword = ref<string>()
const confirmPassword = ref<string>()
const showPassword = ref<boolean>(false)
</script>
