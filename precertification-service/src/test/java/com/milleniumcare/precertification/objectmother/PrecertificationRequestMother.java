package com.milleniumcare.precertification.objectmother;

import com.milleniumcare.precertification.entity.*;

import java.time.LocalDate;

public class PrecertificationRequestMother {
    public static Address.AddressBuilder address() {
        return Address.builder().street1("1 Common Ave").city("Boston").state("MA").zip("01886");
    }

    public static Plan.PlanBuilder plan() {
        return Plan.builder().name("Medicaid").description("Some description");
    }

    public static Eligibility.EligibilityBuilder eligibility() {
        return Eligibility.builder().effectiveDate(LocalDate.parse("2018-01-01"))
                .endDate(LocalDate.parse("2022-01-01"));
    }

    public static Member.MemberBuilder member() {
        return Member.builder().dob(LocalDate.now())
                .firstName("John")
                .lastName("Doe")
                .gender("Male")
                .subscriberNumber("ABC-1111111111");
    }

    public static Provider.ProviderBuilder provider() {
        return Provider.builder()
                .firstName("John")
                .lastName("Smith")
                .gender("Male")
                .npi("473-123030303");
    }

    public static Drug.DrugBuilder drug() {
        return Drug.builder()
                .ndc("DN230303")
                .name("Zoloft")
                .description("Anti-depressant");
    }

    public static Diagnosis.DiagnosisBuilder diagnosis() {
        return Diagnosis.builder()
                .code("D1")
                .description("Sample diagnosis");
    }

    public static PrecertificationRequestDrug.PrecertificationRequestDrugBuilder precertificationRequestDrug() {
        return PrecertificationRequestDrug.builder()
                .precertNumber("DRG-101010101")
                .requestDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(10L))
                .status(PrecertificationRequestStatus.PENDING)
                .statusDate(LocalDate.now())
                .units(1);
    }
}
