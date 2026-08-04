import { setupOpenapiClient } from '@/api/komga-client'

// ignore some warnings thrown when running Storybook in vitest
const originalWarn = console.warn
console.warn = (...args) => {
  const msg = args.join(' ')
  // Ignore the specific decodeEntities warning
  if (msg.includes('decodeEntities option is passed but will be ignored')) {
    return
  }
  originalWarn(...args)
}

setupOpenapiClient()
