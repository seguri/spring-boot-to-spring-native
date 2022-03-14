package com.milleniumcare.precertification.entity;

import com.milleniumcare.precertification.repository.PrecertificationRequestRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Table(name = "precertification_inpatient")
@Entity
@PrimaryKeyJoinColumn(name = "precertification_request_id")
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class PrecertificationRequestInpatient extends PrecertificationRequest {
    private LocalDate admitDate;
    private LocalDate dischargeDate;
    private Facility facility;


    @Override
    @Transient
    public String getRequestType() {
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
    public void generatePrecertNumber(Integer seqNum) {
        setPrecertNumber("IP" + String.format("%010d", seqNum));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        PrecertificationRequestInpatient that = (PrecertificationRequestInpatient) o;
        return dischargeDate.equals(that.dischargeDate) && admitDate.equals(that.admitDate) && facility.equals(that.facility);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), admitDate, dischargeDate, facility);
    }

    @Override
    public boolean checkIfDuplicate(PrecertificationRequestRepo repo) {
        return !repo.findDuplicates(this.getMember().getId(), this.getAdmitDate(), this.getDischargeDate()).isEmpty();
    }
}