<template>
  <v-container max-width="550px">
    <v-row class="justify-center">
      <v-col>
        <v-img
          src="@/assets/logo.svg"
          width="500"
          height="500"
        />
      </v-col>
    </v-row>
    <v-row>
      <v-col>
        <v-progress-linear
          indeterminate
          color="primary"
        />
      </v-col>
    </v-row>
  </v-container>
</template>

<script lang="ts" setup>
import { useCurrentUser } from '@/colada/users'
import { useClaimStatus } from '@/colada/claim'

definePage({ alias: '/next' })

async function checkAuthenticated() {
  const router = useRouter()
  const route = useRoute()
  const { data, error, refresh } = useCurrentUser()
  const { data: claimData, error: claimError, refresh: claimRefresh } = useClaimStatus()

  await refresh()
  await claimRefresh()
  // if we can't get the claim status, most likely the server is unreachable
  if (claimError.value) {
    await nextTick()
    void router.push({ name: '/error' })
  } else if (data.value) {
    await nextTick()
    if (route.query.redirect) {
      void router.push(route.query.redirect.toString())
    } else {
      void router.push('/')
    }
  } else if (error.value) {
    await nextTick()
    if (claimData.value?.isClaimed) {
      void router.push({ name: '/login', query: { redirect: route.query.redirect } })
    } else {
      void router.push({ name: '/claim', query: { redirect: route.query.redirect } })
    }
  }
}

onMounted(() => checkAuthenticated())

// TODO: exchange header token for cookie
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
