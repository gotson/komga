package org.gotson.komga.infrastructure.openapi

import org.springdoc.core.customizers.OpenApiCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order

/**
 * The generated schema for sealed classes is somehow wrong.
 * This customizer will correct the issues.
 */
@Configuration
@Order(Ordered.LOWEST_PRECEDENCE)
class InheritanceFlattenerConfiguration {
  private val schemaPrefix = listOf("SearchOperator", "SearchCondition")

  @Bean
  fun flattenInheritedSchemasCustomizer(): OpenApiCustomizer =
    OpenApiCustomizer { openApi ->
      openApi.components
        ?.schemas
        ?.values
        ?.filter { schema -> schemaPrefix.any { prefix -> schema.name.startsWith(prefix, false) } }
        ?.forEach { schema ->
          // Swagger models inheritance as an allOf list with exactly two items:
          // 1. The $ref to the parent interface
          // 2. An inline schema containing the child's actual properties
          if (schema.allOf != null) {
            val refSchema = schema.allOf.filter { it.`$ref` != null }
            val inlineSchema = schema.allOf.find { it.properties != null }

            // If both are found, we know this is an inherited schema wrapper
            if (refSchema.isNotEmpty() && inlineSchema != null) {
              // Move the properties up to the root schema
              schema.properties = inlineSchema.properties

              // Delete the allOf array entirely
              schema.allOf = null
            }
          }
        }
    }
}
