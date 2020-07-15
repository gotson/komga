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
          class="body-1 mt-2"
        >
          <v-alert type="info"
                   icon="mdi-account-plus"
                   prominent
                   text
          >This Komga server is not yet active, you need to create a user account to be able to access it.<br/><br/>Choose
            an <strong>email</strong> and <strong>password</strong> and click on <strong>Create user account</strong>.
          </v-alert>
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="12" sm="8" md="6" lg="4" xl="2">
          <v-text-field v-model="form.login"
                        label="Email"
                        autocomplete="username"
                        autofocus
          />
        </v-col>
      </v-row>

      <v-row justify="center">
        <v-col cols="12" sm="8" md="6" lg="4" xl="2">
          <v-text-field v-model="form.password"
                        label="Password"
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
          >Login
          </v-btn>
          <v-btn v-if="unclaimed"
                 class="ml-4"
                 color="primary"
                 @click="claim"
          >Create user account
          </v-btn>
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
      >
        Close
      </v-btn>
    </v-snackbar>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'Login',
  data: () => ({
    form: {
      login: '',
      password: '',
    },
    snackbar: false,
    snackText: '',
    unclaimed: false,
  }),
  computed: {
    logoWidth (): number {
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
  },
  mounted () {
    this.getClaimStatus()
  },
  methods: {
    async getClaimStatus () {
      this.unclaimed = !(await this.$komgaClaim.getClaimStatus()).isClaimed
    },
    async performLogin () {
      try {
        await this.$store.dispatch(
          'getMeWithAuth',
          {
            login: this.form.login,
            password: this.form.password,
          })

        await this.$store.dispatch('getLibraries')

        if (this.$route.query.redirect) {
          await this.$router.push({ path: this.$route.query.redirect.toString() })
        } else {
          await this.$router.push({ name: 'home' })
        }
      } catch (e) {
        this.showSnack(e?.message)
      }
    },
    showSnack (message: string) {
      this.snackText = message
      this.snackbar = true
    },
    async claim () {
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
