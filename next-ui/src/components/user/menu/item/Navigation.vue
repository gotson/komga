<template>
  <v-list-item
    to="/account/details"
    :title="
      $formatMessage({
        description: 'User menu: profile',
        defaultMessage: 'Profile',
        id: 'HIKKsB',
      })
    "
    prepend-icon="i-mdi:account"
  />
  <v-list-item
    to="/account/api-keys"
    :title="
      $formatMessage({
        description: 'Drawer menu for My Account > API Keys',
        defaultMessage: 'API Keys',
        id: 'oFOkWZ',
      })
    "
    prepend-icon="i-mdi:key-chain"
  />
  <v-list-item
    to="/account/activity"
    :title="
      $formatMessage({
        description: 'Drawer menu for My Account > Activity',
        defaultMessage: 'My activity',
        id: 'Xx+ITC',
      })
    "
    prepend-icon="i-mdi:account-key"
  />
</template>
