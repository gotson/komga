<template>
  <v-dialog v-model="modal"
            max-width="450"
  >
    <v-card>
      <v-card-title>{{ $t('dialog.edit_recommended.dialog_title') }}</v-card-title>
      <v-btn icon absolute top right @click="dialogClose">
        <v-icon>mdi-close</v-icon>
      </v-btn>

      <v-card-text>
        <v-list>
          <draggable
            v-model="localItems"
            v-bind="dragOptions"
            handle=".handle"
          >
            <v-list-item v-for="(l, index) in localItems" :key="index">
              <v-list-item-icon>
                <v-icon class="handle">mdi-drag-horizontal-variant</v-icon>
              </v-list-item-icon>
              <v-list-item-content>
                <v-list-item-title class="handle">{{ $t(`dashboard.${l.section.toLowerCase()}`) }}</v-list-item-title>
              </v-list-item-content>
              <v-list-item-action>
                <v-switch v-model="enabled[l.section]"/>
              </v-list-item-action>
            </v-list-item>
          </draggable>
        </v-list>
      </v-card-text>

      <v-card-actions>
        <v-spacer/>
        <v-btn text @click="dialogClose">{{ $t('common.cancel') }}</v-btn>
        <v-btn color="error" @click="resetToDefault">{{
            $t('dialog.edit_recommended.button_reset')
          }}
        </v-btn>
        <v-btn color="primary" @click="saveChanges">{{
            $t('dialog.edit_recommended.button_confirm')
          }}
        </v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {UserRoles} from '@/types/enum-users'
import draggable from 'vuedraggable'
import Vue, {PropType} from 'vue'
import {
  ClientSettingsRecommendedView,
  ClientSettingsRecommendedViewSection,
  RECOMMENDED_DEFAULT,
} from '@/types/komga-clientsettings'


export default Vue.extend({
  name: 'EditRecommendedDialog',
  components: {draggable},
  data: function () {
    return {
      UserRoles,
      modal: false,
      localItems: [] as ClientSettingsRecommendedViewSection[],
      enabled: {} as Record<string, boolean>,
    }
  },
  props: {
    value: Boolean,
    viewConfig: {
      type: Object as PropType<ClientSettingsRecommendedView>,
      required: true,
    },
  },
  watch: {
    value(val) {
      this.modal = val
      if (val) {
        this.reset(this.viewConfig)
      }
    },
    modal(val) {
      !val && this.dialogClose()
    },
  },
  computed: {
    dragOptions(): any {
      return {
        animation: 200,
        ghostClass: 'ghost',
      }
    },
  },
  methods: {
    reset(viewConfig: ClientSettingsRecommendedView) {
      this.localItems = viewConfig?.sections || []
      this.enabled = []
      this.localItems.forEach(it => this.enabled[it.section] = true)
      RECOMMENDED_DEFAULT.sections
        .filter(it => !viewConfig?.sections.some(s => s.section === it.section))
        .forEach(it => this.localItems.push(it))
    },
    dialogClose() {
      this.$emit('input', false)
    },
    resetToDefault() {
      this.$emit('reset-defaults')
      this.dialogClose()
    },
    saveChanges() {
      const sections = this.localItems.filter(it => this.enabled[it.section])
      const updated = {
        sections: sections,
      } as ClientSettingsRecommendedView

      this.$emit('update:viewConfig', updated)
      this.dialogClose()
    },
  },
})
</script>

<style scoped>
.handle {
  cursor: grab !important;
}

.ghost {
  opacity: 0.5;
  background: #c8ebfb;
}
</style>
