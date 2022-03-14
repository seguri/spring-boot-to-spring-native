package com.milleniumcare.claims.controller;

import com.milleniumcare.claims.entity.Claim;
import com.milleniumcare.claims.entity.ClaimStatus;
import com.milleniumcare.claims.logging.LogEntryExit;
import com.milleniumcare.claims.repository.ClaimsRepo;
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

import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@Slf4j
@OpenAPIDefinition(info = @Info(title = "Claims Service",
        description = "This is a REST API that is used to create, fetch, void and adjudicate claims", version = "v1"))
@SecurityScheme(
        name = "Authorization",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "jwt",
        scheme = "bearer",
        in = SecuritySchemeIn.HEADER
)
public class ClaimsServiceController {

    private final ClaimsRepo claimsRepo;

    @Autowired
    public ClaimsServiceController(ClaimsRepo claimsRepo) {
        this.claimsRepo = claimsRepo;
    }

    @GetMapping("/claims/{claimNumber}")
    @PreAuthorize("hasAuthority('Intake')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Get claims by the claim number. The caller must have the \"Intake\" role", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to view claims",
                    content = @Content),
            @ApiResponse(responseCode = "200", description = "Found the claim"),
            @ApiResponse(responseCode = "404", description = "Claim not found",
                    content = @Content)
    })
    public Claim byClaimNumber(@PathVariable("claimNumber") String claimNumber) {
        return claimsRepo.findFirstByClaimNumber(claimNumber).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active claim found"));
    }

    @GetMapping("/claims")
    @PreAuthorize("hasAuthority('Intake')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Get claims by the member's subscriber number. The caller must have the \"Intake\" role", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to view claims",
                    content = @Content),
            @ApiResponse(responseCode = "200", description = "Found the claim"),
            @ApiResponse(responseCode = "404", description = "Claims not found",
                    content = @Content)
    })
    public List<Claim> bySubscriberNumber(@RequestParam("subscriberNum") String subscriberNum) {
        List<Claim> claims = claimsRepo.findBySubscriberNumber(subscriberNum);
        if (claims.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active claims found");
        }
        return claims;
    }

    @PostMapping(value = "/claims", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAuthority('Intake')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Create a new claim. The client must have the \"Intake\" role", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to create claims",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Duplicate request. An identical claim already exists for the member.",
                    content = @Content),
            @ApiResponse(responseCode = "400", description = "Bad request. Either the payload was malformed, or contained bad reference data",
                    content = @Content),
            @ApiResponse(responseCode = "201", description = "Claim created.",
                    content = @Content(schema = @Schema(implementation = CreatedClaimNumber.class)))
    })
    public ResponseEntity<Object> createClaim(@RequestBody Claim request) {
        if (request.checkIfDuplicate(claimsRepo)) {
            log.debug("duplicate claim for member {}", request.getMember().getId());
            return ResponseEntity.unprocessableEntity().body("A duplicate or overlapping claim already exists");
        }
        request.generateClaimNumber(claimsRepo.getNextSeqVal());
        request.setStatus(ClaimStatus.PENDING);
        request.setStatusDate(LocalDate.now());
        Claim savedRequest = claimsRepo.save(request);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(savedRequest.getId()).toUri();

        return ResponseEntity.created(location).body(new CreatedClaimNumber(savedRequest.getClaimNumber()));
    }

    @PutMapping("/claims/{claimId}/void")
    @PreAuthorize("hasAuthority('Intake')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Void a pending claim. The client must have the \"Intake\" role", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to void claims",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Void request cannot be completed. Either claim does not exist, or is not in pending state",
                    content = @Content),
            @ApiResponse(responseCode = "200", description = "Successfully voided",
                    content = @Content)
    })
    public ResponseEntity<Object> voidClaim(@PathVariable("claimId") Integer claimId) {
        return doVoidApproveOrDeny(claimId, ClaimStatus.VOID);
    }

    @PutMapping("/claims/{claimId}/approve")
    @PreAuthorize("hasAuthority('Adjudicators')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Approve a pending claim. The client must have the \"Adjudicator\" role.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to approve claims",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Approve request cannot be completed. Either claim does not exist, or is not in pending state, or approved amount is not valid",
                    content = @Content),
            @ApiResponse(responseCode = "200", description = "Successfully approved",
                    content = @Content)
    })
    public ResponseEntity<Object> approveClaim(@PathVariable("claimId") Integer claimId, @RequestParam BigDecimal approvedAmount) {
        if (claimsRepo.approveClaim(claimId, approvedAmount, ClaimStatus.APPROVED, LocalDate.now()) != 1) {
            log.info("could not approve approve claim");
            return ResponseEntity.unprocessableEntity().body("Claim not approved. Claim non existent, not in PENDING state, or approved amount invalid");
        }

        return ResponseEntity.ok().build();
    }

    @PutMapping("/claims/{claimId}/deny")
    @PreAuthorize("hasAuthority('Adjudicators')")
    @LogEntryExit(LogLevel.DEBUG)
    @Operation(summary = "Deny a pending claim. The client must have the \"Adjudicator\" role.", security = @SecurityRequirement(name = "Authorization"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "401", description = "Client not authorized to deny claims",
                    content = @Content),
            @ApiResponse(responseCode = "422", description = "Deny request cannot be completed. Either claim does not exist, or is not in pending state",
                    content = @Content),
            @ApiResponse(responseCode = "200", description = "Successfully denied",
                    content = @Content)
    })
    public ResponseEntity<Object> denyClaim(@PathVariable("claimId") Integer claimId) {
        return doVoidApproveOrDeny(claimId, ClaimStatus.DENIED);
    }

    @LogEntryExit(LogLevel.DEBUG)
    private ResponseEntity<Object> doVoidApproveOrDeny(Integer claimId, ClaimStatus newStatus) {
        if (claimsRepo.setStatus(claimId, ClaimStatus.PENDING, newStatus, LocalDate.now()) != 1) {
            // only pending requests can be voided, approved or denied
            log.info("cannot change  status of claim, either missing or in wrong status");
            return ResponseEntity.unprocessableEntity().body("Claim not found, or is not in PENDING state");
        }

        return ResponseEntity.ok().build();
    }
}

@NoArgsConstructor
@AllArgsConstructor
@Jacksonized
@Getter
class CreatedClaimNumber {
    String claimNumber;
}