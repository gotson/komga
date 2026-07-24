import { OverviewSectionValues } from '@/types/OverviewSection'
import * as v from 'valibot'

export const ClientSettingUser = {
  NextUILibraries: 'komga.nextui.libraries',
  NextUIOverviewSections: 'komga.nextui.overview',
} as const

export type ClientSettingUser = (typeof ClientSettingUser)[keyof typeof ClientSettingUser]

const SchemaClientSettingUserLibrary = v.fallback(
  v.strictObject({
    unpinned: v.optional(v.boolean()),
    order: v.optional(v.number()),
  }),
  {},
)

const SchemaClientSettingUserOverviewSection = v.strictObject({
  section: v.picklist(OverviewSectionValues),
})

export type ClientSettingUserLibrary = v.InferOutput<typeof SchemaClientSettingUserLibrary>
export type ClientSettingUserOverviewSection = v.InferOutput<
  typeof SchemaClientSettingUserOverviewSection
>

export const ClientSettingUserSchemas = {
  [ClientSettingUser.NextUILibraries]: v.record(v.string(), SchemaClientSettingUserLibrary),
  [ClientSettingUser.NextUIOverviewSections]: v.fallback(
    v.record(v.string(), v.array(SchemaClientSettingUserOverviewSection)),
    {},
  ),
} as const satisfies Record<ClientSettingUser, v.GenericSchema>

export type ClientSettingUserSettings = {
  [K in keyof typeof ClientSettingUserSchemas]: v.InferOutput<(typeof ClientSettingUserSchemas)[K]>
}
