<template>
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
          :label="$formatMessage({
            description: 'Login screen: email field label',
            defaultMessage: 'Email',
            id: 'QIr0z7'
          })"
          autofocus
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-text-field
          v-model="password"
          :label="$formatMessage({
            description: 'Login screen: password field label',
            defaultMessage: 'Password',
            id: '5AAGkA'
          })"
          type="password"
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-checkbox
          v-model="rememberMe"
          :label="$formatMessage({
            description: 'Login screen: Remember Me checkbox',
            defaultMessage: 'Remember Me',
            id: '0YG9GQ'
          })"
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-btn
          :text="$formatMessage({
            description: 'Login screen: Sign In button',
            defaultMessage: 'Sign in',
            id: '02SRax'
          })"
          @click="performLogin"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import {komgaClient} from '@/api/komga-client'
import {useMutation, useQueryCache} from '@pinia/colada'

const username = ref('')
const password = ref('')
const rememberMe = ref(false)

const router = useRouter()
const route = useRoute()

function performLogin() {
  const queryCache = useQueryCache()
  const {mutate} = useMutation({
    mutation: () =>
      komgaClient.GET('/api/v2/users/me', {
        headers: {
          authorization: 'Basic ' + btoa(username.value + ':' + password.value),
          'X-Requested-With': 'XMLHttpRequest',
        },
        params: {
          query: {
            'remember-me': rememberMe.value,
          }
        }
      }),
    onSuccess: ({data}) => {
      queryCache.setQueryData(['current-user'], data)
      queryCache.cancelQueries({key: ['current-user']})
      if(route.query.redirect)
        void router.push({path: route.query.redirect.toString()})
      else
        void router.push('/')
    },
    onError: (error) => {
      //TODO: handle error
    },
  })

  mutate()
}
</script>

<script lang="ts">
</script>

<style scoped>

</style>
