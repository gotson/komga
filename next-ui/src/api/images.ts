export function pageHashKnownThumbnailUrl(hash?: string): string | undefined {
  if (hash) return `${import.meta.env.VITE_KOMGA_API_URL}/api/v1/page-hashes/${hash}/thumbnail`
  return undefined
}
