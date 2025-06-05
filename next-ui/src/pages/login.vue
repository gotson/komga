<template>
  <v-form
    v-model="formValid"
    :disabled="isLoading"
    @submit.prevent="submitForm()"
  >
    <v-container max-width="550px">
      <v-row justify="center">
        <v-col>
          <v-img src="@/assets/logo.svg" />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-text-field
            v-model="username"
            :label="
              $formatMessage({
                description: 'Login screen: email field label',
                defaultMessage: 'Email',
                id: 'QIr0z7',
              })
            "
            autofocus
            :rules="[rules.required(), rules.email()]"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-text-field
            v-model="password"
            :label="
              $formatMessage({
                description: 'Login screen: password field label',
                defaultMessage: 'Password',
                id: '5AAGkA',
              })
            "
            type="password"
            :rules="[rules.required()]"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-checkbox
            v-model="rememberMe"
            :label="
              $formatMessage({
                description: 'Login screen: Remember Me checkbox',
                defaultMessage: 'Remember Me',
                id: '0YG9GQ',
              })
            "
            hide-details
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-btn
            :text="
              $formatMessage({
                description: 'Login screen: Sign In button',
                defaultMessage: 'Sign in',
                id: '02SRax',
              })
            "
            :loading="isLoading"
            type="submit"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-snackbar
            v-model="showError"
            color="error"
            :text="showErrorText"
            timer="red"
          >
            <template v-slot:actions>
              <v-btn
                color="white"
                variant="text"
                @click="showError = false"
              >
                Dismiss
              </v-btn>
            </template></v-snackbar
          >
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<script lang="ts" setup>
import { type ErrorCause, komgaClient } from '@/api/komga-client'
import { useMutation, useQueryCache } from '@pinia/colada'
import { useRules } from 'vuetify/labs/rules'

const rules = useRules()

const formValid = ref<boolean>(false)
const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const showError = ref<boolean>(false)
const showErrorText = ref<string>('')

const router = useRouter()
const route = useRoute()

const queryCache = useQueryCache()
const { mutate: performLogin, isLoading } = useMutation({
  mutation: () =>
    komgaClient.GET('/api/v2/users/me', {
      headers: {
        authorization: 'Basic ' + btoa(username.value + ':' + password.value),
        'X-Requested-With': 'XMLHttpRequest',
      },
      params: {
        query: {
          'remember-me': rememberMe.value,
        },
      },
    }),
  onSuccess: ({ data }) => {
    queryCache.setQueryData(['current-user'], data)
    queryCache.cancelQueries({ key: ['current-user'] })
    if (route.query.redirect) void router.push({ path: route.query.redirect.toString() })
    else void router.push('/')
  },
  onError: (error) => {
    showErrorText.value = (error.cause as ErrorCause).message || 'Invalid authentication'
    showError.value = true
  },
})

function submitForm() {
  if (formValid.value) performLogin()
}
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
