import { httpTyped } from '@/mocks/api/httpTyped'
import { response400BadRequest } from '@/mocks/api/handlers'

const emptyPath = { directories: [{ type: 'directory', name: '/', path: '/' }], files: [] }

const rootSlash = {
  parent: '',
  directories: [
    { type: 'directory', name: 'Applications', path: '/Applications' },
    { type: 'directory', name: 'bin', path: '/bin' },
    { type: 'directory', name: 'cores', path: '/cores' },
    { type: 'directory', name: 'comics', path: '/comics' },
    { type: 'directory', name: 'dev', path: '/dev' },
    { type: 'directory', name: 'etc', path: '/etc' },
    { type: 'directory', name: 'home', path: '/home' },
    { type: 'directory', name: 'Library', path: '/Library' },
    { type: 'directory', name: 'opt', path: '/opt' },
    { type: 'directory', name: 'private', path: '/private' },
    { type: 'directory', name: 'sbin', path: '/sbin' },
    { type: 'directory', name: 'System', path: '/System' },
    { type: 'directory', name: 'tmp', path: '/tmp' },
    { type: 'directory', name: 'Users', path: '/Users' },
    { type: 'directory', name: 'usr', path: '/usr' },
    { type: 'directory', name: 'var', path: '/var' },
    { type: 'directory', name: 'Volumes', path: '/Volumes' },
  ],
  files: [],
}

const comics = {
  parent: '/',
  directories: [
    { type: 'directory', name: '_oneshots', path: '/comics/_oneshots' },
    { type: 'directory', name: 'Golden Age', path: '/comics/Golden Age' },
    { type: 'directory', name: 'Wika', path: '/comics/Wika' },
    { type: 'directory', name: 'Zorro', path: '/comics/Zorro' },
  ],
  files: [],
}

const empty = { parent: '/', directories: [], files: [] }

export const filesystemHandlers = [
  httpTyped.post('/api/v1/filesystem', async ({ request, response }) => {
    const data = await request.json()

    if (data?.path === '') {
      return response(200).json(emptyPath)
    } else if (data?.path === '/') {
      return response(200).json(rootSlash)
    } else if (data?.path === '/comics') {
      return response(200).json(comics)
    } else if (
      [...rootSlash.directories, ...comics.directories].some((it) => it.path === data?.path)
    ) {
      return response(200).json(empty)
    }

    return response.untyped(response400BadRequest())
  }),
]
