package org.hejinggang.elibrary.repository;

import org.hejinggang.elibrary.model.entity.ReconciliationReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface ReconciliationReportRepository extends JpaRepository<ReconciliationReport, Long> {

    Optional<ReconciliationReport> findByReportDate(LocalDate reportDate);

    boolean existsByReportDate(LocalDate reportDate);
}