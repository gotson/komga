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
  methods: {
    dropHandler(event: Event) {
      if (event instanceof DragEvent && event.dataTransfer) {
        let droppedFiles = event.dataTransfer.files
        if (!droppedFiles) return

        this.$emit('on-input-change', Array.from(droppedFiles))
      }
      if (event.target instanceof HTMLInputElement && event.target.files) {
        let selectedFiles = event.target.files
        if (!selectedFiles) return

        this.$emit('on-input-change', Array.from(selectedFiles))
      }
    },
  },
})
</script>

<style scoped>
.drop-zone {
  background:repeating-linear-gradient(
    45deg,
    var(--v-base-lighten1),
    var(--v-base-lighten1) 20px,
    var(--v-base-darken1) 20px,
    var(--v-base-darken1) 40px
  );
  color: var(--v-contrast-light-2-base);
  display: block;
  font-weight: 600;
  padding: 3em;
  text-align: center;
  width: 100%;
}

.file-input {
  color: var(--v-info-base);
}

.file-input:hover {
  color: var(--v-info-lighten1);
}
</style>
