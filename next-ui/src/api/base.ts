declare global {
  interface Window {
    resourceBaseUrl: string
  }
}

export const API_BASE_URL =
  import.meta.env.VITE_KOMGA_API_URL || window.location.origin + window.resourceBaseUrl
