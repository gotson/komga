<template>
  <v-container max-width="550px">
    <v-row justify="center">
      <v-col>
        <v-img src="@/assets/logo.svg" />
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
    await router.push({ name: '/error' })
  } else if (data.value) {
    if (route.query.redirect) await router.push({ path: route.query.redirect.toString() })
    else await router.push('/')
  } else if (error.value) {
    if (claimData.value?.isClaimed)
      await router.push({ name: '/login', query: { redirect: route.query.redirect } })
    else await router.push({ name: '/claim', query: { redirect: route.query.redirect } })
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
