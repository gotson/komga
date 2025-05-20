<template>
  <v-list-group value="Import">
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        title="Import"
        prepend-icon="mdi-import"
      />
    </template>

    <v-list-item
      to="/import/books"
      title="Books"
    />

    <v-list-item
      to="/import/readlist"
      title="Read List"
    />
  </v-list-group>
</template>
