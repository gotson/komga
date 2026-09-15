export function useImagePrefetch(urls: MaybeRefOrGetter<string | string[] | undefined>) {
  const prefetchUrl = (url?: string) => {
    if (!url) return

    const img = new Image()
    // Align credentials handling with your v-img requirement
    img.src = url
    img.decoding = 'async'
  }

  watchEffect(() => {
    const value = toValue(urls)
    if (!value) return

    const list = Array.isArray(value) ? value : [value]
    list.forEach(prefetchUrl)
  })
}
