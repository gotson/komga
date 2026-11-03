<template>
  <v-menu>
    <template #activator="{ props }">
      <v-icon-btn
        v-ktooltip:bottom="
          $formatMessage({
            description: 'Locale selector button: tooltip',
            defaultMessage: 'Language',
            id: 'XR+P68',
          })
        "
        v-bind="props"
        icon="i-mdi:translate"
        :aria-label="
          $formatMessage({
            description: 'Locale selector button: aria-label',
            defaultMessage: 'language selector',
            id: 'dQ9kx9',
          })
        "
      />
    </template>

    <LocaleList />
  </v-menu>
</template>
