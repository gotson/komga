<template>
  <template v-if="!user.id">
    <v-text-field
      v-model="user!.email"
      autofocus
      :rules="[rules.required(), rules.email()]"
      :label="$formatMessage({
        description: 'User creationd ialog: Email field',
        defaultMessage: 'Email',
        id: 'b5wVJa'
      })"
      prepend-icon="mdi-account"
    />
    <v-text-field
      v-model="user.password"
      class="mt-1"
      :rules="[rules.required()]"
      :label="$formatMessage({
        description: 'User creation dialog: Password field',
        defaultMessage: 'Password',
        id: 'o+A10T'
      })"
      autocomplete="off"
      :type="showPassword ? 'text' : 'password'"
      :append-inner-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
      prepend-icon="mdi-none"
      @click:append-inner="showPassword = !showPassword"
    />
  </template>

  <!-- Roles  -->
  <v-select
    v-model="user.roles"
    chips
    closable-chips
    multiple
    :label="$formatMessage({
      description: 'User creation/edit dialog: Roles field',
      defaultMessage: 'Roles',
      id: 'CUxhzL'
    })"
    prepend-icon="mdi-key-chain"
    :items="userRoles"
  />

  <!-- Shared libraries -->
  <v-select
    v-model="user.sharedLibraries!.libraryIds"
    multiple
    :label="$formatMessage({
      description: 'User creation/edit dialog: Shared Libraries field',
      defaultMessage: 'Shared Libraries',
      id: 'UvhIIT'
    })"
    :items="libraries"
    item-title="name"
    item-value="id"
    prepend-icon="mdi-book-multiple"
  >
    <!--  Workaround for the lack of a slot to override the whole selection  -->
    <template #prepend-inner>
      <!--  Show an All Libraries chip instead of the selection  -->
      <v-chip
        v-if="user.sharedLibraries?.all"
        :text="$formatMessage({
          description: 'User creation/edit dialog: Shared Libraries field, value shown when user has access to all libraries',
          defaultMessage: 'All libraries',
          id: 'app.user-create-dialog.all_libraries'
        })"
        size="small"
      />
    </template>

    <template #selection="{ item }">
      <!--  Show the selection only if 'all' is false  -->
      <v-chip
        v-if="!user.sharedLibraries?.all"
        size="small"
        :text="item.title"
      />
    </template>

    <template #prepend-item>
      <v-list-item
        :title="$formatMessage({
          description: 'User creation/edit dialog: Shared Libraries field, value shown when user has access to all libraries',
          defaultMessage: 'All libraries',
          id: 'app.user-create-dialog.all_libraries'
        })"
        @click="selectAllLibraries"
      >
        <template #prepend>
          <v-checkbox-btn :model-value="user.sharedLibraries?.all" />
        </template>
      </v-list-item>
    </template>

    <template #item="{ props: itemProps }">
      <v-list-item
        :disabled="user.sharedLibraries?.all"
        v-bind="itemProps"
      >
        <template #prepend="{isSelected}">
          <v-checkbox-btn :model-value="isSelected" />
        </template>
      </v-list-item>
    </template>
  </v-select>

  <!-- Age restriction -->
  <v-row>
    <v-col>
      <v-select
        v-model="user.ageRestriction!.restriction"
        :label="$formatMessage({
          description: 'User creation/edit dialog: Age restriction field label',
          defaultMessage: 'Age restriction',
          id: 'hEOGa9'
        })"
        :items="ageRestrictions"
        prepend-icon="mdi-folder-lock"
      />
    </v-col>
    <v-col>
      <v-number-input
        v-model="user.ageRestriction!.age"
        :disabled="user.ageRestriction?.restriction?.toString() === 'NONE'"
        :label="$formatMessage({
          description: 'User creation/edit dialog: Age Restriction > Age field label',
          defaultMessage: 'Age',
          id: 'jywpqq'
        })"
        :min="0"
        :rules="[rules.required()]"
      />
    </v-col>
  </v-row>

  <!-- Allow labels -->
  <v-combobox
    v-model="user.labelsAllow"
    :label="$formatMessage({
      description: 'User creation/edit dialog: Allow only labels field label',
      defaultMessage: 'Allow only labels',
      id: 'Sj0HXz'
    })"
    chips
    closable-chips
    multiple
    :items="sharingLabels"
    prepend-icon="mdi-none"
  >
    <template #prepend-item>
      <v-list-item>
        <span class="font-weight-medium">
          {{
            $formatMessage({
              description: 'User creation/edit dialog: Allow only labels field selection',
              defaultMessage: 'Select an item or create one',
              id: 'app.user-create-dialog.select_create_one'
            })
          }}
        </span>
      </v-list-item>
    </template>
  </v-combobox>

  <!-- Exclude labels -->
  <v-combobox
    v-model="user.labelsExclude"
    :label="$formatMessage({
      description: 'User creation/edit dialog: Exclude labels field label',
      defaultMessage: 'Exclude labels',
      id: '3W0jUi'
    })"
    chips
    closable-chips
    multiple
    :items="sharingLabels"
    prepend-icon="mdi-none"
  >
    <template #prepend-item>
      <v-list-item>
        <span class="font-weight-medium">
          {{
            $formatMessage({
              description: 'User creation/edit dialog: Exclude labels field selection',
              defaultMessage: 'Select an item or create one',
              id: 'app.user-create-dialog.select_create_one'
            })
          }}
        </span>
      </v-list-item>
    </template>
  </v-combobox>
</template>

<script setup lang="ts">
import {UserRoles} from '@/types/UserRoles.ts'
import type {components} from '@/generated/openapi/komga'
import {useRules} from 'vuetify/labs/rules'
import {useLibraries} from '@/colada/queries/libraries.ts'
import {useSharingLabels} from '@/colada/queries/referential.ts'
import {useIntl} from 'vue-intl'

const rules = useRules()
const intl = useIntl()

interface UserExtend {
  id?: string,
  email: string,
  password?: string,
}

type UserCreation = components['schemas']['UserCreationDto'] & UserExtend
type UserUpdate = components['schemas']['UserUpdateDto'] & UserExtend
const user = defineModel<UserCreation | UserUpdate>({required: true})

const showPassword = ref<boolean>(false)

const {data: libraries} = useLibraries()
const {data: sharingLabels} = useSharingLabels()

function selectAllLibraries() {
  user.value.sharedLibraries!.all = !user.value.sharedLibraries?.all
  user.value.sharedLibraries!.libraryIds = libraries.value?.map(x => x.id) || []
}

const userRoles = computed(() => Object.keys(UserRoles).map(x => ({
  title: x,
  value: x,
})))

const ageRestrictions = [
  {
    title: intl.formatMessage({
      description: 'User creation/edit dialog: Age restriction field possible option',
      defaultMessage: 'No restriction',
      id: 'AeA9Ka'
    }),
    value: 'NONE'
  },
  {
    title: intl.formatMessage({
      description: 'User creation/edit dialog: Age restriction field possible option',
      defaultMessage: 'Allow only under',
      id: '/bathK'
    }),
    value: 'ALLOW_ONLY'
  },
  {
    title: intl.formatMessage({
      description: 'User creation/edit dialog: Age restriction field possible option',
      defaultMessage: 'Exclude over',
      id: 'wmGcF+'
    }),
    value: 'EXCLUDE'
  },
]
</script>
