import { setupOpenapiClient } from '@/api/komga-client'

// ignore some warnings thrown when running Storybook in vitest
const originalWarn = console.warn
console.warn = (...args) => {
  const msg = args.join(' ')
  // Ignore the specific decodeEntities warning
  if (
    msg.includes('decodeEntities option is passed but will be ignored') ||
    msg.includes('ResizeObserver loop completed with undelivered notifications') ||
    msg.includes('inject() can only be used inside setup()')
  ) {
    return
  }
  originalWarn(...args)
}

setupOpenapiClient()
