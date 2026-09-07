<template>
  <div>
    <v-toolbar color="surface">
      <v-spacer />

      <v-btn
        prepend-icon="i-mdi:plus"
        rounded="lg"
        :text="
          $formatMessage({
            description: 'Form edit: Links: add link button',
            defaultMessage: 'Add link',
            id: 'SWm3SC',
          })
        "
        border
        class="me-2"
        @click="addLink()"
      />

      <LockIcon v-model="model.linksLock" />
    </v-toolbar>

    <v-container fluid>
      <v-row
        v-for="(link, i) in model.links"
        :key="i"
      >
        <v-col cols="3">
          <v-text-field
            ref="fieldLabelRef"
            v-model="link.label"
            :rules="[rules.required()]"
            :label="
              $formatMessage({
                description: 'Form edit: Links - Label field label',
                defaultMessage: 'Label',
                id: 'NLiXdm',
              })
            "
          />
        </v-col>

        <v-col cols="9">
          <v-text-field
            ref="fieldUrlRef"
            v-model="link.url"
            :rules="[
              rules.required(),
              [
                'linkUrl',
                $formatMessage({
                  description: 'Form edit: Links - Error message if URL is invalid',
                  defaultMessage: 'Must be a valid URL',
                  id: 'C4S+yv',
                }),
              ] satisfies CustomRuleTuple,
            ]"
            :label="
              $formatMessage({
                description: 'Form edit: Links - URL field label',
                defaultMessage: 'URL',
                id: '2IYW7s',
              })
            "
            append-icon="i-mdi:trash"
            @click:append="removeAtIndex(i)"
          />
        </v-col>
      </v-row>
    </v-container>
  </div>
</template>

<script setup lang="ts">
import type { WebLinkDto } from '@/generated/openapi'
import { useFieldValidity, useLockWatcher } from '@/composables/form'
import { useRules } from 'vuetify'
import type { CustomRuleTuple } from '@/plugins/vuetify'
import { VTextField } from 'vuetify/components'

const rules = useRules()

const model = defineModel<{ links: WebLinkDto[]; linksLock: boolean }>({ required: true })
useLockWatcher(model, ['links'])

const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()

const fields = {
  label: useTemplateRef<InstanceType<typeof VTextField>[]>('fieldLabelRef'),
  url: useTemplateRef<InstanceType<typeof VTextField>[]>('fieldUrlRef'),
}
useFieldValidity(fields, (errorCount) => emit('update:errorCount', errorCount))

function addLink() {
  model.value.links.push({ label: '', url: '' })
}

function removeAtIndex(index: number) {
  model.value.links.splice(index, 1)
}
</script>
