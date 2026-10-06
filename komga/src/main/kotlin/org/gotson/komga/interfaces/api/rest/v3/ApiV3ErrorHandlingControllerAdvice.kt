package org.gotson.komga.interfaces.api.rest.v3

import jakarta.validation.ConstraintViolationException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ProblemDetail
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

/**
 * Applies strictly to controllers in the `v3` subpackage.
 * Emits RFC 9457 ProblemDetail error responses.
 */
@RestControllerAdvice(basePackages = ["org.gotson.komga.interfaces.api.rest.v3"])
class ApiV3ErrorHandlingControllerAdvice : ResponseEntityExceptionHandler() {
  override fun handleMethodArgumentNotValid(
    ex: MethodArgumentNotValidException,
    headers: HttpHeaders,
    status: HttpStatusCode,
    request: WebRequest,
  ): ResponseEntity<Any>? {
    val body = ex.updateAndGetBody(messageSource, request.locale)
    val violations = ex.bindingResult.fieldErrors.map { Violation(it.field, it.defaultMessage) }
    body.setProperty("violations", violations)
    return handleExceptionInternal(ex, body, headers, status, request)
  }

  @ExceptionHandler(ConstraintViolationException::class)
  fun onConstraintValidationException(e: ConstraintViolationException): ProblemDetail {
    val problemDetail =
      ProblemDetail.forStatusAndDetail(
        HttpStatus.BAD_REQUEST,
        "Validation failed for parameter constraints.",
      )
    val violations = e.constraintViolations.map { Violation(it.propertyPath.toString().substringAfterLast('.'), it.message) }
    problemDetail.setProperty("violations", violations)
    return problemDetail
  }
}

data class Violation(
  val field: String? = null,
  val message: String? = null,
)
