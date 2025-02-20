package org.gotson.komga.infrastructure.openapi

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Parameter(
  description = "Zero-based page index (0..N)",
  `in` = ParameterIn.QUERY,
  name = "page",
  schema = Schema(type = "integer"),
)
annotation class PageAsQueryParam

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Parameters(
  Parameter(
    description = "Zero-based page index (0..N)",
    `in` = ParameterIn.QUERY,
    name = "page",
    schema = Schema(type = "integer"),
  ),
  Parameter(
    description = "The size of the page to be returned",
    `in` = ParameterIn.QUERY,
    name = "size",
    schema = Schema(type = "integer"),
  ),
)
annotation class PageableWithoutSortAsQueryParam

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Parameters(
  Parameter(
    description = "Zero-based page index (0..N)",
    `in` = ParameterIn.QUERY,
    name = "page",
    schema = Schema(type = "integer"),
  ),
  Parameter(
    description = "The size of the page to be returned",
    `in` = ParameterIn.QUERY,
    name = "size",
    schema = Schema(type = "integer"),
  ),
  Parameter(
    description = "Sorting criteria in the format: property(,asc|desc). Default sort order is ascending. Multiple sort criteria are supported.",
    `in` = ParameterIn.QUERY,
    name = "sort",
    array = ArraySchema(schema = Schema(type = "string")),
  ),
)
annotation class PageableAsQueryParam
