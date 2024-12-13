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
  val domainModelShouldNotAccessOtherPackages: ArchRule =
    noClasses()
      .that()
      .resideInAPackage("..domain..model..")
      .should()
      .dependOnClassesThat()
      .resideInAnyPackage(
        "..infrastructure..",
        "..interfaces..",
        "..domain.persistence..",
        "..domain.service..",
      )

  @ArchTest
  var classesNamedControllerShouldBeInAnInterfacesPackage: ArchRule =
    classes()
      .that()
      .haveSimpleNameContaining("Controller")
      .should()
      .resideInAPackage("..interfaces..")
}
