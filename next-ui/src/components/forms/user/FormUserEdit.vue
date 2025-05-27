<template>
  <template v-if="!user.id">
    <v-text-field
      v-model="user!.email"
      autofocus
      :rules="[rules.required(), rules.email()]"
      label="Email"
      prepend-icon="mdi-account"
    />
    <v-text-field
      v-model="user.password"
      class="mt-1"
      :rules="[rules.required()]"
      label="Password"
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
    label="Roles"
    prepend-icon="mdi-key-chain"
    :items="userRoles"
  />

  <!-- Shared libraries -->
  <v-select
    v-model="user.sharedLibraries!.libraryIds"
    multiple
    label="Shared Libraries"
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
        text="All libraries"
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
        title="All libraries"
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
        label="Age restriction"
        :items="ageRestrictions"
        prepend-icon="mdi-folder-lock"
      />
    </v-col>
    <v-col>
      <v-number-input
        v-model="user.ageRestriction!.age"
        :disabled="user.ageRestriction?.restriction?.toString() === 'NONE'"
        label="Age"
        :min="0"
        :rules="[rules.required()]"
      />
    </v-col>
  </v-row>

  <!-- Allow labels -->
  <v-combobox
    v-model="user.labelsAllow"
    label="Allow only labels"
    chips
    closable-chips
    multiple
    :items="sharingLabels"
    prepend-icon="mdi-none"
  >
    <template #prepend-item>
      <v-list-item>
        <span class="font-weight-medium">Select an item or create one</span>
      </v-list-item>
    </template>
  </v-combobox>

  <!-- Exclude labels -->
  <v-combobox
    v-model="user.labelsExclude"
    label="Exclude labels"
    chips
    closable-chips
    multiple
    :items="sharingLabels"
    prepend-icon="mdi-none"
  >
    <template #prepend-item>
      <v-list-item>
        <span class="font-weight-medium">Select an item or create one</span>
      </v-list-item>
    </template>
  </v-combobox>
</template>

<script setup lang="ts">
import {UserRoles} from '@/types/UserRoles.ts'
import type {components} from '@/generated/openapi/komga'
import { useRules } from 'vuetify/labs/rules'
import {useLibraries} from '@/colada/queries/libraries.ts'
import {useSharingLabels} from '@/colada/queries/referential.ts'

const rules = useRules()

interface UserExtend {
  id?: string,
  email: string,
  password?: string,
}
type UserCreation = components["schemas"]["UserCreationDto"] & UserExtend
type UserUpdate = components["schemas"]["UserUpdateDto"] & UserExtend
const user = defineModel<UserCreation |  UserUpdate>({required: true})

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
  {title: 'No restriction', value: 'NONE'},
  {title: 'Allow only under', value: 'ALLOW_ONLY'},
  {title: 'Exclude over', value: 'EXCLUDE'},
]
</script>
