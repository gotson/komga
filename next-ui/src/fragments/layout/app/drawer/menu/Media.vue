<template>
  <v-list-group value="Media">
    <template #activator="{ props }">
      <v-list-item
        v-bind="props"
        :title="
          $formatMessage({
            description: 'Drawer menu for Media',
            defaultMessage: 'Media',
            id: 'Hl9H/B',
          })
        "
        prepend-icon="i-mdi:book-cog"
      />
    </template>

    <v-list-item
      to="/media/analysis"
      :title="
        $formatMessage({
          description: 'Drawer menu for Media > Media Analysis',
          defaultMessage: 'Media Analysis',
          id: 'DxtDpt',
        })
      "
    />
    <v-list-item
      to="/media/missing-posters"
      :title="
        $formatMessage({
          description: 'Drawer menu for Media > Missing Posters',
          defaultMessage: 'Missing Posters',
          id: 'Nb0V0p',
        })
      "
    />
    <v-list-item
      to="/media/duplicate-files"
      :title="
        $formatMessage({
          description: 'Drawer menu for Media > Duplicate Files',
          defaultMessage: 'Duplicate Files',
          id: 'eW3fXu',
        })
      "
    />

    <v-list-group value="Duplicate Pages">
      <template #activator="{ props }">
        <v-list-item
          v-bind="props"
          :title="
            $formatMessage({
              description: 'Drawer menu for Media > Duplicate Pages',
              defaultMessage: 'Duplicate Pages',
              id: 'cAu/I6',
            })
          "
        />
      </template>

      <v-list-item
        to="/media/duplicate-pages/known"
        :title="
          $formatMessage({
            description: 'Drawer menu for Media > Duplicate Pages > Known',
            defaultMessage: 'Known',
            id: 'MvwDsn',
          })
        "
      />
      <v-list-item
        to="/media/duplicate-pages/unknown"
        :title="
          $formatMessage({
            description: 'Drawer menu for Media > Duplicate Pages > Unknown',
            defaultMessage: 'Unknown',
            id: 'qiZm6U',
          })
        "
      />
    </v-list-group>
  </v-list-group>
</template>
<script setup lang="ts"></script>
