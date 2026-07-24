import type { OverviewSection } from '@/types/OverviewSection'

export const ClientSettingUser = {
  NextUILibraries: 'komga.nextui.libraries',
  NextUIOverviewSections: 'komga.nextui.overview',
} as const

export type ClientSettingUser = (typeof ClientSettingUser)[keyof typeof ClientSettingUser]

export type ClientSettingUserLibrary = {
  unpinned?: boolean
  order?: number
}

export type ClientSettingUserOverviewSection = {
  section: OverviewSection
}
