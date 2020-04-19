package org.gotson.komga.infrastructure.swagger

import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.Parameters
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Schema

@Target(AnnotationTarget.ANNOTATION_CLASS, AnnotationTarget.FUNCTION)
@Parameters(
  Parameter(description = "Zero-based page index (0..N)", `in` = ParameterIn.QUERY, name = "page", schema = Schema(type = "integer", defaultValue = "0")),
  Parameter(description = "The size of the page to be returned", `in` = ParameterIn.QUERY, name = "size", schema = Schema(type = "integer", defaultValue = "20"))
)
annotation class PageableWithoutSort
