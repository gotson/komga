import fs from 'node:fs'
import openapiTS, { astToString } from 'openapi-typescript'
import ts from 'typescript'

// From https://openapi-ts.dev/node
// We use the Node.js API as the CLI does not support Date types

const mySchema = new URL('../komga/docs/openapi.json', import.meta.url)

const DATE = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('Date')) // `Date`
const FILE = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('File')) // `File`
const BLOB = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier('Blob')) // `Blob`
const NULL = ts.factory.createLiteralTypeNode(ts.factory.createNull()) // `null`

const ast = await openapiTS(mySchema, {
  transform(schemaObject, metadata) {
    if (schemaObject.format === 'date-time') {
      return schemaObject.nullable ? ts.factory.createUnionTypeNode([DATE, NULL]) : DATE
    }
    if (schemaObject.format === 'binary') {
      console.log(schemaObject)
      console.log(metadata)
      if (metadata.path.includes('multipart~1form-data')) {
        return schemaObject.nullable ? ts.factory.createUnionTypeNode([FILE, NULL]) : FILE
      }
      if (metadata.path.includes('application~1octet-stream')) {
        return schemaObject.nullable ? ts.factory.createUnionTypeNode([BLOB, NULL]) : BLOB
      }
    }
  },
})

const contents = astToString(ast)

// (optional) write to file
fs.writeFileSync('./src/generated/openapi/komga.d.ts', contents)
