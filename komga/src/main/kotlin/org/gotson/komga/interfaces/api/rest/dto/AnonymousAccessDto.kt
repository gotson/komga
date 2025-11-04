package org.gotson.komga.interfaces.api.rest.dto

data class AnonymousAccessSettingsDto(
  val libraryId: String,
  val libraryName: String,
  val allowAnonymousAccess: Boolean,
  val hideAdultContentForAnonymous: Boolean,
  val ageRestrictionForAnonymous: Int,
)

data class AgeRatingDto(
  val code: String,
  val minimumAge: Int,
  val requiresAuth: Boolean,
  val isAdultContent: Boolean,
  val isExplicitContent: Boolean,
)

data class ContentAccessDto(
  val canAccess: Boolean,
  val requiresAuthentication: Boolean,
  val reason: String? = null,
)

data class LibraryAccessDto(
  val libraryId: String,
  val libraryName: String,
  val canAccess: Boolean,
  val allowsAnonymousAccess: Boolean,
  val maxAgeRatingForAnonymous: Int,
)
