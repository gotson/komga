<template>
  <v-row justify-md="center" justify-sm="start">
    <v-col cols="5" class="text-left" align-self="center">
      <span class="">{{ label }}</span>
    </v-col>
    <v-col cols="7">
      <v-select
        filled
        dense
        solo
        :items="items"
        v-model="input"
        @input="updateInput"
        @change="updateInput"
        hide-details="true"
      />
    </v-col>
  </v-row>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'SettingsSelect',
  props: {
    items: null as any,
    label: {
      type: String,
    },
    value: {
      type: [String, Number],
    },
    display: {
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

<style>
  .v-text-field__details, div.v-input__control {
    min-height: 0 !important;
  }
  .v-text-field__details {
    display: none !important;
  }

</style>
