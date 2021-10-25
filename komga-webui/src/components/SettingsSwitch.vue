<template>
  <v-row justify-md="center" justify-sm="start">
    <v-col cols="5" align-self="center">
      <span>{{ label }}</span>
    </v-col>
    <v-col cols="4" align-self="center" class="text-right">
      <span>{{ status }}</span>
    </v-col>
    <v-col cols="3" align-self="center" class="pa-0">
      <v-switch v-model="input" dense
                @input="updateInput"
                @change="updateInput"
                class="float-right"
                :disabled="disabled"
      >
      </v-switch>
    </v-col>
  </v-row>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'SettingsSwitch',
  props: {
    label: {
      type: String,
    },
    value: {
      type: Boolean,
    },
    status: {
      type: String,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
  },
  data () {
    return {
      input: '',
    }
  },
  watch: {
    value: {
      handler (after) {
        this.input = after
      },
      immediate: true,
    },
  },
  methods: {
    updateInput () {
      this.$emit('input', this.input)
    },
  },
})
</script>
