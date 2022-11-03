package org.gotson.komga.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
import com.tngtech.archunit.library.GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME
import org.gotson.komga.Application

@AnalyzeClasses(packagesOf = [Application::class], importOptions = [ImportOption.DoNotIncludeTests::class])
class CodingRulesTest {

  @ArchTest
  private val no_access_to_standard_streams = NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS

  @ArchTest
  private val no_generic_exceptions = NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS

  @ArchTest
  private val no_jodatime = NO_CLASSES_SHOULD_USE_JODATIME

  @ArchTest
  private val no_java_util_logging = NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING
}

@AnalyzeClasses(packagesOf = [Application::class], importOptions = [ImportOption.OnlyIncludeTests::class])
class TestCodingRulesTest {

  @ArchTest
  private val no_junit_assertions = noClasses().should().dependOnClassesThat().haveFullyQualifiedName("org.junit.jupiter.api.Assertions")
}
