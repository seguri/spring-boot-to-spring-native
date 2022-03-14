package com.milleniumcare.precertification.repository;

import com.milleniumcare.precertification.entity.*;
import com.milleniumcare.precertification.logging.LogEntryExit;
import org.springframework.boot.logging.LogLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PrecertificationRequestRepo extends JpaRepository<PrecertificationRequest, Integer> {
    @Query("select pr from PrecertificationRequest pr where pr.precertNumber = ?1 and pr.status <> com.milleniumcare.precertification.entity.PrecertificationRequestStatus.VOID")
    @LogEntryExit(LogLevel.DEBUG)
    Optional<PrecertificationRequest> findFirstByPrecertNumber(String precertNumber);

    @Query("select pr from PrecertificationRequest pr inner join pr.member mem where mem.subscriberNumber = ?1 and pr.status <> com.milleniumcare.precertification.entity.PrecertificationRequestStatus.VOID")
    @LogEntryExit(LogLevel.DEBUG)
    List<PrecertificationRequest> findBySubscriberNumber(String subscriberNumber);

    @Query("select prd from PrecertificationRequestDrug prd inner join prd.member mem where mem.id = ?1 and prd.status <> com.milleniumcare.precertification.entity.PrecertificationRequestStatus.VOID and prd.drug.id = ?2")
    @LogEntryExit(LogLevel.DEBUG)
    List<PrecertificationRequestDrug> findDuplicates(Integer memberId, Integer drugId);

    @Query("select pri from PrecertificationRequestInpatient pri inner join pri.member mem where mem.id = ?1 and pri.status <> com.milleniumcare.precertification.entity.PrecertificationRequestStatus.VOID " +
            "and (?2 between pri.admitDate and pri.dischargeDate or ?3 between pri.admitDate and pri.dischargeDate)")
    @LogEntryExit(LogLevel.DEBUG)
    List<PrecertificationRequestInpatient> findDuplicates(Integer memberId, LocalDate admitDate, LocalDate dischargeDate);

    @Query("select pro from PrecertificationRequestOutpatient pro inner join pro.member mem where mem.id = ?1 and pro.status <> com.milleniumcare.precertification.entity.PrecertificationRequestStatus.VOID " +
            "and pro.procedure.id = ?2 and pro.procedureDate = ?3")
    @LogEntryExit(LogLevel.DEBUG)
    List<PrecertificationRequestOutpatient> findDuplicates(Integer memberId, Integer procedureId, LocalDate procedureDate);

    @Query(value = "select nextval('precert_serial')", nativeQuery = true)
    @LogEntryExit(LogLevel.DEBUG)
    Integer getNextSeqVal();

    @Modifying
    @Query("update PrecertificationRequest pr set pr.status = ?3, pr.statusDate = ?4 where pr.id = ?1 and pr.status = ?2")
    @Transactional
    @LogEntryExit(LogLevel.DEBUG)
    int setStatus(Integer id, PrecertificationRequestStatus oldStatus, PrecertificationRequestStatus newStatus, LocalDate statusDate);
}
