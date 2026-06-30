import { defineStore } from 'pinia'

/**
 * Store the current date for a given ID.
 * The version can be appended to any URL to trigger a browser cache bust.
 */
export const useImageCacheStore = defineStore('image-cache', () => {
  const cacheVersions = ref<Record<string, number>>({})

  function bustCache(entityId: string) {
    cacheVersions.value[entityId] = Date.now()
  }

  function getVersion(entityId: string): number {
    return cacheVersions.value[entityId] || 0
  }

  return {
    bustCache,
    getVersion,
  }
})
