<template>
  <v-container fluid>
    <v-row>
      <v-col>
        <v-text-field
          ref="fieldNameRef"
          v-model="model.name"
          :rules="[rules.required()]"
          :label="
            $formatMessage({
              description: 'Form add/edit library: General - library name',
              defaultMessage: 'Library name',
              id: 's1nzhU',
            })
          "
        />
      </v-col>
    </v-row>

    <v-row
      class="align-baseline"
      density="comfortable"
    >
      <v-col>
        <v-text-field
          ref="fieldRootRef"
          v-model="model.root"
          :rules="[rules.required()]"
          :label="
            $formatMessage({
              description: 'Form add/edit library: General - root directory',
              defaultMessage: 'Root directory',
              id: 'afXGQS',
            })
          "
        />
      </v-col>
      <v-col cols="auto">
        <v-btn
          :id="id"
          :text="
            $formatMessage({
              description: 'Form add/edit library: General - root folder browse button',
              defaultMessage: 'Browse',
              id: 'E1kQun',
            })
          "
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-alert
          type="info"
          variant="tonal"
        >
          <template #text>
            <FormattedMessage
              :message-descriptor="{
                description: 'Form add/edit library: General - root directory guideline',
                defaultMessage:
                  'When configuring your library on a removable drive or shared network mount (like NFS), do not point the root directory directly to the root of the drive (e.g., <code>E:\\</code> or <code>/mnt/media/</code>).<br></br><br></br>To prevent data loss, please create a dedicated subdirectory for your files (e.g., <code>E:\\ebooks\\</code> or <code>/mnt/media/ebooks/</code>) and select that as your library root.',
                id: 'QWjkQT',
              }"
            >
              <template #code="Content">
                <code class="bg-grey-lighten-3 px-1 rounded">
                  <component :is="Content" />
                </code>
              </template>
              <template #br>
                <br />
              </template>
            </FormattedMessage>
          </template>
        </v-alert>
      </v-col>
    </v-row>
  </v-container>

  <DialogConfirmEdit
    v-model:record="model.root"
    :title="
      $formatMessage({
        description: 'Form add/edit library: General - root directory selection dialog title',
        defaultMessage: 'Library root directory',
        id: 'CJaS7j',
      })
    "
    :ok-text="
      $formatMessage({
        description:
          'Form add/edit library: General - root directory selection dialog confirmation button label',
        defaultMessage: 'Ok',
        id: 'laWvMU',
      })
    "
    max-width="600"
    close-on-save
    scrollable
    :fullscreen="display.xs.value"
    :activator="`#${id}`"
    @update:record="(val) => (model.root = val as string)"
  >
    <template #text="{ proxyModel }">
      <RemoteFileList v-model="proxyModel.value as string" />
    </template>
  </DialogConfirmEdit>
</template>

<script setup lang="ts">
import RemoteFileList from '@/components/RemoteFileList.vue'
import { useDisplay } from 'vuetify'
import { useRules } from 'vuetify'
import type { LibraryCreationDto } from '@/generated/openapi'
import { VTextField } from 'vuetify/components'

const display = useDisplay()
const rules = useRules()

const id = useId()

const fieldNameRef = ref<InstanceType<typeof VTextField> | null>(null)
const fieldRootRef = ref<InstanceType<typeof VTextField> | null>(null)

type LibraryCreationGeneral = Pick<LibraryCreationDto, 'name' | 'root'>

const model = defineModel<LibraryCreationGeneral>({ required: true })

const emit = defineEmits<{
  'update:errorCount': [errorCount: number]
}>()

watch(
  [() => fieldNameRef.value?.isValid, () => fieldRootRef.value?.isValid],
  ([nameValid, rootValid]) => {
    const fields = [nameValid, rootValid]
    emit('update:errorCount', fields.filter((it) => it === false).length)
  },
)
</script>
