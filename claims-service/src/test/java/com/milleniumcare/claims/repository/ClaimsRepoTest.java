package com.milleniumcare.claims.repository;

import com.milleniumcare.claims.entity.*;
import com.milleniumcare.claims.objectmother.ClaimMother;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ClaimsRepoTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ClaimsRepo repo;

    @Test
    void injectedComponentsAreNotNull() {
        assertThat(repo).isNotNull();
    }

    @Test
    void createAndfindFirstByClaimNumber_success() {
        Address a = ClaimMother.address().build();
        em.persist(a);

        Plan p = ClaimMother.plan().build();
        em.persist(p);

        Eligibility e = ClaimMother.eligibility().plan(p).build();
        em.persist(e);

        Member m = ClaimMother.member().address(a).eligibilities(Collections.singletonList(e)).build();
        em.persist(m);

        Diagnosis di = ClaimMother.diagnosis().build();
        em.persist(di);

        Drug dr = ClaimMother.drug().build();
        em.persist(dr);

        Provider pr = ClaimMother.provider().address(a).build();
        em.persist(pr);

        Claim prd = ClaimMother
                .claimDrug()
                .drug(dr)
                .member(m)
                .submittingProvider(pr)
                .diagnosis(di).claimNumber("PR-1").build();
        repo.save(prd);

        Claim found = repo.findFirstByClaimNumber("PR-1").get();
        assertThat(found.getClaimNumber()).isEqualTo("PR-1");
    }
}
