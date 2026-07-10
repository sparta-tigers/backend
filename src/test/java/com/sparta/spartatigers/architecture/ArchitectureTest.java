package com.sparta.spartatigers.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAPackage;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.simpleNameEndingWith;
import static com.tngtech.archunit.base.DescribedPredicate.not;

@AnalyzeClasses(packages = "com.sparta.spartatigers.domain", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchitectureTest {

    // Rule 1: Foundation은 상위 계층(core, support)을 참조할 수 없다.
    @ArchTest
    static final ArchRule foundation_must_not_depend_on_core_or_support =
        noClasses()
            .that().resideInAPackage("..domain.foundation..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..domain.core..", "..domain.support..");

    // Rule 2: Support는 Core의 서비스 계층(Service, Repository, Controller)을 참조할 수 없다.
    @ArchTest
    static final ArchRule support_must_not_call_core_service_layer =
        noClasses()
            .that().resideInAPackage("..domain.support..")
            .should().dependOnClassesThat(
                resideInAPackage("..domain.core..").and(
                    simpleNameEndingWith("Service")
                        .or(simpleNameEndingWith("Repository"))
                        .or(simpleNameEndingWith("Controller"))
                )
            );

    // Rule 3: Core 도메인 간 서비스 계층 직접 참조 금지 (attendance)
    @ArchTest
    static final ArchRule attendance_must_not_call_other_core_service_layer =
        noClasses()
            .that().resideInAPackage("..domain.core.attendance..")
            .should().dependOnClassesThat(
                resideInAPackage("..domain.core..").and(
                    not(resideInAPackage("..domain.core.attendance.."))
                ).and(
                    simpleNameEndingWith("Service")
                        .or(simpleNameEndingWith("Repository"))
                        .or(simpleNameEndingWith("Controller"))
                )
            );

    // Rule 3: Core 도메인 간 서비스 계층 직접 참조 금지 (trade)
    @ArchTest
    static final ArchRule trade_must_not_call_other_core_service_layer =
        noClasses()
            .that().resideInAPackage("..domain.core.trade..")
            .should().dependOnClassesThat(
                resideInAPackage("..domain.core..").and(
                    not(resideInAPackage("..domain.core.trade.."))
                ).and(
                    simpleNameEndingWith("Service")
                        .or(simpleNameEndingWith("Repository"))
                        .or(simpleNameEndingWith("Controller"))
                )
            );

    // Rule 3: Core 도메인 간 서비스 계층 직접 참조 금지 (direct)
    @ArchTest
    static final ArchRule direct_must_not_call_other_core_service_layer =
        noClasses()
            .that().resideInAPackage("..domain.core.direct..")
            .should().dependOnClassesThat(
                resideInAPackage("..domain.core..").and(
                    not(resideInAPackage("..domain.core.direct.."))
                ).and(
                    simpleNameEndingWith("Service")
                        .or(simpleNameEndingWith("Repository"))
                        .or(simpleNameEndingWith("Controller"))
                )
            );

    // Rule 3: Core 도메인 간 서비스 계층 직접 참조 금지 (ticketalarm)
    @ArchTest
    static final ArchRule ticketalarm_must_not_call_other_core_service_layer =
        noClasses()
            .that().resideInAPackage("..domain.core.ticketalarm..")
            .should().dependOnClassesThat(
                resideInAPackage("..domain.core..").and(
                    not(resideInAPackage("..domain.core.ticketalarm.."))
                ).and(
                    simpleNameEndingWith("Service")
                        .or(simpleNameEndingWith("Repository"))
                        .or(simpleNameEndingWith("Controller"))
                )
            );
}
