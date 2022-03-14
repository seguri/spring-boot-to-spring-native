package com.milleniumcare.claims.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.milleniumcare.claims.repository.ClaimsRepo;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION)
@JsonSubTypes({
        @JsonSubTypes.Type(ClaimDrug.class),
        @JsonSubTypes.Type(ClaimOutpatient.class),
        @JsonSubTypes.Type(ClaimInpatient.class)}
)
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "claim")
@Entity
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class Claim {
    private int id;
    private String claimNumber;
    private String precertNumber;
    private LocalDate claimDate;
    private BigDecimal submittedAmount;
    private BigDecimal approvedAmount;
    private LocalDate paymentDate;
    private String paymentNumber;
    private ClaimStatus status;
    private LocalDate statusDate;
    private Member member;
    private Provider submittingProvider;
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
    @Column(name = "claim_number")
    public String getClaimNumber() {
        return claimNumber;
    }

    public void setClaimNumber(String claimNumber) {
        this.claimNumber = claimNumber;
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
    public abstract String getClaimType();

    @Basic
    @Column(name = "claim_date")
    public LocalDate getClaimDate() {
        return claimDate;
    }

    public void setClaimDate(LocalDate claimDate) {
        this.claimDate = claimDate;
    }

    @Basic
    @Column(name = "payment_date")
    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    @Enumerated(EnumType.ORDINAL)
    @Column(name = "status")
    public ClaimStatus getStatus() {
        return status;
    }

    public void setStatus(ClaimStatus status) {
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
    @JoinColumn(name = "submitting_provider_id", referencedColumnName = "id", nullable = false)
    public Provider getSubmittingProvider() {
        return submittingProvider;
    }

    public void setSubmittingProvider(Provider submittingProvider) {
        this.submittingProvider = submittingProvider;
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
    public Member getMember() {
        return member;
    }

    public void setMember(Member member) {
        this.member = member;
    }

    @Basic
    @Column(name = "submitted_amount")
    public BigDecimal getSubmittedAmount() {
        return submittedAmount;
    }

    public void setSubmittedAmount(BigDecimal submittedAmount) {
        this.submittedAmount = submittedAmount;
    }

    @Basic
    @Column(name = "approved_amount")
    public BigDecimal getApprovedAmount() {
        return approvedAmount;
    }

    public void setApprovedAmount(BigDecimal approvedAmount) {
        this.approvedAmount = approvedAmount;
    }

    @Basic
    @Column(name = "payment_number")
    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public abstract void generateClaimNumber(Integer seqNum);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Claim claim = (Claim) o;
        return id == claim.id && Objects.equals(claimNumber, claim.claimNumber) && Objects.equals(precertNumber, claim.precertNumber) && Objects.equals(claimDate, claim.claimDate) && Objects.equals(submittedAmount, claim.submittedAmount) && Objects.equals(approvedAmount, claim.approvedAmount) && Objects.equals(paymentDate, claim.paymentDate) && Objects.equals(paymentNumber, claim.paymentNumber) && status == claim.status && Objects.equals(statusDate, claim.statusDate) && Objects.equals(member, claim.member) && Objects.equals(submittingProvider, claim.submittingProvider) && Objects.equals(diagnosis, claim.diagnosis);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, claimNumber, precertNumber, claimDate, submittedAmount, approvedAmount, paymentDate, paymentNumber, status, statusDate, member, submittingProvider, diagnosis);
    }

    public abstract boolean checkIfDuplicate(ClaimsRepo repo);

}