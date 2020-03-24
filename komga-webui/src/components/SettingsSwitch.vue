<template>
  <v-row justify-md="center" justify-sm="start">
    <v-col cols="5" md="3" align-self="center">
      <v-label> {{ label }} </v-label>
    </v-col>
    <v-col cols="4" md="3" align-self="center" class="text-right">
      <v-label> {{ status }} </v-label>
    </v-col>
    <v-col cols="3" md="2" align-self="center">
      <v-switch v-model="input"
                @input="updateInput"
                @change="updateInput"
                class="float-right"
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
