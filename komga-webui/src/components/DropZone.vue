<template>
  <label class="drop-zone" v-cloak @drop.prevent="dropHandler" @dragover.prevent>
    <span class="file-input">Choose an image</span> - drag and drop
    <input hidden aria-hidden="true" type="file" accept="image/*" multiple @change="dropHandler" >
  </label>
</template>

<script lang="ts">
import Vue from 'vue'

export default Vue.extend({
  name: 'DropZone',
  data() {
    return {
      files: [] as File[],
    }
  },
  watch: {
    files: function (value) {
      this.$emit('onInputChange', value)
    },
  },
  methods: {
    dropHandler: function (event: Event) {
      this.files = []
      if (event instanceof DragEvent && event.dataTransfer) {
        let droppedFiles = event.dataTransfer.files
        if (!droppedFiles) return

        ([...droppedFiles]).forEach(file => {
          this.files.push(file)
        })
      }
      if (event.target instanceof HTMLInputElement && event.target.files) {
        let selectedFiles = event.target.files
        if (!selectedFiles) return;

        ([...selectedFiles]).forEach(file => {
          this.files.push(file)
        })
      }
    },
  },
})
</script>

<style scoped>
.drop-zone {
  display: block;
  color: black;
  font-weight: 600;
  background:repeating-linear-gradient(
      45deg,
      #ececec,
      #ececec 20px,
      #e3e3e3 20px,
      #e3e3e3 40px
  );
  padding: 3em;
}

.file-input {
  color: #146BD6;
}

.file-input:hover {
  color: #177FFF;
}
</style>
