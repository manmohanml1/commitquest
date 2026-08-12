package com.commitquest;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

    private final JavaClasses classes = new ClassFileImporter().importPackages("com.commitquest");

    @Test
    void domainDoesNotDependOnTransportOrProviderCode() {
        noClasses()
                .that()
                .resideInAPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..application..", "..provider..", "..web..")
                .check(classes);
    }

    @Test
    void applicationDoesNotDependOnAdapters() {
        noClasses()
                .that()
                .resideInAPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..provider..", "..web..")
                .check(classes);
    }
}
