<template>
  <v-dialog
    offset-y :max-height="$vuetify.breakpoint.height"
    v-model="model"
    scrollable
  >
    <v-card>
      <v-btn icon absolute top right @click="model = false">
        <v-icon>mdi-close</v-icon>
      </v-btn>
      <v-card-text>
        <v-row>
          <template v-for="(category, i) in Object.keys(shortcuts)">
            <v-col :key="i" cols="12" md="4">
              <div class="text-center text-h6">
                {{ category }}
              </div>
              <v-simple-table>
                <template v-slot:default>
                  <thead>
                  <tr>
                    <th class="text-left">{{ $t('dialog.shortcut_help.label_key') }}</th>
                    <th class="text-left">{{ $t('dialog.shortcut_help.label_description') }}</th>
                  </tr>
                  </thead>
                  <tbody>
                  <tr v-for="(s, j) in shortcuts[category]"
                      :key="j"
                  >
                    <td>
                      <kbd style="font-size: 1.2em" class="text-truncate">
                        {{ s.display }}
                      </kbd>
                    </td>
                    <td>{{ $t(s.description) }}</td>
                  </tr>
                  </tbody>
                </template>
              </v-simple-table>
            </v-col>
          </template>
        </v-row>
      </v-card-text>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'ShortcutHelpDialog',
  data: () => {
    return {
      model: false,
    }
  },
  props: {
    value: {
      type: Boolean,
      default: false,
    },
    shortcuts: {
      type: Object,
      required: true,
    },
  },
  watch: {
    model (val) {
      this.$emit('input', val)
    },
    value (val) {
      this.model = val
    },
  },
})
</script>

<style scoped>

</style>
