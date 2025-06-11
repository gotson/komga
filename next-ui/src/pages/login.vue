<template>
  <v-form
    v-model="formValid"
    :disabled="isLoading"
    @submit.prevent="submitForm()"
  >
    <v-container max-width="450px">
      <v-row justify="center">
        <v-col cols="10">
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
            :error-messages="loginError"
            @update:modelValue="loginError = ''"
          />
        </v-col>
      </v-row>

      <v-row
        align="center"
        justify="space-between"
      >
        <v-col cols="auto">
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

        <v-col cols="auto">
          <a
            href="https://komga.org/docs/faq#i-forgot-my-password"
            target="_blank"
            class="link-underline"
          >
            {{
              $formatMessage({
                description: 'Login screen: Forgot your password link',
                defaultMessage: 'Forgot your password?',
                id: 'r6JNfI',
              })
            }}
          </a>
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
            block
          />
        </v-col>
      </v-row>

      <v-divider class="my-8" />

      <v-row justify="center">
        <v-col cols="auto">
          <div class="d-flex ga-4">
            <LocaleSelector />
            <ThemeSelector />
          </div>
        </v-col>
      </v-row>
    </v-container>
  </v-form>
</template>

<script lang="ts" setup>
import { type ErrorCause, komgaClient } from '@/api/komga-client'
import { useMutation, useQueryCache } from '@pinia/colada'
import { useRules } from 'vuetify/labs/rules'
import { useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'

const rules = useRules()
const messagesStore = useMessagesStore()
const intl = useIntl()

const formValid = ref<boolean>(false)
const username = ref('')
const password = ref('')
const rememberMe = ref(false)
const loginError = ref<string>('')

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
    if ((error?.cause as ErrorCause)?.status === 401)
      loginError.value = intl.formatMessage({
        description: 'Login screen: error message displayed when login failed',
        defaultMessage: 'Invalid login or password',
        id: 'AjWlka',
      })
    else
      messagesStore.messages.push({
        text:
          (error?.cause as ErrorCause)?.message || intl.formatMessage(commonMessages.networkError),
      })
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
