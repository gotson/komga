<template>
  <div class="ma-3">
    <v-container style="max-width: 550px">
      <v-row align="center" justify="center" class="ma-3">
        <v-img src="../assets/logo.svg"
               :max-width="logoWidth"
        />
      </v-row>

      <form novalidate @submit.prevent="performLogin">
        <v-row justify="center" v-if="unclaimed">
          <v-col
            class="text-body-1 mt-2"
          >
            <v-alert type="info"
                     icon="mdi-account-plus"
                     prominent
                     text
                     v-html="$t('login.unclaimed_html')"
            >
            </v-alert>
          </v-col>
        </v-row>

        <div v-if="!hideLogin">
          <v-row>
            <v-col>
              <v-text-field v-model="form.login"
                            :label="$t('common.email')"
                            :error-messages="getErrors('login')"
                            :autocomplete="hideLogin ? '' : 'username'"
                            autofocus
                            @blur="$v.form.login.$touch()"
              />
            </v-col>
          </v-row>

          <v-row>
            <v-col>
              <v-text-field v-model="form.password"
                            :label="$t('common.password')"
                            :error-messages="getErrors('password')"
                            type="password"
                            :autocomplete="hideLogin ? '' : 'current-password'"
                            @input="$v.form.password.$touch()"
                            @blur="$v.form.password.$touch()"
              />
            </v-col>
          </v-row>

          <v-row>
            <v-col>
              <v-checkbox v-model="rememberMe"
                          :label="$t('common.remember-me')"
                          hide-details
                          class="mt-0"
              />
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="auto">
              <v-btn color="primary"
                     type="submit"
                     :disabled="unclaimed"
              >{{ $t('login.login') }}
              </v-btn>
            </v-col>
            <v-col cols="auto">
              <v-btn v-if="unclaimed"
                     color="primary"
                     @click="claim"
              >{{ $t('login.create_user_account') }}
              </v-btn>
            </v-col>
          </v-row>

          <v-divider class="my-4 mt-2"/>

        </div>

        <v-row justify="center">
          <v-col
            v-for="provider in oauth2Providers"
            :key="provider.registrationId"
            cols="auto"
          >
            <v-btn
              :disabled="unclaimed"
              @click="oauth2Login(provider)"
              min-width="250"
              :class="$_.get(socialButtons[provider.registrationId.toLowerCase()], 'text') ? `${socialButtons[provider.registrationId.toLowerCase()].text}--text` : undefined"
              :color="$_.get(socialButtons[provider.registrationId.toLowerCase()], 'color')"
            >
              <v-icon left>mdi-{{ provider.registrationId }}</v-icon>
              Sign in with {{ provider.name }}
            </v-btn>
          </v-col>
        </v-row>

        <v-row justify="center">
          <v-col cols="6">
            <v-select v-model="locale"
                      :items="locales"
                      :label="$t('home.translation')"
                      prepend-icon="mdi-translate"
            >
            </v-select>
          </v-col>

          <v-col cols="6">
            <v-select v-model="theme"
                      :items="themes"
                      :label="$t('home.theme')"
                      :prepend-icon="themeIcon"
            >
            </v-select>
          </v-col>
        </v-row>
      </form>
    </v-container>

    <v-snackbar
      v-model="snackbar"
      bottom
      color="error"
    >
      {{ snackText }}
      <v-btn
        text
        @click="snackbar = false"
      >{{ $t('common.close') }}
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import {email, required} from 'vuelidate/lib/validators'
import {Theme} from '@/types/themes'
import {OAuth2ClientDto} from '@/types/komga-oauth2'
import urls from '@/functions/urls'
import {socialButtons} from '@/types/social'
import {convertErrorCodes} from '@/functions/error-codes'
import {CLIENT_SETTING} from '@/types/komga-clientsettings'

