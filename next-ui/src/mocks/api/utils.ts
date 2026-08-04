import { HttpResponse, type JsonBodyType } from 'msw'

export const response200OK = (body: JsonBodyType) => HttpResponse.json(body, { status: 200 })
export const response202Empty = () => HttpResponse.json(undefined, { status: 202 })
export const response204Empty = () => HttpResponse.json(undefined, { status: 204 })
export const response400BadRequest = () =>
  HttpResponse.json({ error: 'Bad Request' }, { status: 400 })
export const response400 = (body: JsonBodyType) => HttpResponse.json(body, { status: 400 })
export const response404NotFound = () => HttpResponse.json({ error: 'NotFound' }, { status: 404 })
export const response401Unauthorized = () =>
  HttpResponse.json({ error: 'Unauthorized' }, { status: 401 })
export const response502BadGateway = () =>
  HttpResponse.json({ error: 'Bad gateway' }, { status: 502 })
