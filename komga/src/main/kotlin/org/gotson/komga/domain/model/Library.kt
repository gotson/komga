package org.gotson.komga.domain.model

import java.nio.file.FileSystem
import java.nio.file.FileSystems

data class Library(
    val name: String,
    val root: String,
    val fileSystem: FileSystem = FileSystems.getDefault()
)