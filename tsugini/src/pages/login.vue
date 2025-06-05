<template>
  <q-page padding>
    <div class="row justify-center">
      <div class="col-xs-8 col-sm-6 col-md-3">
        <q-img
          src="~assets/logo.svg"
          no-spinner
        />
      </div>
    </div>

    <q-form
      greedy
      @submit="performLogin()"
    >
      <div class="row justify-center">
        <div class="col-xs-10 col-sm-6 col-md-4">
          <q-input
            v-model="username"
            label="Email"
            autofocus
            :disable="isLoading"
            lazy-rules
            :rules="[required(), (x, r) => r.email(x) || 'Must be a valid email address']"
          />
        </div>
      </div>

      <div class="row justify-center">
        <div class="col-xs-10 col-sm-6 col-md-4">
          <q-input
            v-model="password"
            label="Password"
            type="password"
            :disable="isLoading"
            lazy-rules
            :rules="[required()]"
          />
        </div>
      </div>

      <div class="row justify-center q-my-sm">
        <div class="col-xs-10 col-sm-6 col-md-4">
          <q-checkbox
            v-model="app.loginRememberMe"
            label="Remember Me"
            :disable="isLoading"
          />
        </div>
      </div>

      <div class="row justify-center q-my-sm">
        <div class="col-xs-10 col-sm-6 col-md-4">
          <q-btn
            class="row"
            label="Sign in"
            :loading="isLoading"
            color="primary"
            type="submit"
          />
        </div>
      </div>
    </q-form>
  </q-page>
</template>

<script lang="ts" setup>
import { useMutation, useQueryCache } from '@pinia/colada'
import type { ErrorCause } from 'api/komga-client'
import { komgaClient } from 'api/komga-client'
import { useAppStore } from 'stores/app'
import { Notify } from 'quasar'
import { required } from 'utils/rules'

const username = ref('')
const password = ref('')

const router = useRouter()
const route = useRoute()

const app = useAppStore()

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
          'remember-me': app.loginRememberMe,
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
    Notify.create({
      type: 'negative',
      progress: true,
      message: (error.cause as ErrorCause).message!,
      actions: [{ label: 'Dismiss', color: 'white' }],
    })
  },
})
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
