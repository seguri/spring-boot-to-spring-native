package com.milleniumcare.claims.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Objects;

@Entity
@JsonIdentityInfo(
        generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id")
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
public class Member {
    private int id;
    private String subscriberNumber;
    private String firstName;
    private String lastName;
    private String gender;
    private LocalDate dob;
    private Collection<Eligibility> eligibilities;
    private Collection<Claim> claims;
    private Address address;


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
    @Column(name = "subscriber_number")
    public String getSubscriberNumber() {
        return subscriberNumber;
    }

    public void setSubscriberNumber(String subscriberNumber) {
        this.subscriberNumber = subscriberNumber;
    }

    @Basic
    @Column(name = "first_name")
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    @Basic
    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Basic
    @Column(name = "gender")
    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    @Basic
    @Column(name = "dob")
    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Member member = (Member) o;
        return id == member.id && Objects.equals(subscriberNumber, member.subscriberNumber) && Objects.equals(firstName, member.firstName) && Objects.equals(lastName, member.lastName) && Objects.equals(gender, member.gender) && Objects.equals(dob, member.dob);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, subscriberNumber, firstName, lastName, gender, dob);
    }

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    public Collection<Eligibility> getEligibilities() {
        return eligibilities;
    }

    public void setEligibilities(Collection<Eligibility> eligibilities) {
        this.eligibilities = eligibilities;
    }

    @ManyToOne
    @JoinColumn(name = "address_id", referencedColumnName = "id", nullable = false)
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    @OneToMany(mappedBy = "member")
    @JsonIgnore
    public Collection<Claim> getClaims() {
        return claims;
    }

    public void setClaims(Collection<Claim> claims) {
        this.claims = claims;
    }
}
