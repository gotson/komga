package org.gotson.komga.architecture

import com.tngtech.archunit.core.importer.ImportOption
import com.tngtech.archunit.junit.AnalyzeClasses
import com.tngtech.archunit.junit.ArchTest
import com.tngtech.archunit.lang.ArchRule
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses
import org.gotson.komga.Application


@AnalyzeClasses(packagesOf = [Application::class], importOptions = [ImportOption.DoNotIncludeTests::class])
class DomainDrivenDesignRulesTest {

  @ArchTest
  val domain_persistence_can_only_contain_interfaces: ArchRule =
    classes()
      .that().resideInAPackage("..domain..persistence..")
      .should().beInterfaces()

  @ArchTest
  val domain_model_should_not_access_other_packages: ArchRule =
    noClasses()
      .that().resideInAPackage("..domain..model..")
      .should().dependOnClassesThat().resideInAnyPackage(
        "..infrastructure..",
        "..interfaces..",
        "..domain.persistence..",
        "..domain.service.."
      )

  @ArchTest
  var classes_named_controller_should_be_in_an_interfaces_package: ArchRule =
    classes()
      .that().haveSimpleNameContaining("Controller")
      .should().resideInAPackage("..interfaces..")
}
