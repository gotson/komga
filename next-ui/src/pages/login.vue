<template>
  <v-form
    ref="form"
    :disabled="isLoading"
    @submit.prevent="submitForm()"
  >
    <v-container max-width="400px">
      <v-row justify="center">
        <v-col
          cols="7"
          sm="10"
        >
          <v-img
            src="@/assets/logo.svg"
            :alt="
              $formatMessage({
                description: 'Login page: Komga logo alt text',
                defaultMessage: 'Komga logo',
                id: 'uXY7Eg',
              })
            "
          />
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
            :rules="['required', 'email']"
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
            :rules="['required']"
            :error-messages="loginError"
            @update:model-value="loginError = ''"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col class="py-0">
          <v-checkbox
            v-model="appStore.rememberMe"
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
            block
          />
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="auto">
          <a
            href="https://komga.org/docs/faq#i-forgot-my-password"
            target="_blank"
            class="link-underline text-body-2"
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

      <v-divider class="my-4" />

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
import { type ErrorCause } from '@/api/komga-client'
import { useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useAppStore } from '@/stores/app'
import { useLogin } from '@/colada/users'
import { useClaimStatus } from '@/colada/claim'

const messagesStore = useMessagesStore()
const intl = useIntl()
const appStore = useAppStore()

const form = ref()
const username = ref('')
const password = ref('')
const loginError = ref<string>('')

const router = useRouter()
const route = useRoute()

const { mutateAsync: performLogin, isLoading } = useLogin()

async function submitForm() {
  const { valid } = await form.value.validate()
  if (valid)
    performLogin({
      username: username.value,
      password: password.value,
      rememberMe: appStore.rememberMe,
    })
      .then(() => {
        if (route.query.redirect) void router.push({ path: route.query.redirect.toString() })
        else void router.push('/')
      })
      .catch((error) => {
        if ((error?.cause as ErrorCause)?.status === 401)
          loginError.value = intl.formatMessage({
            description: 'Login screen: error message displayed when login failed',
            defaultMessage: 'Invalid login or password',
            id: 'AjWlka',
          })
        else
          messagesStore.messages.push({
            text:
              (error?.cause as ErrorCause)?.message ||
              intl.formatMessage(commonMessages.networkError),
          })
      })
}

void useClaimStatus()
  .refresh()
  .then(({ data, error }) => {
    if (error) void router.push('/error')
    else if (data?.isClaimed == false) void router.push('/')
  })
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
