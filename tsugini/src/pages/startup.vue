<template>
  <div>Komga startup</div>
</template>

<script lang="ts" setup>
import { useCurrentUser } from 'colada/queries/current-user'

async function checkAuthenticated() {
  const router = useRouter()
  const route = useRoute()
  const { data, error, refresh } = useCurrentUser()

  await refresh()
  if (data.value) {
    if (route.query.redirect) await router.push({ path: route.query.redirect.toString() })
    else await router.push('/')
  }
  if (error.value) {
    await router.push({ name: '/login', query: { redirect: route.query.redirect } })
  }
}

onMounted(() => checkAuthenticated())
</script>

<route lang="yaml">
meta:
  layout: single
  noAuth: true
</route>
