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
          <v-img src="@/assets/logo.svg" />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-alert
            color="info"
            variant="tonal"
            icon="i-mdi:info"
            :text="
              $formatMessage({
                description: 'Claim server screen: information banner',
                defaultMessage:
                  'This Komga server is not yet active, you need to create a user account to be able to access it.',
                id: '2p+JVw',
              })
            "
          >
          </v-alert>
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-text-field
            v-model="username"
            :label="
              $formatMessage({
                description: 'Claim server screen: email field label',
                defaultMessage: 'Email',
                id: 'aDFZOW',
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
                description: 'Claim server screen: password field label',
                defaultMessage: 'Password',
                id: 'U3Uo3q',
              })
            "
            :rules="['required']"
            autocomplete="off"
            :type="showPassword ? 'text' : 'password'"
            :append-inner-icon="showPassword ? 'i-mdi:eye' : 'i-mdi:eye-off'"
            @click:append-inner="showPassword = !showPassword"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-text-field
            :label="
              $formatMessage({
                description: 'Claim server screen: Confirm password field label',
                defaultMessage: 'Confirm password',
                id: 'ezgnXr',
              })
            "
            :rules="[
              [
                'sameAs',
                password,
                $formatMessage({
                  description: 'Claim server screen:: Error message if passwords differ',
                  defaultMessage: 'Passwords must be identical',
                  id: 'ZWFPAg',
                }),
              ],
            ]"
            autocomplete="off"
            :type="showPassword ? 'text' : 'password'"
            :append-inner-icon="showPassword ? 'i-mdi:eye' : 'i-mdi:eye-off'"
            @click:append-inner="showPassword = !showPassword"
          />
        </v-col>
      </v-row>

      <v-row>
        <v-col>
          <v-btn
            :text="
              $formatMessage({
                description: 'Claim server screen:: Create User Account button',
                defaultMessage: 'Create User Account',
                id: 'd8lMZe',
              })
            "
            :loading="isLoading"
            type="submit"
            block
          />
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
import { useClaimServer, useClaimStatus } from '@/colada/claim'
import { useLogin } from '@/colada/users'

const messagesStore = useMessagesStore()
const intl = useIntl()

const form = ref()
const username = ref('')
const password = ref('')
const showPassword = ref<boolean>(false)

const router = useRouter()
const route = useRoute()

const { mutateAsync: login, isLoading: isLoadingLogin } = useLogin()
const { mutateAsync: claimServer, isLoading: isLoadingClaim } = useClaimServer()

const isLoading = computed(() => isLoadingClaim.value || isLoadingLogin.value)

async function submitForm() {
  const { valid } = await form.value.validate()
  if (valid) {
    const credentials = {
      username: username.value,
      password: password.value,
    }

    claimServer(credentials)
      .then(() => {
        void login(credentials).then(() => {
          if (route.query.redirect) void router.push({ path: route.query.redirect.toString() })
          else void router.push('/')
        })
      })
      .catch((error) => {
        messagesStore.messages.push({
          text:
            (error?.cause as ErrorCause)?.message ||
            intl.formatMessage(commonMessages.networkError),
        })
      })
  }
}

void useClaimStatus()
  .refresh()
  .then(({ data, error }) => {
    if (error) void router.push('/error')
    else if (data?.isClaimed) void router.push('/')
  })
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
