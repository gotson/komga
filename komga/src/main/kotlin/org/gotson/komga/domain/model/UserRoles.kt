package org.gotson.komga.domain.model

enum class UserRoles {
  ADMIN,
  FILE_DOWNLOAD,
  PAGE_STREAMING,
  KOBO_SYNC,
  KOREADER_SYNC,
  ;

  companion object {
    /**
     * Returns a Set composed of the enum constant of this type with the specified name.
     * The string must match exactly an identifier used to declare an enum constant in this type.
     * (Extraneous whitespace characters are not permitted.)
     */
    fun valuesOf(roles: Iterable<String>): Set<UserRoles> =
      roles
        .mapNotNull {
          try {
            valueOf(it)
          } catch (_: Exception) {
            null
          }
        }.toSet()
  }
}
