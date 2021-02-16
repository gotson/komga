<template>
  <div class="ma-3">
    <v-row align="center" justify="center">
      <v-img src="../assets/logo.svg"
             :max-width="logoWidth"
      />
    </v-row>

    <form novalidate @submit.prevent="performLogin">
      <v-row justify="center" v-if="unclaimed">
        <v-col
          cols="12" sm="8" md="6" lg="4" xl="2"
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

      <v-row justify="center">
        <v-col cols="12" sm="8" md="6" lg="4" xl="2">
          <v-text-field v-model="form.login"
                        :label="$t('common.email')"
                        autocomplete="username"
                        autofocus
          />
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="12" sm="8" md="6" lg="4" xl="2">
          <v-text-field v-model="form.password"
                        :label="$t('common.password')"
                        type="password"
                        autocomplete="current-password"
          />
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="12" sm="8" md="6" lg="4" xl="2">
          <v-btn color="primary"
                 type="submit"
                 :disabled="unclaimed"
          >{{ $t('login.login') }}
          </v-btn>
          <v-btn v-if="unclaimed"
                 class="ml-4"
                 color="primary"
                 @click="claim"
          >{{ $t('login.create_user_account') }}
          </v-btn>
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="12" sm="6" md="4" lg="2" xl="2">
          <v-select v-model="locale"
                    :items="locales"
                    :label="$t('home.translation')"
                    prepend-icon="mdi-translate"
          >
          </v-select>
        </v-col>
      </v-row>
    </form>

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

const cookieLocale = 'locale'

export default Vue.extend({
  name: 'Login',
  data: function () {
    return {
      form: {
        login: '',
        password: '',
      },
      snackbar: false,
      snackText: '',
      unclaimed: false,
      locales: this.$i18n.availableLocales.map((x: any) => ({text: this.$i18n.t('common.locale_name', x), value: x})),
    }
  },
  computed: {
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
      return l / (this.unclaimed ? 2 : 1)
    },


    locale: {
      get: function (): string {
        return this.$i18n.locale
      },
      set: function (locale: string): void {
        if (this.$i18n.availableLocales.includes(locale)) {
          this.$i18n.locale = locale
          this.$cookies.set(cookieLocale, locale, Infinity)
        }
      },
    },
  },
  mounted() {
    this.getClaimStatus()
  },
  methods: {
    async getClaimStatus() {
      this.unclaimed = !(await this.$komgaClaim.getClaimStatus()).isClaimed
    },
    async performLogin() {
      try {
        await this.$store.dispatch(
          'getMeWithAuth',
          {
            login: this.form.login,
            password: this.form.password,
          })

        await this.$store.dispatch('getLibraries')

        if (this.$route.query.redirect) {
          await this.$router.push({path: this.$route.query.redirect.toString()})
        } else {
          await this.$router.push({name: 'home'})
        }
      } catch (e) {
        this.showSnack(e?.message)
      }
    },
    showSnack(message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async claim() {
      try {
        await this.$komgaClaim.claimServer({
          email: this.form.login,
          password: this.form.password,
        } as ClaimAdmin)

        await this.performLogin()
      } catch (e) {
        this.showSnack(e.message)
      }
    },
  },
})
</script>

<style scoped>

</style>
