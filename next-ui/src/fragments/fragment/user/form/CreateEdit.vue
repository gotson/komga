<template>
  <v-container class="px-0">
    <template v-if="!user.id">
      <v-row>
        <v-col>
          <v-text-field
            v-model="user!.email"
            autofocus
            :rules="['required', 'email']"
            :label="
              $formatMessage({
                description: 'User creation dialog: Email field',
                defaultMessage: 'Email',
                id: 'ToD0+o',
              })
            "
          />
        </v-col>
      </v-row>
      <v-row>
        <v-col>
          <v-text-field
            v-model="user.password"
            :rules="['required']"
            :label="
              $formatMessage({
                description: 'User creation dialog: Password field',
                defaultMessage: 'Password',
                id: 'o+A10T',
              })
            "
            autocomplete="off"
            :type="showPassword ? 'text' : 'password'"
            :append-inner-icon="showPassword ? 'i-mdi:eye' : 'i-mdi:eye-off'"
            @click:append-inner="showPassword = !showPassword"
          />
        </v-col>
      </v-row>
    </template>

    <v-row>
      <v-col>
        <v-divider
          v-if="!user.id"
          class="mb-4"
        />
        <div class="text-subtitle-2">Permissions</div>
      </v-col>
    </v-row>
    <!-- Roles  -->
    <v-row>
      <v-col>
        <v-select
          v-model="user.roles"
          chips
          closable-chips
          multiple
          hide-details
          :label="
            $formatMessage({
              description: 'User creation/edit dialog: Roles field',
              defaultMessage: 'Roles',
              id: 'CUxhzL',
            })
          "
          :items="userRoles"
        />
      </v-col>
    </v-row>

    <!-- Shared libraries -->
    <v-row>
      <v-col>
        <v-select
          v-model="user.sharedLibraries!.libraryIds"
          multiple
          hide-details
          :label="
            $formatMessage({
              description: 'User creation/edit dialog: Shared Libraries field',
              defaultMessage: 'Shared Libraries',
              id: 'UvhIIT',
            })
          "
          :items="libraries"
          item-title="name"
          item-value="id"
        >
          <!--  Workaround for the lack of a slot to override the whole selection  -->
          <template #prepend-inner>
            <!--  Show an All Libraries chip instead of the selection  -->
            <v-chip
              v-if="user.sharedLibraries?.all"
              :text="
                $formatMessage({
                  description:
                    'User creation/edit dialog: Shared Libraries field, value shown when user has access to all libraries',
                  defaultMessage: 'All libraries',
                  id: 'app.user-create-dialog.all_libraries',
                })
              "
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
              :title="
                $formatMessage({
                  description:
                    'User creation/edit dialog: Shared Libraries field, value shown when user has access to all libraries',
                  defaultMessage: 'All libraries',
                  id: 'app.user-create-dialog.all_libraries',
                })
              "
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
              <template #prepend="{ isSelected }">
                <v-checkbox-btn :model-value="isSelected" />
              </template>
            </v-list-item>
          </template>
        </v-select>
      </v-col>
    </v-row>

    <!-- Age restriction -->
    <v-row>
      <v-col>
        <v-divider class="mb-4" />
        <div class="text-subtitle-2">Age restriction</div>
      </v-col>
    </v-row>
    <v-row>
      <v-col
        cols="12"
        sm="6"
      >
        <v-select
          v-model="user.ageRestriction!.restriction"
          :label="
            $formatMessage({
              description: 'User creation/edit dialog: Age restriction field label',
              defaultMessage: 'Age restriction',
              id: 'hEOGa9',
            })
          "
          :items="ageRestrictions"
          hide-details
        />
      </v-col>
      <v-col
        cols="12"
        sm="6"
      >
        <v-number-input
          v-model="user.ageRestriction!.age"
          :disabled="user.ageRestriction?.restriction?.toString() === 'NONE'"
          :label="
            $formatMessage({
              description: 'User creation/edit dialog: Age Restriction > Age field label',
              defaultMessage: 'Age',
              id: 'jywpqq',
            })
          "
          :min="0"
          :rules="['required']"
        />
      </v-col>
    </v-row>

    <v-row>
      <v-col>
        <v-divider class="mb-4" />
        <div class="text-subtitle-2">Label restrictions</div>
      </v-col>
    </v-row>
    <!-- Allow labels -->
    <v-row>
      <v-col>
        <v-combobox
          v-model="user.labelsAllow"
          :label="
            $formatMessage({
              description: 'User creation/edit dialog: Allow only labels field label',
              defaultMessage: 'Allow only labels',
              id: 'Sj0HXz',
            })
          "
          chips
          closable-chips
          multiple
          hide-details
          :items="sharingLabels"
        >
          <template #prepend-item>
            <v-list-item>
              <span class="font-weight-medium">
                {{ $formatMessage(commonMessages.selectItemOrCreateOne) }}
              </span>
            </v-list-item>
          </template>
        </v-combobox>
      </v-col>
    </v-row>

    <!-- Exclude labels -->
    <v-row>
      <v-col>
        <v-combobox
          v-model="user.labelsExclude"
          :label="
            $formatMessage({
              description: 'User creation/edit dialog: Exclude labels field label',
              defaultMessage: 'Exclude labels',
              id: '3W0jUi',
            })
          "
          chips
          closable-chips
          multiple
          hide-details
          :items="sharingLabels"
        >
          <template #prepend-item>
            <v-list-item>
              <span class="font-weight-medium">
                {{ $formatMessage(commonMessages.selectItemOrCreateOne) }}
              </span>
            </v-list-item>
          </template>
        </v-combobox>
      </v-col>
    </v-row>
  </v-container>
</template>

<script setup lang="ts">
import { UserRoles, userRolesMessages } from '@/types/UserRoles'
import type { components } from '@/generated/openapi/komga'
import { useLibraries } from '@/colada/libraries'
import { useSharingLabels } from '@/colada/referential'
import { useIntl } from 'vue-intl'
import { commonMessages } from '@/utils/i18n/common-messages'

const intl = useIntl()

interface UserExtend {
  id?: string
  email: string
  password?: string
}

type UserCreation = components['schemas']['UserCreationDto'] & UserExtend
type UserUpdate = components['schemas']['UserUpdateDto'] & UserExtend
const user = defineModel<UserCreation | UserUpdate>({ required: true })

const showPassword = ref<boolean>(false)

const { data: libraries } = useLibraries()
const { data: sharingLabels } = useSharingLabels()

function selectAllLibraries() {
  user.value.sharedLibraries!.all = !user.value.sharedLibraries?.all
  user.value.sharedLibraries!.libraryIds = libraries.value?.map((x) => x.id) || []
}

const userRoles = computed(() =>
  Object.keys(UserRoles).map((x) => ({
    title: intl.formatMessage(userRolesMessages[x as UserRoles]),
    value: x,
  })),
)

const ageRestrictions = [
  {
    title: intl.formatMessage({
      description: 'User creation/edit dialog: Age restriction field possible option',
      defaultMessage: 'No restriction',
      id: 'AeA9Ka',
    }),
    value: 'NONE',
  },
  {
    title: intl.formatMessage({
      description: 'User creation/edit dialog: Age restriction field possible option',
      defaultMessage: 'Allow only under',
      id: '/bathK',
    }),
    value: 'ALLOW_ONLY',
  },
  {
    title: intl.formatMessage({
      description: 'User creation/edit dialog: Age restriction field possible option',
      defaultMessage: 'Exclude over',
      id: 'wmGcF+',
    }),
    value: 'EXCLUDE',
  },
]
</script>
