package com.milleniumcare.precertification.entity;

import com.milleniumcare.precertification.repository.PrecertificationRequestRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Table(name = "precertification_outpatient")
@Entity
@PrimaryKeyJoinColumn(name = "precertification_request_id")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class PrecertificationRequestOutpatient extends PrecertificationRequest {
    private Procedure procedure;
    private LocalDate procedureDate;

    @Override
    @Transient
    public String getRequestType() {
        return "OUTPATIENT";
    }

    @ManyToOne
    @JoinColumn(name = "procedure_code_id", referencedColumnName = "id", nullable = false)
    public Procedure getProcedure() {
        return procedure;
    }

    public void setProcedure(Procedure procedure) {
        this.procedure = procedure;
    }

    @Basic
    @Column(name = "procedure_date")
    public LocalDate getProcedureDate() {
        return procedureDate;
    }

    public void setProcedureDate(LocalDate procedureDate) {
        this.procedureDate = procedureDate;
    }

    @Override
    public void generatePrecertNumber(Integer seqNum) {
        setPrecertNumber("OP" + String.format("%010d", seqNum));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrecertificationRequestOutpatient that = (PrecertificationRequestOutpatient) o;
        return procedure.equals(that.procedure) && procedureDate.equals(that.procedureDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), procedure, procedureDate);
    }

    @Override
    public boolean checkIfDuplicate(PrecertificationRequestRepo repo) {
        return !repo.findDuplicates(this.getMember().getId(), this.getProcedure().getId(), this.getProcedureDate()).isEmpty();
    }
}