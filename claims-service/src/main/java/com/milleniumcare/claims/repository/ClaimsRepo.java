package com.milleniumcare.claims.repository;

import com.milleniumcare.claims.entity.*;
import com.milleniumcare.claims.logging.LogEntryExit;
import org.springframework.boot.logging.LogLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ClaimsRepo extends JpaRepository<Claim, Integer> {
    @Query("select cl from Claim cl where cl.claimNumber = ?1 and cl.status <> com.milleniumcare.claims.entity.ClaimStatus.VOID")
    @LogEntryExit(LogLevel.DEBUG)
    Optional<Claim> findFirstByClaimNumber(String claimNumber);

    @Query("select cl from Claim cl inner join cl.member mem where mem.subscriberNumber = ?1 and cl.status <> com.milleniumcare.claims.entity.ClaimStatus.VOID")
    @LogEntryExit(LogLevel.DEBUG)
    List<Claim> findBySubscriberNumber(String subscriberNumber);

    @Query("select cd from ClaimDrug cd inner join cd.member mem where mem.id = ?1 and cd.status <> com.milleniumcare.claims.entity.ClaimStatus.VOID and cd.drug.id = ?2")
    @LogEntryExit(LogLevel.DEBUG)
    List<ClaimDrug> findDuplicates(Integer memberId, Integer drugId);

    @Query("select ci from ClaimInpatient ci inner join ci.member mem where mem.id = ?1 and ci.status <> com.milleniumcare.claims.entity.ClaimStatus.VOID " +
            "and (?2 between ci.admitDate and ci.dischargeDate or ?3 between ci.admitDate and ci.dischargeDate)")
    @LogEntryExit(LogLevel.DEBUG)
    List<ClaimInpatient> findDuplicates(Integer memberId, LocalDate admitDate, LocalDate dischargeDate);

    @Query("select co from ClaimOutpatient co inner join co.member mem where mem.id = ?1 and co.status <> com.milleniumcare.claims.entity.ClaimStatus.VOID " +
            "and co.procedure.id = ?2 and co.procedureDate = ?3")
    @LogEntryExit(LogLevel.DEBUG)
    List<ClaimOutpatient> findDuplicates(Integer memberId, Integer procedureId, Date procedureDate);

    @Query(value = "select nextval('claims_serial')", nativeQuery = true)
    @LogEntryExit(LogLevel.DEBUG)
    Integer getNextSeqVal();

    @Modifying
    @Query("update Claim cl set cl.status = ?3, cl.statusDate = ?4 where cl.id = ?1 and cl.status = ?2")
    @Transactional
    @LogEntryExit(LogLevel.DEBUG)
    int setStatus(Integer id, ClaimStatus oldStatus, ClaimStatus newStatus, LocalDate statusDate);

    @Modifying
    @Query("update Claim cl set cl.approvedAmount = ?2, cl.status = ?3, cl.statusDate = ?4 where cl.id = ?1 and cl.status = com.milleniumcare.claims.entity.ClaimStatus.PENDING and cl.submittedAmount >= ?2")
    @Transactional
    @LogEntryExit(LogLevel.DEBUG)
    int approveClaim(Integer id, BigDecimal approvedAmount, ClaimStatus approved, LocalDate statusDate);
}
