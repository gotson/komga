<template>
  <v-dialog v-model="modal"
            max-width="600"
  >
    <v-card>
      <v-card-title>{{ $t('dialog.add_api_key.dialog_title') }}</v-card-title>
      <v-btn icon absolute top right @click="dialogClose">
        <v-icon>mdi-close</v-icon>
      </v-btn>

      <v-card-text>
        <v-container fluid>
          <v-row>
            <v-col>{{ $t('dialog.add_api_key.context') }}</v-col>
          </v-row>
          <v-row v-if="!apiKey">
            <v-col>
              <v-text-field v-model.trim="form.comment"
                            autofocus
                            :label="$t('dialog.add_api_key.field_comment')"
                            :hint="$t('dialog.add_api_key.field_comment_hint')"
                            :error-messages="getErrors('comment')"
                            @blur="$v.form.comment.$touch()"
              />
            </v-col>
          </v-row>

          <v-row v-if="apiKey">
            <v-col>
              <v-alert type="info" class="body-2">{{ $t('dialog.add_api_key.info_copy') }}</v-alert>
            </v-col>
          </v-row>

          <v-row v-if="apiKey">
            <v-col>
              <v-icon color="success">mdi-check</v-icon>
              {{ apiKey.key }}

              <v-tooltip top v-model="copied" v-if="isClipboardApiAvailable">
                <template v-slot:activator="on">
                  <v-btn v-on="on"
                         icon
                         x-small
                         class="align-content-end"
                         @click="copyApiKeyToClipboard"
                  >
                    <v-icon v-if="copied" color="success">mdi-check</v-icon>
                    <v-icon v-else>mdi-content-copy</v-icon>
                  </v-btn>
                </template>
                <span>{{ $t('common.copied') }}</span>
              </v-tooltip>
            </v-col>
          </v-row>
        </v-container>

      </v-card-text>

      <v-card-actions>
        <v-spacer/>
        <v-btn text @click="dialogClose">{{ $t('common.close') }}</v-btn>
        <v-btn color="primary" @click="generateApiKey" :disabled="!!apiKey">{{ $t('dialog.add_api_key.button_confirm') }}</v-btn>
      </v-card-actions>
    </v-card>
  </v-dialog>
</template>

<script lang="ts">
import {UserRoles} from '@/types/enum-users'
import Vue from 'vue'
import {required} from 'vuelidate/lib/validators'
import {ERROR} from '@/types/events'
import {ApiKeyDto, ApiKeyRequestDto} from '@/types/komga-users'

function validComment(value: string) {
  return !this.alreadyUsedComment.includes(value)
}

export default Vue.extend({
  name: 'ApiKeyAddDialog',
  data: function () {
    return {
      UserRoles,
      modal: false,
      apiKey: undefined as ApiKeyDto,
      copied: false,
      alreadyUsedComment: [] as string[],
      form: {
        comment: '',
      },
    }
  },
  props: {
    value: Boolean,
  },
  watch: {
    value(val) {
      this.modal = val
      if (val) {
        this.clear()
      }
    },
    modal(val) {
      !val && this.dialogClose()
    },
  },
  validations: {
    form: {
      comment: {required, validComment},
    },
  },
  computed: {
    isClipboardApiAvailable(): boolean {
      return !!navigator.clipboard
    },
  },
  methods: {
    clear() {
      this.apiKey = undefined
      this.alreadyUsedComment = []
      this.form.comment = ''
      this.$v.$reset()
    },
    dialogClose() {
      this.$emit('input', false)
    },
    getErrors(fieldName: string): string[] {
      const errors = [] as string[]

      const field = this.$v.form!![fieldName] as any
      if (field && field.$invalid && field.$dirty) {
        if (!field.validComment) errors.push(this.$t('error_codes.ERR_1034').toString())
        if (!field.required) errors.push(this.$t('common.required').toString())
      }
      return errors
    },
    validateInput(): ApiKeyRequestDto {
      this.$v.$touch()

      if (!this.$v.$invalid) {
        return {
          comment: this.form.comment,
        }
      }
      return undefined
    },
    async generateApiKey() {
      const apiKeyRequest = this.validateInput()
      if (apiKeyRequest) {
        try {
          this.apiKey = await this.$komgaUsers.createApiKey(apiKeyRequest)
          this.$emit('generate')
        } catch (e) {
          if (e.message.includes('ERR_1034'))
            this.alreadyUsedComment.push(this.form.comment)
          else
            this.$eventHub.$emit(ERROR, {message: e.message} as ErrorEvent)
        }
      }
    },
    copyApiKeyToClipboard() {
      navigator.clipboard.writeText(this.apiKey.key)
      this.copied = true
      setTimeout(() => this.copied = false, 3000)
    },
  },
})
</script>

<style scoped>

</style>
