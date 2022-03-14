package com.milleniumcare.claims.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milleniumcare.claims.entity.*;
import com.milleniumcare.claims.objectmother.ClaimMother;
import com.milleniumcare.claims.repository.ClaimsRepo;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClaimsServiceController.class)
public class ClaimsServiceControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    ClaimsRepo claimsRepo;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void byClaimNumber_success() throws Exception {
        Mockito.when(claimsRepo.findFirstByClaimNumber("DRG-101010101")).thenReturn(Optional.of(getMockClaimDrug()));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/claims/DRG-101010101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("claimNumber", is("DRG-101010101")));
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void bySubscriberNumber_success() throws Exception {
        Mockito.when(claimsRepo.findBySubscriberNumber("ABC-1111111111")).thenReturn(Collections.singletonList(getMockClaimDrug()));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/claims?subscriberNum=ABC-1111111111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].claimNumber", is("DRG-101010101")));
    }


    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void bySubscriberNumber_empty() throws Exception {
        Mockito.when(claimsRepo.findBySubscriberNumber("ABC-1111111111")).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/claims?subscriberNum=ABC-1111111111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void createClaim_success() throws Exception {
        Mockito.when(claimsRepo.save(Mockito.any(Claim.class))).thenReturn(getMockClaimDrug());
        Mockito.when(claimsRepo.getNextSeqVal()).thenReturn(1);

        mockMvc.perform(post(URI.create("/claims"))
                        .with(csrf())
                        .content(mapper.writeValueAsString(getMockClaimDrug()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("claimNumber", is("DRG-101010101")));
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void createClaim_duplicate() throws Exception {
        Mockito.when(claimsRepo.findDuplicates(1, 1)).thenReturn(Collections.singletonList(getMockClaimDrug()));

        mockMvc.perform(post(URI.create("/claims"))
                        .with(csrf())
                        .content(mapper.writeValueAsString(getMockClaimDrug()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void voidClaim_success() throws Exception {
        Mockito.when(claimsRepo.setStatus(1, ClaimStatus.PENDING, ClaimStatus.VOID, LocalDate.now())).thenReturn(1);
        mockMvc.perform(put(URI.create("/claims/1/void"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Adjudicators"})
    public void approveClaim_success() throws Exception {
        Mockito.when(claimsRepo.approveClaim(1, BigDecimal.valueOf(10.0), ClaimStatus.APPROVED, LocalDate.now())).thenReturn(1);
        mockMvc.perform(put(URI.create("/claims/1/approve?approvedAmount=10.0"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Adjudicators"})
    public void approveClaim_deny() throws Exception {
        Mockito.when(claimsRepo.setStatus(1, ClaimStatus.PENDING, ClaimStatus.DENIED, LocalDate.now())).thenReturn(1);
        mockMvc.perform(put(URI.create("/claims/1/deny"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void voidClaim_failure() throws Exception {
        Mockito.when(claimsRepo.setStatus(1, ClaimStatus.PENDING, ClaimStatus.VOID, LocalDate.now())).thenReturn(0);
        mockMvc.perform(put(URI.create("/claims/1/void"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    private ClaimDrug getMockClaimDrug() {
        Address a = ClaimMother.address().id(1).build();
        Plan p = ClaimMother.plan().id(1).build();
        Eligibility e = ClaimMother.eligibility().id(1).plan(p).build();
        Member m = ClaimMother.member().id(1).address(a).eligibilities(Collections.singletonList(e)).build();
        Diagnosis d = ClaimMother.diagnosis().id(1).build();
        Drug dr = ClaimMother.drug().id(1).build();
        Provider pr = ClaimMother.provider().id(1).address(a).build();

        return (ClaimDrug) ClaimMother.claimDrug().drug(dr).diagnosis(d).member(m).submittingProvider(pr).id(1).claimNumber("DRG-101010101").build();
    }
}
