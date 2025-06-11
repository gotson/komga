import { actuatorHandlers } from '@/mocks/api/handlers/actuator'
import { announcementsHandlers } from '@/mocks/api/handlers/announcements'
import { releasesHandlers } from '@/mocks/api/handlers/releases'

export const handlers = [...actuatorHandlers, ...announcementsHandlers, ...releasesHandlers]
