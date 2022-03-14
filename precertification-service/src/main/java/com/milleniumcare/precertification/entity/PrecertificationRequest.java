package com.milleniumcare.precertification.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.milleniumcare.precertification.repository.PrecertificationRequestRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(PrecertificationRequestDrug.class),
        @JsonSubTypes.Type(PrecertificationRequestOutpatient.class),
        @JsonSubTypes.Type(PrecertificationRequestInpatient.class)}
)
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "precertification_request")
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class PrecertificationRequest {
    private int id;
    private String precertNumber;
    private LocalDate requestDate;
    private LocalDate dueDate;
    private PrecertificationRequestStatus status;
    private LocalDate statusDate;
    private Member member;
    private Provider requestingProvider;
    private Diagnosis diagnosis;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "precert_number")
    public String getPrecertNumber() {
        return precertNumber;
    }

    public void setPrecertNumber(String precertNumber) {
        this.precertNumber = precertNumber;
    }

    @Transient
    public abstract String getRequestType();

    @Basic
    @Column(name = "request_date")
    public LocalDate getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(LocalDate requestDate) {
        this.requestDate = requestDate;
    }

    @Basic
    @Column(name = "due_date")
    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    @Basic
    @Column(name = "status")
    public PrecertificationRequestStatus getStatus() {
        return status;
    }

    public void setStatus(PrecertificationRequestStatus status) {
        this.status = status;
    }

    @Basic
    @Column(name = "status_date")
    public LocalDate getStatusDate() {
        return statusDate;
    }

    public void setStatusDate(LocalDate statusDate) {
        this.statusDate = statusDate;
    }

    @ManyToOne
    @JoinColumn(name = "requesting_provider_id", referencedColumnName = "id", nullable = false)
    public Provider getRequestingProvider() {
        return requestingProvider;
    }

    public void setRequestingProvider(Provider requestingProvider) {
        this.requestingProvider = requestingProvider;
    }

    @ManyToOne
    @JoinColumn(name = "diagnosis_id", referencedColumnName = "id", nullable = false)
    public Diagnosis getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(Diagnosis diagnosis) {
        this.diagnosis = diagnosis;
    }


    @ManyToOne
    @JoinColumn(name = "member_id", referencedColumnName = "id", nullable = false)
    //@JsonBackReference
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    public abstract void generatePrecertNumber(Integer seqNum);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrecertificationRequest that = (PrecertificationRequest) o;
        return id == that.id && precertNumber.equals(that.precertNumber) && requestDate.equals(that.requestDate) && dueDate.equals(that.dueDate) && status.equals(that.status) && statusDate.equals(that.statusDate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, precertNumber, requestDate, dueDate, status, statusDate);
    }

    public abstract boolean checkIfDuplicate(PrecertificationRequestRepo repo);

}