package com.milleniumcare.precertification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.milleniumcare.precertification.entity.*;
import com.milleniumcare.precertification.objectmother.PrecertificationRequestMother;
import com.milleniumcare.precertification.repository.PrecertificationRequestRepo;
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

@WebMvcTest(PrecertificationServiceController.class)
public class PrecertificationServiceControllerTest {
    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper mapper;

    @MockBean
    PrecertificationRequestRepo precertificationRequestRepo;

    @MockBean
    private JwtDecoder jwtDecoder;

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void byPrecertNumber_success() throws Exception {
        Mockito.when(precertificationRequestRepo.findFirstByPrecertNumber("DRG-101010101")).thenReturn(Optional.of(getMockPrecertificationRequestDrug()));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/precertificationrequests/DRG-101010101")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("precertNumber", is("DRG-101010101")));
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void bySubscriberNumber_success() throws Exception {
        Mockito.when(precertificationRequestRepo.findBySubscriberNumber("ABC-1111111111")).thenReturn(Collections.singletonList(getMockPrecertificationRequestDrug()));
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/precertificationrequests?subscriberNum=ABC-1111111111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].precertNumber", is("DRG-101010101")));
    }


    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void bySubscriberNumber_empty() throws Exception {
        Mockito.when(precertificationRequestRepo.findBySubscriberNumber("ABC-1111111111")).thenReturn(Collections.emptyList());
        mockMvc.perform(MockMvcRequestBuilders
                        .get("/precertificationrequests?subscriberNum=ABC-1111111111")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void createPrecertificationRequest_success() throws Exception {
        Mockito.when(precertificationRequestRepo.save(Mockito.any(PrecertificationRequest.class))).thenReturn(getMockPrecertificationRequestDrug());
        Mockito.when(precertificationRequestRepo.getNextSeqVal()).thenReturn(1);

        mockMvc.perform(post(URI.create("/precertificationrequests"))
                        .with(csrf())
                        .content(mapper.writeValueAsString(getMockPrecertificationRequestDrug()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("precertNumber", is("DRG-101010101")));
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void createPrecertificationRequest_duplicate() throws Exception {
        Mockito.when(precertificationRequestRepo.findDuplicates(1, 1)).thenReturn(Collections.singletonList(getMockPrecertificationRequestDrug()));

        mockMvc.perform(post(URI.create("/precertificationrequests"))
                        .with(csrf())
                        .content(mapper.writeValueAsString(getMockPrecertificationRequestDrug()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void voidPrecertificationRequest_success() throws Exception {
        Mockito.when(precertificationRequestRepo.setStatus(1, PrecertificationRequestStatus.PENDING, PrecertificationRequestStatus.VOID, LocalDate.now())).thenReturn(1);
        mockMvc.perform(put(URI.create("/precertificationrequests/1/void"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Adjudicators"})
    public void approvePrecertificationRequest_success() throws Exception {
        Mockito.when(precertificationRequestRepo.setStatus(1, PrecertificationRequestStatus.PENDING, PrecertificationRequestStatus.APPROVED, LocalDate.now())).thenReturn(1);
        mockMvc.perform(put(URI.create("/precertificationrequests/1/approve"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Adjudicators"})
    public void approvePrecertificationRequest_deny() throws Exception {
        Mockito.when(precertificationRequestRepo.setStatus(1, PrecertificationRequestStatus.PENDING, PrecertificationRequestStatus.DENIED, LocalDate.now())).thenReturn(1);
        mockMvc.perform(put(URI.create("/precertificationrequests/1/deny"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "intake_user", authorities = {"Intake"})
    public void voidPrecertificationRequest_failure() throws Exception {
        Mockito.when(precertificationRequestRepo.setStatus(1, PrecertificationRequestStatus.PENDING, PrecertificationRequestStatus.VOID, LocalDate.now())).thenReturn(0);
        mockMvc.perform(put(URI.create("/precertificationrequests/1/void"))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity());
    }

    private PrecertificationRequestDrug getMockPrecertificationRequestDrug() {
        Address a = PrecertificationRequestMother.address().id(1).build();
        Plan p = PrecertificationRequestMother.plan().id(1).build();
        Eligibility e = PrecertificationRequestMother.eligibility().id(1).plan(p).build();
        Member m = PrecertificationRequestMother.member().id(1).address(a).eligibilities(Collections.singletonList(e)).build();
        Diagnosis d = PrecertificationRequestMother.diagnosis().id(1).build();
        Drug dr = PrecertificationRequestMother.drug().id(1).build();
        Provider pr = PrecertificationRequestMother.provider().id(1).address(a).build();

        return (PrecertificationRequestDrug) PrecertificationRequestMother.precertificationRequestDrug().drug(dr).diagnosis(d).member(m).requestingProvider(pr).id(1).precertNumber("DRG-101010101").build();
    }
}
