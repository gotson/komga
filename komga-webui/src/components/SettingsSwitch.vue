<template>
  <v-layout class="flex-row justify-sm-left justify-md-center">
    <v-flex xs6 md2 class="text-left d-flex flex-column justify-center">
      <v-label> {{ label }} </v-label>
    </v-flex>
    <v-flex xs6 md2 >
      <v-switch v-model="input"
                label=""
                @input="updateInput"
                @change="updateInput"
                class="float-right"
      >
      </v-switch>
    </v-flex>
  </v-layout>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'SettingsSwitch',
  props: {
    label: {
      type: String
    },
    value: {
      type: Boolean
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
