<template>
  <v-row justify-md="center" justify-sm="start">
    <v-col cols="5" md="4" class="text-left" align-self="center">
      <v-label class=""> {{ label }} </v-label>
    </v-col>
    <v-col cols="7" md="4" >
      <v-combobox
        filled
        dense
        solo
        :items="items"
        v-model="input"
        hide-selected
        @input="updateInput"
        @change="updateInput"
        hide-details="true"
      >
        <template v-slot:item="data">
          <slot name="item" v-bind="data"></slot>
        </template>
        <template v-slot:selection="data">
          <slot name="selection" v-bind="data"></slot>
        </template>
      </v-combobox>
    </v-col>
  </v-row>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'SettingsCombo',
  props: {
    items: null as any,
    label: {
      type: String
    },
    value: {
      type: String
    },
    display: {
      type: String
    }
  },
  data () {
    return {
      input: ''
    }
  },
  watch: {
    value: {
      handler (after) {
        this.input = after
      },
      immediate: true
    }
  },
  methods: {
    updateInput () {
      this.$emit('input', this.input)
    }
  }
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
