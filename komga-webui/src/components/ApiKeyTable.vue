<template>
  <div style="position: relative">
    <div v-if="apiKeys.length > 0">
      <v-list elevation="3"
              two-line
      >
        <div v-for="(apiKey, index) in apiKeys" :key="apiKey.id">
          <v-list-item>
            <v-list-item-content>
              <v-list-item-title>{{ apiKey.comment }}</v-list-item-title>
              <v-list-item-subtitle>
                {{
                  $t('account_settings.api_key.created_date', {
                    date:
                      new Intl.DateTimeFormat($i18n.locale, {
                        dateStyle: 'medium',
                        timeStyle: 'short'
                      }).format(apiKey.createdDate)
                  })
                }}
              </v-list-item-subtitle>
            </v-list-item-content>

            <v-list-item-action>
              <v-btn icon @click="promptDeleteApiKey(apiKey)">
                <v-icon>mdi-delete</v-icon>
              </v-btn>
            </v-list-item-action>
          </v-list-item>

          <v-divider v-if="index !== apiKeys.length-1"/>
        </div>
      </v-list>

      <v-btn fab absolute bottom color="primary"
             :right="!$vuetify.rtl"
             :left="$vuetify.rtl"
             class="mx-6"
             small
             @click="generateApiKey"
      >
        <v-icon>mdi-plus</v-icon>
      </v-btn>
    </div>

    <div v-else>
      <v-container fluid class="pa-0">
        <v-row>
          <v-col>{{ $t('account_settings.api_key.no_keys') }}</v-col>
        </v-row>
        <v-row>
          <v-col>
            <v-btn color="primary" @click="generateApiKey">{{ $t('account_settings.api_key.generate_api_key') }}</v-btn>
          </v-col>

        </v-row>

      </v-container>
    </div>

    <confirmation-dialog
      v-model="modalDeleteApiKey"
      :title="$t('dialog.delete_apikey.dialog_title')"
      :body-html="$t('dialog.delete_apikey.warning_html')"
      :confirm-text=" $t('dialog.delete_apikey.confirm_delete', {name: apiKeyToDelete.comment})"
      :button-confirm="$t('dialog.delete_apikey.button_confirm')"
      button-confirm-color="error"
      @confirm="deleteApiKey"
    />

    <api-key-add-dialog
      v-model="modalGenerateApiKey"
      @generate="loadApiKeys"
    />
  </div>

</template>

<script lang="ts">
import Vue from 'vue'
import {ApiKeyDto} from '@/types/komga-users'
import {ERROR} from '@/types/events'
import ConfirmationDialog from '@/components/dialogs/ConfirmationDialog.vue'
import ApiKeyAddDialog from '@/components/dialogs/ApiKeyAddDialog.vue'

export default Vue.extend({
  name: 'ApiKeyTable',
  components: {ApiKeyAddDialog, ConfirmationDialog},
  data: () => {
    return {
      apiKeys: [] as ApiKeyDto[],
      apiKeyToDelete: {} as ApiKeyDto,
      modalDeleteApiKey: false,
      modalGenerateApiKey: false,
    }
  },
  mounted() {
    this.loadApiKeys()
  },
  methods: {
    async loadApiKeys() {
      try {
        this.apiKeys = await this.$komgaUsers.getApiKeys()
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
    promptDeleteApiKey(apiKey: ApiKeyDto) {
      this.apiKeyToDelete = apiKey
      this.modalDeleteApiKey = true
    },
    async deleteApiKey() {
      try {
        await this.$komgaUsers.deleteApiKey(this.apiKeyToDelete.id)
        await this.loadApiKeys()
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
    generateApiKey() {
      this.modalGenerateApiKey = true
    },
  },
})
</script>
