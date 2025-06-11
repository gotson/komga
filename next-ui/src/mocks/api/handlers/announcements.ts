import { http, HttpResponse } from 'msw'
import { baseUrl } from '@/mocks/api/handlers/base'

export const announcementsHandlers = [
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
]
