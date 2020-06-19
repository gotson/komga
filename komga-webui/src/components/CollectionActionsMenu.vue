<template>
  <div>
    <v-menu offset-y v-if="isAdmin">
      <template v-slot:activator="{ on }">
        <v-btn icon v-on="on" @click.prevent="">
          <v-icon>mdi-dots-vertical</v-icon>
        </v-btn>
      </template>
      <v-list>
        <v-list-item @click="promptDeleteCollection"
                     class="list-warning">
          <v-list-item-title>Delete</v-list-item-title>
        </v-list-item>
      </v-list>
    </v-menu>

    <collection-delete-dialog v-model="modalDeleteCollection"
                              :collection="collection"
                              @deleted="afterDelete"
    />
  </div>
</template>
<script lang="ts">
import CollectionDeleteDialog from '@/components/CollectionDeleteDialog.vue'
import Vue from 'vue'

export default Vue.extend({
  name: 'CollectionActionsMenu',
  components: { CollectionDeleteDialog },
  data: function () {
    return {
      modalDeleteCollection: false,
    }
  },
  props: {
    collection: {
      type: Object,
      required: true,
    },
  },
  computed: {
    isAdmin (): boolean {
      return this.$store.getters.meAdmin
    },
  },
  methods: {
    promptDeleteCollection () {
      this.modalDeleteCollection = true
    },
    afterDelete () {
      this.$emit('deleted', true)
    },
  },
})
</script>
<style scoped>
.list-warning:hover {
  background: #F44336;
}

.list-warning:hover .v-list-item__title {
  color: white;
}
</style>
