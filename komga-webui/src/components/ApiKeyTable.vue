<template>
  <div style="position: relative">
    <div v-if="apiKeys">
      <div v-if="apiKeys.length > 0">
        <v-list elevation="3"
                three-line
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
                <v-list-item-subtitle v-if="apiKeyLastActivity[apiKey.id] !== undefined">
                  {{
                    $t('settings_user.latest_activity', {
                      date:
                        new Intl.DateTimeFormat($i18n.locale, {
                          dateStyle: 'medium',
                          timeStyle: 'short'
                        }).format(apiKeyLastActivity[apiKey.id])
                    })
                  }}
                </v-list-item-subtitle>
                <v-list-item-subtitle v-else>{{ $t('settings_user.no_recent_activity') }}</v-list-item-subtitle>
              </v-list-item-content>

              <v-list-item-action>
                <v-tooltip bottom>
                  <template v-slot:activator="{ on }">
                    <v-btn icon @click="promptSyncPointDelete(apiKey)" v-on="on">
                      <v-icon>mdi-book-refresh</v-icon>
                    </v-btn>
                  </template>
                  <span>{{ $t('account_settings.api_key.force_kobo_sync') }}</span>
                </v-tooltip>
              </v-list-item-action>

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
              <v-btn color="primary" @click="generateApiKey">{{
                  $t('account_settings.api_key.generate_api_key')
                }}
              </v-btn>
            </v-col>

          </v-row>

        </v-container>
      </div>
    </div>

    <confirmation-dialog
      v-model="modalDeleteSyncPoints"
      :title="$t('dialog.force_kobo_sync.dialog_title')"
      :body-html="$t('dialog.force_kobo_sync.warning_html')"
      :button-confirm="$t('common.i_understand')"
      button-confirm-color="warning"
      @confirm="deleteSyncPoint"
    />

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
      apiKeys: undefined as ApiKeyDto[],
      apiKeyToDelete: {} as ApiKeyDto,
      apiKeySyncPointsToDelete: {} as ApiKeyDto,
      modalDeleteApiKey: false,
      modalDeleteSyncPoints: false,
      modalGenerateApiKey: false,
      apiKeyLastActivity: {} as any,
    }
  },
  mounted() {
    this.loadApiKeys()
  },
  methods: {
    async loadApiKeys() {
      try {
        this.apiKeys = await this.$komgaUsers.getApiKeys()
        this.apiKeys.forEach((a: ApiKeyDto) => {
          this.$komgaUsers.getLatestAuthenticationActivityForUser(a.userId, a.id)
            .then(value => this.$set(this.apiKeyLastActivity, `${a.id}`, value.dateTime))
            .catch(e => {
            })
        })
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
    },
    promptDeleteApiKey(apiKey: ApiKeyDto) {
      this.apiKeyToDelete = apiKey
      this.modalDeleteApiKey = true
    },
    promptSyncPointDelete(apiKey: ApiKeyDto) {
      this.apiKeySyncPointsToDelete = apiKey
      this.modalDeleteSyncPoints = true
    },
    async deleteSyncPoint() {
      try {
        await this.$komgaSyncPoints.deleteMySyncPointsByApiKey(this.apiKeySyncPointsToDelete.id)
      } catch (e) {
        this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
      }
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