export default Vue.extend({
  name: 'LoginView',
  data: function () {
    return {
      urls,
      socialButtons,
      form: {
        login: '',
        password: '',
      },
      snackbar: false,
      snackText: '',
      unclaimed: false,
      oauth2Providers: [] as OAuth2ClientDto[],
      locales: this.$i18n.availableLocales.map((x: any) => ({text: this.$i18n.t('common.locale_name', x), value: x})),
      clientSettings: {} as Record<string, ClientSettingDto>,
    }
  },
  validations: {
    form: {
      login: {required, email},
      password: {required},
    },
  },
  computed: {
    hideLogin(): boolean {
      return !this.unclaimed
        && this.oauth2Providers.length > 0
        && (this.clientSettings[CLIENT_SETTING.WEBUI_OAUTH2_HIDE_LOGIN]?.value === 'true')
    },
    autoOauth2Login(): boolean {
      return !this.unclaimed
        && this.oauth2Providers.length == 1
        && (this.clientSettings[CLIENT_SETTING.WEBUI_OAUTH2_AUTO_LOGIN]?.value === 'true')
        && !this.$route.query.error
        && !this.$route.query.logout
    },
    logoWidth(): number {
      let l = 100
      switch (this.$vuetify.breakpoint.name) {
        case 'xs':
          l = 100
        case 'sm':
        case 'md':
          l = 200
        case 'lg':
        case 'xl':
        default:
          l = 300
      }
      return l
    },

    locale: {
      get: function (): string {
        return this.$i18n.locale
      },
      set: function (locale: string): void {
        if (this.$i18n.availableLocales.includes(locale)) {
          this.$store.commit('setLocale', locale)
        }
      },
    },

    rememberMe: {
      get: function (): boolean {
        return this.$store.state.persistedState.rememberMe
      },
      set: function (value: boolean): void {
        this.$store.commit('setRememberMe', value)
      },
    },

    themes(): object[] {
      return [
        {text: this.$i18n.t(Theme.LIGHT), value: Theme.LIGHT},
        {text: this.$i18n.t(Theme.DARK), value: Theme.DARK},
        {text: this.$i18n.t(Theme.SYSTEM), value: Theme.SYSTEM},
      ]
    },
    themeIcon(): string {
      switch (this.theme) {
        case Theme.LIGHT:
          return 'mdi-brightness-7'
        case Theme.DARK:
          return 'mdi-brightness-3'
        case Theme.SYSTEM:
          return 'mdi-brightness-auto'
      }
      return ''
    },

    theme: {
      get: function (): Theme {
        return this.$store.state.persistedState.theme
      },
      set: function (theme: Theme): void {
        if (Object.values(Theme).includes(theme)) {
          this.$store.commit('setTheme', theme)
        }
      },
    },
  },
  async mounted() {
    this.getClaimStatus()
    this.clientSettings = await this.$komgaSettings.getClientSettingsGlobal()
    this.oauth2Providers = await this.$komgaOauth2.getProviders()
    if (this.$route.query.error) this.showSnack(convertErrorCodes(this.$route.query.error.toString()))
    if (this.hideLogin && this.autoOauth2Login) this.oauth2Login(this.oauth2Providers[0])
  },
  methods: {
    oauth2Login(provider: OAuth2ClientDto) {
      const url = `${urls.originNoSlash}/oauth2/authorization/${provider.registrationId}`
      const height = 600
      const width = 600
      const y = window.top!.outerHeight / 2 + window.top!.screenY - (height / 2)
      const x = window.top!.outerWidth / 2 + window.top!.screenX - (width / 2)
      window.open(url, 'oauth2Login',
        `toolbar=no,
        location=off,
        status=no,
        menubar=no,
        scrollbars=yes,
        resizable=yes,
        top=${y},
        left=${x},
        width=${height},
        height=${width}`,
      )
    },
    getErrors(fieldName: string): string[] {
      const errors = [] as string[]

      const field = this.$v.form!![fieldName] as any
      if (field && field.$invalid && field.$dirty) {
        if (!field.required) errors.push(this.$t('common.required').toString())
        if (!field.email) errors.push(this.$t('dialog.add_user.field_email_error').toString())
      }
      return errors
    },
    async getClaimStatus() {
      this.unclaimed = !(await this.$komgaClaim.getClaimStatus()).isClaimed
    },
    async performLogin() {
      if (this.isUserValid()) {
        try {
          await this.$store.dispatch(
            'getMeWithAuth',
            {
              login: this.form.login,
              password: this.form.password,
              rememberMe: this.rememberMe,
            })

          await this.$store.dispatch('getLibraries')
          await this.$store.dispatch('getClientSettingsGlobal')
          await this.$store.dispatch('getClientSettingsUser')

          if (this.$route.query.redirect) {
            await this.$router.push({path: this.$route.query.redirect.toString()})
          } else {
            await this.$router.push({name: 'home'})
          }
        } catch (e) {
          this.showSnack(e?.message)
        }
      }
    },
    showSnack(message: string) {
      this.snackText = message
      this.snackbar = true
    },
    isUserValid(): boolean {
      this.$v.$touch()
      return !this.$v.$invalid
    },
    async claim() {
      if (this.isUserValid()) {
        try {
          await this.$komgaClaim.claimServer({
            email: this.form.login,
            password: this.form.password,
          } as ClaimAdmin)

          await this.performLogin()
        } catch (e) {
          this.showSnack(e.message)
        }
      }
    },
  },
})
</script>

<style scoped>

</style>
