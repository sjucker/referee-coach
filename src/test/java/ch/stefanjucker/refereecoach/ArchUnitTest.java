package ch.stefanjucker.refereecoach;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.GeneralCodingRules;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@AnalyzeClasses(packages = "ch.stefanjucker.refereecoach", importOptions = ImportOption.DoNotIncludeTests.class)
public class ArchUnitTest {

    // always use DateUtil
    @ArchTest
    public static final ArchRule no_now_without_zone = noClasses()
            .should().callMethod(LocalDate.class, "now")
            .orShould().callMethod(LocalDateTime.class, "now")
            .orShould().callMethod(LocalTime.class, "now");

    @ArchTest
    public static final ArchRule no_jetbrains_imports = noClasses()
            .should().dependOnClassesThat()
            .resideInAPackage("org.jetbrains..");

    @ArchTest
    public static final ArchRule no_classes_should_access_standard_streams = GeneralCodingRules.NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS;
    @ArchTest
    public static final ArchRule no_classes_should_throw_generic_exceptions = GeneralCodingRules.NO_CLASSES_SHOULD_THROW_GENERIC_EXCEPTIONS;
    @ArchTest
    public static final ArchRule no_classes_should_use_jodatime = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JODATIME;
    @ArchTest
    public static final ArchRule no_classes_should_use_java_util_logging = GeneralCodingRules.NO_CLASSES_SHOULD_USE_JAVA_UTIL_LOGGING;
    @ArchTest
    public static final ArchRule no_classes_should_use_field_injection = GeneralCodingRules.NO_CLASSES_SHOULD_USE_FIELD_INJECTION;
    @ArchTest
    public static final ArchRule testClassesShouldResideInTheSamePackageAsImplementation = GeneralCodingRules.testClassesShouldResideInTheSamePackageAsImplementation();
    @ArchTest
    public static final ArchRule old_date_and_time_classes_should_not_be_used = GeneralCodingRules.OLD_DATE_AND_TIME_CLASSES_SHOULD_NOT_BE_USED;
    @ArchTest
    public static final ArchRule assertions_should_have_detail_message = GeneralCodingRules.ASSERTIONS_SHOULD_HAVE_DETAIL_MESSAGE;
    @ArchTest
    public static final ArchRule deprecated_api_should_not_be_used = GeneralCodingRules.DEPRECATED_API_SHOULD_NOT_BE_USED;

}
