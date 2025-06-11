import { http, HttpResponse } from 'msw'
import { baseUrl } from '@/mocks/api/handlers/base'

export const actuatorHandlers = [
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
]
