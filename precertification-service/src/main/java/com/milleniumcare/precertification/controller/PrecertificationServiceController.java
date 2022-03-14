package com.milleniumcare.precertification.controller;

import com.milleniumcare.precertification.entity.PrecertificationRequest;
import com.milleniumcare.precertification.entity.PrecertificationRequestStatus;
import com.milleniumcare.precertification.logging.LogEntryExit;
import com.milleniumcare.precertification.repository.PrecertificationRequestRepo;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@OpenAPIDefinition(info = @Info(title = "Precertification Service",
        description = "This is a REST API that is used to create, fetch, void and adjudicate precertification requests", version = "v1"))
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "jwt",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class PrecertificationServiceController {

    private final PrecertificationRequestRepo precertificationRequestRepo;

    @Autowired
    public PrecertificationServiceController(PrecertificationRequestRepo precertificationRequestRepo) {
        this.precertificationRequestRepo = precertificationRequestRepo;
    }

    @GetMapping("/precertificationrequests/{precertNumber}")
    @PreAuthorize("hasAuthority('Intake')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Get precertification requests by the precert number. The caller must have the \"Intake\" role", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to view precertification requests", content = @Content),
            @ApiResponse(responseCode = "200", description = "Found the precertification request"),
            @ApiResponse(responseCode = "404", description = "Precertification request not found", content = @Content)
    })
    public PrecertificationRequest byPrecertNumber(@PathVariable("precertNumber") String precertNumber) {
        return precertificationRequestRepo.findFirstByPrecertNumber(precertNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active pre-certification request found"));
    }

    @GetMapping("/precertificationrequests")
    @PreAuthorize("hasAuthority('Intake')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Get precertification requests by the member's subscriber number. The caller must have the \"Intake\" role", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to view precertification requests", content = @Content),
            @ApiResponse(responseCode = "200", description = "Found the precertification request"),
            @ApiResponse(responseCode = "404", description = "Precertification request not found", content = @Content)
    })
    public List<PrecertificationRequest> bySuscriberNumber(@RequestParam("subscriberNum") String subscriberNum) {
        List<PrecertificationRequest> reqs = precertificationRequestRepo.findBySubscriberNumber(subscriberNum);
        if (reqs.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active pre-certification request found");
        }
        return reqs;
    }

    @PostMapping(value = "/precertificationrequests", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Intake')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Create a new precertification request. The client must have the \"Intake\" role", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to create precertification requests", content = @Content),
            @ApiResponse(responseCode = "422", description = "Duplicate request. An identical precertification request already exists for the member.", content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request. Either the payload was malformed, or contained bad reference data", content = @Content),
            @ApiResponse(responseCode = "201", description = "Precertification request created.", content = @Content(schema = @Schema(implementation = CreatedPrecertNumber.class)))
    })
    public ResponseEntity<Object> createPrecertificationRequest(@RequestBody PrecertificationRequest request) {
        if (request.checkIfDuplicate(precertificationRequestRepo)) {
            log.debug("duplicate precertification request for member {}", request.getMember().getId());
            return ResponseEntity.unprocessableEntity().body("A duplicate or overlapping pre-certification request already exists");
        }
        request.generatePrecertNumber(precertificationRequestRepo.getNextSeqVal());
        request.setStatus(PrecertificationRequestStatus.PENDING);
        request.setStatusDate(LocalDate.now());
        PrecertificationRequest savedRequest = precertificationRequestRepo.save(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedRequest.getId()).toUri();

        return ResponseEntity.created(location).body(new CreatedPrecertNumber(savedRequest.getPrecertNumber()));
    }

    @PutMapping("/precertificationrequests/{precertId}/void")
    @PreAuthorize("hasAuthority('Intake')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Void a pending precertification request. The client must have the \"Intake\" role", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to void precertification requests", content = @Content),
            @ApiResponse(responseCode = "422", description = "Void request cannot be completed. Either precertification does not exist, or is not in pending state", content = @Content),
            @ApiResponse(responseCode = "200", description = "Successfully voided", content = @Content)
    })
    public ResponseEntity<Object> voidPrecertificationRequest(@PathVariable("precertId") Integer precertId) {
        return doVoidApproveOrDeny(precertId, PrecertificationRequestStatus.VOID);
    }

    @PutMapping("/precertificationrequests/{precertId}/approve")
    @PreAuthorize("hasAuthority('Adjudicators')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Approve a pending precertification request. The client must have the \"Adjudicator\" role.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to approve precertification requests", content = @Content),
            @ApiResponse(responseCode = "422", description = "Approve request cannot be completed. Either precertification does not exist, or is not in pending state", content = @Content),
            @ApiResponse(responseCode = "200", description = "Successfully approved", content = @Content)
    })
    public ResponseEntity<Object> approvePrecertificationRequest(@PathVariable("precertId") Integer precertId) {
        return doVoidApproveOrDeny(precertId, PrecertificationRequestStatus.APPROVED);
    }

    @PutMapping("/precertificationrequests/{precertId}/deny")
    @PreAuthorize("hasAuthority('Adjudicators')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Deny a pending precertification request. The client must have the \"Adjudicator\" role.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to deny precertification requests", content = @Content),
            @ApiResponse(responseCode = "422", description = "Deny request cannot be completed. Either precertification does not exist, or is not in pending state", content = @Content),
            @ApiResponse(responseCode = "200", description = "Successfully denied", content = @Content)
    })
    public ResponseEntity<Object> denyPrecertificationRequest(@PathVariable("precertId") Integer precertId) {
        return doVoidApproveOrDeny(precertId, PrecertificationRequestStatus.DENIED);
    }

    @LogEntryExit(LogLevel.DEBUG)
    private ResponseEntity<Object> doVoidApproveOrDeny(Integer precertId, PrecertificationRequestStatus newStatus) {
        if (precertificationRequestRepo.setStatus(precertId, PrecertificationRequestStatus.PENDING, newStatus, LocalDate.now()) != 1) {
            // only pending requests can be voided, approved or denied
            log.info("cannot change  status of precertification request, either missing or in wrong status");
            return ResponseEntity.unprocessableEntity().body("Precertification request not found, or is not in PENDING state");
        }

        return ResponseEntity.ok().build();
    }
}

@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Getter
class CreatedPrecertNumber {
    String precertNumber;
}