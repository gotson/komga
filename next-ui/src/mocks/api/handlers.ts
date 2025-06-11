import { http, HttpResponse } from 'msw'

export const baseUrl = import.meta.env.VITE_KOMGA_API_URL + '/'

export const handlers = [
  http.get(baseUrl + 'actuator/info', () => {
    return HttpResponse.json({
      git: {
        branch: 'master',
        commit: {
          id: 'ABC123',
          time: '2025-05-16T03:26:50Z',
        },
      },
      build: {
        artifact: 'komga',
        name: 'komga',
        version: '9.9.9',
        group: 'komga',
      },
      java: {
        version: '23.0.2',
        vendor: {
          name: 'Eclipse Adoptium',
          version: 'Temurin-23.0.2+7',
        },
        runtime: {
          name: 'OpenJDK Runtime Environment',
          version: '23.0.2+7',
        },
        jvm: {
          name: 'OpenJDK 64-Bit Server VM',
          vendor: 'Eclipse Adoptium',
          version: '23.0.2+7',
        },
      },
      os: {
        name: 'Linux',
        version: '6.8.0-57-generic',
        arch: 'amd64',
      },
    })
  }),
  http.get(baseUrl + 'api/v1/announcements', () => {
    return HttpResponse.json({
      version: 'https://jsonfeed.org/version/1',
      title: 'Announcements',
      home_page_url: 'https://komga.org/blog',
      description: 'Latest Komga announcements',
      items: [
        {
          id: 'https://komga.org/blog/ebook-drop2',
          url: 'https://komga.org/blog/ebook-drop2',
          title: 'eBook drop 2',
          summary: 'Version 1.9.0 contains the second feature drop for Ebooks support.',
          content_html: 'Truncated',
          date_modified: '2023-12-15T00:00:00Z',
          author: {
            name: 'gotson',
            url: 'https://github.com/gotson',
          },
          tags: ['upgrade', 'komga'],
          _komga: {
            read: false,
          },
        },
        {
          id: 'https://komga.org/blog/ebook-support',
          url: 'https://komga.org/blog/ebook-support',
          title: 'eBook support',
          summary: 'Version 1.8.0 is bringing a long awaited feature: proper eBook support!',
          content_html: 'Truncated',
          date_modified: '2023-11-29T00:00:00Z',
          author: {
            name: 'gotson',
            url: 'https://github.com/gotson',
          },
          tags: ['upgrade', 'komga'],
          _komga: {
            read: true,
          },
        },
      ],
    })
  }),
  http.get(baseUrl + 'api/v1/releases', () => {
    return HttpResponse.json([
      {
        version: '9.9.9',
        releaseDate: '2025-05-16T04:31:05Z',
        url: 'https://github.com/gotson/komga/releases/tag/1.21.3',
        latest: true,
        preRelease: false,
        description: 'Truncated',
      },
      {
        version: '1.21.2',
        releaseDate: '2025-03-12T04:19:30Z',
        url: 'https://github.com/gotson/komga/releases/tag/1.21.2',
        latest: false,
        preRelease: false,
        description: 'Truncated',
      },
      {
        version: '1.21.1',
        releaseDate: '2025-03-06T07:31:00Z',
        url: 'https://github.com/gotson/komga/releases/tag/1.21.1',
        latest: false,
        preRelease: false,
        description: 'Truncated',
      },
    ])
  }),
]
