<template>
  <q-dialog
    ref="dialogRef"
    @hide="onDialogHide"
  >
    <q-card
      class="q-dialog-plugin q-pa-xs"
      style="width: 600px"
    >
      <q-card-section v-if="title || subtitle">
        <div class="text-h6">{{ title }}</div>
        <div class="text-subtitle1 text-weight-light">{{ subtitle }}</div>
      </q-card-section>

      <q-form
        greedy
        @submit="onDialogOK(userEdit)"
      >
        <q-card-section>
          <template v-if="!userEdit.id">
            <q-input
              v-model="userEdit!.email"
              autofocus
              :rules="[required(), (x, r) => r.email(x) || 'Must be a valid email address']"
              :label="
                $formatMessage({
                  description: 'User creation dialog: Email field',
                  defaultMessage: 'Email',
                  id: 'ToD0+o',
                })
              "
              icon="mdi-account"
              outlined
            />
            <q-input
              v-model="userEdit.password"
              :rules="[required()]"
              :label="
                $formatMessage({
                  description: 'User creation dialog: Password field',
                  defaultMessage: 'Password',
                  id: 'o+A10T',
                })
              "
              autocomplete="off"
              outlined
              :type="showPassword ? 'text' : 'password'"
            >
              <template #append>
                <q-btn
                  flat
                  round
                  :icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                  @click="showPassword = !showPassword"
                  :ripple="false"
                />
              </template>
            </q-input>
          </template>

          <!-- Roles  -->
          <q-select
            v-model="userEdit.roles"
            multiple
            outlined
            :label="
              $formatMessage({
                description: 'User creation/edit dialog: Roles field',
                defaultMessage: 'Roles',
                id: 'CUxhzL',
              })
            "
            :options="userRoles"
            emit-value
            use-chips
          >
            <template v-slot:before>
              <q-icon name="mdi-key-chain" />
            </template>
          </q-select>

          <!-- Shared libraries -->
          <!--  <v-select-->
          <!--    v-model="user.sharedLibraries!.libraryIds"-->
          <!--    multiple-->
          <!--    :label="-->
          <!--      $formatMessage({-->
          <!--        description: 'User creation/edit dialog: Shared Libraries field',-->
          <!--        defaultMessage: 'Shared Libraries',-->
          <!--        id: 'UvhIIT',-->
          <!--      })-->
          <!--    "-->
          <!--    :items="libraries"-->
          <!--    item-title="name"-->
          <!--    item-value="id"-->
          <!--    prepend-icon="mdi-book-multiple"-->
          <!--  >-->
          <!--    &lt;!&ndash;  Workaround for the lack of a slot to override the whole selection  &ndash;&gt;-->
          <!--    <template #prepend-inner>-->
          <!--      &lt;!&ndash;  Show an All Libraries chip instead of the selection  &ndash;&gt;-->
          <!--      <v-chip-->
          <!--        v-if="user.sharedLibraries?.all"-->
          <!--        :text="-->
          <!--          $formatMessage({-->
          <!--            description:-->
          <!--              'User creation/edit dialog: Shared Libraries field, value shown when user has access to all libraries',-->
          <!--            defaultMessage: 'All libraries',-->
          <!--            id: 'app.user-create-dialog.all_libraries',-->
          <!--          })-->
          <!--        "-->
          <!--        size="small"-->
          <!--      />-->
          <!--    </template>-->

          <!--    <template #selection="{ item }">-->
          <!--      &lt;!&ndash;  Show the selection only if 'all' is false  &ndash;&gt;-->
          <!--      <v-chip-->
          <!--        v-if="!user.sharedLibraries?.all"-->
          <!--        size="small"-->
          <!--        :text="item.title"-->
          <!--      />-->
          <!--    </template>-->

          <!--    <template #prepend-item>-->
          <!--      <v-list-item-->
          <!--        :title="-->
          <!--          $formatMessage({-->
          <!--            description:-->
          <!--              'User creation/edit dialog: Shared Libraries field, value shown when user has access to all libraries',-->
          <!--            defaultMessage: 'All libraries',-->
          <!--            id: 'app.user-create-dialog.all_libraries',-->
          <!--          })-->
          <!--        "-->
          <!--        @click="selectAllLibraries"-->
          <!--      >-->
          <!--        <template #prepend>-->
          <!--          <v-checkbox-btn :model-value="user.sharedLibraries?.all" />-->
          <!--        </template>-->
          <!--      </v-list-item>-->
          <!--    </template>-->

          <!--    <template #item="{ props: itemProps }">-->
          <!--      <v-list-item-->
          <!--        :disabled="user.sharedLibraries?.all"-->
          <!--        v-bind="itemProps"-->
          <!--      >-->
          <!--        <template #prepend="{ isSelected }">-->
          <!--          <v-checkbox-btn :model-value="isSelected" />-->
          <!--        </template>-->
          <!--      </v-list-item>-->
          <!--    </template>-->
          <!--  </v-select>-->

          <!-- Age restriction -->
          <!--  <v-row>-->
          <!--    <v-col>-->
          <!--      <v-select-->
          <!--        v-model="user.ageRestriction!.restriction"-->
          <!--        :label="-->
          <!--          $formatMessage({-->
          <!--            description: 'User creation/edit dialog: Age restriction field label',-->
          <!--            defaultMessage: 'Age restriction',-->
          <!--            id: 'hEOGa9',-->
          <!--          })-->
          <!--        "-->
          <!--        :items="ageRestrictions"-->
          <!--        prepend-icon="mdi-folder-lock"-->
          <!--      />-->
          <!--    </v-col>-->
          <!--    <v-col>-->
          <!--      <v-number-input-->
          <!--        v-model="user.ageRestriction!.age"-->
          <!--        :disabled="user.ageRestriction?.restriction?.toString() === 'NONE'"-->
          <!--        :label="-->
          <!--          $formatMessage({-->
          <!--            description: 'User creation/edit dialog: Age Restriction > Age field label',-->
          <!--            defaultMessage: 'Age',-->
          <!--            id: 'jywpqq',-->
          <!--          })-->
          <!--        "-->
          <!--        :min="0"-->
          <!--        :rules="[rules.required()]"-->
          <!--      />-->
          <!--    </v-col>-->
          <!--  </v-row>-->

          <!-- Allow labels -->
          <!--  <v-combobox-->
          <!--    v-model="user.labelsAllow"-->
          <!--    :label="-->
          <!--      $formatMessage({-->
          <!--        description: 'User creation/edit dialog: Allow only labels field label',-->
          <!--        defaultMessage: 'Allow only labels',-->
          <!--        id: 'Sj0HXz',-->
          <!--      })-->
          <!--    "-->
          <!--    chips-->
          <!--    closable-chips-->
          <!--    multiple-->
          <!--    :items="sharingLabels"-->
          <!--    prepend-icon="mdi-none"-->
          <!--  >-->
          <!--    <template #prepend-item>-->
          <!--      <v-list-item>-->
          <!--        <span class="font-weight-medium">-->
          <!--          {{-->
          <!--            $formatMessage({-->
          <!--              description: 'User creation/edit dialog: Allow only labels field selection',-->
          <!--              defaultMessage: 'Select an item or create one',-->
          <!--              id: 'app.user-create-dialog.select_create_one',-->
          <!--            })-->
          <!--          }}-->
          <!--        </span>-->
          <!--      </v-list-item>-->
          <!--    </template>-->
          <!--  </v-combobox>-->

          <!-- Exclude labels -->
          <!--  <v-combobox-->
          <!--    v-model="user.labelsExclude"-->
          <!--    :label="-->
          <!--      $formatMessage({-->
          <!--        description: 'User creation/edit dialog: Exclude labels field label',-->
          <!--        defaultMessage: 'Exclude labels',-->
          <!--        id: '3W0jUi',-->
          <!--      })-->
          <!--    "-->
          <!--    chips-->
          <!--    closable-chips-->
          <!--    multiple-->
          <!--    :items="sharingLabels"-->
          <!--    prepend-icon="mdi-none"-->
          <!--  >-->
          <!--    <template #prepend-item>-->
          <!--      <v-list-item>-->
          <!--        <span class="font-weight-medium">-->
          <!--          {{-->
          <!--            $formatMessage({-->
          <!--              description: 'User creation/edit dialog: Exclude labels field selection',-->
          <!--              defaultMessage: 'Select an item or create one',-->
          <!--              id: 'app.user-create-dialog.select_create_one',-->
          <!--            })-->
          <!--          }}-->
          <!--        </span>-->
          <!--      </v-list-item>-->
          <!--    </template>-->
          <!--  </v-combobox>-->
        </q-card-section>

        <q-card-actions align="right">
          <q-btn
            label="Cancel"
            flat
            rounded
            color="primary"
            @click="onDialogCancel"
          />
          <q-btn
            label="Save"
            flat
            rounded
            color="primary"
            type="submit"
          />
        </q-card-actions>
      </q-form>
    </q-card>
  </q-dialog>
