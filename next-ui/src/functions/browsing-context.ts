import * as v from 'valibot'
import type { MaybeRefOrGetter } from 'vue'
import type { LocationQueryRaw, RouteLocationRaw } from 'vue-router'

export const ContextQueryParam = 'ctx'
const ContextSeparator = '~'
const ContextStackMaxDepth = 3

export type SingleBrowsingContext =
  | { type: 'series'; id: string }
  | { type: 'readList'; id: string }
  | { type: 'collection'; id: string }
  | { type: 'libraryView'; id: string }

export type BrowsingContext = SingleBrowsingContext | SingleBrowsingContext[]

export const BrowsingContextKey = Symbol() as InjectionKey<
  MaybeRefOrGetter<BrowsingContext | undefined>
>

function parseSingleContext(val: string): SingleBrowsingContext | undefined {
  if (val.startsWith('s_')) return { type: 'series', id: val.slice(2) }
  if (val.startsWith('rl_')) return { type: 'readList', id: val.slice(3) }
  if (val.startsWith('c_')) return { type: 'collection', id: val.slice(2) }
  if (val.startsWith('lv_')) return { type: 'libraryView', id: val.slice(3) }

  return undefined
}

function formatSingleContext(context?: SingleBrowsingContext): string | undefined {
  if (!context) return undefined
  switch (context.type) {
    case 'readList':
      return context.id ? `rl_${context.id}` : undefined
    case 'collection':
      return context.id ? `c_${context.id}` : undefined
    case 'libraryView':
      return context.id ? `lv_${context.id}` : undefined
    case 'series':
      return context.id ? `s_${context.id}` : undefined
    default:
      return undefined
  }
}

const ContextParamSchema = v.pipe(
  v.string(),
  v.transform((raw): SingleBrowsingContext[] | undefined => {
    if (!raw) return undefined

    const segments = raw.split(ContextSeparator)
    const stack: SingleBrowsingContext[] = []

    for (const segment of segments) {
      const parsed = parseSingleContext(segment)
      if (parsed) stack.push(parsed)
    }

    if (stack.length === 0) return undefined

    // Enforce max depth by keeping the most recent items
    return stack.slice(-ContextStackMaxDepth)
  }),
)

/**
 * Parses a query parameter into a browsing context.
 */
export function parseBrowsingContext(raw: unknown): SingleBrowsingContext[] | undefined {
  if (typeof raw !== 'string' || !raw) return undefined
  try {
    return v.parse(ContextParamSchema, raw)
  } catch {
    return undefined
  }
}

/**
 * Formats the given context as a single query string.
 */
export function formatBrowsingContext(context?: BrowsingContext): string | undefined {
  if (!context) return undefined

  const stack = Array.isArray(context) ? context : [context]
  const formattedSegments = stack.map(formatSingleContext).filter((s): s is string => Boolean(s))

  return formattedSegments.length > 0 ? formattedSegments.join(ContextSeparator) : undefined
}

/**
 * Formats the given context as a Vue Router query
 */
export function formatBrowsingContextAsQueryParam(context?: BrowsingContext): LocationQueryRaw {
  const formatted = formatBrowsingContext(context)
  return { [ContextQueryParam]: formatted }
}

/**
 * Builds a Vue Router location from the context.
 *
 * @param context Context to convert to route
 * @param query Optional query parameters
 */
export function browsingContextToRouteLocation(
  context?: SingleBrowsingContext,
  query?: LocationQueryRaw,
): RouteLocationRaw | undefined {
  if (!context) return undefined
  switch (context.type) {
    case 'series':
      return { name: '/series/[id]', params: { id: context.id }, query: query }
    case 'readList':
      return { name: '/readlist/[id]', params: { id: context.id }, query: query }
    case 'collection':
      return { name: '/collection/[id]', params: { id: context.id }, query: query }
    case 'libraryView':
      return { name: '/libraries/[viewId]', params: { viewId: context.id }, query: query }
    default:
      return undefined
  }
}

/**
 * Appends a new context onto an existing stack.
 */
export function pushBrowsingContext(
  existingContext: BrowsingContext | undefined,
  nextContext: SingleBrowsingContext,
): SingleBrowsingContext[] {
  const stack = existingContext
    ? Array.isArray(existingContext)
      ? [...existingContext]
      : [existingContext]
    : []

  // Avoid pushing duplicate back-to-back entries
  const last = stack[stack.length - 1]
  if (last && last.type === nextContext.type && last.id === nextContext.id) {
    return stack
  }

  stack.push(nextContext)

  // Depth capping
  if (stack.length > ContextStackMaxDepth) {
    stack.shift()
  }

  return stack
}

/**
 * Pops the top context from the stack to return to the parent context.
 * Returns the remaining stack and the target parent item.
 */
export function popBrowsingContext(context: BrowsingContext | undefined): {
  top?: SingleBrowsingContext
  remainingStack?: SingleBrowsingContext[]
} {
  if (!context) return {}

  const stack = Array.isArray(context) ? [...context] : [context]
  const top = stack.pop()

  return {
    top,
    remainingStack: stack.length > 0 ? stack : undefined,
  }
}

/**
 * Strips specified context types from the stack, keeping only allowed types.
 */
export function filterBrowsingContext(
  context: BrowsingContext | undefined,
  allowedTypes: SingleBrowsingContext['type'][],
): SingleBrowsingContext[] | undefined {
  if (!context) return undefined

  const stack = Array.isArray(context) ? context : [context]
  const filtered = stack.filter((item) => allowedTypes.includes(item.type))

  return filtered.length > 0 ? filtered : undefined
}
