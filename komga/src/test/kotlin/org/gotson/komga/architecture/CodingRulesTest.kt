package org.gotson.komga.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME
import org.gotson.komga.Application

@AnalyzeClasses(packagesOf = [Application::class], importOptions = [ImportOption.DoNotIncludeTests::class])
class CodingRulesTest {
  @ArchTest
  private val noAccessToStandardStreams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS

  @ArchTest
  private val noGenericExceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS

  @ArchTest
  private val noJodatime = NO_CLASSES_SHOULD_USE_JODATIME

  @ArchTest
  private val noJavaUtilLogging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING

  @ArchTest
  private val noFieldInjection = NO_CLASSES_SHOULD_USE_FIELD_INJECTION
}

@AnalyzeClasses(packagesOf = [Application::class], importOptions = [ImportOption.OnlyIncludeTests::class])
class TestCodingRulesTest {
  @ArchTest
  private val noJunitAssertions = noClasses().should().dependOnClassesThat().haveFullyQualifiedName("org.junit.jupiter.api.Assertions")
}