</template>

<script setup lang="ts">
import { useDialogPluginComponent, extend } from 'quasar'
import { UserRoles } from 'types/UserRoles'
import type { components } from 'openapi/komga'
import { useLibraries } from 'colada/queries/libraries'
import { useSharingLabels } from 'colada/queries/referential'
import { useIntl } from 'vue-intl'
import { required } from 'utils/rules'

const { data: libraries } = useLibraries()
const { data: sharingLabels } = useSharingLabels()

interface Props {
  title?: string
  subtitle?: string
  user?: UserUpdate | UserCreation
}
const {
  subtitle,
  title,
  user = {
    email: '',
    password: '',
    roles: [UserRoles.PAGE_STREAMING, UserRoles.FILE_DOWNLOAD],
    sharedLibraries: {
      all: true,
      libraryIds: [],
    },
    ageRestriction: {
      age: 0,
      restriction: 'NONE',
    },
  } as UserCreation,
} = defineProps<Props>()

const userEdit = reactive<UserCreation | UserUpdate>(extend(true, {}, user))

defineEmits([
  // REQUIRED; need to specify some events that your
  // component will emit through useDialogPluginComponent()
  ...useDialogPluginComponent.emits,
])

const { dialogRef, onDialogHide, onDialogOK, onDialogCancel } = useDialogPluginComponent()
// dialogRef      - Vue ref to be applied to QDialog
// onDialogHide   - Function to be used as handler for @hide on QDialog
// onDialogOK     - Function to call to settle dialog with "ok" outcome
//                    example: onDialogOK() - no payload
//                    example: onDialogOK({ /*...*/ }) - with payload
// onDialogCancel - Function to call to settle dialog with "cancel" outcome

const intl = useIntl()

interface UserExtend {
  id?: string
  email: string
  password?: string
}

type UserCreation = components['schemas']['UserCreationDto'] & UserExtend
type UserUpdate = components['schemas']['UserUpdateDto'] & UserExtend

const showPassword = ref<boolean>(false)

function selectAllLibraries() {
  user.sharedLibraries!.all = !user.sharedLibraries?.all
  user.sharedLibraries!.libraryIds = libraries.value?.map((x) => x.id) || []
}

const userRoles = computed(() =>
  Object.keys(UserRoles).map((x) => ({
    label: x,
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
