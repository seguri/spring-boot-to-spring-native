package com.milleniumcare.precertification.entity;

import com.milleniumcare.precertification.repository.PrecertificationRequestRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.*;
import java.util.Objects;

@Table(name = "precertification_drug")
@Entity
@PrimaryKeyJoinColumn(name = "precertification_request_id")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class PrecertificationRequestDrug extends PrecertificationRequest {
    private int units;
    private Drug drug;

    @Override
    @Transient
    public String getRequestType() {
        return "DRUG";
    }

    @Basic
    @Column(name = "units")
    public int getUnits() {
        return units;
    }

    public void setUnits(int units) {
        this.units = units;
    }

    @ManyToOne
    @JoinColumn(name = "drug_id", referencedColumnName = "id", nullable = false)
    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    @Override
    public void generatePrecertNumber(Integer seqNum) {
        setPrecertNumber("DR" + String.format("%010d", seqNum));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrecertificationRequestDrug that = (PrecertificationRequestDrug) o;
        return units == that.units && drug.equals(that.drug);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), units, drug);
    }

    @Override
    public boolean checkIfDuplicate(PrecertificationRequestRepo repo) {
        return !repo.findDuplicates(this.getMember().getId(), this.getDrug().getId()).isEmpty();
    }
}