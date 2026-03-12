declare global {
  interface Window {
    resourceBaseUrl: string
  }
}

const fullUrl =
  import.meta.env.VITE_KOMGA_API_URL || window.location.origin + (window.resourceBaseUrl || '/')

const baseUrlSlash = !fullUrl.endsWith('/') ? `${fullUrl}/` : fullUrl
const baseUrlNoSlash = baseUrlSlash.endsWith('/') ? baseUrlSlash.slice(0, -1) : baseUrlSlash

export const ApiBaseUrl = {
  slash: baseUrlSlash,
  noSlash: baseUrlNoSlash,
}
