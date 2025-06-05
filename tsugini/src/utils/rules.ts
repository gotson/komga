export function required(err?: string) {
  return (v: unknown) => !!v || err || 'Required'
}

export function sameAs(other?: string, err?: string) {
  return (v: unknown) => other === v || err || 'Field must have the same value'
}
