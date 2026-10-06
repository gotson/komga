<template>
  <v-form
    ref="form"
    :disabled="isLoading"
    @submit.prevent="submitForm()"
  >
    <v-container max-width="400px">
      <v-row class="justify-center mb-8">
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

      <div v-if="shouldShowLoginFields">
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
              @update:model-value="loginError = ''"
            />
          </v-col>
        </v-row>

        <v-row density="compact">
          <v-col>
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

        <v-row density="compact">
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

        <v-row
          class="justify-center"
          density="compact"
        >
          <v-col cols="auto">
            <a
              href="https://komga.org/docs/faq#i-forgot-my-password"
              target="_blank"
              class="link-underline text-body-medium"
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
      </div>

      <v-divider
        v-if="shouldShowLoginFields && shouldShowOauth2Provider"
        class="my-6"
        >{{
          $formatMessage({
            description: 'Login screen: divider between login fields and oauth2 provider list',
            defaultMessage: 'or',
            id: '811PxA',
          })
        }}</v-divider
      >

      <div v-if="shouldShowOauth2Provider">
        <v-row v-if="serverError">
          <v-col>
            <v-alert
              type="error"
              variant="tonal"
              :text="convertErrorCodes(serverError)"
            />
          </v-col>
        </v-row>

        <v-row
          v-for="provider in oauth2Providers"
          :key="provider.registrationId"
          density="compact"
        >
          <v-col>
            <v-btn
              :href="oauth2LoginUrl(provider.registrationId)"
              block
              :text="
                $formatMessage(
                  {
                    description: 'Login screen: oauth2 provider login button',
                    defaultMessage: 'Continue with {provider}',
                    id: '0ESsC0',
                  },
                  {
                    provider: provider.name,
                  },
                )
              "
              :color="oauth2ProviderStyles[provider.registrationId]?.bgColor"
              :prepend-icon="oauth2ProviderStyles[provider.registrationId]?.icon"
              :style="{ color: oauth2ProviderStyles[provider.registrationId]?.textColor }"
              :disabled="isLoading"
            />
          </v-col>
        </v-row>
      </div>

      <v-row class="justify-center mt-8">
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
import { isApiErrorWithCause } from '@/api/komga-client'
import { useMessagesStore } from '@/stores/messages'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'
import { useAppStore } from '@/stores/app'
import { useLogin } from '@/colada/users'
import { useRules } from 'vuetify'
import { useOAuth2Providers } from '@/colada/oauth2'
import { oauth2ProviderStyles } from '@/types/oauth2'
import { useErrorCodeFormatter } from '@/composables/errorCodeFormatter'
import { useRouteQuery } from '@vueuse/router'
import { useClientSettingsGlobal } from '@/colada/client-settings'
import { watchImmediate } from '@vueuse/core'
import { oauth2LoginUrl } from '@/api/links'
import { performOAuth2LoginRedirect } from '@/functions/oauth2'

const messagesStore = useMessagesStore()
const intl = useIntl()
const appStore = useAppStore()
const rules = useRules()
const { convertErrorCodes } = useErrorCodeFormatter()

const form = ref()
const username = ref('')
const password = ref('')
const loginError = ref<string>('')
const isLoading = ref(false)

const router = useRouter()
const route = useRoute()

const { data: oauth2Providers } = useOAuth2Providers()
const { globalSettings } = useClientSettingsGlobal()

const serverError = useRouteQuery('error', '')
const isLogout = useRouteQuery('logout', undefined)

const shouldShowOauth2Provider = computed(
  () => oauth2Providers.value && oauth2Providers.value.length > 0,
)

const shouldShowLoginFields = computed(
  () =>
    !(
      shouldShowOauth2Provider.value &&
      globalSettings.value['komga.nextui.oauth2'].value.hideLoginFields
    ),
)

const shouldAttemptAutoLogin = computed(
  () =>
    oauth2Providers.value &&
    oauth2Providers.value.length === 1 &&
    !serverError.value &&
    globalSettings.value['komga.nextui.oauth2'].value.hideLoginFields &&
    globalSettings.value['komga.nextui.oauth2'].value.autoLogin &&
    !isLogout.value,
)

watchImmediate(shouldAttemptAutoLogin, (val) => {
  if (val) {
    performOAuth2LoginRedirect(oauth2Providers.value?.[0]?.registrationId)
  }
})

const { mutateAsync: performLogin } = useLogin()

async function submitForm() {
  const { valid } = await form.value.validate()
  if (valid) {
    isLoading.value = true
    performLogin({
      username: username.value,
      password: password.value,
      rememberMe: appStore.rememberMe,
    })
      .then(() => {
        if (route.query.redirect) void router.push(route.query.redirect.toString())
        else void router.push('/')
      })
      .catch((error) => {
        if (isApiErrorWithCause(error) && error.cause.status === 401)
          loginError.value = intl.formatMessage({
            description: 'Login screen: error message displayed when login failed',
            defaultMessage: 'Invalid login or password',
            id: 'AjWlka',
          })
        else messagesStore.messages.push(error?.cause?.message ?? commonMessages.networkError)
        isLoading.value = false
      })
  }
}
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
