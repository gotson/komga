<template>
  <v-menu offset-y :max-height="$vuetify.breakpoint.height">
    <template v-slot:activator="{ on, attrs }">
      <v-btn
        icon
        v-on="on"
      >
        <v-icon>mdi-information</v-icon>
      </v-btn>
    </template>
    <v-card>
      <v-card-text>
        <v-row no-gutters>
          <template v-for="(item,i) in shortcutHelp.items()">
            <v-col :key="i" cols="12" md="4">
              <div class="text-center">
                {{ item.key }}
              </div>
              <v-simple-table>
                <template v-slot:default>
                  <thead>
                  <tr>
                    <th class="text-left">Key</th>
                    <th class="text-left">Description</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(s,i) in item.value" :key="i">
                    <td>
                      <kbd style="height: 24px;" class="text-truncate">
                        <v-icon class="white--text text-capitalize"  style="font-size: 1rem;">
                          {{ s.key }}
                        </v-icon>
                      </kbd>
                    </td>
                    <td>{{ s.desc }}</td>
                  </tr>
                  </tbody>
                </template>
              </v-simple-table>
            </v-col>
          </template>
        </v-row>
      </v-card-text>
    </v-card>
  </v-menu>
</template>

<script lang="ts">
import Vue from 'vue'
import { shortcutHelp } from '@/functions/shortcuts'

export default Vue.extend({
  name: 'ShortcutHelpMenu',
  data: () => {
    return {
      shortcutHelp: shortcutHelp,
    }
  },
})
</script>

<style scoped>

</style>
