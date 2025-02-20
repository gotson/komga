package org.gotson.komga.infrastructure.openapi

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Parameters(
  Parameter(
    description = "Author criteria in the format: name,role. Multiple author criteria are supported.",
    `in` = ParameterIn.QUERY,
    name = "author",
    array = ArraySchema(schema = Schema(type = "string")),
  ),
)
annotation class AuthorsAsQueryParam
