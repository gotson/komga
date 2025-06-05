<template>
  <v-list-group value="Import">
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        :title="
          $formatMessage({
            description: 'Drawer menu for Import',
            defaultMessage: 'Import',
            id: 'N7+QXi',
          })
        "
        prepend-icon="mdi-import"
      />
    </template>

    <v-list-item
      to="/import/books"
      :title="
        $formatMessage({
          description: 'Drawer menu for Import > Books',
          defaultMessage: 'Books',
          id: 'fQIepD',
        })
      "
    />

    <v-list-item
      to="/import/readlist"
      :title="
        $formatMessage({
          description: 'Drawer menu for Import > Read List',
          defaultMessage: 'Read List',
          id: 'Y6VlM9',
        })
      "
    />
  </v-list-group>
</template>
