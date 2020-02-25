<template>
  <v-layout class="flex-row justify-sm-left justify-md-center">
    <v-flex xs6 md2 class="text-left d-flex flex-column justify-center">
      <v-label class=""> {{ label }} </v-label>
    </v-flex>
    <v-flex xs6 md2 >
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
        <template slot="item" slot-scope="data">
          <slot name="item" v-bind="data"></slot>
        </template>
        <template slot="selection" slot-scope="data">
          <slot name="selection" v-bind="data"></slot>
        </template>
      </v-combobox>
    </v-flex>
  </v-layout>
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
