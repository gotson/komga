import fs from "node:fs";
import openapiTS, { astToString } from "openapi-typescript";
import ts from "typescript";

// From https://openapi-ts.dev/node
// We use the Node.js API as the CLI does not support Date types

const mySchema = new URL("../komga/docs/openapi.json", import.meta.url)

const DATE = ts.factory.createTypeReferenceNode(ts.factory.createIdentifier("Date")); // `Date`
const NULL = ts.factory.createLiteralTypeNode(ts.factory.createNull()); // `null`

const ast = await openapiTS(mySchema, {
  transform(schemaObject, metadata) {
    if (schemaObject.format === "date-time") {
      return schemaObject.nullable
        ? ts.factory.createUnionTypeNode([DATE, NULL])
        : DATE;
    }
  },
});

const contents = astToString(ast);

// (optional) write to file
fs.writeFileSync("./src/generated/openapi/komga.d.ts", contents);
