package com.milleniumcare.claims.entity;

import com.milleniumcare.claims.repository.ClaimsRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.*;
import java.sql.Date;
import java.util.Objects;

@Table(name = "claim_outpatient")
@Entity
@PrimaryKeyJoinColumn(name = "claim_id")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class ClaimOutpatient extends Claim {
    private Procedure procedure;
    private Date procedureDate;

    @Override
    @Transient
    public String getClaimType() {
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
    public Date getProcedureDate() {
        return procedureDate;
    }

    public void setProcedureDate(Date procedureDate) {
        this.procedureDate = procedureDate;
    }

    @Override
    public void generateClaimNumber(Integer seqNum) {
        setClaimNumber("CLM-OP" + String.format("%010d", seqNum));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClaimOutpatient that = (ClaimOutpatient) o;
        return procedure.equals(that.procedure) && procedureDate.equals(that.procedureDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), procedure, procedureDate);
    }

    @Override
    public boolean checkIfDuplicate(ClaimsRepo repo) {
        return !repo.findDuplicates(this.getMember().getId(), this.getProcedure().getId(), this.getProcedureDate()).isEmpty();
    }
}