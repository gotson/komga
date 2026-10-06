import * as v from 'valibot'

export const ClientSettingGlobal = {
  NextUIOAuth2Login: 'komga.nextui.oauth2',
} as const

export type ClientSettingGlobal = (typeof ClientSettingGlobal)[keyof typeof ClientSettingGlobal]

const SchemaClientSettingOauth2Login = v.fallback(
  v.strictObject({
    hideLoginFields: v.optional(v.boolean()),
    autoLogin: v.optional(v.boolean()),
  }),
  {},
)
export type ClientSettingOauth2Login = v.InferOutput<typeof SchemaClientSettingOauth2Login>

export const ClientSettingGlobalSchemas = {
  [ClientSettingGlobal.NextUIOAuth2Login]: SchemaClientSettingOauth2Login,
} as const satisfies Record<ClientSettingGlobal, v.GenericSchema>

export type ClientSettingGlobalSettings = {
  [K in keyof typeof ClientSettingGlobalSchemas]: {
    allowUnauthorized: boolean
    value: v.InferOutput<(typeof ClientSettingGlobalSchemas)[K]>
  }
}
