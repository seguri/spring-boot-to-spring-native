package com.milleniumcare.precertification.repository;

import com.milleniumcare.precertification.entity.*;
import com.milleniumcare.precertification.objectmother.PrecertificationRequestMother;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import javax.persistence.EntityManager;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class PrecertificationRequestRepoTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private PrecertificationRequestRepo repo;

    @Test
    void injectedComponentsAreNotNull() {
        assertThat(repo).isNotNull();
    }

    @Test
    void createAndfindFirstByPrecertNumber_success() {
        Address a = PrecertificationRequestMother.address().build();
        em.persist(a);

        Plan p = PrecertificationRequestMother.plan().build();
        em.persist(p);

        Eligibility e = PrecertificationRequestMother.eligibility().plan(p).build();
        em.persist(e);

        Member m = PrecertificationRequestMother.member().address(a).eligibilities(Collections.singletonList(e)).build();
        em.persist(m);

        Diagnosis di = PrecertificationRequestMother.diagnosis().build();
        em.persist(di);

        Drug dr = PrecertificationRequestMother.drug().build();
        em.persist(dr);

        Provider pr = PrecertificationRequestMother.provider().address(a).build();
        em.persist(pr);

        PrecertificationRequest prd = PrecertificationRequestMother
                .precertificationRequestDrug()
                .drug(dr)
                .member(m)
                .requestingProvider(pr)
                .diagnosis(di).precertNumber("PR-1").build();
        repo.save(prd);

        PrecertificationRequest found = repo.findFirstByPrecertNumber("PR-1").get();
        assertThat(found.getPrecertNumber()).isEqualTo("PR-1");
    }
}
