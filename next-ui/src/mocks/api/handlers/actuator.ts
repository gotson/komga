import { httpTyped } from '@/mocks/api/httpTyped'

export const actuatorInfo = {
  git: {
    branch: 'master',
    commit: {
      id: '9be980d',
      time: '2025-03-12T03:40:38Z',
    },
  },
  build: {
    artifact: 'komga',
    name: 'komga',
    version: '1.21.2',
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
}

export const actuatorHandlers = [
  httpTyped.get('/actuator/info', ({ response }) => response(200).json(actuatorInfo)),
]
