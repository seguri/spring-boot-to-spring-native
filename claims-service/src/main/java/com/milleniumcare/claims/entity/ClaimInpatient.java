package com.milleniumcare.claims.entity;

import com.milleniumcare.claims.repository.ClaimsRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Table(name = "claim_inpatient")
@Entity
@PrimaryKeyJoinColumn(name = "claim_id")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class ClaimInpatient extends Claim {
    private LocalDate admitDate;
    private LocalDate dischargeDate;
    private Facility facility;


    @Override
    @Transient
    public String getClaimType() {
        return "INPATIENT";
    }

    @Basic
    @Column(name = "admit_date")
    public LocalDate getAdmitDate() {
        return admitDate;
    }

    public void setAdmitDate(LocalDate admitDate) {
        this.admitDate = admitDate;
    }

    @Basic
    @Column(name = "discharge_date")
    public LocalDate getDischargeDate() {
        return dischargeDate;
    }

    public void setDischargeDate(LocalDate dischargeDate) {
        this.dischargeDate = dischargeDate;
    }

    @ManyToOne
    @JoinColumn(name = "facility_id", referencedColumnName = "id", nullable = false)
    public Facility getFacility() {
        return facility;
    }

    public void setFacility(Facility facility) {
        this.facility = facility;
    }

    @Override
    public void generateClaimNumber(Integer seqNum) {
        setClaimNumber("CLM-IP" + String.format("%010d", seqNum));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ClaimInpatient that = (ClaimInpatient) o;
        return dischargeDate.equals(that.dischargeDate) && admitDate.equals(that.admitDate) && facility.equals(that.facility);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), admitDate, dischargeDate, facility);
    }

    @Override
    public boolean checkIfDuplicate(ClaimsRepo repo) {
        return !repo.findDuplicates(this.getMember().getId(), this.getAdmitDate(), this.getDischargeDate()).isEmpty();
    }
}